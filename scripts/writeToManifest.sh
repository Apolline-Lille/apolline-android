#!/usr/bin/env bash

function copyEnvVarsToManifest {
    ANDROID_MANIFEST=${TRAVIS_BUILD_DIR}"/app/src/main/AndroidManifest.xml"

    export ANDROID_MANIFEST
    echo "AndroidManifest should exist at $ANDROID_MANIFEST"
    sed -i -e "s/\"\${google_map_api_key}\"/"\"$GOOGLE_MAP_API_KEY\""/g" $ANDROID_MANIFEST
    sed -i -e "s/\"\${fabric_api_key}\"/"\"$FABRIC_API_KEY\""/g" $ANDROID_MANIFEST
}