package com.cyd.catalogservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ScheduleSlot {
//    // 星期（如MONDAY、TUESDAY，需符合枚举规范）
//    @Column(name = "schedule_dayOfWeek", nullable = false)
//    @NotBlank(message = "Day of week cannot be blank")
//    private String dayOfWeek;
//
//    // 开始时间（如08:00）
//    @Column(name = "schedule_startTime", nullable = false)
//    @NotBlank(message = "Start time cannot be blank")
//    private String startTime;
//
//    // 结束时间（如10:00）
//    @Column(name = "schedule_endTime", nullable = false)
//    @NotBlank(message = "End time cannot be blank")
//    private String endTime;
//
//    // 预期出勤人数，需为正数
//    @Column(name = "schedule_expectedAttendance", nullable = false)
//    @Positive(message = "Expected attendance must be positive")
//    private Integer expectedAttendance;
    @Column(name = "schedule_day_Of_Week", nullable = false)  // 匹配实际列名
    @NotBlank(message = "Day of week cannot be blank")
    private String dayOfWeek;

    @Column(name = "schedule_start_Time", nullable = false)
    @NotBlank(message = "Start time cannot be blank")
    private String startTime;

    @Column(name = "schedule_end_Time", nullable = false)
    @NotBlank(message = "End time cannot be blank")
    private String endTime;

    @Column(name = "schedule_expected_Attendance", nullable = false)
    @Positive(message = "Expected attendance must be positive")
    private Integer expectedAttendance;

}