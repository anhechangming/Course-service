package com.cyd.enrollmentservice.model;

// 选课状态枚举：符合文档“使用枚举类型表示选课状态”要求（{insert\_element\_14\_}）
public enum EnrollmentStatus {
    ACTIVE,    // 已选课（活跃状态）
    DROPPED,   // 已退课
    COMPLETED  // 已结课（可选，根据业务扩展）
}