package com.cyd.catalogservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "courses",
        // 补充：为课程代码添加唯一索引（文档要求“课程代码唯一”）
        uniqueConstraints = @UniqueConstraint(columnNames = "code"),
        // 补充：为排课信息添加索引（优化时间冲突查询效率）
        indexes = {
                @Index(name = "idx_course_schedule",
                        columnList = "schedule_dayOfWeek, schedule_startTime, schedule_endTime")
        }
)
public class Course {
    // 课程唯一ID（系统自动生成UUID）
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // 课程代码（如CS101），非空
    @Column(unique = true,nullable = false)
    private String code;

    // 课程名称（如计算机科学导论），非空
    @Column(nullable = false)
    private String title;

    // 讲师信息（嵌套验证）
    @Embedded
    private Instructor instructor;

    // 排课信息（嵌套验证）
    @Embedded
    private ScheduleSlot schedule;

    // 课程容量（最大选课人数），需为正数
    @Positive(message = "Course capacity must be positive")
    private Integer capacity;

    // 已选人数（初始为0，选课时自动增加）
    private Integer enrolled = 0;
    // 自动填充创建时间，无需手动设置
 //   @CreationTimestamp
    @Column(updatable = false)  // 禁止更新
    private LocalDateTime createTime;

    // 补充 @PrePersist 方法，同时填充时间戳（可选）和其他默认值
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        // 填充 enrolled 字段默认值为 0
        if (this.enrolled == null) {
            this.enrolled = 0;
        }
    }

}