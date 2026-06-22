#!/usr/bin/env bash
# =============================================================================
# Local build script for Cull — uses pre-cached Android SDK and Maven deps
# Run setup.sh from kiurchv/android-sdk-cache first
# =============================================================================
set -euo pipefail

ANDROID_SDK_DIR="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
GREEN='\033[0;32m'; RED='\033[0;31m'; NC='\033[0m'
info() { echo -e "${GREEN}[build]${NC} $*"; }
err()  { echo -e "${RED}[error]${NC} $*"; exit 1; }

# Check prerequisites
[ -f "app/build.gradle.kts" ] || err "Run from the Cull project root"
[ -d "$ANDROID_SDK_DIR/platforms/android-35" ] || \
  err "Android SDK not found at $ANDROID_SDK_DIR — run setup.sh from android-sdk-cache first"
command -v java >/dev/null || err "Java not found"

# Write local.properties
echo "sdk.dir=$ANDROID_SDK_DIR" > local.properties
info "sdk.dir=$ANDROID_SDK_DIR"

# Export env
export ANDROID_SDK_ROOT="$ANDROID_SDK_DIR"
export JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"

TASK="${1:-assembleDebug}"
info "Running: ./gradlew $TASK --offline"
echo ""

./gradlew "$TASK" \
  --offline \
  --no-daemon \
  --stacktrace \
  2>&1

echo ""
info "Done! APK:"
find app/build/outputs/apk -name "*.apk" 2>/dev/null | while read f; do
  echo "  $f ($(du -sh "$f" | cut -f1))"
done
