-- =============================================
-- gamebox_user 库：关注/粉丝 + 私信（二期增量脚本）
-- 在 01~04 已执行过的库上运行；全新环境请先跑 01
-- =============================================
USE gamebox_user;

-- 用户表新增计数列
ALTER TABLE t_user
  ADD COLUMN follow_count INT NOT NULL DEFAULT 0 COMMENT '关注数' AFTER bio,
  ADD COLUMN fans_count   INT NOT NULL DEFAULT 0 COMMENT '粉丝数' AFTER follow_count;

-- 关注关系
CREATE TABLE IF NOT EXISTS t_follow (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT UNSIGNED NOT NULL COMMENT '关注者',
  follow_user_id BIGINT UNSIGNED NOT NULL COMMENT '被关注者',
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_follow (user_id, follow_user_id),
  KEY idx_follow_user (follow_user_id)
) ENGINE = InnoDB COMMENT '关注关系';

-- 私信会话（user_a < user_b，一对用户唯一）
CREATE TABLE IF NOT EXISTS t_conversation (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_a       BIGINT UNSIGNED NOT NULL COMMENT '较小的用户ID',
  user_b       BIGINT UNSIGNED NOT NULL COMMENT '较大的用户ID',
  last_message VARCHAR(500) NOT NULL DEFAULT '' COMMENT '最后一条消息摘要',
  last_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后消息时间',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pair (user_a, user_b),
  KEY idx_user_a (user_a),
  KEY idx_user_b (user_b)
) ENGINE = InnoDB COMMENT '私信会话';

-- 私信消息
CREATE TABLE IF NOT EXISTS t_message (
  id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  conversation_id BIGINT UNSIGNED NOT NULL COMMENT '所属会话',
  from_user_id    BIGINT UNSIGNED NOT NULL COMMENT '发送者',
  to_user_id      BIGINT UNSIGNED NOT NULL COMMENT '接收者',
  content         VARCHAR(500) NOT NULL COMMENT '消息内容（纯文本）',
  read_flag       TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_conversation (conversation_id, id),
  KEY idx_to_read (to_user_id, read_flag)
) ENGINE = InnoDB COMMENT '私信消息';
