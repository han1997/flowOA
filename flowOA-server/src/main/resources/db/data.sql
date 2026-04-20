-- FlowOA Initial Data
USE flowoa;

-- 初始化角色
INSERT INTO sys_role (id, name, code, status) VALUES
(1, '超级管理员', 'admin', 1),
(2, '部门经理', 'manager', 1),
(3, '普通员工', 'employee', 1);

-- 初始化部门
INSERT INTO sys_dept (id, parent_id, name, sort, leader, status) VALUES
(1, 0, '总公司', 1, '管理员', 1),
(2, 1, '技术部', 1, NULL, 1),
(3, 1, '市场部', 2, NULL, 1),
(4, 1, '财务部', 3, NULL, 1),
(5, 1, '人事部', 4, NULL, 1);

-- 初始化用户 (密码: admin123, 使用BCrypt加密)
INSERT INTO sys_user (id, username, password, name, phone, email, dept_id, role_id, status) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '管理员', '13800138000', 'admin@flowoa.com', 1, 1, 1),
(2, 'manager1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张经理', '13800138001', 'manager1@flowoa.com', 2, 2, 1),
(3, 'employee1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李员工', '13800138002', 'employee1@flowoa.com', 2, 3, 1),
(4, 'finance1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王财务', '13800138003', 'finance1@flowoa.com', 4, 3, 1);
