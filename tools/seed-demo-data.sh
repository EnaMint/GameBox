#!/usr/bin/env bash
# 演示数据注入脚本（需先启动全套后端服务）
set -e
GW=http://localhost:8080/api
TA=$(grep -o '"token":"[^"]*"' /tmp/login_a.json | cut -d'"' -f4)
TB=$(grep -o '"token":"[^"]*"' /tmp/login_b.json | cut -d'"' -f4)
P=/tmp/seed
mkdir -p $P

echo "== 1. 用户资料：头像与签名 =="
cat > $P/me_a.json <<'EOF'
{"nickname":"盒友DemoA","avatar":"/api/files/avatar/20260901/demo-a.png","bio":"魂系游戏爱好者，正在冲击女武神"}
EOF
cat > $P/me_b.json <<'EOF'
{"nickname":"盒友DemoB","avatar":"/api/files/avatar/20260901/demo-b.png","bio":"CRPG 与 FPS 双修，晚上常驻排位"}
EOF
curl -s -X PUT $GW/user/me -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/me_a.json; echo
curl -s -X PUT $GW/user/me -H "Authorization: Bearer $TB" -H "Content-Type: application/json" --data-binary @$P/me_b.json; echo

echo "== 2. 补充攻略 1 封面 =="
cat > $P/s1.json <<'EOF'
{"title":"艾尔登法环 新手开荒完全指南","category":"1","gameId":1,"cover":"/api/files/cover/20260901/demo-elden.png","summary":"从创建角色到前期加点、武器选择与地图路线，一篇带你渡过新手期。","content":"# 开荒必读\n\n## 1. 开局职业怎么选\n新手推荐**流浪骑士**或**观星者**，容错率最高。\n\n## 2. 前期加点\n- 生命力优先点到 20-30 点\n- 耐力保证穿得动装备\n- 主属性按武器需求点到软上限即可\n\n## 3. 路线建议\n宁姆格福 → 啜泣半岛 → 史东薇尔城。遇到大树卫不要硬刚，绕路即可。\n\n> 褪色者，愿黄金树赐福于你。"}
EOF
curl -s -X PUT $GW/strategy/1 -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/s1.json; echo

echo "== 3. 新攻略 × 4 =="
cat > $P/s2.json <<'EOF'
{"title":"黑神话：悟空 虎先锋无伤打法详解","category":"1","gameId":2,"cover":"/api/files/cover/20260901/demo-wukong.png","summary":"虎先锋快慢刀交替极其恶心，本文给出站位与识破时机，附无伤思路。","content":"# 虎先锋无伤思路\n\n## 核心机制\n虎先锋的连招分**快刀**与**慢刀**两套，关键在于第二段停顿的长短。\n\n## 打法要点\n1. 保持中距离引诱其出快刀连段，最后一刀后撤识破。\n2. 慢刀起手会下蹲，看到下蹲立刻准备闪避重击。\n3. 定身术留给其拔刀突进，可稳定打出硬直。\n4. 二阶段血池回血时，优先打碎池边雕像打断回复。\n\n按此节奏，无伤可期。"}
EOF
cat > $P/s3.json <<'EOF'
{"title":"博德之门3 新手开荒职业推荐与队伍搭配","category":"1","gameId":3,"cover":"/api/files/cover/20260901/demo-bg3.png","summary":"第一次玩 BG3 选什么职业？本文给出三种新手友好构筑与队伍组合。","content":"# 新手职业推荐\n\n## 1. 圣武士（复仇之誓）\n近战爆发高，至圣斩伤害直观，适合第一次接触 D&D 规则的玩家。\n\n## 2. 游侠（猎人）\n自带宠物与远程输出，探索章节体验顺滑。\n\n## 3. 术士（龙脉）\n魅力系法术位灵活，对话检定优势大。\n\n## 队伍搭配建议\n前排圣武士 + 远程游侠 + 辅助牧师 + 控场法师，覆盖绝大多数遭遇战场景。"}
EOF
cat > $P/s4.json <<'EOF'
{"title":"赛博朋克2077 2.0 版本流派强度榜","category":"2","gameId":4,"cover":"/api/files/cover/20260901/demo-cyber.png","summary":"2.0 重做天赋后哪些流派崛起？T0 到 T2 一次讲清楚。","content":"# 2.0 流派强度榜（测评）\n\n## T0\n- **忍者刀反射流**：子弹时间 + 武士刀反弹，观赏性与强度并存。\n- **智能武器黑客流**：过热连锁清场效率极高。\n\n## T1\n- 大猩猩拳头近战流、霰弹枪突脸流。\n\n## T2\n- 传统手枪点射流在 2.0 后相对疲软，需要义体补强。\n\n> 结论：2.0 之后近战与黑客全面崛起，纯枪法党需要重新点天赋。"}
EOF
cat > $P/s5.json <<'EOF'
{"title":"王国之泪 100 小时通关心得：为什么它是神作","category":"4","gameId":6,"summary":"从初始空岛到海拉鲁地底，聊聊这作让人沉迷的底层设计。","content":"# 100 小时之后，我想聊聊这款神作（心得）\n\n## 究极手改变了一切\n这作的核心不是战斗，而是**创造**。究极手 + 余料建造让每个玩家的解法都独一无二。\n\n## 地底与空岛的分层设计\n三层世界的垂直探索让好奇心永远有回报，每座空岛都是一个小型解谜盒。\n\n## 遗憾\n耐久系统依旧劝退，部分地底场景重复度偏高。\n\n综合评分：**9.5 / 10**。"}
EOF
S2=$(curl -s -X POST $GW/strategy -H "Authorization: Bearer $TB" -H "Content-Type: application/json" --data-binary @$P/s2.json); echo "s2=$S2"
S3=$(curl -s -X POST $GW/strategy -H "Authorization: Bearer $TB" -H "Content-Type: application/json" --data-binary @$P/s3.json); echo "s3=$S3"
S4=$(curl -s -X POST $GW/strategy -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/s4.json); echo "s4=$S4"
S5=$(curl -s -X POST $GW/strategy -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/s5.json); echo "s5=$S5"

echo "== 4. 互动：点赞与评论 =="
for id in 2 3; do curl -s -X POST $GW/strategy/$id/like -H "Authorization: Bearer $TA" >/dev/null; done
for id in 4 5; do curl -s -X POST $GW/strategy/$id/like -H "Authorization: Bearer $TB" >/dev/null; done
cat > $P/c1.json <<'EOF'
{"content":"按这个加点开荒舒服多了，感谢楼主！"}
EOF
cat > $P/c2.json <<'EOF'
{"content":"虎先锋卡了我两天，这就去试试慢刀识破。"}
EOF
cat > $P/c3.json <<'EOF'
{"content":"圣武士确实适合新人，我就是这么入坑的。"}
EOF
curl -s -X POST $GW/strategy/1/comments -H "Authorization: Bearer $TB" -H "Content-Type: application/json" --data-binary @$P/c1.json >/dev/null
curl -s -X POST $GW/strategy/2/comments -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/c2.json >/dev/null
curl -s -X POST $GW/strategy/3/comments -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/c3.json >/dev/null
echo "likes & comments done"

echo "== 5. 新组队帖 × 2 =="
cat > $P/t1.json <<'EOF'
{"gameId":14,"title":"CS2 晚间排位车队 缺2人 冲优先段位","content":"每晚 20:00-23:00 开黑，要求有麦、心态好不压力队友。目前 3 缺 2，来个稳定长期的一起上分。","memberLimit":5,"needVoice":1,"playTime":"每晚 20:00-23:00"}
EOF
cat > $P/t2.json <<'EOF'
{"gameId":10,"title":"星露谷物语 休闲农场互助 来唠嗑","content":"纯休闲玩家，周末一起种田钓鱼布置农场，不赶进度，聊天为主。","memberLimit":3,"needVoice":0,"playTime":"周末白天"}
EOF
T1=$(curl -s -X POST $GW/team -H "Authorization: Bearer $TB" -H "Content-Type: application/json" --data-binary @$P/t1.json); echo "t1=$T1"
T2=$(curl -s -X POST $GW/team -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/t2.json); echo "t2=$T2"

echo "== 6. demoa 申请 CS2 车队（待审核示例） =="
cat > $P/apply.json <<'EOF'
{"message":"老兵玩家，主玩突破位，每晚在线稳定。"}
EOF
curl -s -X POST $GW/team/3/apply -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/apply.json; echo

echo "== 7. 新战绩 × 3 =="
cat > $P/r1.json <<'EOF'
{"gameId":2,"content":"鏖战三小时，终于过了黄风大圣！识破慢刀之后节奏完全不一样了。","images":["/api/files/record/20260901/demo-r2.png"]}
EOF
cat > $P/r2.json <<'EOF'
{"gameId":14,"content":"今晚手感爆棚，五杀拿下关键局，队伍冲分成功！","images":["/api/files/record/20260901/demo-r1.png"]}
EOF
cat > $P/r3.json <<'EOF'
{"gameId":10,"content":"周末休闲局，农场终于升到五星，钓鱼图鉴也差两条了。","images":["/api/files/record/20260901/demo-r3.png"]}
EOF
R1=$(curl -s -X POST $GW/record -H "Authorization: Bearer $TB" -H "Content-Type: application/json" --data-binary @$P/r1.json); echo "r1=$R1"
R2=$(curl -s -X POST $GW/record -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/r2.json); echo "r2=$R2"
R3=$(curl -s -X POST $GW/record -H "Authorization: Bearer $TB" -H "Content-Type: application/json" --data-binary @$P/r3.json); echo "r3=$R3"

echo "== 8. 战绩互赞 =="
for id in 1 2; do curl -s -X POST $GW/record/$id/like -H "Authorization: Bearer $TB" >/dev/null; done
for id in 3 4; do curl -s -X POST $GW/record/$id/like -H "Authorization: Bearer $TA" >/dev/null; done
echo "== 9. demoa 游戏库补齐 =="
cat > $P/g1.json <<'EOF'
{"gameId":14,"status":2,"playHours":120,"rating":4,"remark":"主玩竞技模式"}
EOF
curl -s -X POST $GW/ugame -H "Authorization: Bearer $TA" -H "Content-Type: application/json" --data-binary @$P/g1.json; echo
echo "== done =="
