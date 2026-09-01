-- =============================================
-- gamebox_record 库：战绩动态、战绩点赞
-- 供 gamebox-record 服务使用
-- =============================================
CREATE DATABASE IF NOT EXISTS gamebox_record DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE gamebox_record;

CREATE TABLE IF NOT EXISTS t_game_record (
  id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT UNSIGNED NOT NULL COMMENT '发布者ID',
  game_id    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '游戏ID，0=未关联',
  images     VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '图片URL，逗号分隔，最多9张',
  content    VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '描述',
  like_count INT           NOT NULL DEFAULT 0 COMMENT '点赞数',
  status     TINYINT       NOT NULL DEFAULT 1 COMMENT '1正常 0已删除',
  created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user (user_id),
  KEY idx_created (created_at)
) ENGINE = InnoDB COMMENT '战绩动态';

CREATE TABLE IF NOT EXISTS t_record_like (
  id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  record_id  BIGINT UNSIGNED NOT NULL,
  user_id    BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_record_user (record_id, user_id)
) ENGINE = InnoDB COMMENT '战绩点赞';
