#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${1:-/tmp/aircraftwar-runtime-with-assets}"
JAVA_RELEASE="${JAVA_RELEASE:-17}"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

javac --release "$JAVA_RELEASE" -encoding UTF-8 \
  $(find "$ROOT_DIR/src" "$ROOT_DIR/modules/common/src" "$ROOT_DIR/modules/server/src" "$ROOT_DIR/modules/client-desktop/src" "$ROOT_DIR/modules/server/test" "$ROOT_DIR/modules/client-desktop/test" "$ROOT_DIR/test" -name '*.java') \
  -d "$OUT_DIR"

mkdir -p "$OUT_DIR/assets/images" "$OUT_DIR/assets/audio"
if [ -d "$ROOT_DIR/src/assets/images" ]; then
  cp -R "$ROOT_DIR/src/assets/images/." "$OUT_DIR/assets/images/"
fi
if [ -d "$ROOT_DIR/src/assets/audio" ]; then
  cp -R "$ROOT_DIR/src/assets/audio/." "$OUT_DIR/assets/audio/"
fi
find "$ROOT_DIR/src/assets" -maxdepth 1 -type f \( -name '*.wav' -o -name '*.mp3' \) -exec cp {} "$OUT_DIR/assets/audio/" \;

echo "Runtime build ready at: $OUT_DIR (target Java $JAVA_RELEASE)"
