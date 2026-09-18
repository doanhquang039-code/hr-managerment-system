-- V25__Add_Audit_Fields_To_Remaining_Tables.sql
-- Wrapped with existence checks for idempotency

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='company_announcement' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE company_announcement ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='company_asset' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE company_asset ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='employee_benefit' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE employee_benefit ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='employee_warning' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE employee_warning ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='hr_audit_log' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE hr_audit_log ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='leaverequest' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE leaverequest ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='notification' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE notification ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='okr_progress' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE okr_progress ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='qr_codes' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE qr_codes ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='recognitions' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE recognitions ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='shift_assignments' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE shift_assignments ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='training_program' AND COLUMN_NAME='updated_at');
SET @s=IF(@c=0,'ALTER TABLE training_program ADD COLUMN updated_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- Missing created_at columns
SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='candidates' AND COLUMN_NAME='created_at');
SET @s=IF(@c=0,'ALTER TABLE candidates ADD COLUMN created_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @c=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='system_setting' AND COLUMN_NAME='created_at');
SET @s=IF(@c=0,'ALTER TABLE system_setting ADD COLUMN created_at DATETIME','SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
