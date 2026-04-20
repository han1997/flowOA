package com.flowoa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String name;

    private String phone;

    private String email;

    private Long deptId;

    private Long roleId;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /** 部门名称(非数据库字段) */
    @TableField(exist = false)
    private String deptName;

    /** 角色名称(非数据库字段) */
    @TableField(exist = false)
    private String roleName;

    /** 角色编码(非数据库字段) */
    @TableField(exist = false)
    private String roleCode;
}
