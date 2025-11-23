package com.cyd.enrollmentservice.repository;


import com.cyd.enrollmentservice.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// 继承 JpaRepository<实体类, 主键类型>，自动获得基础 CRUD 方法
public interface StudentRepository extends JpaRepository<Student, String> {

    // 1. 保留原“按学号查询学生”方法（任务三要求：按学号唯一查询（{insert\_element\_0\_}））
    Optional<Student> findByStudentId(String studentId);

    // 2. 新增：按邮箱查询学生（任务三要求：按邮箱唯一查询（{insert\_element\_1\_}），文档要求学生邮箱唯一（{insert\_element\_2\_}））
    Optional<Student> findByEmail(String email);

    // 3. 保留原“校验学号是否已存在”方法（任务三要求：判重检查（{insert\_element\_3\_}））
    boolean existsByStudentId(String studentId);

    // 4. 新增：校验邮箱是否已存在（任务三要求：判重检查（{insert\_element\_4\_}），文档要求学生邮箱唯一（{insert\_element\_5\_}））
    boolean existsByEmail(String email);

    // 5. 新增：按专业筛选学生（任务三要求：按专业筛选（{insert\_element\_6\_}），支持分页）
    Page<Student> findByMajor(String major, Pageable pageable);

    // 6. 新增：按年级筛选学生（任务三要求：按年级筛选（{insert\_element\_7\_}），支持分页）
    Page<Student> findByGrade(Integer grade, Pageable pageable);

    // 7. 新增：按专业+年级组合筛选学生（任务三扩展要求：多条件组合查询，适配复杂业务场景）
    List<Student> findByMajorAndGrade(String major, Integer grade);

    // 注：原内存实现中的 save/findAll/findById/deleteById 方法，JpaRepository 已默认实现，无需重复编写
}