package com.flowoa.entity;

import com.baomidou.mybatisplus.annotation.*;
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

    /** 申请类型 */
    private String applyType;

    /** 申请内容(JSON) */
    private String contentJson;

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
