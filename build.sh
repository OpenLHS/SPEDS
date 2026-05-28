#!/bin/bash
#lastupdate="2026-03-20T14:40-0400"

CALL_DIR=$( pwd -P )
timestamp=$( date "+%Y%m%dT%H%M%S" )
SANDBOX="${CALL_DIR}/sandbox_${timestamp}"

SPEDS_PROJECT_DIR=$( ls -d 4* )

[[ -d "${SPEDS_PROJECT_DIR}" && -x "${SPEDS_PROJECT_DIR}" ]] || {
    echo "Error : ${SPEDS_PROJECT_DIR} is not available." >&2
    exit 1
}

EXPECTED_JAVA_VERSION=17
CHECK_JAVA_VERSION() {
    [[ -n "$1" ]] || return 1

    local requested_version="$1"
    local current_version
    current_version=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')

    [[ "$current_version" == "$requested_version"* ]]
}

CHECK_JAVA_VERSION ${EXPECTED_JAVA_VERSION} || {
    echo "Error : Java version is not ${EXPECTED_JAVA_VERSION}" >&2
    exit 1
}

TMP_DIR=$(mktemp -d)
PRODUCT_DIR="${TMP_DIR}/product"
BUILD_DIR="${TMP_DIR}/product/build"
LIBS_DIR="${TMP_DIR}/product/build/libs"

mkdir "${PRODUCT_DIR}"
mkdir "${BUILD_DIR}"
mkdir "${LIBS_DIR}"

# ------ Source
echo " "
echo " #####   #####   ##### "
echo " "
cp -r "${CALL_DIR}/${SPEDS_PROJECT_DIR}" "${TMP_DIR}"

cd "${TMP_DIR}/${SPEDS_PROJECT_DIR}"  || exit 128

./build.sh "${LIBS_DIR}"

\rm -rf "${TMP_DIR}"
