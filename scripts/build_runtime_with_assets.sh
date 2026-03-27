#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${1:-/tmp/aircraftwar-runtime-with-assets}"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

javac -encoding UTF-8 \
  $(find "$ROOT_DIR/src" "$ROOT_DIR/modules/common/src" "$ROOT_DIR/modules/server/src" "$ROOT_DIR/modules/client-desktop/src" "$ROOT_DIR/modules/server/test" "$ROOT_DIR/modules/client-desktop/test" "$ROOT_DIR/test" -name '*.java') \
  -d "$OUT_DIR"

mkdir -p "$OUT_DIR/images"
cp "$ROOT_DIR"/src/images/* "$OUT_DIR/images/"
if [ -d "$ROOT_DIR/最终素材" ]; then
  mkdir -p "$OUT_DIR/images/最终素材"
  find "$ROOT_DIR/最终素材" -maxdepth 1 -type f ! -name '.DS_Store' -exec cp {} "$OUT_DIR/images/最终素材/" \;
fi

echo "Runtime build ready at: $OUT_DIR"
