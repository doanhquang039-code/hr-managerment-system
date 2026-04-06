-- Fix migration history - xóa record failed V13 để cho phép app start
DELETE FROM flyway_schema_history WHERE version = 13 AND success = 0;
