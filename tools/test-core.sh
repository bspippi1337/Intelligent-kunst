#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT INT TERM
javac -d "$OUT" app/src/main/java/no/blckswan/intelligentkunst/core/*.java tools/CoreSmoke.java
java -cp "$OUT" CoreSmoke
