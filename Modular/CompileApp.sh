#!/bin/bash

echo "---------------------------------"
echo "       Building the App          "
echo "---------------------------------"
cd "AppSource" || exit 1
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleRelease
cd ..

APK_UNSIGNED="AppSource/app/build/outputs/apk/release/app-release-unsigned.apk"
APK_SIGNED="AppSource/app/build/outputs/apk/release/app-release.apk"
APK_ALIGNED="AppSource/app/build/outputs/apk/release/app-release-aligned.apk"
MODULES_DIR="Modules"

# If gradle didn't produce a signed APK but did produce an unsigned one, sign it
if [ ! -f "$APK_SIGNED" ] && [ -f "$APK_UNSIGNED" ]; then
    echo "Unsigned APK detected. Signing with AnyaKey.jks..."
    
    # Find latest build-tools
    BUILD_TOOLS_DIR=$(ls -1d /home/yamada/Android/Sdk/build-tools/* 2>/dev/null | sort -V | tail -n 1)
    APKSIGNER="$BUILD_TOOLS_DIR/apksigner"
    ZIPALIGN="$BUILD_TOOLS_DIR/zipalign"
    
    if [ -f "$APKSIGNER" ] && [ -f "$ZIPALIGN" ]; then
        KEY_ALIAS="key0"
        KS_PASS="aw240706"
        
        echo "Aligning APK..."
        "$ZIPALIGN" -v -p 4 "$APK_UNSIGNED" "$APK_ALIGNED" > /dev/null
        
        echo "Signing APK..."
        "$APKSIGNER" sign --ks "AnyaKey.jks" --ks-key-alias "$KEY_ALIAS" --ks-pass "pass:$KS_PASS" --key-pass "pass:$KS_PASS" --out "$APK_SIGNED" "$APK_ALIGNED"
        
        if [ $? -eq 0 ]; then
            echo "Signing successful!"
            rm -f "$APK_ALIGNED"
        else
            echo "ERROR: Signing failed!"
        fi
    else
        echo "WARNING: apksigner or zipalign not found in /home/yamada/Android/Sdk/build-tools/. Skipping signing."
    fi
fi

if [ -f "$APK_SIGNED" ]; then
    cp "$APK_SIGNED" "$MODULES_DIR/AnyaThermal.apk"
    echo "Copied app-release.apk to Modules/AnyaThermal.apk"
elif [ -f "$APK_UNSIGNED" ]; then
    cp "$APK_UNSIGNED" "$MODULES_DIR/AnyaThermal.apk"
    echo "Copied app-release-unsigned.apk to Modules/AnyaThermal.apk (Unsigned)"
else
    echo "ERROR: No release APK found!"
fi
