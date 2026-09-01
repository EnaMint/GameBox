-- =============================================
-- gamebox_team 库：组队帖、组队申请
-- 供 gamebox-team 服务使用
-- =============================================
CREATE DATABASE IF NOT EXISTS gamebox_team DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE gamebox_team;

CREATE TABLE IF NOT EXISTS t_team_post (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT UNSIGNED NOT NULL COMMENT '队长ID',
  game_id      BIGINT UNSIGNED NOT NULL COMMENT '游戏ID',
  title        VARCHAR(100)  NOT NULL COMMENT '标题',
  content      VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '招募说明',
  member_limit INT           NOT NULL DEFAULT 4 COMMENT '目标人数',
  member_count INT           NOT NULL DEFAULT 1 COMMENT '当前人数（含队长）',
  need_voice   TINYINT       NOT NULL DEFAULT 0 COMMENT '1需要开麦',
  play_time    VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '游戏时间，如：周末晚上',
  status       TINYINT       NOT NULL DEFAULT 1 COMMENT '1招募中 2已满员 3已关闭',
  created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_game_status (game_id, status),
  KEY idx_user (user_id),
  KEY idx_created (created_at)
) ENGINE = InnoDB COMMENT '组队帖';

CREATE TABLE IF NOT EXISTS t_team_application (
  id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  post_id    BIGINT UNSIGNED NOT NULL COMMENT '组队帖ID',
  user_id    BIGINT UNSIGNED NOT NULL COMMENT '申请人ID',
  message    VARCHAR(500) NOT NULL DEFAULT '' COMMENT '申请留言',
  status     TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2拒绝 3撤回',
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_user (post_id, user_id),
  KEY idx_user (user_id)
) ENGINE = InnoDB COMMENT '组队申请';
