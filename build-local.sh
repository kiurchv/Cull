#!/usr/bin/env bash
# =============================================================================
# Local build script for Cull — uses pre-cached Android SDK and Maven deps
# Run setup.sh from kiurchv/android-sdk-cache first
# =============================================================================
set -euo pipefail

ANDROID_SDK_DIR="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
GRADLE_ZIP="/tmp/gradle-8.14.5-bin.zip"
GREEN='\033[0;32m'; RED='\033[0;31m'; NC='\033[0m'
info() { echo -e "${GREEN}[build]${NC} $*"; }
err()  { echo -e "${RED}[error]${NC} $*"; exit 1; }

[ -f "app/build.gradle.kts" ] || err "Run from the Cull project root"
[ -d "$ANDROID_SDK_DIR/platforms/android-35" ] || \
  err "Android SDK not found — run setup.sh from android-sdk-cache first"
command -v java >/dev/null || err "Java not found"

echo "sdk.dir=$ANDROID_SDK_DIR" > local.properties
info "sdk.dir=$ANDROID_SDK_DIR"

export ANDROID_SDK_ROOT="$ANDROID_SDK_DIR"
export JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"

# Patch wrapper to use local Gradle zip (restore on exit)
WRAPPER="gradle/wrapper/gradle-wrapper.properties"
ORIG_URL=$(grep distributionUrl "$WRAPPER" | cut -d= -f2-)
GRADLE_HASH_DIR="$HOME/.gradle/wrapper/dists/gradle-8.14.5-bin/91wvqqe4qmsefb2bitamjj9bp"

if [ -f "$GRADLE_HASH_DIR/gradle-8.14.5/bin/gradle" ]; then
  # Gradle already in wrapper cache — no patching needed
  info "Gradle 8.14.5 found in wrapper cache"
elif [ -f "$GRADLE_ZIP" ]; then
  info "Patching wrapper to use local gradle zip"
  sed -i "s|distributionUrl=.*|distributionUrl=file://$GRADLE_ZIP|" "$WRAPPER"
  trap "sed -i 's|distributionUrl=.*|distributionUrl=$ORIG_URL|' $WRAPPER" EXIT
fi

TASK="${1:-assembleDebug}"
info "Running: ./gradlew $TASK --offline --no-daemon"
echo ""

./gradlew "$TASK" --offline --no-daemon 2>&1

echo ""
info "Done! APK:"
find app/build/outputs/apk -name "*.apk" 2>/dev/null | while read f; do
  echo "  $f ($(du -sh "$f" | cut -f1))"
done
