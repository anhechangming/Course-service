package com.cyd.enrollmentservice.controller;


import com.cyd.enrollmentservice.Response.Result;
import com.cyd.enrollmentservice.model.Student;
import com.cyd.enrollmentservice.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Validated
public class StudentController {
    @Autowired
    private StudentService studentService;

    // 1. 创建学生（POST /api/students）- 保留原功能与异常处理
    @PostMapping
    public ResponseEntity<Result<Student>> createStudent(@Valid @RequestBody Student student) {
        try {
            Student createdStudent = studentService.createStudent(student);
            return new ResponseEntity<>(Result.created(createdStudent), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // 学号/邮箱重复返回 400（业务冲突，符合文档要求（{insert\_element\_20\_}））
            return new ResponseEntity<>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // 2. 查询所有学生（GET /api/students）- 保留原功能
    @GetMapping
    public Result<List<Student>> getAllStudents() {
        List<Student> students = studentService.findAllStudents();
        return Result.success(students);
    }

    // 3. 按ID查询学生（GET /api/students/{id}）- 保留原功能
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable String id) {
        return studentService.findStudentById(id)
                .map(Result::success)
                .orElse(Result.error(404, "Student not found with id: " + id)); // 404 资源不存在（{insert\_element\_21\_}）
    }

    // 4. 按学号查询学生（新增，适配选课功能中的学生信息查询）
    @GetMapping("/studentId/{studentId}")
    public Result<Student> getStudentByStudentId(@PathVariable String studentId) {
        return studentService.findStudentByStudentId(studentId)
                .map(Result::success)
                .orElse(Result.error(404, "Student not found with studentId: " + studentId));
    }

    // 5. 按专业分页查询学生（新增，适配任务三按专业筛选要求（{insert\_element\_22\_}））
    @GetMapping("/major/{major}")
    public Result<Page<Student>> getStudentsByMajor(
            @PathVariable String major,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Page<Student> studentPage = studentService.findStudentsByMajor(major, pageNum, pageSize);
        return Result.success(studentPage);
    }

    // 6. 按年级分页查询学生（新增，适配任务三按年级筛选要求（{insert\_element\_23\_}））
    @GetMapping("/grade/{grade}")
    public Result<Page<Student>> getStudentsByGrade(
            @PathVariable Integer grade,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Page<Student> studentPage = studentService.findStudentsByGrade(grade, pageNum, pageSize);
        return Result.success(studentPage);
    }

    // 7. 更新学生信息（PUT /api/students/{id}）- 保留原功能与异常处理
    @PutMapping("/{id}")
    public ResponseEntity<Result<Student>> updateStudent(
            @PathVariable String id,
            @Valid @RequestBody Student student
    ) {
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            return new ResponseEntity<>(Result.success(updatedStudent), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    // 8. 删除学生（DELETE /api/students/{id}）- 保留原功能，新增关联检查异常处理
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteStudent(@PathVariable String id) {
        try {
            studentService.deleteStudent(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 无内容（符合 REST 规范）
        } catch (IllegalArgumentException e) {
            // 有选课记录禁止删除，返回 400（业务冲突，{insert\_element\_24\_}）
            return new ResponseEntity<>(Result.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Result.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }
}