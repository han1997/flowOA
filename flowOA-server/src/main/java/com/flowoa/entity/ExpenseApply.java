package com.flowoa.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    /** travel/meal/office/communication/other */
    private String expenseType;

    private BigDecimal amount;

    private String description;

    /** draft/pending/approved/rejected/cancelled */
    private String status;

    private Long flowInstanceId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** Applicant name (non-persistent field). */
    @TableField(exist = false)
    private String userName;

    /** Applicant department (non-persistent field). */
    @TableField(exist = false)
    private String deptName;

    /** Current node name (non-persistent field). */
    @TableField(exist = false)
    private String currentNodeName;

    /** Current approvers (non-persistent field). */
    @TableField(exist = false)
    private String currentApprovers;
}
