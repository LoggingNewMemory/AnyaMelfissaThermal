#!/bin/bash

#========================
# NON INTERACTIVE MODE
# Remove this for interactive mode
# 1 = Enable | 0 = Disable
#========================
export MODULEVERSION="8.0"
export FLASHTODEVICE="1"
export SENDTOTELEGRAM="0"

#========================
# BUILD VARIANT
# 1 = Full
# 2 = Minimal
#========================
export VARIANT="1"

MODULES_DIR="Modules"
BUILD_DIR="Build"

mkdir -p "$BUILD_DIR"

welcome() {
    clear
    echo "---------------------------------"
    echo "      Yamada Module Builder      "
    echo "---------------------------------"
    echo ""
}

success() {
    echo "---------------------------------"
    echo "    Build Process Completed      "
    printf "     Ambatukam : %s seconds\n" "$SECONDS"
    echo "---------------------------------"
}

build_modules() {
    rm -rf "$BUILD_DIR"/*

    if [ -n "$MODULEVERSION" ]; then
        VERSION="$MODULEVERSION"
        echo "Version: $VERSION"
    else
        read -p "Enter Version (e.g., V1.0): " VERSION
    fi

    # --- C Compilation ---
    if [ -f "Modular/CompileCusingNDK.sh" ]; then
        bash Modular/CompileCusingNDK.sh
        if [ $? -ne 0 ]; then
            echo "Error during C compilation. Aborting."
            exit 1
        fi
    fi

    # --- Build App ---
    if [ -f "Modular/CompileApp.sh" ]; then
        bash Modular/CompileApp.sh
    fi

    cd "$MODULES_DIR" || exit 1
    MODULE_ID=$(grep "^id=" "module.prop" | cut -d'=' -f2 | tr -d '[:space:]')

    # Fix: Use sed without attempting to preserve permissions
    if [ -f "module.prop" ]; then
        cp "module.prop" "module.prop.tmp"
        sed "s/^version=.*$/version=$VERSION/" "module.prop.tmp" > "module.prop"
        rm "module.prop.tmp"
    fi

    if [ -f "customize.sh" ]; then
        cp "customize.sh" "customize.sh.tmp"
        sed "s/^ui_print \"Version : .*$/ui_print \"Version : $VERSION\"/" "customize.sh.tmp" > "customize.sh"
        rm "customize.sh.tmp"
    fi

    # --- Build FULL Variant ---
    ZIP_NAME_FULL="${MODULE_ID}-${VERSION}-FULL.zip"
    ZIP_PATH_FULL="../$BUILD_DIR/$ZIP_NAME_FULL"
    FULL_DIR="../$BUILD_DIR/full_tmp"
    mkdir -p "$FULL_DIR"
    cp -r ./* "$FULL_DIR/"
    sed -i 's/^name=.*/& [FULL]/' "$FULL_DIR/module.prop"
    
    pushd "$FULL_DIR" >/dev/null
    zip -q -r "../$ZIP_NAME_FULL" ./*
    popd >/dev/null
    rm -rf "$FULL_DIR"
    echo "Created: $ZIP_NAME_FULL (Full Variant)"

    # --- Build MINIMAL Variant ---
    ZIP_NAME_MINIMAL="${MODULE_ID}-${VERSION}-MINIMAL.zip"
    ZIP_PATH_MINIMAL="../$BUILD_DIR/$ZIP_NAME_MINIMAL"
    MINIMAL_DIR="../$BUILD_DIR/minimal_tmp"
    mkdir -p "$MINIMAL_DIR"
    cp -r ./* "$MINIMAL_DIR/"
    rm -f "$MINIMAL_DIR/AnyaThermal.apk"
    rm -f "$MINIMAL_DIR/AnyaConfig.txt"
    sed -i 's/^name=.*/& [MINIMAL]/' "$MINIMAL_DIR/module.prop"
    
    pushd "$MINIMAL_DIR" >/dev/null
    zip -q -r "../$ZIP_NAME_MINIMAL" ./*
    popd >/dev/null
    rm -rf "$MINIMAL_DIR"
    echo "Created: $ZIP_NAME_MINIMAL (Minimal Variant)"

    cd ..

    # Decide which one to flash
    if [ "$VARIANT" = "2" ]; then
        FLASH_ZIP="$ZIP_NAME_MINIMAL"
    else
        FLASH_ZIP="$ZIP_NAME_FULL"
    fi

    # --- ADB Flash Prompt ---
    if [ -f "Modular/FlashToDevice.sh" ]; then
        echo "Flashing: $FLASH_ZIP"
        bash Modular/FlashToDevice.sh "$BUILD_DIR/$FLASH_ZIP" "$BUILD_DIR"
    fi

    # --- Telegram Post ---
    if [ -f "Modular/SendToTelegram.sh" ]; then
        echo "Sending FULL variant:"
        bash Modular/SendToTelegram.sh "$MODULE_ID-FULL" "$VERSION" "$BUILD_DIR/$ZIP_NAME_FULL"
        echo "Sending MINIMAL variant:"
        bash Modular/SendToTelegram.sh "$MODULE_ID-MINIMAL" "$VERSION" "$BUILD_DIR/$ZIP_NAME_MINIMAL"
    fi
}

welcome
SECONDS=0  # Start timing
build_modules
success
