package com.flowoa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("leave_apply")
public class LeaveApply implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private Long userId;

    /** 请假类型: annual/sick/personal/maternity/marriage/bereavement */
    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal days;

    private String reason;

    /** 状态: draft/pending/approved/rejected/cancelled */
    private String status;

    private Long flowInstanceId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 申请人姓名(非数据库字段) */
    @TableField(exist = false)
    private String userName;

    /** 申请人部门(非数据库字段) */
    @TableField(exist = false)
    private String deptName;
}
