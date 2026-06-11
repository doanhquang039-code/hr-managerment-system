SET @drop_uk = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE collaboration_group_role_permissions DROP INDEX uk_collaboration_group_role_feature',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'collaboration_group_role_permissions'
      AND index_name = 'uk_collaboration_group_role_feature'
);

PREPARE drop_uk_stmt FROM @drop_uk;
EXECUTE drop_uk_stmt;
DEALLOCATE PREPARE drop_uk_stmt;

SET @drop_uk2 = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE collaboration_group_role_permissions DROP INDEX UKfg77fpn9jk8gsuq5rafpydktj',
        'SELECT 1'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'collaboration_group_role_permissions'
      AND index_name = 'UKfg77fpn9jk8gsuq5rafpydktj'
);

PREPARE drop_uk2_stmt FROM @drop_uk2;
EXECUTE drop_uk2_stmt;
DEALLOCATE PREPARE drop_uk2_stmt;

SET @drop_col = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE collaboration_group_role_permissions DROP COLUMN role',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'collaboration_group_role_permissions'
      AND column_name = 'role'
);

PREPARE drop_col_stmt FROM @drop_col;
EXECUTE drop_col_stmt;
DEALLOCATE PREPARE drop_col_stmt;
