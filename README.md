# GameBox 游戏攻略与玩家社区平台

毕业设计项目：一个类似"小黑盒"的游戏攻略与玩家交流社区，支持**攻略发布、个人游戏数据库、玩家组队、战绩分享**四大核心功能。

- 后端：Spring Boot 3.2 + Spring Cloud Alibaba（Nacos 注册中心 + Gateway 网关 + OpenFeign 服务调用）+ MyBatis-Plus + MySQL + JWT 无状态认证
- 前端：Vue 3 + Vite + Element Plus（深色主题）+ Pinia + Vue Router + md-editor-v3（Markdown 编辑器）

## 目录结构

```
graduate_design\
├── gamebox-backend\            # Maven 多模块后端
│   ├── pom.xml                 # 父 POM（统一版本管理）
│   ├── sql\                    # 建库脚本 01~04（含 21 款游戏种子数据）
│   ├── gamebox-common\         # 公共模块：统一返回/异常/JWT/用户上下文
│   ├── gamebox-gateway\  :8080 # 网关：路由 + JWT 校验 + CORS
│   ├── gamebox-auth\     :8081 # 认证：注册/登录签发 JWT
│   ├── gamebox-user\     :8082 # 用户：资料/游戏字典/游戏库/文件上传  (gamebox_user 库)
│   ├── gamebox-strategy\ :8083 # 攻略：发布/评论/点赞                (gamebox_strategy 库)
│   ├── gamebox-team\     :8084 # 组队：组队帖/申请审批                (gamebox_team 库)
│   └── gamebox-record\   :8085 # 战绩：动态流/点赞                    (gamebox_record 库)
├── gamebox-web\                # Vue3 前端（Vite 代理 /api → 8080）
└── tools\                      # 辅助脚本（占位图生成、演示数据注入）
```

## 环境要求

| 软件 | 版本 |
|---|---|
| JDK | 17+（开发用 21） |
| Maven | 3.9+ |
| Node.js | 18+（开发用 22/24） |
| MySQL | 5.7+（root 用户） |
| Nacos Server | 2.5.x，standalone 模式 |

> 本机实测：JDK 21.0.2 / Maven 3.9.11 / Node v22 / MySQL 5.7 / Nacos 2.5.1。

## 启动步骤（按顺序）

### 0. 一键启动 / 停止（推荐）

完成下面的「建库」后，日常启停可直接在项目根目录用脚本（Git Bash 中执行）：

```
bash start-all.sh     # 检查 MySQL/Nacos → 依次启动 6 个后端服务 → 启动前端，完成后自动打开独立应用窗口
bash start-all.sh --no-browser  # 只启动服务，不打开窗口
bash open-app.sh      # 服务已在运行时，单独把前端以独立窗口（无地址栏/标签页）打开
bash stop-all.sh      # 停止 6 个后端服务 + 前端（保留 Nacos）
bash stop-all.sh --all  # 连 Nacos 一起停止
```

若已打包桌面应用（见下文「桌面客户端」），脚本优先启动独立的 `GameBox` Electron 程序；
否则回退为 Edge/Chrome 的**应用模式**，呈现为一个没有浏览器外壳的独立窗口（标题为
「GameBox - 游戏攻略与玩家社区」）。桌面已生成 `GameBox` 快捷方式，双击即可一键启动并开窗。

桌面应用带**系统托盘**：点窗口右上角关闭仅隐藏到托盘（后台继续运行），右键托盘图标
可唤起主窗口、直达游戏库/攻略/组队/战绩页面，或点「退出」彻底关闭；左键/双击托盘图标
唤起窗口。`start-all.sh` 打开窗口前会检测 `electron/`、`src/` 等源码是否比打包产物新，
是则自动重新打包 `win-unpacked`（GameBox 正在运行时跳过并提示）；`stop-all.sh` 会一并
结束托盘后台的 `GameBox.exe`。

**双击 `GameBox.exe`（或桌面快捷方式）本身就是完整的一键启动**，不再需要先开终端：程序
启动时先探测网关 `8080`，未就绪则弹出一个 Steam 风格的**加载窗口**（进度条 + Loading...），
由它在后台隐藏执行 `start-all.sh --no-browser`，全部服务就绪后加载窗口关闭、自动进入主界面，
全程没有终端窗口。后端已在运行时双击则直接开窗，不显示加载窗口；启动失败时加载窗口会
转为错误态并给出「重试 / 查看日志 / 退出」。加载窗口里的「取消」会尝试结束脚本进程树，
但已 `nohup` 起的 java 服务可能残留（对下次启动无害，脚本会按端口跳过），需要彻底清理就执行
`bash stop-all.sh --all`。注意：走 exe 启动时不会自动重打包，改动 `gamebox-web/electron/`
或前端源码后仍需用终端 `bash start-all.sh`（或 `npm run dist`）更新打包产物。

首次使用仍需先执行第 1 步建库；Nacos 默认在 `D:\nacos`，其他位置可用
`NACOS_HOME=/x/nacos bash start-all.sh` 指定。日志统一输出到 `logs/` 目录。

以下步骤为手动启动方式，便于理解各组件关系。

### 1. MySQL 建库

用任意 MySQL 客户端依次执行 `gamebox-backend/sql/` 下 4 个脚本：

```
01_gamebox_user.sql      -- 用户/游戏字典/用户游戏库
02_gamebox_strategy.sql  -- 攻略/评论/点赞
03_gamebox_team.sql      -- 组队帖/申请
04_gamebox_record.sql    -- 战绩/点赞
05_follow_message.sql    -- 二期增量：关注/粉丝计数列 + 关注表 + 私信表
```

每个脚本自带 `CREATE DATABASE IF NOT EXISTS`，可重复执行；`05` 为增量脚本，
在 `01` 之后执行一次即可（全新环境与老库升级都适用，勿重复执行）。

**数据库账号**：默认 `root / 123456`。若你的密码不同，修改以下文件中的数据源配置：

- `gamebox-auth/src/main/resources/application.yml`
- `gamebox-user/src/main/resources/application.yml`
- `gamebox-strategy/src/main/resources/application.yml`
- `gamebox-team/src/main/resources/application.yml`
- `gamebox-record/src/main/resources/application.yml`

### 2. 启动 Nacos

```
cd <nacos安装目录>\bin
startup.cmd -m standalone
```

浏览器打开 `http://localhost:8848/nacos`（默认 nacos/nacos）能看到控制台即可。
注意同时放行 **9848**（gRPC，服务注册实际走该端口）。

### 3. 构建后端

```
cd gamebox-backend
mvn clean install -DskipTests
```

### 4. 启动后端服务（顺序建议）

```
java -jar gamebox-user/target/gamebox-user.jar          # 8082
java -jar gamebox-auth/target/gamebox-auth.jar          # 8081
java -jar gamebox-strategy/target/gamebox-strategy.jar  # 8083
java -jar gamebox-team/target/gamebox-team.jar          # 8084
java -jar gamebox-record/target/gamebox-record.jar      # 8085
java -jar gamebox-gateway/target/gamebox-gateway.jar    # 8080（最后启动）
```

在 Nacos 控制台「服务列表」中能看到 6 个服务（gateway/auth/user/strategy/team/record）即注册成功。

### 5. 启动前端

```
cd gamebox-web
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`。

### 6.（可选）注入演示数据

先注册两个账号（页面注册或调接口），然后修改 `tools/seed-demo-data.sh` 顶部的
token 来源后执行，会写入中文攻略/组队/战绩与占位图。

游戏封面图存放在 `image/` 目录，执行以下脚本可复制到上传目录并更新 `t_game.cover`：

```
bash tools/import-game-covers.sh
```
占位图由 `tools/gen-placeholder-png.js` 生成（无需 ImageGen 依赖）：

```
node tools/gen-placeholder-png.js "D:/graduate_design-uploads"
```

上传目录可在 `gamebox-user` 的 `application.yml` 中通过
`gamebox.upload.dir` 与 `gamebox.upload.url-prefix` 调整。

### 7.（可选）桌面客户端（Electron 打包）

前端可用 Electron 打包为独立 Windows 程序，运行后呈现为带任务栏图标的原生窗口：

```
cd gamebox-web
npm run dist            # 构建前端 + 打包（首次会自动下载 electron 运行时，已配置国内镜像）
```

产物位于 `gamebox-web/release/`：

| 产物 | 说明 |
|---|---|
| `win-unpacked/GameBox.exe` | 绿色免安装版，直接双击运行 |
| `GameBox-Portable-1.0.0.exe` | 单文件便携版 |
| `GameBox Setup 1.0.0.exe` | NSIS 安装包（可选安装目录、创建桌面快捷方式） |

**运行原理**：打包产物内置 `dist` 构建产物，Electron 主进程在
`127.0.0.1:5199~5219` 启动本地 HTTP 服务提供静态页面，并把 `/api/**` 代理到网关
`127.0.0.1:8080`，因此前端代码零改动。启动时若网关未就绪，会先显示**加载窗口**并自动
执行 `start-all.sh --no-browser` 拉起全部后端（见上文「一键启动 / 停止」）；就绪后自动
进入主界面。若程序找不到项目根的 `start-all.sh` 或 `bash.exe`（可用环境变量
`GAMEBOX_ROOT`、`GAMEBOX_BASH` 指定），则跳过加载窗口直接开窗，此时页面内请求会提示
「后端服务不可用」。加载窗口由 `electron/boot.cjs`（启动编排与进度解析）、
`electron/splash.html`、`electron/splash-preload.cjs` 三个文件构成，随 `electron/**/*`
一起打进 `app.asar`。

开发调试桌面窗口：`npm run app:dev`（直连 Vite 5173 开发服务器，支持热更新，不显示加载窗口）。

> 打包配置说明：`package.json` 的 `build.electronDist` 指向 `tools/electron-dist-win32-x64`
> （预先解压好的 Electron 发行版），用于规避部分机器上杀软锁定文件导致的解包重命名失败
> （`EPERM: rename win-unpacked.tmp`）。升级 Electron 版本后需重新解压对应的
> `electron-vX-win32-x64.zip` 到该目录。

## 功能与演示路径（答辩演示参考）

1. **注册/登录**：`/register` 注册 → `/login` 登录（JWT 有效期 7 天，退出仅清前端 token，见下文说明）
2. **游戏字典与游戏库**：`/games` 浏览 21 款游戏 → 类型下拉按**标签**筛选（选项来自数据库，如选「开放世界」会聚合原神、塞尔达等所有含该标签的游戏），卡片上的标签胶囊可点击直接筛选 → 点「想玩/在玩/已通关」一键收录 → `/my/games` 管理状态、时长、评分、备注
3. **攻略**：`/strategy` 列表（分类/游戏/关键词筛选，最新/最热排序）→ 发布（Markdown 编辑器 + 封面上传）→ 详情页点赞、评论
4. **组队**：`/team` 大厅 → 发布组队帖（人数/开麦/游戏时间）→ 其他账号申请 → 队长审批；满员自动转为「已满员」
5. **战绩**：`/record` 动态流 → 发布战绩（最多 9 图）→ 点赞互动
6. **个人中心**：`/profile` 换头像（选区裁剪）/签名，四页签查看我的攻略/组队/申请/战绩；`/user/:id` 查看他人主页
7. **关注/粉丝**：他人主页点「+ 关注」，双方关注数/粉丝数实时 +1；主页计数可点进关注列表/粉丝列表，支持取关与回关
8. **私信**：他人主页「发私信」或顶栏私信图标进入 `/messages`；左侧会话列表（未读角标）+ 右侧聊天窗（Enter 发送）；对方回复前最多发 2 条，回复后解锁正常聊天

演示账号（若已注入演示数据）：`demoa / demoa123`、`demob / demob123`

## 技术要点

- **认证链路**：网关 `AuthGlobalFilter` 统一校验 JWT → 剥离客户端伪造的 `X-User-Id` 头 → 注入真实用户身份；下游服务 `UserContextFilter` 从请求头取出存入 `UserHolder`（ThreadLocal）。GET/HEAD 允许匿名浏览，写操作必须携带 token。
- **服务调用**：内容服务通过 OpenFeign 调 `/user/inner/batch`、`/game/inner/batch` 批量组装作者与游戏快照；被调方宕机时降级为「未知用户/未知游戏」，不产生 500。
- **并发安全**：点赞/入队依赖唯一索引兜底；组队审批通过采用条件 UPDATE（`member_count < member_limit`）防止超员。
- **分页结构**：`{records, total, size, current, pages}`（MyBatis-Plus 分页）。

## 已知限制（一期）

- JWT 注销为纯前端清除，无服务端失效能力；二期计划引入 Redis 维护黑名单。
- 文件上传存本地磁盘（`D:/graduate_design-uploads`），二期可迁移 OSS。
- 未引入 Sentinel/限流，降级逻辑为手写 try-catch。

## 常见问题

- **启动报 Nacos 连接失败**：确认 `startup.cmd -m standalone` 已执行且 8848/9848 端口未被占用。
- **网关 503**：对应业务服务未启动或未注册到 Nacos。
- **前端请求 404**：确认 `vite.config.js` 代理指向网关 `http://localhost:8080`，且网关已启动。
- **MySQL 中文乱码**：建库脚本已指定 `utf8mb4`，连接串保留 `useUnicode=true&characterEncoding=utf8`。
