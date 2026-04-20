-- FlowOA Database Schema

CREATE DATABASE IF NOT EXISTS flowoa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE flowoa;

-- ----------------------------
-- 系统角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  NOT NULL COMMENT '角色名称',
    code        VARCHAR(50)  NOT NULL COMMENT '角色编码',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(1正常 0停用)',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB COMMENT='系统角色表';

-- ----------------------------
-- 系统部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父部门ID',
    name        VARCHAR(50)  NOT NULL COMMENT '部门名称',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    leader      VARCHAR(50)  DEFAULT NULL COMMENT '负责人',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(1正常 0停用)',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='系统部门表';

-- ----------------------------
-- 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(50)  DEFAULT NULL COMMENT '邮箱',
    dept_id     BIGINT       DEFAULT NULL COMMENT '部门ID',
    role_id     BIGINT       DEFAULT NULL COMMENT '角色ID',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(1正常 0停用)',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='系统用户表';

-- ----------------------------
-- 请假申请表
-- ----------------------------
DROP TABLE IF EXISTS leave_apply;
CREATE TABLE leave_apply (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title            VARCHAR(100) NOT NULL COMMENT '标题',
    user_id          BIGINT       NOT NULL COMMENT '申请人ID',
    leave_type       VARCHAR(20)  NOT NULL COMMENT '请假类型(annual/sick/personal/maternity/marriage/bereavement)',
    start_date       DATE         NOT NULL COMMENT '开始日期',
    end_date         DATE         NOT NULL COMMENT '结束日期',
    days             DECIMAL(4,1) NOT NULL COMMENT '请假天数',
    reason           VARCHAR(500) NOT NULL COMMENT '请假原因',
    status           VARCHAR(20)  NOT NULL DEFAULT 'draft' COMMENT '状态(draft/pending/approved/rejected/cancelled)',
    flow_instance_id BIGINT       DEFAULT NULL COMMENT '流程实例ID',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='请假申请表';

-- ----------------------------
-- 报销申请表
-- ----------------------------
DROP TABLE IF EXISTS expense_apply;
CREATE TABLE expense_apply (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    title            VARCHAR(100)  NOT NULL COMMENT '标题',
    user_id          BIGINT        NOT NULL COMMENT '申请人ID',
    expense_type     VARCHAR(20)   NOT NULL COMMENT '报销类型(travel/meal/office/communication/other)',
    amount           DECIMAL(12,2) NOT NULL COMMENT '报销金额',
    description      VARCHAR(500)  DEFAULT NULL COMMENT '报销说明',
    status           VARCHAR(20)   NOT NULL DEFAULT 'draft' COMMENT '状态(draft/pending/approved/rejected/cancelled)',
    flow_instance_id BIGINT        DEFAULT NULL COMMENT '流程实例ID',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='报销申请表';

-- ----------------------------
-- 通用申请表
-- ----------------------------
DROP TABLE IF EXISTS generic_apply;
CREATE TABLE generic_apply (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title            VARCHAR(100) NOT NULL COMMENT '标题',
    user_id          BIGINT       NOT NULL COMMENT '申请人ID',
    apply_type       VARCHAR(50)  NOT NULL COMMENT '申请类型',
    content_json     TEXT         DEFAULT NULL COMMENT '申请内容(JSON)',
    status           VARCHAR(20)  NOT NULL DEFAULT 'draft' COMMENT '状态(draft/pending/approved/rejected/cancelled)',
    flow_instance_id BIGINT       DEFAULT NULL COMMENT '流程实例ID',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='通用申请表';

-- ----------------------------
-- warm-flow 流程定义表
-- ----------------------------
DROP TABLE IF EXISTS flow_definition;
CREATE TABLE flow_definition (
    id              BIGINT       NOT NULL COMMENT '主键',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    create_by       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    update_by       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    tenant_id       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标记(0正常 1删除)',
    flow_code       VARCHAR(64)  NOT NULL COMMENT '流程编码',
    flow_name       VARCHAR(128) NOT NULL COMMENT '流程名称',
    model_value     VARCHAR(32)  DEFAULT NULL COMMENT '设计器模型',
    category        VARCHAR(64)  DEFAULT NULL COMMENT '流程类别',
    version         VARCHAR(32)  DEFAULT NULL COMMENT '流程版本',
    is_publish      TINYINT      DEFAULT 0 COMMENT '是否发布(0未开启 1开启)',
    form_custom     CHAR(1)      DEFAULT 'N' COMMENT '审批表单是否自定义(Y是 N否)',
    form_path       VARCHAR(256) DEFAULT NULL COMMENT '审批表单路径',
    activity_status TINYINT      DEFAULT 1 COMMENT '流程激活状态(0挂起 1激活)',
    listener_type   VARCHAR(256) DEFAULT NULL COMMENT '监听器类型',
    listener_path   VARCHAR(256) DEFAULT NULL COMMENT '监听器路径',
    ext             TEXT         DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='流程定义表';

-- ----------------------------
-- warm-flow 流程节点表
-- ----------------------------
DROP TABLE IF EXISTS flow_node;
CREATE TABLE flow_node (
    id              BIGINT       NOT NULL COMMENT '主键',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    create_by       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    update_by       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    tenant_id       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标记(0正常 1删除)',
    node_type       INT          NOT NULL COMMENT '节点类型(0开始 1中间 2结束 3互斥网关 4并行网关)',
    definition_id   BIGINT       NOT NULL COMMENT '流程定义ID',
    node_code       VARCHAR(64)  NOT NULL COMMENT '节点编码',
    node_name       VARCHAR(128) DEFAULT NULL COMMENT '节点名称',
    permission_flag VARCHAR(512) DEFAULT NULL COMMENT '权限标识',
    node_ratio      VARCHAR(64)  DEFAULT NULL COMMENT '签署比例值',
    coordinate      VARCHAR(256) DEFAULT NULL COMMENT '节点坐标',
    version         VARCHAR(32)  DEFAULT NULL COMMENT '版本',
    any_node_skip   VARCHAR(64)  DEFAULT NULL COMMENT '任意节点跳转',
    listener_type   VARCHAR(256) DEFAULT NULL COMMENT '监听器类型',
    listener_path   VARCHAR(256) DEFAULT NULL COMMENT '监听器路径',
    form_custom     CHAR(1)      DEFAULT 'N' COMMENT '审批表单是否自定义(Y是 N否)',
    form_path       VARCHAR(256) DEFAULT NULL COMMENT '审批表单路径',
    ext             TEXT         DEFAULT NULL COMMENT '节点扩展属性',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='流程节点表';

-- ----------------------------
-- warm-flow 节点跳转关联表
-- ----------------------------
DROP TABLE IF EXISTS flow_skip;
CREATE TABLE flow_skip (
    id              BIGINT       NOT NULL COMMENT '主键',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    create_by       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    update_by       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    tenant_id       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标记(0正常 1删除)',
    definition_id   BIGINT       NOT NULL COMMENT '流程定义ID',
    now_node_code   VARCHAR(64)  NOT NULL COMMENT '当前节点编码',
    now_node_type   INT          DEFAULT NULL COMMENT '当前节点类型',
    next_node_code  VARCHAR(64)  NOT NULL COMMENT '下一节点编码',
    next_node_type  INT          DEFAULT NULL COMMENT '下一节点类型',
    skip_name       VARCHAR(128) DEFAULT NULL COMMENT '跳转名称',
    skip_type       VARCHAR(32)  DEFAULT NULL COMMENT '跳转类型(PASS通过 REJECT退回)',
    skip_condition  VARCHAR(512) DEFAULT NULL COMMENT '跳转条件',
    coordinate      VARCHAR(256) DEFAULT NULL COMMENT '跳转坐标',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='节点跳转关联表';

-- ----------------------------
-- warm-flow 流程实例表
-- ----------------------------
DROP TABLE IF EXISTS flow_instance;
CREATE TABLE flow_instance (
    id              BIGINT       NOT NULL COMMENT '主键',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    create_by       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    update_by       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    tenant_id       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标记(0正常 1删除)',
    definition_id   BIGINT       NOT NULL COMMENT '流程定义ID',
    business_id     VARCHAR(64)  DEFAULT NULL COMMENT '业务ID',
    node_type       INT          DEFAULT NULL COMMENT '节点类型',
    node_code       VARCHAR(64)  DEFAULT NULL COMMENT '节点编码',
    node_name       VARCHAR(128) DEFAULT NULL COMMENT '节点名称',
    variable        TEXT         DEFAULT NULL COMMENT '流程变量',
    flow_status     VARCHAR(32)  DEFAULT NULL COMMENT '流程状态(0待提交 1审批中 2审批通过 4终止 5作废 6撤销 8已完成 9已退回 10失效 11拿回)',
    activity_status TINYINT      DEFAULT 1 COMMENT '流程激活状态(0挂起 1激活)',
    def_json        TEXT         DEFAULT NULL COMMENT '流程定义JSON',
    ext             TEXT         DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='流程实例表';

-- ----------------------------
-- warm-flow 待办任务表
-- ----------------------------
DROP TABLE IF EXISTS flow_task;
CREATE TABLE flow_task (
    id              BIGINT       NOT NULL COMMENT '主键',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    create_by       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    update_by       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    tenant_id       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标记(0正常 1删除)',
    definition_id   BIGINT       NOT NULL COMMENT '流程定义ID',
    instance_id     BIGINT       NOT NULL COMMENT '流程实例ID',
    node_code       VARCHAR(64)  DEFAULT NULL COMMENT '节点编码',
    node_name       VARCHAR(128) DEFAULT NULL COMMENT '节点名称',
    node_type       INT          DEFAULT NULL COMMENT '节点类型',
    flow_status     VARCHAR(32)  DEFAULT NULL COMMENT '流程状态',
    form_custom     CHAR(1)      DEFAULT 'N' COMMENT '审批表单是否自定义',
    form_path       VARCHAR(256) DEFAULT NULL COMMENT '审批表单路径',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='待办任务表';

-- ----------------------------
-- warm-flow 流程用户表
-- ----------------------------
DROP TABLE IF EXISTS flow_user;
CREATE TABLE flow_user (
    id              BIGINT       NOT NULL COMMENT '主键',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    create_by       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    update_by       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    tenant_id       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标记(0正常 1删除)',
    type            CHAR(1)      NOT NULL COMMENT '人员类型(1审批人 2转办人 3委托人)',
    processed_by    VARCHAR(64)  NOT NULL COMMENT '权限人',
    associated      BIGINT       NOT NULL COMMENT '任务表ID',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='流程用户表';

-- ----------------------------
-- warm-flow 历史任务表
-- ----------------------------
DROP TABLE IF EXISTS flow_his_task;
CREATE TABLE flow_his_task (
    id                BIGINT       NOT NULL COMMENT '主键',
    create_time       DATETIME     DEFAULT NULL COMMENT '任务开始时间',
    update_time       DATETIME     DEFAULT NULL COMMENT '审批完成时间',
    tenant_id         VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    del_flag          CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标记(0正常 1删除)',
    definition_id     BIGINT       NOT NULL COMMENT '流程定义ID',
    instance_id       BIGINT       NOT NULL COMMENT '流程实例ID',
    task_id           BIGINT       DEFAULT NULL COMMENT '任务表ID',
    cooperate_type    INT          DEFAULT NULL COMMENT '协作方式(1审批 2转办 3委派 4会签 5票签 6加签 7减签)',
    node_code         VARCHAR(64)  DEFAULT NULL COMMENT '开始节点编码',
    node_name         VARCHAR(128) DEFAULT NULL COMMENT '开始节点名称',
    node_type         INT          DEFAULT NULL COMMENT '开始节点类型',
    target_node_code  VARCHAR(64)  DEFAULT NULL COMMENT '目标节点编码',
    target_node_name  VARCHAR(128) DEFAULT NULL COMMENT '目标节点名称',
    approver          VARCHAR(64)  DEFAULT NULL COMMENT '审批者',
    collaborator      VARCHAR(64)  DEFAULT NULL COMMENT '协作人',
    skip_type         VARCHAR(32)  DEFAULT NULL COMMENT '跳转类型(PASS通过 REJECT退回 NONE无动作)',
    flow_status       VARCHAR(32)  DEFAULT NULL COMMENT '流程状态',
    message           TEXT         DEFAULT NULL COMMENT '审批意见',
    variable          TEXT         DEFAULT NULL COMMENT '流程变量',
    ext               TEXT         DEFAULT NULL COMMENT '业务详情',
    form_custom       CHAR(1)      DEFAULT 'N' COMMENT '审批表单是否自定义',
    form_path         VARCHAR(256) DEFAULT NULL COMMENT '审批表单路径',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='历史任务表';

-- ----------------------------
-- warm-flow 表单表
-- ----------------------------
DROP TABLE IF EXISTS flow_form;
CREATE TABLE flow_form (
    id              BIGINT       NOT NULL COMMENT '主键',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    create_by       VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    update_by       VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    del_flag        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标记(0正常 1删除)',
    form_code       VARCHAR(64)  DEFAULT NULL COMMENT '表单编码',
    form_name       VARCHAR(128) DEFAULT NULL COMMENT '表单名称',
    version         VARCHAR(32)  DEFAULT NULL COMMENT '表单版本',
    is_publish      TINYINT      DEFAULT 0 COMMENT '是否发布(0未发布 1已发布 9失效)',
    form_type       INT          DEFAULT NULL COMMENT '表单类型(0内置 1外挂)',
    form_path       VARCHAR(256) DEFAULT NULL COMMENT '表单路径',
    form_content    TEXT         DEFAULT NULL COMMENT '表单内容',
    ext             TEXT         DEFAULT NULL COMMENT '扩展字段',
    tenant_id       VARCHAR(64)  DEFAULT NULL COMMENT '租户ID',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='表单表';
