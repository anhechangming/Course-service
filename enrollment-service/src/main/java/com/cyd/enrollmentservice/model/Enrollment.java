package com.cyd.enrollmentservice.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity  // 标记为JPA实体，对应数据库表
@Table(
        name = "enrollments",  // 映射数据库表名，符合文档“实体-表名”对应要求（{insert\_element\_15\_}）
        // 核心约束：课程与学生双重唯一（同一学生不能重复选同一门课，文档要求{insert\_element\_16\_}）
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"course_id", "student_id"},
                name = "uk_enrollment_course_student"
        ),
        // 索引配置：优化“按课程/学生/状态组合查询”效率（文档要求支持“组合查询”{insert\_element\_17\_}）
        indexes = {
                @Index(columnList = "course_id", name = "idx_enrollment_course"),
                @Index(columnList = "student_id", name = "idx_enrollment_student"),
                @Index(columnList = "course_id, status", name = "idx_enrollment_course_status"),
                @Index(columnList = "student_id,status", name = "idx_enrollment_student_status")
        }
)
public class Enrollment {
    // 选课记录唯一ID（系统自动生成UUID）
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // 课程ID（关联Course的id字段），必填
    @Column(name = "course_id", nullable = false)
    @NotBlank(message = "Course ID cannot be blank")
    private String courseId;

    // 学生学号（关联Student的studentId字段），必填
    @Column(name = "student_id", nullable = false)
    @NotBlank(message = "Student ID (studentId) cannot be blank")
    private String studentId;

    // 选课状态：枚举类型（文档要求“使用枚举类型表示选课状态”{insert\_element\_20\_}）
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)  // 枚举值以字符串形式存储（可读性更高，避免数字枚举歧义）
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;  // 默认值：已选课（ACTIVE）

    // 选课时间：自动填充（文档要求“包含选课时长”，此处精确到时间戳{insert\_element\_21\_}）
   // @CreationTimestamp  // Hibernate自动填充选课时间，无需手动set
    @Column(name = "enroll_time", updatable = false,nullable = false)  // 禁止更新选课时间
    private LocalDateTime enrollTime;
    // -------------------------- @PrePersist 回调方法 --------------------------
    @PrePersist
    public void prePersist() {
        // 1. 填充“选课时间”为当前时间（时间戳）
        this.enrollTime = LocalDateTime.now();
        // 2. 填充“默认状态”为 ACTIVE（已选课，文档隐含“默认选课状态为有效”的需求）
        this.status = EnrollmentStatus.ACTIVE;
    }

}