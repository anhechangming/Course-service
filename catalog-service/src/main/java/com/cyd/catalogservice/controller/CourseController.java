package com.cyd.catalogservice.controller;


import com.cyd.catalogservice.DTO.PageQueryDTO;
import com.cyd.catalogservice.Response.Result;
import com.cyd.catalogservice.model.Course;
import com.cyd.catalogservice.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")  // 基础URL：/api/courses
@Validated  // 开启参数验证
public class CourseController {
    @Autowired
    private CourseService courseService;
    // 分页查询
    @GetMapping("/page")
    public ResponseEntity<Result<List<Course>>> getCoursesByPage(PageQueryDTO pageQuery) {
        List<Course> courses = courseService.getCoursesByPage(pageQuery);
        return ResponseEntity.ok(Result.success(courses));
    }
    // 1. 查询所有课程（GET /api/courses）
    @GetMapping
    public Result<List<Course>> getAllCourses() {
        List<Course> courses = courseService.findAllCourses();
        return Result.success(courses);
    }

    // 2. 按ID查询课程（GET /api/courses/{id}）
    @GetMapping("/{id}")
    public Result<Course> getCourseById(@PathVariable String id) {
        return courseService.findCourseById(id)
                .map(Result::success)  // 存在则返回200+课程信息
                .orElse(Result.error(404, "Course not found"));  // 不存在返回404
    }
    // 新增：按课程代码查询课程
    @GetMapping("/code/{code}")
    public Result<Course> getCourseByCode(@PathVariable String code) {
        return courseService.findCourseByCode(code)
                .map(Result::success)  // 存在则返回200+课程信息
                .orElse(Result.error(404, "Course not found by code: " + code));  // 不存在返回404
    }


    // 3. 创建课程（POST /api/courses）
    // 创建课程：捕获非法参数异常
    @PostMapping
    public ResponseEntity<Result<Course>> createCourse(@Valid @RequestBody Course course) {
        try {
            Course createdCourse = courseService.createCourse(course);
            return new ResponseEntity<>(Result.created(createdCourse), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    // 4. 更新课程（PUT /api/courses/{id}）
    // 更新课程：捕获非法参数异常
    @PutMapping("/{id}")
    public ResponseEntity<Result<Course>> updateCourse(
            @PathVariable String id,
            @Valid @RequestBody Course course) {
        try {
            Course updatedCourse = courseService.updateCourse(id, course);
            return new ResponseEntity<>(Result.success(updatedCourse), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }


    // 5. 删除课程（DELETE /api/courses/{id}）
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteCourse(@PathVariable String id) {
        try {
            courseService.deleteCourse(id);
            return new ResponseEntity<>(Result.success(), HttpStatus.OK); // 或保持204，根据需求选
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
    // 全局参数验证异常处理（如字段为空、格式错误）
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public Result<Void> handleValidationException(jakarta.validation.ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("Invalid request parameters");
        return Result.error(400, message);
    }
}