package com.zjgsu.cyd.course.repository;

import com.zjgsu.cyd.course.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    // 1. 保留原“查询所有课程”方法签名，JpaRepository 已默认实现（无需手动编写）
    @Override
    List<Course> findAll();

    // 2. 保留原“按ID查询课程”方法签名，JpaRepository 已默认实现
    @Override
    Optional<Course> findById(String id);

    // 3. 保留原“保存课程”方法签名，JpaRepository 已默认实现（支持新增/更新）
    @Override
    <S extends Course> S save(S entity);

    // 4. 保留原“按ID删除课程”方法签名，JpaRepository 已默认实现
    @Override
    void deleteById(String id);

    // 新增：按课程代码查询课程
    Optional<Course> findByCode(String code);

    // 5. 保留原“检查课程代码是否已存在”方法，按 Spring Data 规范实现
    boolean existsByCode(String code);

    // 6. 新增：按讲师编号查询课程（嵌入式对象字段用“_”关联）
    List<Course> findByInstructor_Id(String instructorId);

    // 7. 新增：筛选有剩余容量的课程（已选人数 < 总容量）
    List<Course> findByEnrolledLessThan(Integer capacity);

    // 8. 新增：课程标题模糊查询（忽略大小写，支持分页）
    Page<Course> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    // 9. 新增：自定义JPQL查询冲突课程（用于时间冲突检查）
    @Query("SELECT c FROM Course c WHERE " +
            "c.schedule.dayOfWeek = :dayOfWeek " +
            "AND c.schedule.startTime < :endTime " +
            "AND c.schedule.endTime > :startTime")
    List<Course> findConflictingCourses(
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );
}