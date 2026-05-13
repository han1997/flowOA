package com.flowoa.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("generic_apply")
public class GenericApply implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private Long userId;

    /** Application type. */
    private String applyType;

    /** Application content in JSON. */
    private String contentJson;

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