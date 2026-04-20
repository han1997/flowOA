package com.flowoa.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyApplyVO {
    private Long id;
    private String title;
    private String type;
    private String typeName;
    private String status;
    private String userName;
    private LocalDateTime createTime;
}
