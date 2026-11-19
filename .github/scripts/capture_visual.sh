#!/usr/bin/env bash
set -euo pipefail
gradle --no-daemon :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n no.blckswan.intelligentkunst/.MainActivity
sleep 20
mkdir -p visual
adb exec-out screencap -p > visual/study-00.png
for n in 01 02 03 04 05; do
    # The configured Pixel 6 profile has a 1080x2400 viewport.
    adb shell input tap 250 2010
    sleep 9
    adb exec-out screencap -p > "visual/study-${n}.png"
    test -s "visual/study-${n}.png"
done
sha256sum visual/*.png
