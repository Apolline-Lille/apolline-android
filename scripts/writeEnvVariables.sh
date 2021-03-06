#!/usr/bin/env bash

function copyEnvVarsToManifest {
    ANDROID_MANIFEST=${TRAVIS_BUILD_DIR}"/app/src/main/AndroidManifest.xml"
    export ANDROID_MANIFEST
    echo "AndroidManifest should exist at $ANDROID_MANIFEST"
    sed -i -e "s/\"\${google_map_api_key}\"/"\"$GOOGLE_MAP_API_KEY\""/g" $ANDROID_MANIFEST
    sed -i -e "s/\"\${fabric_api_key}\"/"\"$FABRIC_API_TOKEN\""/g" $ANDROID_MANIFEST
}

function copyEnvVarsToSigningProperties {
    KEYSTORE_AUTH=${TRAVIS_BUILD_DIR}"/keystore.properties"
    export KEYSTORE_AUTH
    echo "keystore.properties should exist at $KEYSTORE_AUTH"
    sed -i -e "s/myStorePassword/"$STORE_PASSWORD"/g" $KEYSTORE_AUTH
    sed -i -e "s/myKeyPassword/"$KEY_PASSWORD"/g" $KEYSTORE_AUTH
    sed -i -e "s/myKeyAlias/"$KEY_ALIAS"/g" $KEYSTORE_AUTH
    sed -i -e "s_myStoreFile_"${TRAVIS_BUILD_DIR}/${STORE_FILE}"_g" $KEYSTORE_AUTH
}

function copyEnvVarsToFastlaneConfiguration {
    FASTLANE_CREDENTIAL=${TRAVIS_BUILD_DIR}"/fastlane/Appfile"

    export FASTLANE_CREDENTIAL
    echo "Appfile should exist at $FASTLANE_CREDENTIAL"
    sed -i -e "s/\"\${secret_credentials}\"/"\"$FASTLANE_FILE\""/g" $FASTLANE_CREDENTIAL
}