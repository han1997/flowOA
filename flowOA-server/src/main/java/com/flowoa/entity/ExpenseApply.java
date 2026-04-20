package com.flowoa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("expense_apply")
public class ExpenseApply implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private Long userId;

    /** 报销类型: travel/meal/office/communication/other */
    private String expenseType;

    private BigDecimal amount;

    private String description;

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
