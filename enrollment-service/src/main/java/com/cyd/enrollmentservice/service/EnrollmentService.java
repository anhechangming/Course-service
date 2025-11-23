package com.cyd.enrollmentservice.service;


import com.cyd.enrollmentservice.common.ResourceNotFoundException;
import com.cyd.enrollmentservice.model.Enrollment;
import com.cyd.enrollmentservice.model.EnrollmentStatus;
import com.cyd.enrollmentservice.model.Student;
import com.cyd.enrollmentservice.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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

    // 1. 学生选课（核心业务逻辑：校验+级联更新）
    @Transactional
    public Enrollment enrollCourse(Enrollment enrollment) {
        String courseId = enrollment.getCourseId();
        String studentId = enrollment.getStudentId();

        // 校验1：调用catalog-service验证课程是否存在+获取课程信息（{insert\_element\_3\_}）
        String courseApiUrl = catalogServiceUrl + "/api/courses/" + courseId;
        Map<String, Object> courseData; // 存储catalog-service返回的课程数据（JSON转Map）
        try {
            // 调用catalog-service的GET接口，获取课程信息（含capacity、enrolled）
            courseData = restTemplate.getForObject(courseApiUrl, Map.class);
            // 假设catalog-service返回格式为{data: {id: "xxx", capacity: 60, enrolled: 10, ...}}（需与catalog-service接口响应一致）
            if (courseData == null || courseData.get("data") == null) {
                throw new RuntimeException("Course not found with id: " + courseId);
            }
            courseData = (Map<String, Object>) courseData.get("data");
        } catch (HttpClientErrorException e) {
            // 捕获404异常：课程不存在（{insert\_element\_4\_}）
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Course not found with id: " + courseId);
            }
            // 其他HTTP异常（如catalog-service宕机）
            throw new RuntimeException("Failed to call catalog-service: " + e.getMessage());
        }
        // 校验2：学生是否存在（保留原逻辑，需确保StudentService是本地服务）
        Student student = studentService.findStudentByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with studentId: " + studentId));

        // 校验3：是否重复选课（保留原逻辑，符合{insert\_element\_5\_}的重复检查）
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new IllegalArgumentException("Duplicate enrollment: Student " + studentId + " already enrolled in Course " + courseId);
        }

        // 校验4：课程容量是否已满（从catalog-service返回的courseData中获取capacity和enrolled，{insert\_element\_6\_}）
        Integer courseCapacity = (Integer) courseData.get("capacity");
        Integer currentEnrolled = (Integer) courseData.get("enrolled");
        if (currentEnrolled >= courseCapacity) {
            throw new IllegalArgumentException("Course capacity exceeded: Current enrolled " + currentEnrolled + ", Capacity " + courseCapacity);
        }

        // 执行选课：设置默认状态（保留原逻辑，符合{insert\_element\_7\_}的ACTIVE状态）
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 级联更新：调用catalog-service更新课程已选人数（enrolled+1，{insert\_element\_8\_}）
        updateCourseEnrolledCount(courseId, currentEnrolled + 1);

        return savedEnrollment;
    }
    // 新增工具方法：调用catalog-service更新课程已选人数（{insert\_element\_9\_}）
    private void updateCourseEnrolledCount(String courseId, int newEnrolledCount) {
        String updateApiUrl = catalogServiceUrl + "/api/courses/" + courseId;
        // 构造更新数据：仅传递enrolled字段（符合catalog-service的PUT接口要求）
        Map<String, Object> updateData = Map.of("enrolled", newEnrolledCount);
        try {
            // 调用catalog-service的PUT接口，更新课程已选人数
            restTemplate.put(updateApiUrl, updateData);
        } catch (Exception e) {
            // 文档要求：更新失败不影响主流程，仅记录日志（{insert\_element\_10\_}）
            System.err.println("Failed to update course enrolled count: " + e.getMessage());
            // 生产环境建议用日志框架（如SLF4J）记录，而非System.err
            // log.error("Failed to update course enrolled count for course {}: {}", courseId, e.getMessage());
        }
    }


    // 2. 学生退课（级联更新课程已选人数）
    @Transactional
    public void dropCourse(String enrollmentId) {
        // 校验选课记录是否存在（保留原逻辑）
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));

        // 校验：是否已退课（避免重复退课）
        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new IllegalArgumentException("Enrollment already dropped: " + enrollmentId);
        }

        // 退课：更新状态（保留原逻辑）
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        // 级联更新：调用catalog-service获取当前已选人数，再减1（{insert\_element\_11\_}）
        String courseId = enrollment.getCourseId();
        String courseApiUrl = catalogServiceUrl + "/api/courses/" + courseId;
        try {
            // 1. 先获取课程当前已选人数
            Map<String, Object> courseData = restTemplate.getForObject(courseApiUrl, Map.class);
            if (courseData == null || courseData.get("data") == null) {
                throw new RuntimeException("Course not found with id: " + courseId);
            }
            courseData = (Map<String, Object>) courseData.get("data");
            Integer currentEnrolled = (Integer) courseData.get("enrolled");

            // 2. 计算新的已选人数（避免为负）
            int newEnrolledCount = Math.max(0, currentEnrolled - 1);

            // 3. 调用catalog-service更新人数
            updateCourseEnrolledCount(courseId, newEnrolledCount);
        } catch (Exception e) {
            // 同样，更新失败仅记录日志，不影响退课主流程
            System.err.println("Failed to update course enrolled count when dropping: " + e.getMessage());
        }
    }

    // 3. 查询所有选课记录（保留原功能，适配Repository）
    public List<Enrollment> findAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    // 4. 按课程ID查询选课记录（保留原功能，适配Repository）
//    public List<Enrollment> findEnrollmentsByCourseId(String courseId) {
//        // 保留原校验逻辑
//        courseService.findCourseById(courseId)
//                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
//        return enrollmentRepository.findByCourseId(courseId);
//    }
    // 4. 按课程ID查询选课记录（改造：HTTP调用catalog-service校验课程，保留查询功能）
    public List<Enrollment> findEnrollmentsByCourseId(String courseId) {
        // 改造点1：调用catalog-service验证课程是否存在（替换原courseService）
        String courseApiUrl = catalogServiceUrl + "/api/courses/" + courseId;
        try {
            // 调用catalog-service的GET接口，仅需验证课程存在（无需完整课程数据）
            restTemplate.getForObject(courseApiUrl, Map.class);
            // 若接口返回200，则课程存在；若返回404，会抛HttpClientErrorException
        } catch (HttpClientErrorException e) {
            // 改造点2：捕获404异常，抛出统一的“资源不存在”异常
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Course", courseId); // 自定义异常，需提前创建
            }
            // 改造点3：处理其他HTTP异常（如catalog-service宕机）
            throw new RuntimeException("Failed to verify course from catalog-service: " + e.getMessage());
        } catch (Exception e) {
            // 兜底处理：网络异常、接口响应格式错误等
            throw new RuntimeException("Error verifying course existence: " + e.getMessage());
        }

        // 保留原逻辑：查询该课程下所有选课记录（选课服务的核心职责，）
        return enrollmentRepository.findByCourseId(courseId);
    }

//    // 4-1. 课程+状态组合查询（任务三要求按课程、状态组合查询（{insert\_element\_7\_}），复用Repository方法）
//    public List<Enrollment> findEnrollmentsByCourseIdAndStatus(String courseId, EnrollmentStatus status) {
//        courseService.findCourseById(courseId)
//                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
//        return enrollmentRepository.findByCourseIdAndStatus(courseId, status);
//    }

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
//
//    // 7. 统计课程活跃人数（任务三要求统计课程活跃人数（{insert\_element\_9\_}），复用Repository方法）
//    public long countActiveEnrollmentsByCourseId(String courseId) {
//        courseService.findCourseById(courseId)
//                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
//        return enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
//    }
}