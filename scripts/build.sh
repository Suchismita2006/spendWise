#!/usr/bin/env bash
set -e
echo "Building SpendWise Core Java..."
mkdir -p bin
find src/main/java src/test/java -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin -cp "lib/*" @sources.txt
rm -f sources.txt
cp -r src/main/resources/* bin/ 2>/dev/null || true
echo "[SUCCESS] Build finished."
