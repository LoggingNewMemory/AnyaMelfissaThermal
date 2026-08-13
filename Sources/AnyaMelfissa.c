/*
 * Anya Melfissa - Disable Thermal
 * Standalone C Implementation
 * Copyright (C) 2026 Kanagawa Yamada
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

// Spoof thermal properties
static void spoof_thermal_props(const char *state) {
    char cmd[512];
    snprintf(cmd, sizeof(cmd),
        "getprop | grep -E '^\\[init\\.svc\\..*thermal' | cut -d'[' -f2 | cut -d']' -f1 | "
        "while read -r prop; do [ -n \"$prop\" ] && resetprop -n \"$prop\" \"%s\"; done", state);
    system(cmd);
}

// ==========================================
// Anya Kawaii (Restore thermals)
// ==========================================
void exec_anya_kawaii() {
    // Unmount all blocked thermal binaries
    system("awk '{print $2}' /proc/mounts | grep 'thermal' | while read -r mnt; do "
           "  umount \"$mnt\" 2>/dev/null; "
           "done; "
           "rm -f /data/local/tmp/empty_thermal 2>/dev/null");

    // Enable msm_thermal
    system("find /sys/ -name enabled 2>/dev/null | grep 'msm_thermal' | while read -r msm; do "
           "echo 'Y' > \"$msm\" 2>/dev/null; "
           "echo '1' > \"$msm\" 2>/dev/null; "
           "done");

    // Restart thermal services safely (Stop first to kill rogue processes, clear spoof, then start)
    system("getprop | grep -E '^\\[init\\.svc\\..*thermal' | "
           "cut -d: -f1 | tr -d '[]' | sed 's/init\\.svc\\.//g' | "
           "while read -r svc; do stop \"$svc\" 2>/dev/null; resetprop -n \"init.svc.$svc\" \"stopped\" 2>/dev/null; start \"$svc\" 2>/dev/null; done");

    spoof_thermal_props("running");
}

// ==========================================
// Anya Melfissa (Kill thermals)
// ==========================================
void exec_anya_melfissa() {
    // 1. Block ALL running thermal binaries so they cannot be restarted by init
    system("touch /data/local/tmp/empty_thermal 2>/dev/null; "
           "for p in $(pgrep -l 'thermal' | cut -d' ' -f1); do "
           "  exe=$(readlink -f /proc/$p/exe 2>/dev/null); "
           "  if [ -n \"$exe\" ] && [ -f \"$exe\" ]; then "
           "    mount -o bind /data/local/tmp/empty_thermal \"$exe\" 2>/dev/null; "
           "  fi; "
           "done; "
           "rm -f /data/vendor/thermal/config /data/vendor/thermal/*.dump 2>/dev/null");

    // 2. Kill & stop all thermal processes and services
    system("getprop | grep -E '^\\[init\\.svc\\..*thermal' | "
           "cut -d: -f1 | tr -d '[]' | sed 's/init\\.svc\\.//g' | "
           "while read -r svc; do stop \"$svc\"; done; "
           "killall -9 thermald 2>/dev/null; "
           "pgrep -l 'thermal' | cut -d' ' -f1 | xargs -r kill -9 2>/dev/null");

    // Disable msm_thermal
    system("find /sys/ -name enabled 2>/dev/null | grep 'msm_thermal' | while read -r msm; do "
           "echo 'N' > \"$msm\" 2>/dev/null; "
           "echo '0' > \"$msm\" 2>/dev/null; "
           "done");

    // Spoof thermal props + OEM check
    system("for prop in $(getprop | grep -E 'sys\\..*thermal|thermal_config' | "
           "cut -d: -f1 | tr -d '[]'); do resetprop -n \"$prop\" \"0\"; done; "
           "resetprop debug.thermal.throttle.support 2>/dev/null | grep -q 'yes' && "
           "resetprop -n debug.thermal.throttle.support no");

    spoof_thermal_props("running");
}

int main(int argc, char *argv[]) {
    if (argc > 1) {
        if (strcmp(argv[1], "0") == 0) {
            exec_anya_kawaii();
        } else if (strcmp(argv[1], "1") == 0) {
            exec_anya_melfissa();
        }
    }
    return 0;
}
