package com.cyd.enrollmentservice.DTO;

import lombok.Data;

@Data
public class PageQueryDTO {
    private Integer pageNum = 1;   // 页码，默认第1页
    private Integer pageSize = 10; // 每页条数，默认10条
}