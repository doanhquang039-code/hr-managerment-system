SET @measurement_type_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'key_results'
      AND column_name = 'measurement_type'
);

SET @measurement_type_sql = IF(
    @measurement_type_exists = 0,
    'ALTER TABLE key_results ADD COLUMN measurement_type VARCHAR(50) NOT NULL DEFAULT ''NUMBER'' AFTER metric_type',
    'SELECT 1'
);

PREPARE measurement_type_stmt FROM @measurement_type_sql;
EXECUTE measurement_type_stmt;
DEALLOCATE PREPARE measurement_type_stmt;

UPDATE key_results
SET measurement_type = COALESCE(NULLIF(measurement_type, ''), metric_type, 'NUMBER');
