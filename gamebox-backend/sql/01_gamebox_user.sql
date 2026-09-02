-- =============================================
-- gamebox_user 库：用户、游戏字典、用户游戏库
-- 供 gamebox-auth / gamebox-user 服务使用
-- =============================================
CREATE DATABASE IF NOT EXISTS gamebox_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE gamebox_user;

CREATE TABLE IF NOT EXISTS t_user (
  id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(32)  NOT NULL COMMENT '登录名',
  password   VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密',
  nickname   VARCHAR(32)  NOT NULL COMMENT '昵称',
  avatar     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像URL',
  bio        VARCHAR(200) NOT NULL DEFAULT '' COMMENT '个性签名',
  status     TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) ENGINE = InnoDB COMMENT '用户';

CREATE TABLE IF NOT EXISTS t_game (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(64)  NOT NULL COMMENT '游戏名',
  cover       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面URL，可为空',
  genre       VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '类型',
  tags        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '标签，逗号分隔',
  platform    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '平台',
  description VARCHAR(500) NOT NULL DEFAULT '' COMMENT '简介',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_name (name)
) ENGINE = InnoDB COMMENT '游戏字典';

CREATE TABLE IF NOT EXISTS t_user_game (
  id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT UNSIGNED NOT NULL,
  game_id    BIGINT UNSIGNED NOT NULL,
  status     TINYINT      NOT NULL COMMENT '1想玩 2在玩 3已通关 4搁置',
  play_hours DECIMAL(8,1) NOT NULL DEFAULT 0 COMMENT '游玩时长(小时)',
  rating     TINYINT      NOT NULL DEFAULT 0 COMMENT '评分1-5，0=未评',
  remark     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '备注',
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_game (user_id, game_id),
  KEY idx_user_status (user_id, status)
) ENGINE = InnoDB COMMENT '用户游戏库';

-- 游戏字典种子数据（21 款）
INSERT INTO t_game (name, genre, platform, description, tags) VALUES
('艾尔登法环', '动作RPG', 'PC/PS/Xbox', 'FromSoftware 开放世界魂系巨作，交界地的冒险等待褪色者。', '开放世界,动作,魂系,RPG'),
('黑神话：悟空', '动作RPG', 'PC/PS5', '国产 3A 动作游戏，重走西游之路，直面天命。', '动作,魂系,国产,单机'),
('博德之门3', 'CRPG', 'PC/PS5/Xbox', '拉瑞安出品，D&D 规则的角色扮演巅峰之作。', 'CRPG,回合制,剧情'),
('赛博朋克2077', '动作RPG', 'PC/PS/Xbox', '夜之城的未来都市开放世界冒险。', '开放世界,动作,RPG,剧情'),
('巫师3：狂猎', 'RPG', 'PC/PS/Xbox/Switch', '杰洛特的猎魔史诗，开放世界 RPG 经典。', '开放世界,RPG,剧情'),
('塞尔达传说：王国之泪', '冒险', 'Switch', '海拉鲁大陆的空岛与地底探索，究极手创造无限可能。', '开放世界,冒险,解谜'),
('只狼：影逝二度', '动作', 'PC/PS/Xbox', '苇名国的忍者复仇之旅，打铁节奏的极致体验。', '动作,魂系,单机'),
('空洞骑士', '类银河恶魔城', 'PC/PS/Xbox/Switch', '圣巢废墟下的虫之王国，探索与战斗的独立神作。', '类银河恶魔城,动作,独立游戏'),
('哈迪斯', 'Roguelike', 'PC/PS/Xbox/Switch', '逃离冥界，每一次死亡都是新的开始。', 'Roguelike,动作,独立游戏'),
('星露谷物语', '模拟经营', 'PC/PS/Xbox/Switch/移动端', '继承爷爷的农场，种田钓鱼交友的治愈生活。', '模拟经营,休闲,独立游戏'),
('怪物猎人：世界', '动作狩猎', 'PC/PS/Xbox', '新大陆调查团，狩猎巨型怪物的共斗经典。', '动作,联机,共斗'),
('黑暗之魂3', '动作RPG', 'PC/PS/Xbox', '传火之末，魂系列集大成之作。', '魂系,动作,RPG'),
('女神异闻录5皇家版', 'JRPG', 'PC/PS/Xbox/Switch', '白天上学，晚上偷心，怪盗团的华丽冒险。', 'JRPG,回合制,剧情'),
('反恐精英2（CS2）', 'FPS', 'PC', '经典竞技射击，5v5 爆破模式的电竞常青树。', 'FPS,竞技,联机'),
('Apex 英雄', 'FPS大逃杀', 'PC/PS/Xbox/Switch', '快节奏英雄大逃杀，三人小队配合制胜。', 'FPS,大逃杀,联机'),
('Dota 2', 'MOBA', 'PC', '深度与复杂度并存的 MOBA 电竞标杆。', 'MOBA,竞技,联机'),
('原神', '开放世界动作RPG', 'PC/PS/iOS/Android', '米哈游开放世界冒险，游历提瓦特七国，元素反应构筑战斗。', '开放世界,动作,RPG,二次元,联机'),
('异环', '开放世界动作', 'PC/iOS/Android', '超自然都市题材开放世界动作游戏，穿梭现代都市调查诡异异象。', '开放世界,动作,二次元'),
('明日方舟', '塔防策略', 'PC/iOS/Android', '末世题材塔防策略，罗德岛博士指挥干员对抗天灾与源石之灾。', '塔防,策略,二次元'),
('明日方舟：终末地', '即时策略RPG', 'PC/iOS/Android', '明日方舟同世界观的 3D 即时策略 RPG，开拓终末之地并建设工业管线。', '策略,RPG,二次元'),
('世界计划 缤纷舞台！feat. 初音未来', '音乐节奏', 'iOS/Android', 'SEGA 与 Colorful Palette 出品的初音未来企划音游，原创团体与虚拟歌手同台演出。', '音乐,二次元,休闲');
