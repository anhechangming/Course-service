package com.cyd.enrollmentservice.service;


import com.cyd.enrollmentservice.model.Student;
import com.cyd.enrollmentservice.repository.EnrollmentRepository;
import com.cyd.enrollmentservice.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    // 注入 EnrollmentRepository：用于删除学生前的关联检查（任务四要求：删除前的关联检查（{insert\_element\_8\_}））
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // 1. 创建学生：添加事务、复用 Repository 判重，移除内存逻辑（任务四要求：事务与数据校验（{insert\_element\_9\_}））
    @Transactional
    public Student createStudent(Student student) {
        // 校验1：学号唯一性（复用 Repository existsByStudentId，替代内存流遍历（{insert\_element\_10\_}））
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("Student ID (studentId) already exists: " + student.getStudentId());
        }

        // 校验2：邮箱唯一性（新增，文档要求学生邮箱唯一（{insert\_element\_11\_}），任务三要求判重（{insert\_element\_12\_}））
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("Student email already exists: " + student.getEmail());
        }

        // 保留原 ID/创建时间生成逻辑（若 Student 类已通过 @PrePersist 或 @GeneratedValue 自动生成，可删除）
        // student.init();

        // 保存学生（Repository 持久化到数据库，替代内存 Map 存储）
        return studentRepository.save(student);
    }

    // 2. 查询所有学生：复用 Repository 排序，移除内存排序（任务四要求：数据库层面优化（{insert\_element\_13\_}））
    public List<Student> findAllStudents() {
        // 按学号升序排序（通过 Sort 参数由数据库执行，效率高于内存排序）
        Sort sort = Sort.by("studentId").ascending();
        return studentRepository.findAll(sort);
    }

    // 3. 按ID查询学生：直接复用 Repository 方法（无改造，保留原逻辑）
    public Optional<Student> findStudentById(String id) {
        return studentRepository.findById(id);
    }

    // 4. 按学号查询学生：直接复用 Repository 方法（适配选课功能中的学生验证）
    public Optional<Student> findStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    // 5. 新增：按专业分页查询学生（任务三要求：按专业筛选（{insert\_element\_14\_}），适配分页接口）
    public Page<Student> findStudentsByMajor(String major, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("studentId").ascending());
        return studentRepository.findByMajor(major, pageable);
    }

    // 6. 新增：按年级分页查询学生（任务三要求：按年级筛选（{insert\_element\_15\_}））
    public Page<Student> findStudentsByGrade(Integer grade, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("studentId").ascending());
        return studentRepository.findByGrade(grade, pageable);
    }

    // 7. 更新学生信息：添加事务、保留业务规则（任务四要求：事务一致性（{insert\_element\_16\_}））
    @Transactional
    public Student updateStudent(String id, Student updatedStudent) {
        // 校验1：学生是否存在（Repository 查数据库，替代内存 Map 查询）
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        // 校验2：禁止修改学号（保留原业务规则，文档要求学号唯一且不可篡改）
        if (!existingStudent.getStudentId().equals(updatedStudent.getStudentId())) {
            throw new IllegalArgumentException("Student ID (studentId) cannot be modified (original: " + existingStudent.getStudentId() + ")");
        }

        // 校验3：邮箱唯一性（若更新邮箱，需排除自身ID判重）
        if (!existingStudent.getEmail().equals(updatedStudent.getEmail())
                && studentRepository.existsByEmail(updatedStudent.getEmail())) {
            throw new IllegalArgumentException("Student email already exists: " + updatedStudent.getEmail());
        }

        // 更新合法字段并保存（Repository 持久化到数据库）
        existingStudent.setName(updatedStudent.getName());
        existingStudent.setMajor(updatedStudent.getMajor());
        existingStudent.setGrade(updatedStudent.getGrade());
        existingStudent.setEmail(updatedStudent.getEmail());
        return studentRepository.save(existingStudent);
    }

    // 8. 删除学生：添加事务、关联检查（任务四要求：删除前的关联检查（{insert\_element\_17\_}））
    @Transactional
    public void deleteStudent(String id) {
        // 校验1：学生是否存在
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }

        // 校验2：关联检查（若学生有选课记录，禁止删除，任务四要求（{insert\_element\_18\_}））
        Student student = studentRepository.findById(id).get();
        if (enrollmentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("Cannot delete student: Student has existing enrollments");
        }

        // 删除学生（Repository 从数据库删除，替代内存 Map 移除）
        studentRepository.deleteById(id);
    }
}