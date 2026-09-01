#!/usr/bin/env bash
# 导入游戏封面：把 image/ 下的封面图复制到上传目录并更新 t_game.cover
# 用法: bash tools/import-game-covers.sh
# 依赖: 本机 MySQL（root/123456，可用 MYSQL_USER/MYSQL_PASS 覆盖）
set -u

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IMG_DIR="$ROOT/image"
UPLOAD_DIR="${UPLOAD_DIR:-D:/graduate_design-uploads}"
DATE_DIR="20260901"
MYSQL_EXE="${MYSQL_EXE:-C:/Program Files/MySQL/MySQL Server 5.7/bin/mysql.exe}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASS="${MYSQL_PASS:-123456}"

# id|源文件名
MAP=(
  "1|Elden Ring.jpg"
  "2|Black Myth.jpg"
  "3|Baldurs Gate.jpg"
  "4|CyberPink.jpg"
  "5|The Wither.jpg"
  "6|Tears of the Kingdom.png"
  "7|Sekiro.jpg"
  "8|Hollow Knight.jpg"
  "9|Hades.png"
  "10|StarValley.png"
  "11|Monster Hunter.jpg"
  "12|Dark Souls 3.jpg"
  "13|Persona 5.jpg"
  "14|CS2.jpg"
  "15|Apex.jpg"
  "16|Dota2.png"
)

mkdir -p "$UPLOAD_DIR/cover/$DATE_DIR"

SQL=""
for entry in "${MAP[@]}"; do
  id="${entry%%|*}"
  src="${entry#*|}"
  ext="${src##*.}"
  dest="game-$id.$ext"
  if [ ! -f "$IMG_DIR/$src" ]; then
    echo "  [SKIP] 缺少 $IMG_DIR/$src"
    continue
  fi
  cp -f "$IMG_DIR/$src" "$UPLOAD_DIR/cover/$DATE_DIR/$dest"
  SQL+="UPDATE t_game SET cover='/api/files/cover/$DATE_DIR/$dest' WHERE id=$id;"
  echo "  [OK] $src -> cover/$DATE_DIR/$dest"
done

if [ -n "$SQL" ]; then
  "$MYSQL_EXE" -u"$MYSQL_USER" -p"$MYSQL_PASS" --default-character-set=utf8mb4 gamebox_user -e "$SQL" \
    && echo "数据库 cover 字段已更新" \
    || { echo "数据库更新失败"; exit 1; }
fi
