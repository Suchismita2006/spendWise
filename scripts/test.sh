#!/usr/bin/env bash
if [ ! -f "bin/com/spendwise/UserEncapsulationTest.class" ]; then
    ./scripts/build.sh
fi
java -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "bin:lib/mysql-connector-j-8.3.0.jar" --scan-class-path
