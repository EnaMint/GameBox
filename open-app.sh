#!/usr/bin/env bash
# GameBox 独立窗口打开器：以浏览器应用模式打开前端（无地址栏/标签页，形似独立应用）
# 用法:
#   bash open-app.sh            打开 http://localhost:5173 的独立窗口
#   bash open-app.sh 5174       指定其他端口
set -u

PORT="${1:-5173}"
URL="http://localhost:$PORT"

find_browser() {
  local p
  for p in \
    "/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe" \
    "/c/Program Files/Microsoft/Edge/Application/msedge.exe" \
    "/c/Program Files/Google/Chrome/Application/chrome.exe" \
    "/c/Program Files (x86)/Google/Chrome/Application/chrome.exe"; do
    [ -f "$p" ] && { printf '%s' "$p"; return 0; }
  done
  return 1
}

BROWSER=$(find_browser) || {
  echo "未找到 Edge/Chrome，请手动打开: $URL"
  exit 1
}

# 应用模式：独立窗口，无浏览器外壳
start "" "$BROWSER" --app="$URL"
echo "已以独立窗口打开: $URL"
echo "(窗口标题为页面标题，任务栏显示为 GameBox)"
