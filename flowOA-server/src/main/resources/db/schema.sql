
CREATE DATABASE IF NOT EXISTS flowoa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE flowoa;

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    name        VARCHAR(50)  NOT NULL,
    sort        INT          NOT NULL DEFAULT 0,
    leader      VARCHAR(50)  DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    email       VARCHAR(50)  DEFAULT NULL,
    dept_id     BIGINT       DEFAULT NULL,
    role_id     BIGINT       DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS leave_apply;
CREATE TABLE leave_apply (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    title            VARCHAR(100) NOT NULL,
    user_id          BIGINT       NOT NULL,
    leave_type       VARCHAR(20)  NOT NULL,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    days             DECIMAL(4,1) NOT NULL,
    reason           VARCHAR(500) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'draft',
    flow_instance_id BIGINT       DEFAULT NULL,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS expense_apply;
CREATE TABLE expense_apply (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    title            VARCHAR(100)  NOT NULL,
    user_id          BIGINT        NOT NULL,
    expense_type     VARCHAR(20)   NOT NULL,
    amount           DECIMAL(12,2) NOT NULL,
    description      VARCHAR(500)  DEFAULT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'draft',
    flow_instance_id BIGINT        DEFAULT NULL,
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS generic_apply;
CREATE TABLE generic_apply (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    title            VARCHAR(100) NOT NULL,
    user_id          BIGINT       NOT NULL,
    apply_type       VARCHAR(50)  NOT NULL,
    content_json     TEXT         DEFAULT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'draft',
    flow_instance_id BIGINT       DEFAULT NULL,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS flow_definition;
CREATE TABLE flow_definition (
    id              BIGINT       NOT NULL,
    create_time     DATETIME     DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    create_by       VARCHAR(64)  DEFAULT NULL,
    update_by       VARCHAR(64)  DEFAULT NULL,
    tenant_id       VARCHAR(64)  DEFAULT NULL,
    del_flag        CHAR(1)      NOT NULL DEFAULT '0',
    flow_code       VARCHAR(64)  NOT NULL,
    flow_name       VARCHAR(128) NOT NULL,
    model_value     VARCHAR(32)  DEFAULT NULL,
    category        VARCHAR(64)  DEFAULT NULL,
    version         VARCHAR(32)  DEFAULT NULL,
    is_publish      TINYINT      DEFAULT 0,
    form_custom     CHAR(1)      DEFAULT 'N',
    form_path       VARCHAR(256) DEFAULT NULL,
    activity_status TINYINT      DEFAULT 1,
    listener_type   VARCHAR(256) DEFAULT NULL,
    listener_path   VARCHAR(256) DEFAULT NULL,
    ext             TEXT         DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS flow_node;
CREATE TABLE flow_node (
    id              BIGINT       NOT NULL,
    create_time     DATETIME     DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    create_by       VARCHAR(64)  DEFAULT NULL,
    update_by       VARCHAR(64)  DEFAULT NULL,
    tenant_id       VARCHAR(64)  DEFAULT NULL,
    del_flag        CHAR(1)      NOT NULL DEFAULT '0',
    node_type       INT          NOT NULL,
    definition_id   BIGINT       NOT NULL,
    node_code       VARCHAR(64)  NOT NULL,
    node_name       VARCHAR(128) DEFAULT NULL,
    permission_flag VARCHAR(512) DEFAULT NULL,
    node_ratio      VARCHAR(64)  DEFAULT NULL,
    coordinate      VARCHAR(256) DEFAULT NULL,
    version         VARCHAR(32)  DEFAULT NULL,
    any_node_skip   VARCHAR(64)  DEFAULT NULL,
    listener_type   VARCHAR(256) DEFAULT NULL,
    listener_path   VARCHAR(256) DEFAULT NULL,
    form_custom     CHAR(1)      DEFAULT 'N',
    form_path       VARCHAR(256) DEFAULT NULL,
    ext             TEXT         DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS flow_skip;
CREATE TABLE flow_skip (
    id              BIGINT       NOT NULL,
    create_time     DATETIME     DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    create_by       VARCHAR(64)  DEFAULT NULL,
    update_by       VARCHAR(64)  DEFAULT NULL,
    tenant_id       VARCHAR(64)  DEFAULT NULL,
    del_flag        CHAR(1)      NOT NULL DEFAULT '0',
    definition_id   BIGINT       NOT NULL,
    now_node_code   VARCHAR(64)  NOT NULL,
    now_node_type   INT          DEFAULT NULL,
    next_node_code  VARCHAR(64)  NOT NULL,
    next_node_type  INT          DEFAULT NULL,
    skip_name       VARCHAR(128) DEFAULT NULL,
    skip_type       VARCHAR(32)  DEFAULT NULL,
    skip_condition  VARCHAR(512) DEFAULT NULL,
    coordinate      VARCHAR(256) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS flow_instance;
CREATE TABLE flow_instance (
    id              BIGINT       NOT NULL,
    create_time     DATETIME     DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    create_by       VARCHAR(64)  DEFAULT NULL,
    update_by       VARCHAR(64)  DEFAULT NULL,
    tenant_id       VARCHAR(64)  DEFAULT NULL,
    del_flag        CHAR(1)      NOT NULL DEFAULT '0',
    definition_id   BIGINT       NOT NULL,
    business_id     VARCHAR(64)  DEFAULT NULL,
    node_type       INT          DEFAULT NULL,
    node_code       VARCHAR(64)  DEFAULT NULL,
    node_name       VARCHAR(128) DEFAULT NULL,
    variable        TEXT         DEFAULT NULL,
    flow_status     VARCHAR(32)  DEFAULT NULL,
    activity_status TINYINT      DEFAULT 1,
    def_json        TEXT         DEFAULT NULL,
    ext             TEXT         DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS flow_task;
CREATE TABLE flow_task (
    id              BIGINT       NOT NULL,
    create_time     DATETIME     DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    create_by       VARCHAR(64)  DEFAULT NULL,
    update_by       VARCHAR(64)  DEFAULT NULL,
    tenant_id       VARCHAR(64)  DEFAULT NULL,
    del_flag        CHAR(1)      NOT NULL DEFAULT '0',
    definition_id   BIGINT       NOT NULL,
    instance_id     BIGINT       NOT NULL,
    node_code       VARCHAR(64)  DEFAULT NULL,
    node_name       VARCHAR(128) DEFAULT NULL,
    node_type       INT          DEFAULT NULL,
    flow_status     VARCHAR(32)  DEFAULT NULL,
    form_custom     CHAR(1)      DEFAULT 'N',
    form_path       VARCHAR(256) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS flow_user;
CREATE TABLE flow_user (
    id              BIGINT       NOT NULL,
    create_time     DATETIME     DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    create_by       VARCHAR(64)  DEFAULT NULL,
    update_by       VARCHAR(64)  DEFAULT NULL,
    tenant_id       VARCHAR(64)  DEFAULT NULL,
    del_flag        CHAR(1)      NOT NULL DEFAULT '0',
    type            CHAR(1)      NOT NULL,
    processed_by    VARCHAR(64)  NOT NULL,
    associated      BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS flow_his_task;
CREATE TABLE flow_his_task (
    id                BIGINT       NOT NULL,
    create_time       DATETIME     DEFAULT NULL,
    update_time       DATETIME     DEFAULT NULL,
    tenant_id         VARCHAR(64)  DEFAULT NULL,
    del_flag          CHAR(1)      NOT NULL DEFAULT '0',
    definition_id     BIGINT       NOT NULL,
    instance_id       BIGINT       NOT NULL,
    task_id           BIGINT       DEFAULT NULL,
    cooperate_type    INT          DEFAULT NULL,
    node_code         VARCHAR(64)  DEFAULT NULL,
    node_name         VARCHAR(128) DEFAULT NULL,
    node_type         INT          DEFAULT NULL,
    target_node_code  VARCHAR(64)  DEFAULT NULL,
    target_node_name  VARCHAR(128) DEFAULT NULL,
    approver          VARCHAR(64)  DEFAULT NULL,
    collaborator      VARCHAR(64)  DEFAULT NULL,
    skip_type         VARCHAR(32)  DEFAULT NULL,
    flow_status       VARCHAR(32)  DEFAULT NULL,
    message           TEXT         DEFAULT NULL,
    variable          TEXT         DEFAULT NULL,
    ext               TEXT         DEFAULT NULL,
    form_custom       CHAR(1)      DEFAULT 'N',
    form_path         VARCHAR(256) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS flow_form;
CREATE TABLE flow_form (
    id              BIGINT       NOT NULL,
    create_time     DATETIME     DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    create_by       VARCHAR(64)  DEFAULT NULL,
    update_by       VARCHAR(64)  DEFAULT NULL,
    del_flag        CHAR(1)      NOT NULL DEFAULT '0',
    form_code       VARCHAR(64)  DEFAULT NULL,
    form_name       VARCHAR(128) DEFAULT NULL,
    version         VARCHAR(32)  DEFAULT NULL,
    is_publish      TINYINT      DEFAULT 0,
    form_type       INT          DEFAULT NULL,
    form_path       VARCHAR(256) DEFAULT NULL,
    form_content    TEXT         DEFAULT NULL,
    ext             TEXT         DEFAULT NULL,
    tenant_id       VARCHAR(64)  DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;
