#!/usr/bin/env bash
set -euo pipefail
gradle --no-daemon :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n no.blckswan.intelligentkunst.lab/no.blckswan.intelligentkunst.MainActivity
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
# Visual lineage: click EVOLVER after the sixth generated composition.
# Capturing both sides allows independent appraisal of conceptual continuity.
adb exec-out screencap -p > visual/evolution-parent.png
adb shell input tap 550 2010
sleep 14
adb exec-out screencap -p > visual/evolution-child.png
test -s visual/evolution-parent.png
test -s visual/evolution-child.png

# Launch a deterministic non-network test of the new curved vector renderer.
adb shell am force-stop no.blckswan.intelligentkunst.lab
adb shell am start -n no.blckswan.intelligentkunst.lab/no.blckswan.intelligentkunst.MainActivity \
    --ez ik_visual_fixture true
sleep 12
adb exec-out screencap -p > visual/study-vector-fixture.png
test -s visual/study-vector-fixture.png
sha256sum visual/*.png
