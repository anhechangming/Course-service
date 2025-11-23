package com.cyd.catalogservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Instructor {
    // 讲师ID（如T001），非空
    @Column(name = "instructor_id", nullable = false)  // 列名改为 instructor_id
    @NotBlank(message = "Instructor ID cannot be blank")
    private String id;

    // 讲师姓名，非空
    @Column(name = "instructor_name", nullable = false)  // 建议同步指定姓名列名（可选，增强可读性）
    @NotBlank(message = "Instructor name cannot be blank")
    private String name;

    // 讲师邮箱，需符合标准格式
    @Column(name = "instructor_email", nullable = false)  // 建议同步指定姓名列名（可选，增强可读性）
    @NotBlank(message = "Instructor email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;
}