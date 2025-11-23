package com.cyd.enrollmentservice.controller;


import com.cyd.enrollmentservice.Response.Result;
import com.cyd.enrollmentservice.model.Enrollment;
import com.cyd.enrollmentservice.model.EnrollmentStatus;
import com.cyd.enrollmentservice.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@Validated
public class EnrollmentController {
    @Autowired
    private EnrollmentService enrollmentService;

    // 1. 学生选课（POST /api/enrollments）- 保留原功能与异常处理
    @PostMapping
    public ResponseEntity<Result<Enrollment>> enrollCourse(@Valid @RequestBody Enrollment enrollment) {
        try {
            Enrollment savedEnrollment = enrollmentService.enrollCourse(enrollment);
            return new ResponseEntity<>(Result.created(savedEnrollment), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // 2. 学生退课（DELETE /api/enrollments/{id}）- 保留原功能与异常处理
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> dropCourse(@PathVariable String id) {
        try {
            enrollmentService.dropCourse(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // 3. 查询所有选课记录（GET /api/enrollments）- 保留原功能
    @GetMapping
    public Result<List<Enrollment>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.findAllEnrollments();
        return Result.success(enrollments);
    }

    // 4. 按课程ID查询选课记录（GET /api/enrollments/course/{courseId}）- 保留原功能
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Result<List<Enrollment>>> getEnrollmentsByCourseId(@PathVariable String courseId) {
        try {
            List<Enrollment> enrollments = enrollmentService.findEnrollmentsByCourseId(courseId);
            return new ResponseEntity<>(Result.success(enrollments), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // 4-1. 课程+状态组合查询（适配任务三组合查询要求（{insert\_element\_10\_}））
    @GetMapping("/course/{courseId}/status")
    public ResponseEntity<Result<List<Enrollment>>> getEnrollmentsByCourseIdAndStatus(
            @PathVariable String courseId,
            @RequestParam EnrollmentStatus status
    ) {
        try {
            List<Enrollment> enrollments = enrollmentService.findEnrollmentsByCourseIdAndStatus(courseId, status);
            return new ResponseEntity<>(Result.success(enrollments), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // 5. 按学生学号查询选课记录（GET /api/enrollments/student/{studentId}）- 保留原功能
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Result<List<Enrollment>>> getEnrollmentsByStudentId(@PathVariable String studentId) {
        try {
            List<Enrollment> enrollments = enrollmentService.findEnrollmentsByStudentId(studentId);
            return new ResponseEntity<>(Result.success(enrollments), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // 5-1. 学生+状态组合查询（适配任务三组合查询要求（{insert\_element\_11\_}））
    @GetMapping("/student/{studentId}/status")
    public ResponseEntity<Result<List<Enrollment>>> getEnrollmentsByStudentIdAndStatus(
            @PathVariable String studentId,
            @RequestParam EnrollmentStatus status
    ) {
        try {
            List<Enrollment> enrollments = enrollmentService.findEnrollmentsByStudentIdAndStatus(studentId, status);
            return new ResponseEntity<>(Result.success(enrollments), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // 6. 统计课程活跃人数（适配任务三统计要求（{insert\_element\_12\_}））
    @GetMapping("/course/{courseId}/active-count")
    public ResponseEntity<Result<Long>> getActiveEnrollmentCountByCourseId(@PathVariable String courseId) {
        try {
            long count = enrollmentService.countActiveEnrollmentsByCourseId(courseId);
            return new ResponseEntity<>(Result.success(count), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}