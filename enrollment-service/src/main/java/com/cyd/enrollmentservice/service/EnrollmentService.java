package com.cyd.enrollmentservice.service;


import com.cyd.enrollmentservice.model.Enrollment;
import com.cyd.enrollmentservice.model.EnrollmentStatus;
import com.cyd.enrollmentservice.model.Student;
import com.cyd.enrollmentservice.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StudentService studentService;
    // 2. 新增：从配置文件读取catalog-service地址（避免硬编码，{insert\_element\_2\_}）
    @Value("${catalog-service.url}")
    private String catalogServiceUrl;

    // 1. 学生选课（核心业务逻辑：校验+级联更新）- 移除init()，适配Repository
    @Transactional  // 任务四要求：@Transactional确保选课操作一致性（{insert\_element\_0\_}）
    public Enrollment enrollCourse(Enrollment enrollment) {
        String courseId = enrollment.getCourseId();
        String studentId = enrollment.getStudentId();

        // 校验1：课程是否存在（保留原逻辑）
        Course course = courseService.findCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        // 校验2：学生是否存在（保留原逻辑）
        Student student = studentService.findStudentByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with studentId: " + studentId));

        // 校验3：是否重复选课（复用Repository方法，任务三要求判断学生是否已选课（{insert\_element\_1\_}））
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new IllegalArgumentException("Duplicate enrollment: Student " + studentId + " already enrolled in Course " + courseId);
        }

        // 校验4：课程容量是否已满（复用Repository统计方法，任务三要求统计课程活跃人数（{insert\_element\_2\_}））
        long activeEnrollCount = enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
        if (activeEnrollCount >= course.getCapacity()) {
            throw new IllegalArgumentException("Course capacity exceeded: Current enrolled " + activeEnrollCount + ", Capacity " + course.getCapacity());
        }

        // 执行选课：设置默认状态（任务二要求使用枚举类型（{insert\_element\_3\_}）），移除init()避免解析错误
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 级联更新：课程已选人数（保留原逻辑）
        course.setEnrolled(course.getEnrolled() + 1);
        courseService.updateCourse(courseId, course);

        return savedEnrollment;
    }

    // 2. 学生退课（级联更新课程已选人数）- 保留原功能，适配Repository
    @Transactional  // 任务四要求：@Transactional确保退课操作一致性（{insert\_element\_4\_}）
    public void dropCourse(String enrollmentId) {
        // 校验选课记录是否存在（保留原逻辑）
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));

        // 退课：更新状态（任务二要求保留状态枚举（{insert\_element\_5\_}），复用Repository保存方法）
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        // 级联更新：课程已选人数（复用Repository统计方法，任务三要求统计课程活跃人数（{insert\_element\_6\_}））
        Course course = courseService.findCourseById(enrollment.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + enrollment.getCourseId()));
        long newActiveCount = enrollmentRepository.countByCourseIdAndStatus(enrollment.getCourseId(), EnrollmentStatus.ACTIVE);
        course.setEnrolled(Math.max(0, (int) newActiveCount));
        courseService.updateCourse(course.getId(), course);
    }

    // 3. 查询所有选课记录（保留原功能，适配Repository）
    public List<Enrollment> findAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    // 4. 按课程ID查询选课记录（保留原功能，适配Repository）
    public List<Enrollment> findEnrollmentsByCourseId(String courseId) {
        // 保留原校验逻辑
        courseService.findCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        return enrollmentRepository.findByCourseId(courseId);
    }

    // 4-1. 课程+状态组合查询（任务三要求按课程、状态组合查询（{insert\_element\_7\_}），复用Repository方法）
    public List<Enrollment> findEnrollmentsByCourseIdAndStatus(String courseId, EnrollmentStatus status) {
        courseService.findCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        return enrollmentRepository.findByCourseIdAndStatus(courseId, status);
    }

    // 5. 按学生学号查询选课记录（保留原功能，适配Repository）
    public List<Enrollment> findEnrollmentsByStudentId(String studentId) {
        // 保留原校验逻辑
        studentService.findStudentByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with studentId: " + studentId));
        return enrollmentRepository.findByStudentId(studentId);
    }

    // 5-1. 学生+状态组合查询（任务三要求按学生、状态组合查询（{insert\_element\_8\_}），复用Repository方法）
    public List<Enrollment> findEnrollmentsByStudentIdAndStatus(String studentId, EnrollmentStatus status) {
        studentService.findStudentByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with studentId: " + studentId));
        return enrollmentRepository.findByStudentIdAndStatus(studentId, status);
    }

    // 6. 校验学生是否有选课记录（供学生Service删除学生时使用）- 保留原功能
    public boolean existsByStudentId(String studentId) {
        return enrollmentRepository.existsByStudentId(studentId);
    }

    // 7. 统计课程活跃人数（任务三要求统计课程活跃人数（{insert\_element\_9\_}），复用Repository方法）
    public long countActiveEnrollmentsByCourseId(String courseId) {
        courseService.findCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        return enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }
}