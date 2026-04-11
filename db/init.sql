-- NewsBack Database Schema
-- Auto-executed on first container startup

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NULL COMMENT '소셜 가입자는 null',
    `social_provider` VARCHAR(20) NOT NULL COMMENT 'LOCAL, GOOGLE, KAKAO',
    `fcm_token` TEXT NULL COMMENT '로그아웃 시 NULL로 업데이트',
    `refresh_token` VARCHAR(1024) NULL UNIQUE COMMENT '단일 세션용 refresh token',
    `global_push_enabled` BOOLEAN DEFAULT TRUE,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Keywords Dictionary Table
CREATE TABLE IF NOT EXISTS `keywords` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE
);

-- 2-1. User-Keywords Mapping Table
CREATE TABLE IF NOT EXISTS `user_keywords` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `keyword_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_user_keywords_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_keywords_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`id`) ON DELETE CASCADE
);

-- 4. Cluster News (AI Server Grouping Result)
CREATE TABLE IF NOT EXISTS `cluster_news` (
    `id` CHAR(36) PRIMARY KEY COMMENT 'UUID',
    `keyword_id` BIGINT NULL COMMENT 'AI가 추출한 키워드와 매칭된 결과',
    `title` VARCHAR(500) NULL COMMENT '대표 제목',
    `representative_summary` TEXT NULL COMMENT '바구니 요약 내용',
    `news_count` INT DEFAULT 1 COMMENT '현재 포함된 기사 수',
    `last_summarized_count` INT DEFAULT 1 COMMENT '마지막 요약 시점의 기사 수 (5개 추가 체크용)',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '요약 갱신 시각 (20분 주기 감지용)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_cluster_news_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`id`) ON DELETE SET NULL
);

-- 3. News Original Table (references cluster_news)
CREATE TABLE IF NOT EXISTS `news` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT 'uuid / title hash',
    `cluster_id` CHAR(36) NULL COMMENT '클러스터링 전에는 null',
    `title` VARCHAR(500) NOT NULL,
    `content` TEXT NOT NULL,
    `source` VARCHAR(50) NOT NULL,
    `author` VARCHAR(255) DEFAULT 'Unknown',
    `language` VARCHAR(10) NOT NULL,
    `region` VARCHAR(50) NOT NULL,
    `url` VARCHAR(768) NOT NULL UNIQUE,
    `thumbnail_url` TEXT,
    `processing_status` VARCHAR(20) DEFAULT 'READY' COMMENT 'READY, PROCESSING, COMPLETED, ERROR',
    `published_at` DATETIME NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_news_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `cluster_news` (`id`) ON DELETE SET NULL
);

-- 5. Keyword Summary Table
CREATE TABLE IF NOT EXISTS `keyword_news` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `keyword_id` BIGINT NOT NULL,
    `summary_text` TEXT NOT NULL,
    `summary_hash` VARCHAR(64) NULL COMMENT '이전 요약본과의 유사도 비교용',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_keyword_news_keyword` FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`id`) ON DELETE CASCADE
);

-- 5-1. Final Summary-Cluster Mapping Table
CREATE TABLE IF NOT EXISTS `keyword_news_clusters` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `keyword_news_id` BIGINT NOT NULL,
    `cluster_id` CHAR(36) NOT NULL COMMENT '최종 요약에 사용된 중요5+최신5 클러스터 ID들을 저장',
    CONSTRAINT `fk_knc_keyword_news` FOREIGN KEY (`keyword_news_id`) REFERENCES `keyword_news` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_knc_cluster` FOREIGN KEY (`cluster_id`) REFERENCES `cluster_news` (`id`) ON DELETE CASCADE
);

-- 6. Notification History
CREATE TABLE IF NOT EXISTS `notification_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `keyword_news_id` BIGINT NOT NULL,
    `sent_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_noti_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_noti_keyword_news` FOREIGN KEY (`keyword_news_id`) REFERENCES `keyword_news` (`id`) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_user_keywords_user_id ON user_keywords(user_id);
CREATE INDEX idx_user_keywords_keyword_id ON user_keywords(keyword_id);
CREATE INDEX idx_cluster_news_keyword_id ON cluster_news(keyword_id);
CREATE INDEX idx_news_cluster_id ON news(cluster_id);
CREATE INDEX idx_news_published_at ON news(published_at);
CREATE INDEX idx_keyword_news_keyword_id ON keyword_news(keyword_id);
CREATE INDEX idx_notification_history_user_id ON notification_history(user_id);
