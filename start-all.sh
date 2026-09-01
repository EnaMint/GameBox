#!/usr/bin/env bash
# GameBox 一键启动
# 用法:
#   bash start-all.sh              启动全部（已运行的组件自动跳过），完成后自动打开独立应用窗口
#                                  （若前端/Electron 源码比打包产物新，会先自动重新打包 win-unpacked）
#   bash start-all.sh --no-browser 只启动服务，不打开窗口
#   NACOS_HOME=/d/nacos bash start-all.sh   （Nacos 不在 D:\nacos 时用环境变量指定）
# 启动顺序: MySQL(检查) → Nacos → user → auth → strategy → team → record → gateway → 前端
set -u

OPEN_BROWSER=1
if [ "${1:-}" = "--no-browser" ]; then
  OPEN_BROWSER=0
fi

ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND="$ROOT/gamebox-backend"
WEB="$ROOT/gamebox-web"
LOG_DIR="$ROOT/logs"
NACOS_HOME="${NACOS_HOME:-/d/nacos}"
mkdir -p "$LOG_DIR"

port_pid() {
  netstat -ano | grep ":$1 " | grep LISTENING | awk '{print $5}' | head -1
}

wait_port() {
  local port=$1 name=$2 timeout=${3:-120} i=0
  while [ $i -lt $timeout ]; do
    if [ -n "$(port_pid $port)" ]; then
      echo "  [OK] $name 就绪 (:$port)"
      return 0
    fi
    sleep 1
    i=$((i + 1))
  done
  echo "  [FAIL] $name ${timeout}s 内未就绪，查看日志: $LOG_DIR"
  return 1
}

start_jar() {
  local module=$1 port=$2 jar
  if [ -n "$(port_pid $port)" ]; then
    echo "  [SKIP] $module 已在运行 (:$port)"
    return 0
  fi
  jar="$BACKEND/$module/target/$module.jar"
  if [ ! -f "$jar" ]; then
    echo "  [FAIL] 找不到 $jar，请先在 gamebox-backend 下执行: mvn clean install -DskipTests"
    return 1
  fi
  echo "  启动 $module ..."
  ( cd "$BACKEND" && nohup java -jar "$jar" > "$LOG_DIR/$module.log" 2>&1 & )
  wait_port "$port" "$module"
}

FAILED=""
report() {
  local name=$1 port=$2
  if [ -n "$(port_pid $port)" ]; then
    printf "  %-18s :%-6s [运行中]\n" "$name" "$port"
  else
    printf "  %-18s :%-6s [未启动]\n" "$name" "$port"
    FAILED="$FAILED $name"
  fi
}

echo "== [1/4] 检查 MySQL =="
if [ -n "$(port_pid 3306)" ]; then
  echo "  [OK] MySQL 正在运行 (:3306)"
else
  echo "  [FAIL] MySQL(:3306) 未运行，请先启动 MySQL 服务（services.msc 或 net start）后重试"
  exit 1
fi

echo "== [2/4] 检查 Nacos =="
if [ -n "$(port_pid 8848)" ]; then
  echo "  [SKIP] Nacos 已在运行 (:8848)"
else
  if [ ! -f "$NACOS_HOME/bin/startup.cmd" ]; then
    echo "  [FAIL] 未找到 Nacos: $NACOS_HOME/bin/startup.cmd（可用 NACOS_HOME 环境变量指定安装目录）"
    exit 1
  fi
  echo "  启动 Nacos(standalone) ..."
  ( cd "$NACOS_HOME/bin" && nohup cmd //c startup.cmd -m standalone > "$LOG_DIR/nacos.log" 2>&1 & )
  wait_port 8848 "Nacos" 90 || exit 1
fi

echo "== [3/4] 启动后端服务 =="
start_jar gamebox-user 8082
start_jar gamebox-auth 8081
start_jar gamebox-strategy 8083
start_jar gamebox-team 8084
start_jar gamebox-record 8085
start_jar gamebox-gateway 8080

echo "== [4/4] 启动前端 =="
if [ -n "$(port_pid 5173)" ]; then
  echo "  [SKIP] 前端已在运行 (:5173)"
else
  if [ ! -d "$WEB/node_modules" ]; then
    echo "  首次运行，安装前端依赖 (npm install) ..."
    ( cd "$WEB" && npm install ) || FAILED="$FAILED web"
  fi
  echo "  启动 Vite 开发服务器 ..."
  ( cd "$WEB" && nohup npm run dev > "$LOG_DIR/gamebox-web.log" 2>&1 & )
  wait_port 5173 "前端" 60
fi

echo ""
echo "== 状态总览 =="
report "nacos" 8848
report "gateway" 8080
report "auth" 8081
report "user" 8082
report "strategy" 8083
report "team" 8084
report "record" 8085
report "web" 5173
echo ""
if [ -n "$FAILED" ]; then
  echo "启动未全部成功:$FAILED （日志目录: $LOG_DIR）"
  exit 1
fi
echo "全部就绪，浏览器打开: http://localhost:5173"
echo "停止全部: bash stop-all.sh （加 --all 连 Nacos 一起停）"

if [ "$OPEN_BROWSER" = "1" ]; then
  echo ""
  echo "== 打开独立应用窗口 =="
  EXE_DIR="$WEB/release"
  ASAR="$EXE_DIR/win-unpacked/resources/app.asar"
  if [ -f "$ASAR" ]; then
    STALE=$(find "$WEB/electron" "$WEB/src" "$WEB/package.json" "$WEB/index.html" -newer "$ASAR" -type f 2>/dev/null | head -1)
    if [ -n "$STALE" ]; then
      if tasklist 2>/dev/null | grep -qi "GameBox.exe"; then
        echo "  [WARN] 源码比打包产物新，但 GameBox 正在运行，跳过自动更新（右键托盘退出后重跑 start-all.sh 即可更新）"
      else
        echo "  检测到前端/Electron 源码更新，自动重新打包 (win-unpacked) ..."
        if ( cd "$WEB" && npm run build && npx electron-builder --win dir ) > "$LOG_DIR/electron-dist.log" 2>&1; then
          echo "  [OK] 桌面应用已更新到最新"
        else
          echo "  [WARN] 自动打包失败，继续使用旧版（日志: $LOG_DIR/electron-dist.log）"
        fi
      fi
    fi
  fi
  if [ -f "$EXE_DIR/win-unpacked/GameBox.exe" ]; then
    echo "  启动桌面应用 (Electron): release/win-unpacked/GameBox.exe"
    ( cd "$EXE_DIR/win-unpacked" && start "" "GameBox.exe" > /dev/null 2>&1 )
  elif ls "$EXE_DIR"/GameBox-Portable-*.exe > /dev/null 2>&1; then
    PORTABLE=$(ls "$EXE_DIR"/GameBox-Portable-*.exe | head -1)
    echo "  启动桌面应用 (Electron 便携版): $(basename "$PORTABLE")"
    ( cd "$EXE_DIR" && start "" "$(basename "$PORTABLE")" > /dev/null 2>&1 )
  elif [ -n "$(port_pid 5173)" ]; then
    echo "  未找到打包 exe，使用浏览器应用窗口模式"
    bash "$ROOT/open-app.sh" 5173
  fi
fi
