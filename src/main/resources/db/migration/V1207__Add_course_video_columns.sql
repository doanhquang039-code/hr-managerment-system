SET @add_video_url = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE courses ADD COLUMN video_url VARCHAR(500)',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'courses'
      AND column_name = 'video_url'
);

PREPARE add_video_url_stmt FROM @add_video_url;
EXECUTE add_video_url_stmt;
DEALLOCATE PREPARE add_video_url_stmt;

SET @add_video_public_id = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE courses ADD COLUMN video_public_id VARCHAR(255)',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'courses'
      AND column_name = 'video_public_id'
);

PREPARE add_video_public_id_stmt FROM @add_video_public_id;
EXECUTE add_video_public_id_stmt;
DEALLOCATE PREPARE add_video_public_id_stmt;
