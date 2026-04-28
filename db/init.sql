-- NewsBack Database Schema
-- Auto-executed on first container startup

-- 1. Users
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NULL COMMENT '소셜 가입자는 null',
    `social_provider` VARCHAR(20) NOT NULL COMMENT 'LOCAL, GOOGLE, KAKAO',
    `fcm_token` TEXT NULL COMMENT '로그아웃 시 NULL로 업데이트',
    `refresh_token` VARCHAR(255) NULL COMMENT 'BCrypt 해시 refresh token (단일 세션, 재로그인 시 갱신)',
    `global_push_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Keywords
CREATE TABLE IF NOT EXISTS `keywords` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    `usage_count` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. User-Keywords
CREATE TABLE IF NOT EXISTS `user_keywords` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `keyword_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_user_keywords_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_keywords_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`id`) ON DELETE CASCADE
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
    `keyword_id` BIGINT NULL COMMENT '팀 합의 전 임시 필드',
    `title` VARCHAR(500) NULL COMMENT '대표 제목',
    `representative_summary` TEXT NULL COMMENT '클러스터 대표 요약',
    `news_count` INT DEFAULT 1 COMMENT '현재 포함된 기사 수',
    `last_summarized_count` INT DEFAULT 1 COMMENT '마지막 요약 시점의 기사 수',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_cluster_news_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`id`) ON DELETE SET NULL
);

-- 6. Keyword News
CREATE TABLE IF NOT EXISTS `keyword_news` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `keyword_id` BIGINT NOT NULL,
    `summary_text` TEXT NOT NULL,
    `summary_hash` VARCHAR(64) NULL COMMENT '이전 요약본과의 유사도 비교용',
    `cluster_news_count` INT DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_keyword_news_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`id`) ON DELETE CASCADE
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
ALTER TABLE `keyword_news`
    ADD COLUMN IF NOT EXISTS `cluster_news_count` INT DEFAULT 1;
ALTER TABLE `keyword_news`
    ADD COLUMN IF NOT EXISTS `summary_hash` VARCHAR(64) NULL COMMENT '이전 요약본과의 유사도 비교용';
