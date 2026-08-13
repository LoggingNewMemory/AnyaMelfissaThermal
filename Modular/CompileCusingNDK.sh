#!/bin/bash

# ==========================================
# Compile C files using Android NDK
# ==========================================

export NDK=/opt/android-ndk
export API=28
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin

SRC_DIR="Sources"
COMPILED_DIR="Modules/Compiled"
mkdir -p "$COMPILED_DIR"

if [ -z "$(ls -A $SRC_DIR/*.c 2>/dev/null)" ]; then
    echo "No C source files found in $SRC_DIR, skipping C compilation."
    exit 0
fi

echo "Compiling C files using NDK..."

for c_file in $SRC_DIR/*.c; do
    filename=$(basename -- "$c_file")
    binary_name="${filename%.*}"
    
    echo "Building $binary_name..."
    if ! $TOOLCHAIN/aarch64-linux-android$API-clang -Wall -O2 \
        -o "$COMPILED_DIR/$binary_name" \
        "$c_file"; then
        echo "Error: Compilation of $binary_name failed!"
        exit 1
    fi
    $TOOLCHAIN/llvm-strip "$COMPILED_DIR/$binary_name"
    echo "Successfully compiled $binary_name"
done
