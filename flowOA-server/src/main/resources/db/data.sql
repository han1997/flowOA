-- FlowOA initial data
USE flowoa;

-- Roles
INSERT INTO sys_role (id, name, code, status) VALUES
(1, 'Administrator', 'admin', 1),
(2, 'Department Manager', 'manager', 1),
(3, 'Employee', 'employee', 1);

-- Departments
INSERT INTO sys_dept (id, parent_id, name, sort, leader, status) VALUES
(1, 0, 'Headquarters', 1, 'Admin User', 1),
(2, 1, 'IT', 1, NULL, 1),
(3, 1, 'Marketing', 2, NULL, 1),
(4, 1, 'Finance', 3, NULL, 1),
(5, 1, 'HR', 4, NULL, 1);

-- Users (password: admin123, BCrypt hash)
INSERT INTO sys_user (id, username, password, name, phone, email, dept_id, role_id, status) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'Admin User', '13800138000', 'admin@flowoa.com', 1, 1, 1),
(2, 'manager1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'Manager One', '13800138001', 'manager1@flowoa.com', 2, 2, 1),
(3, 'employee1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'Employee One', '13800138002', 'employee1@flowoa.com', 2, 3, 1),
(4, 'finance1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'Finance One', '13800138003', 'finance1@flowoa.com', 4, 3, 1);