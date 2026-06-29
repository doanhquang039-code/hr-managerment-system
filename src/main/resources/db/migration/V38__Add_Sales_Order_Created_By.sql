SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sales_orders'
      AND COLUMN_NAME = 'created_by'
);

SET @sql := IF(
    @column_exists = 0,
    'ALTER TABLE sales_orders ADD COLUMN created_by INT NULL',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sales_orders'
      AND INDEX_NAME = 'idx_sales_orders_created_by'
);

SET @sql := IF(
    @index_exists = 0,
    'CREATE INDEX idx_sales_orders_created_by ON sales_orders(created_by)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sales_orders'
      AND CONSTRAINT_NAME = 'fk_sales_orders_created_by'
);

SET @sql := IF(
    @fk_exists = 0,
    'ALTER TABLE sales_orders ADD CONSTRAINT fk_sales_orders_created_by FOREIGN KEY (created_by) REFERENCES `user`(id)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
