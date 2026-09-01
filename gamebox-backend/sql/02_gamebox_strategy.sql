-- =============================================
-- gamebox_strategy 库：攻略、评论、点赞
-- 供 gamebox-strategy 服务使用
-- =============================================
CREATE DATABASE IF NOT EXISTS gamebox_strategy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE gamebox_strategy;

CREATE TABLE IF NOT EXISTS t_strategy (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT UNSIGNED NOT NULL COMMENT '作者ID',
  game_id       BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联游戏ID，0=未关联',
  title         VARCHAR(100) NOT NULL COMMENT '标题',
  category      VARCHAR(20)  NOT NULL COMMENT '分类：攻略/评测/资讯/心得/其他',
  cover         VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面URL',
  summary       VARCHAR(300) NOT NULL DEFAULT '' COMMENT '列表页摘要',
  content       MEDIUMTEXT   NOT NULL COMMENT 'Markdown 正文',
  view_count    INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
  like_count    INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  comment_count INT          NOT NULL DEFAULT 0 COMMENT '评论数',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0已删除',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_user (user_id),
  KEY idx_category (category),
  KEY idx_game (game_id),
  KEY idx_created (created_at)
) ENGINE = InnoDB COMMENT '攻略';

CREATE TABLE IF NOT EXISTS t_strategy_comment (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  strategy_id BIGINT UNSIGNED NOT NULL,
  user_id     BIGINT UNSIGNED NOT NULL,
  parent_id   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=顶级评论',
  content     VARCHAR(1000) NOT NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_strategy (strategy_id, created_at)
) ENGINE = InnoDB COMMENT '攻略评论';

CREATE TABLE IF NOT EXISTS t_strategy_like (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  strategy_id BIGINT UNSIGNED NOT NULL,
  user_id     BIGINT UNSIGNED NOT NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_strategy_user (strategy_id, user_id)
) ENGINE = InnoDB COMMENT '攻略点赞';
