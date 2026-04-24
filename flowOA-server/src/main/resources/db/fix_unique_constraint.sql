-- Fix unique constraints to support soft delete
-- Problem: Original unique constraints don't consider deleted status
-- Solution: Use MySQL 8.0 functional index to only enforce uniqueness for non-deleted records (deleted=0)
-- This allows multiple deleted records with the same code/username

USE flowoa;

-- Fix sys_role: change unique constraint to functional index
-- Only enforce uniqueness when deleted = 0
ALTER TABLE sys_role DROP INDEX uk_code;
ALTER TABLE sys_role ADD UNIQUE INDEX uk_code_active ((CASE WHEN deleted = 0 THEN code ELSE NULL END));

-- Fix sys_user: change unique constraint to functional index
-- Only enforce uniqueness when deleted = 0
ALTER TABLE sys_user DROP INDEX uk_username;
ALTER TABLE sys_user ADD UNIQUE INDEX uk_username_active ((CASE WHEN deleted = 0 THEN username ELSE NULL END));
