package com.cyd.enrollmentservice.repository;


import com.cyd.enrollmentservice.model.Enrollment;
import com.cyd.enrollmentservice.model.EnrollmentStatus;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    // 1. 保存选课记录（新增/更新通用）
    // 1. 保留原“保存选课记录”方法签名，JpaRepository 已默认实现（支持新增/更新）
    @Override
    <S extends Enrollment> S save(S entity);

    // 2. 保留原“查询所有选课记录”方法签名，JpaRepository 已默认实现

    @Override
    List<Enrollment> findAll();

    // 3. 保留原“按ID查询选课记录”方法签名，JpaRepository 已默认实现
    @Override
    Optional<Enrollment> findById(String id);

    // 4. 保留原“按课程ID查询选课记录”方法，按 Spring Data 规范实现）
    List<Enrollment> findByCourseId(String courseId);

    // 5. 保留原“按学生学号查询选课记录”方法，按 Spring Data 规范实现
    List<Enrollment> findByStudentId(String studentId);

    // 6. 保留原“校验重复选课”方法，按 Spring Data 规范实现
    boolean existsByCourseIdAndStudentId(String courseId, String studentId);

     // 7. 保留原“按ID删除选课记录”方法签名，JpaRepository 已默认实现
    @Override
    void deleteById(String id);

    // 8. 保留原“校验学生是否有选课记录”方法，按 Spring Data 规范实现
    boolean existsByStudentId(String studentId);

    // 9. 新增：按课程ID+状态组合查询
    List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);

    // 10. 新增：按学生ID+状态组合查询
    List<Enrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status);

    // 11. 新增：统计某课程的活跃人数
    long countByCourseIdAndStatus(String courseId, EnrollmentStatus status);

    // 12. 新增：自定义JPQL查询某课程某状态的选课记录
    @Query("SELECT e FROM Enrollment e WHERE e.courseId = :courseId AND e.status = :status")
    List<Enrollment> findEnrollmentsByCourseIdAndStatus(
            @Param("courseId") String courseId,
            @Param("status") EnrollmentStatus status
    );
}