/*
 * Anya Melfissa - Disable Thermal
 * Pure Native POSIX C Implementation
 * Copyright (C) 2026 Kanagawa Yamada
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <dirent.h>
#include <sys/mount.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <signal.h>

#define DUMMY_FILE "/data/local/tmp/empty_thermal"

// Helper to write an empty dummy file
void create_dummy_file() {
    int fd = open(DUMMY_FILE, O_CREAT | O_WRONLY, 0644);
    if (fd >= 0) close(fd);
}

// ---------------------------------------------------------
// Native Syscalls
// ---------------------------------------------------------

// Natively find and block all thermal processes
void native_block_and_kill_thermals() {
    DIR *dir = opendir("/proc");
    if (!dir) return;

    create_dummy_file();

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        int pid = atoi(ent->d_name);
        if (pid > 0) {
            char path[256];
            char comm[256];
            snprintf(path, sizeof(path), "/proc/%d/comm", pid);
            
            FILE *f = fopen(path, "r");
            if (f) {
                if (fgets(comm, sizeof(comm), f)) {
                    comm[strcspn(comm, "\n")] = 0; // Remove newline
                    
                    if (strstr(comm, "thermal") != NULL) {
                        char exe_link[256];
                        char exe_target[512];
                        snprintf(exe_link, sizeof(exe_link), "/proc/%d/exe", pid);
                        
                        ssize_t len = readlink(exe_link, exe_target, sizeof(exe_target) - 1);
                        if (len != -1) {
                            exe_target[len] = '\0';
                            // Native Bind Mount
                            mount(DUMMY_FILE, exe_target, NULL, MS_BIND, NULL);
                        }
                        
                        // Native Kill
                        kill(pid, SIGKILL);
                    }
                }
                fclose(f);
            }
        }
    }
    closedir(dir);
    
    // Native File Deletion
    unlink("/data/vendor/thermal/config");
    system("rm -f /data/vendor/thermal/*.dump 2>/dev/null &");
}

// Natively unmount all thermal bind mounts
void native_unmount_thermals() {
    FILE *f = fopen("/proc/mounts", "r");
    if (!f) return;

    char line[1024];
    while (fgets(line, sizeof(line), f)) {
        if (strstr(line, "thermal") != NULL) {
            char *dev = strtok(line, " ");
            char *mnt = strtok(NULL, " ");
            
            if (mnt && strstr(mnt, "thermal") != NULL) {
                umount2(mnt, 0); // Native Unmount
            }
        }
    }
    fclose(f);
    
    unlink(DUMMY_FILE); // Clean up dummy file
}

// ---------------------------------------------------------
// Magisk Property Interfacing (Still requires shell for resetprop)
// ---------------------------------------------------------

void spoof_thermal_props(const char *state) {
    char cmd[512];
    snprintf(cmd, sizeof(cmd),
        "(getprop | grep -E '^\\[init\\.svc\\..*thermal' | cut -d'[' -f2 | cut -d']' -f1 | "
        "while read -r prop; do [ -n \"$prop\" ] && resetprop -n \"$prop\" \"%s\"; done) &", state);
    system(cmd);
}

void stop_thermal_services() {
    system("(getprop | grep -E '^\\[init\\.svc\\..*thermal' | cut -d: -f1 | tr -d '[]' | sed 's/init\\.svc\\.//g' | while read -r svc; do stop \"$svc\"; done) &");
}

void restore_thermal_services() {
    system("(getprop | grep -E '^\\[init\\.svc\\..*thermal' | cut -d: -f1 | tr -d '[]' | sed 's/init\\.svc\\.//g' | while read -r svc; do stop \"$svc\" 2>/dev/null; resetprop -n \"init.svc.$svc\" \"stopped\" 2>/dev/null; start \"$svc\" 2>/dev/null; done) &");
}

void spoof_other_props() {
    system("(for prop in $(getprop | grep -E 'sys\\..*thermal|thermal_config' | cut -d: -f1 | tr -d '[]'); do resetprop -n \"$prop\" \"0\"; done; "
           "resetprop debug.thermal.throttle.support 2>/dev/null | grep -q 'yes' && resetprop -n debug.thermal.throttle.support no) &");
}

// ==========================================
// Execution
// ==========================================
void exec_anya_kawaii() {
    native_unmount_thermals();
    restore_thermal_services();
    spoof_thermal_props("running");
}

void exec_anya_melfissa() {
    native_block_and_kill_thermals();
    stop_thermal_services();
    spoof_other_props();
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
