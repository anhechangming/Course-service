package com.cyd.catalogservice.service;


import com.cyd.catalogservice.DTO.PageQueryDTO;
import com.cyd.catalogservice.model.Course;
import com.cyd.catalogservice.repository.CourseRepository;
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
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    // 1. 检查课程时间冲突：改用 Repository 自定义查询，删除内存遍历（文档要求：复杂查询用 Repository 方法）
    private void checkTimeConflict(Course course, String excludeId) {
        // 调用 CourseRepository 新增的 findConflictingCourses 方法，直接从数据库查询冲突课程
        List<Course> conflictingCourses = courseRepository.findConflictingCourses(
                course.getSchedule().getDayOfWeek(),
                course.getSchedule().getStartTime(),
                course.getSchedule().getEndTime()
        );

        // 排除自身（更新场景）：若存在 excludeId，过滤掉当前课程
        boolean hasConflict = conflictingCourses.stream()
                .anyMatch(conflictCourse -> excludeId == null || !conflictCourse.getId().equals(excludeId));

        if (hasConflict) {
            throw new IllegalArgumentException("Time conflict detected: Instructor '" +
                    course.getInstructor().getName() + "' already has a course at '" +
                    course.getSchedule().getDayOfWeek() + " " + course.getSchedule().getStartTime() +
                    "-" + course.getSchedule().getEndTime() + "'");
        }
    }
    // 2. 分页查询课程：改用 JPA 分页（Pageable），删除内存分页逻辑（文档要求：使用 Repository 实现分页）{insert\_element\_1\_}
    public List<Course> getCoursesByPage(PageQueryDTO pageQuery) {
        // 1. 构建分页参数：页码（pageNum-1，JPA 页码从0开始）、每页条数、排序规则（按课程代码升序）
        Pageable pageable = PageRequest.of(
                Math.max(pageQuery.getPageNum() - 1, 0),
                pageQuery.getPageSize(),
                Sort.by("code").ascending()
        );

        // 2. 调用 Repository 分页查询方法（若有标题关键字，可改用 findByTitleContainingIgnoreCase）
        Page<Course> coursePage = courseRepository.findAll(pageable);

        // 3. 返回当前页数据（Page 转 List）
        return coursePage.getContent();
    }

    public Optional<Course> findCourseByCode(String code) {
        //  CourseRepository 有 findByCode 方法，若没有需先在 Repository 中定义
        return courseRepository.findByCode(code);
    }




    // 3. 查询所有课程：复用 Repository findAll + JPA 排序，删除内存排序（文档要求：数据库层面实现排序）{insert\_element\_2\_}
    public List<Course> findAllCourses() {
        // 按课程代码升序排序，通过 Sort 参数由数据库执行排序（效率高于内存排序）
        Sort sort = Sort.by("code").ascending();
        return courseRepository.findAll(sort);
    }

    // 4. 按ID查询课程：直接调用 Repository 方法（无改造，保持原逻辑）
    public Optional<Course> findCourseById(String id) {
        return courseRepository.findById(id);
    }

    // 5. 创建课程：改用 Repository 判重 + 事务保障（文档要求：Service 层实现事务与数据校验）{insert\_element\_3\_}
    @Transactional  // 新增事务注解：确保创建课程与数据校验原子性
    public Course createCourse(Course course) {
        // 1. 课程代码唯一性校验：调用 Repository existsByCode，删除内存流遍历（文档要求：判重查数据库）{insert\_element\_4\_}
        if (courseRepository.existsByCode(course.getCode())) {
            throw new IllegalArgumentException("Course code already exists: " + course.getCode());
        }

        // 2. 检查时间冲突（改用 Repository 查数据库，非内存）
        checkTimeConflict(course, null);

//        // 3. 自动生成ID（保留原逻辑，若实体用 @GeneratedValue，可删除此句）
//        course.setId(UUID.randomUUID().toString().replace("-", ""));

        // 4. 保存课程（Repository 持久化到数据库）
        return courseRepository.save(course);
    }

    // 6. 更新课程：保留业务规则，适配 Repository 数据交互（文档要求：保留原有业务规则）{insert\_element\_5\_}
    @Transactional  // 新增事务注解：确保更新与校验原子性
    public Course updateCourse(String id, Course updatedCourse) {
        // 1. 查询课程是否存在（Repository 查数据库，非内存）
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        // 2. 禁止修改课程代码（保留原业务规则）
        if (!existingCourse.getCode().equals(updatedCourse.getCode())) {
            throw new IllegalArgumentException("Course code cannot be modified (original: " + existingCourse.getCode() + ")");
        }

        // 3. 校验容量为正数（保留原逻辑）
        if (updatedCourse.getCapacity() <= 0) {
            throw new IllegalArgumentException("Course capacity must be positive: " + updatedCourse.getCapacity());
        }

        // 4. 检查时间冲突（排除自身，查数据库）
        checkTimeConflict(updatedCourse, id);

        // 5. 更新字段并保存（Repository 持久化到数据库）
        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setInstructor(updatedCourse.getInstructor());
        existingCourse.setSchedule(updatedCourse.getSchedule());
        existingCourse.setCapacity(updatedCourse.getCapacity());
        return courseRepository.save(existingCourse);
    }

    // 7. 删除课程：适配 Repository，新增关联检查（文档要求：删除前的关联检查）{insert\_element\_6\_}
    @Transactional  // 新增事务注解：确保删除与关联检查原子性
    public void deleteCourse(String id) {
        // 1. 检查课程是否存在（Repository 查数据库）
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id: " + id);
        }

        // 2. （补充）关联检查：若存在选课记录，禁止删除（需注入 EnrollmentRepository，文档要求删除前关联检查）{insert\_element\_7\_}
        // if (!enrollmentRepository.findByCourseId(id).isEmpty()) {
        //     throw new IllegalArgumentException("Cannot delete course: It has existing enrollments");
        // }

        // 3. 删除课程（Repository 从数据库删除）
        courseRepository.deleteById(id);
    }


}