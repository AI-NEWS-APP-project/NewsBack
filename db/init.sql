-- NewsBack Database Schema
-- Auto-executed on first container startup

-- 1. Users
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NULL COMMENT '소셜 가입자는 null',
    `social_provider` VARCHAR(20) NOT NULL COMMENT 'LOCAL, GOOGLE, KAKAO',
    `refresh_token` VARCHAR(255) NULL COMMENT 'BCrypt 해시 refresh token (단일 세션, 재로그인 시 갱신)',
    `global_push_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Keywords
CREATE TABLE IF NOT EXISTS `keyword` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    `usage_count` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. User-Keywords
CREATE TABLE IF NOT EXISTS `user_keyword` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `keyword_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_user_keyword_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_keyword_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`id`) ON DELETE CASCADE,
    CONSTRAINT `uk_user_keyword_user_keyword` UNIQUE (`user_id`, `keyword_id`)
);

-- 4. News
CREATE TABLE IF NOT EXISTS `news` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT 'UUID',
    `cluster_id` CHAR(36) NULL COMMENT 'AI server cluster id',
    `title` VARCHAR(500) NOT NULL,
    `content` TEXT NOT NULL,
    `source` VARCHAR(50) NOT NULL,
    `author` VARCHAR(255) NULL,
    `language` VARCHAR(10) NOT NULL,
    `region` VARCHAR(50) NOT NULL,
    `url` VARCHAR(768) NOT NULL UNIQUE,
    `thumbnail_url` TEXT NULL,
    `published_at` DATETIME NOT NULL,
    `processing_status` VARCHAR(20) NOT NULL DEFAULT 'READY',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 5. Cluster News
CREATE TABLE IF NOT EXISTS `cluster_news` (
    `id` CHAR(36) PRIMARY KEY COMMENT 'UUID',
    `title` VARCHAR(500) NULL COMMENT '대표 제목',
    `representative_summary` TEXT NULL COMMENT '클러스터 대표 요약',
    `news_count` INT DEFAULT 1 COMMENT '현재 포함된 기사 수',
    `last_summarized_count` INT DEFAULT 1 COMMENT '마지막 요약 시점의 기사 수',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 6. Keyword News
CREATE TABLE IF NOT EXISTS `keyword_news` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `keyword_id` BIGINT NOT NULL,
    `summary_text` TEXT NOT NULL,
    `cluster_news_count` INT DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_keyword_news_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keyword` (`id`) ON DELETE CASCADE
);

-- 7. Keyword News Links
CREATE TABLE IF NOT EXISTS `keyword_news_links` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `keyword_news_id` BIGINT NOT NULL,
    `url` VARCHAR(768) NOT NULL,
    `title` VARCHAR(500) NOT NULL,
    CONSTRAINT `fk_keyword_news_links_keyword_news` FOREIGN KEY (`keyword_news_id`) REFERENCES `keyword_news` (`id`) ON DELETE CASCADE
);

-- 8. Keyword News-Cluster Mapping
CREATE TABLE IF NOT EXISTS `keyword_news_clusters` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `keyword_news_id` BIGINT NOT NULL,
    `cluster_id` CHAR(36) NOT NULL,
    CONSTRAINT `fk_knc_keyword_news` FOREIGN KEY (`keyword_news_id`) REFERENCES `keyword_news` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_knc_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `cluster_news` (`id`) ON DELETE CASCADE
);

-- 9. Today News Summaries
CREATE TABLE IF NOT EXISTS `today_news_summaries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(500) NOT NULL,
    `summary` TEXT NOT NULL,
    `news_count` INT DEFAULT 0,
    `generated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. Today News Summary-News Mapping
CREATE TABLE IF NOT EXISTS `today_news_summary_news` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `today_news_summary_id` BIGINT NOT NULL,
    `news_id` VARCHAR(64) NOT NULL,
    CONSTRAINT `fk_today_summary_news_summary` FOREIGN KEY (`today_news_summary_id`) REFERENCES `today_news_summaries` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_today_summary_news_news` FOREIGN KEY (`news_id`) REFERENCES `news` (`id`) ON DELETE CASCADE
);

-- 11. FCM Tokens
CREATE TABLE IF NOT EXISTS `fcm_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `token` VARCHAR(512) NOT NULL UNIQUE,
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `last_used_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_fcm_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

-- 12. Notification Histories
CREATE TABLE IF NOT EXISTS `notification_histories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `keyword_news_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `body` VARCHAR(1000) NOT NULL,
    `route` VARCHAR(512) NOT NULL,
    `success` BOOLEAN NOT NULL,
    `failure_reason` TEXT NULL,
    `sent_at` DATETIME NOT NULL,
    `read_at` DATETIME NULL COMMENT 'NULL이면 미읽음, 값이 있으면 읽은 시각',
    CONSTRAINT `uk_notification_histories_user_keyword_news` UNIQUE (`user_id`, `keyword_news_id`),
    CONSTRAINT `fk_notification_histories_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_notification_histories_keyword_news` FOREIGN KEY (`keyword_news_id`) REFERENCES `keyword_news` (`id`) ON DELETE CASCADE
);

-- Backfill / migration-safe alters for existing environments.
-- Note: docker-entrypoint-initdb.d scripts run only on first DB volume initialization.
ALTER TABLE `users`
    ADD COLUMN IF NOT EXISTS `refresh_token` VARCHAR(255) NULL COMMENT 'BCrypt 해시 refresh token (단일 세션, 재로그인 시 갱신)';
ALTER TABLE `users`
    MODIFY COLUMN `refresh_token` VARCHAR(255) NULL COMMENT 'BCrypt 해시 refresh token (단일 세션, 재로그인 시 갱신)';
ALTER TABLE `users`
    ADD COLUMN IF NOT EXISTS `global_push_enabled` BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE `users`
    MODIFY COLUMN `global_push_enabled` BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE `users`
    DROP COLUMN IF EXISTS `fcm_token`;
CREATE INDEX IF NOT EXISTS `idx_user_keyword_user_id` ON `user_keyword` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_user_keyword_keyword_id` ON `user_keyword` (`keyword_id`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_user_keyword_user_keyword` ON `user_keyword` (`user_id`, `keyword_id`);
SET @drop_cluster_news_keyword_fk = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.TABLE_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA = DATABASE()
              AND TABLE_NAME = 'cluster_news'
              AND CONSTRAINT_NAME = 'fk_cluster_news_keyword'
              AND CONSTRAINT_TYPE = 'FOREIGN KEY'
        ),
        'ALTER TABLE `cluster_news` DROP FOREIGN KEY `fk_cluster_news_keyword`',
        'SELECT 1'
    )
);
PREPARE drop_cluster_news_keyword_fk_stmt FROM @drop_cluster_news_keyword_fk;
EXECUTE drop_cluster_news_keyword_fk_stmt;
DEALLOCATE PREPARE drop_cluster_news_keyword_fk_stmt;
ALTER TABLE `cluster_news`
    DROP COLUMN IF EXISTS `keyword_id`;
ALTER TABLE `keyword_news`
    ADD COLUMN IF NOT EXISTS `cluster_news_count` INT DEFAULT 1;
ALTER TABLE `notification_histories`
    ADD COLUMN IF NOT EXISTS `read_at` DATETIME NULL COMMENT 'NULL이면 미읽음, 값이 있으면 읽은 시각';
ALTER TABLE `notification_histories`
    ADD COLUMN IF NOT EXISTS `title` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `notification_histories`
    ADD COLUMN IF NOT EXISTS `body` VARCHAR(1000) NOT NULL DEFAULT '';
ALTER TABLE `notification_histories`
    ADD COLUMN IF NOT EXISTS `route` VARCHAR(512) NOT NULL DEFAULT '';
