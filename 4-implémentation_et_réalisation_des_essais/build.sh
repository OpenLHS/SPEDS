#!/bin/bash
#lastupdate="2026-03-20T10:44-0400"

declare -a projects=(
    "slf4j-ext-griis"
    "logger-library"
    "security-library"
    "speds-toolkit-library"
    "speds-link-library"
    "speds-network-library"
    "speds-transport-library"
    "speds-session-library"
    "speds-presentation-library"
    "speds-application-library"
    "speds-library"
)

## now loop through the above array
for i in "${projects[@]}"
do
    echo "  "
    echo "Building $i"
    cd "$i" || exit 128
    ./gradlew build --warning-mode none
    ./gradlew publishToMavenLocal
    [[ -d "${1}" && -x "${1}" ]] && {
      cp "build/libs/"* "$1"
      }
    cd ..
done