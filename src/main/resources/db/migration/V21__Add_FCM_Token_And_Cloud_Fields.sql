-- V21: Thêm FCM token cho push notifications + cloud storage fields

-- FCM token cho Firebase push notifications
ALTER TABLE user
    ADD COLUMN fcm_token VARCHAR(500) DEFAULT NULL COMMENT 'Firebase Cloud Messaging token';

-- Google Drive file ID cho documents
ALTER TABLE employee_document
    ADD COLUMN drive_file_id VARCHAR(200) DEFAULT NULL COMMENT 'Google Drive file ID',
    ADD COLUMN s3_key        VARCHAR(500) DEFAULT NULL COMMENT 'AWS S3 object key';

-- S3 backup key cho contracts
ALTER TABLE contract
    ADD COLUMN s3_key VARCHAR(500) DEFAULT NULL COMMENT 'AWS S3 backup key';

CREATE INDEX idx_user_fcm ON user(fcm_token(100));
