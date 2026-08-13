#!/bin/bash

# ==========================================
# Compile C files using Android NDK
# ==========================================

export NDK=/opt/android-ndk
export API=28
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin

SRC_DIR="Sources"
if ! ls $SRC_DIR/*.c >/dev/null 2>&1; then
    echo "No C source files found in $SRC_DIR, skipping C compilation."
    exit 0
fi

echo "Compiling C files using NDK..."

for c_file in $SRC_DIR/*.c; do
    filename=$(basename -- "$c_file")
    binary_name="${filename%.*}"
    TARGET_DIR="Modules/$binary_name"
    mkdir -p "$TARGET_DIR"
    
    echo "Building $binary_name..."
    if ! $TOOLCHAIN/aarch64-linux-android$API-clang -Wall -O2 \
        -o "$TARGET_DIR/$binary_name" \
        "$c_file"; then
        echo "Error: Compilation of $binary_name failed!"
        exit 1
    fi
    $TOOLCHAIN/llvm-strip "$TARGET_DIR/$binary_name"
    echo "Successfully compiled $binary_name"
done
