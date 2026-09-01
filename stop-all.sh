#!/usr/bin/env bash
# GameBox 一键停止
# 用法:
#   bash stop-all.sh          停止后端 6 个服务 + 前端（保留 Nacos）
#   bash stop-all.sh --all    连 Nacos 一起停止
set -u

port_pid() {
  netstat -ano | grep ":$1 " | grep LISTENING | awk '{print $5}' | head -1
}

stop_port() {
  local port=$1 name=$2 pid
  pid=$(port_pid "$port")
  if [ -z "$pid" ]; then
    echo "  [SKIP] $name(:$port) 未在运行"
    return 0
  fi
  if taskkill -PID "$pid" -F > /dev/null 2>&1; then
    echo "  [OK] 已停止 $name(:$port) PID=$pid"
  else
    echo "  [FAIL] 无法停止 $name(:$port) PID=$pid，请手动结束"
  fi
}

echo "== 停止后端服务 =="
stop_port 8080 gateway
stop_port 8081 auth
stop_port 8082 user
stop_port 8083 strategy
stop_port 8084 team
stop_port 8085 record

echo "== 停止前端 =="
stop_port 5173 web

echo "== 停止桌面应用 =="
if tasklist 2>/dev/null | grep -qi "GameBox.exe"; then
  if taskkill -IM GameBox.exe -F > /dev/null 2>&1; then
    echo "  [OK] 已停止 GameBox 桌面应用"
  else
    echo "  [FAIL] 无法停止 GameBox.exe，请手动结束"
  fi
else
  echo "  [SKIP] GameBox 桌面应用未在运行"
fi

if [ "${1:-}" = "--all" ]; then
  echo "== 停止 Nacos =="
  stop_port 8848 nacos
  stop_port 9848 "nacos-gRPC"
fi

echo "完成。重新启动: bash start-all.sh"
