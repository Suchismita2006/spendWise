#!/usr/bin/env bash
if [ ! -f "bin/com/spendwise/Main.class" ]; then
    ./scripts/build.sh
fi
java -cp "bin:lib/*" com.spendwise.Main
