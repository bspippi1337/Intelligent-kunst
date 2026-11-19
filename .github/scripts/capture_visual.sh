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

# Keyless academic adult life-drawing fixture through the actual Android UI.
adb shell am force-stop no.blckswan.intelligentkunst.lab
adb shell am start -n no.blckswan.intelligentkunst.lab/no.blckswan.intelligentkunst.MainActivity \
    --ez ik_classical_fixture true
sleep 9
adb exec-out screencap -p > visual/study-classical-figure.png
test -s visual/study-classical-figure.png

# Capture the genuine external-model handoff. The emulator intentionally has
# no model application or weights installed and makes NO network requests.
adb shell am force-stop no.blckswan.intelligentkunst.lab
adb shell am start -n no.blckswan.intelligentkunst.lab/no.blckswan.intelligentkunst.MainActivity \
    --ez ik_neural_fixture true
sleep 6
adb exec-out screencap -p > visual/study-local-neural-handoff.png
test -s visual/study-local-neural-handoff.png

# Compact landscape: dismiss dialog, rotate, and verify the atelier still
# renders without crashing with independently scrollable controls.
adb shell input keyevent KEYCODE_BACK
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 1
sleep 6
adb exec-out screencap -p > visual/study-local-neural-landscape.png
test -s visual/study-local-neural-landscape.png
adb shell settings put system user_rotation 0
adb shell settings put system accelerometer_rotation 1

# A deterministic, clearly labelled synthetic still-life allows verifying
# the actual oil/charcoal edge engines, NOT claiming a real photographic sample.
for mode in oil charcoal; do
    adb shell am force-stop no.blckswan.intelligentkunst.lab
    adb shell am start -n no.blckswan.intelligentkunst.lab/no.blckswan.intelligentkunst.MainActivity \
        --es ik_photo_fixture "$mode"
    sleep 15
    adb exec-out screencap -p > "visual/study-photo-$mode.png"
    test -s "visual/study-photo-$mode.png"
done 
# A classical life study and a photo painting must never render as the
# same image. We previously found a null-intent-fixture regression here.
if cmp -s visual/study-classical-figure.png visual/study-photo-oil.png; then
    echo "Visual regression: classical and oil fixtures identical" >&2
    exit 1
fi
if cmp -s visual/study-photo-oil.png visual/study-photo-charcoal.png; then
    echo "Visual regression: oil and charcoal fixtures identical" >&2
    exit 1
fi
adb shell pidof no.blckswan.intelligentkunst.lab >/dev/null
sha256sum visual/*.png
