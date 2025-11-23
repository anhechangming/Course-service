package com.cyd.enrollmentservice.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "student_id", name = "uk_student_studentid"),
                @UniqueConstraint(columnNames = "email", name = "uk_student_email")
        },
        indexes = {
                @Index(columnList = "major", name = "idx_student_major"),
                @Index(columnList = "grade", name = "idx_student_grade")
        }
)
public class Student {
    // 系统自动生成UUID，无需请求体提供
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // 学号（如S2024001），全局唯一，必填
    @Column(name = "student_id", nullable = false, length = 20)
    @NotBlank(message = "Student ID (studentId) cannot be blank")
    private String studentId;

    // 学生姓名，必填
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Student name cannot be blank")
    private String name;

    // 专业名称，必填
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Student major cannot be blank")
    private String major;

    // 入学年份（如2024），必填
    @Column(nullable = false)
    @NotNull(message = "Student grade cannot be null")
    private Integer grade;

    // 邮箱，需符合标准格式，必填
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Student email cannot be blank")
    @Email(message = "Invalid email format (e.g. xxx@xxx.com)")
    private String email;

    // 创建时间：自动填充，禁止更新（文档要求“自动维护创建时间”{insert\_element\_9\_}）
 //   @CreationTimestamp  // Hibernate自动填充当前时间，无需手动set
    @Column(name = "created_at", updatable = false,nullable = false)  // 数据库字段名标准化，禁止更新
    private LocalDateTime createdAt;

   // -------------------------- @PrePersist 回调方法 --------------------------
    // 作用：在 Student 实体被首次保存到数据库（insert）之前，自动执行此方法
    @PrePersist
    public void prePersist() {
        // 1. 填充“创建时间”为当前时间（时间戳）
        this.createdAt = LocalDateTime.now();
        // （可选）若有其他默认值，也可在此设置，如“默认专业为‘未分配’”
        // if (this.major == null) {
        //     this.major = "未分配";
        // }
    }
}