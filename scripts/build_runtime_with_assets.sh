#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${1:-/tmp/aircraftwar-runtime-with-assets}"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

javac -encoding UTF-8 \
  $(find "$ROOT_DIR/src" "$ROOT_DIR/modules/common/src" "$ROOT_DIR/modules/server/src" "$ROOT_DIR/modules/client-desktop/src" "$ROOT_DIR/modules/server/test" "$ROOT_DIR/modules/client-desktop/test" "$ROOT_DIR/test" -name '*.java') \
  -d "$OUT_DIR"

mkdir -p "$OUT_DIR/images" "$OUT_DIR/videos/images" "$OUT_DIR/videos/audio"
cp "$ROOT_DIR"/src/images/* "$OUT_DIR/images/"
cp "$ROOT_DIR"/src/images/* "$OUT_DIR/videos/images/"
if [ -d "$ROOT_DIR/src/videos/images" ]; then
  cp -R "$ROOT_DIR/src/videos/images/." "$OUT_DIR/videos/images/"
fi
if [ -d "$ROOT_DIR/src/videos/audio" ]; then
  cp -R "$ROOT_DIR/src/videos/audio/." "$OUT_DIR/videos/audio/"
fi
find "$ROOT_DIR/src/videos" -maxdepth 1 -type f \( -name '*.wav' -o -name '*.mp3' \) -exec cp {} "$OUT_DIR/videos/audio/" \;
if [ -d "$ROOT_DIR/最终素材" ]; then
  mkdir -p "$OUT_DIR/images/最终素材"
  find "$ROOT_DIR/最终素材" -maxdepth 1 -type f ! -name '.DS_Store' -exec cp {} "$OUT_DIR/images/最终素材/" \;
  mkdir -p "$OUT_DIR/videos/images/最终素材"
  find "$ROOT_DIR/最终素材" -maxdepth 1 -type f ! -name '.DS_Store' -exec cp {} "$OUT_DIR/videos/images/最终素材/" \;
fi

echo "Runtime build ready at: $OUT_DIR"
