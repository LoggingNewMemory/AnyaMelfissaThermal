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

// To create a dummy file for hard to kill thermal
void create_dummy_file() {
    int fd = open(DUMMY_FILE, O_CREAT | O_WRONLY, 0644);
    if (fd >= 0) close(fd);
}

void block_kill() {
    DIR *dir = opendir("/proc");
    if (!dir) return;

    create_dummy_file();

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        int pid = atoi(ent->d_name);
        if (pid > 0) {
            char path[256];
            char comm[256] = {0};
            char cmdline[1024] = {0};
            int is_thermal = 0;

            snprintf(path, sizeof(path), "/proc/%d/comm", pid);
            FILE *f = fopen(path, "r");
            if (f) {
                if (fgets(comm, sizeof(comm), f)) {
                    comm[strcspn(comm, "\n")] = 0;
                    if (strstr(comm, "thermal") != NULL) {
                        is_thermal = 1;
                    }
                }
                fclose(f);
            }

            if (!is_thermal) {
                snprintf(path, sizeof(path), "/proc/%d/cmdline", pid);
                f = fopen(path, "r");
                if (f) {
                    size_t n = fread(cmdline, 1, sizeof(cmdline) - 1, f);
                    if (n > 0) {
                        cmdline[n] = 0;
                        if (strstr(cmdline, "thermal") != NULL) {
                            is_thermal = 1;
                        }
                    }
                    fclose(f);
                }
            }

            if (is_thermal) {
                char exe_link[256];
                char exe_target[512] = {0};
                int is_app = 0;

                snprintf(exe_link, sizeof(exe_link), "/proc/%d/exe", pid);
                ssize_t len = readlink(exe_link, exe_target, sizeof(exe_target) - 1);
                if (len != -1) {
                    exe_target[len] = '\0';
                    if (strstr(exe_target, "app_process") != NULL) {
                        is_app = 1;
                    } else {
                        mount(DUMMY_FILE, exe_target, NULL, MS_BIND, NULL);
                    }
                }
                
                if (is_app) {
                    kill(pid, SIGSTOP); // Freeze app processes to prevent restart
                } else {
                    kill(pid, SIGKILL); // Kill native services
                }
            }
        }
    }
    closedir(dir);
}

void unmount_thermals() {
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

// Spoofer for running

void spoof_thermal_props(const char *state) {
    char cmd[512];
    snprintf(cmd, sizeof(cmd),
        "(getprop | grep -E '^\\[init\\.svc\\..*thermal' | cut -d'[' -f2 | cut -d']' -f1 | "
        "while read -r prop; do [ -n \"$prop\" ] && resetprop -n \"$prop\" \"%s\"; done) &", state);
    system(cmd);
}

void stop_thermal_services() {
    const char *script = 
        "("
        "  getprop | "
        "  grep -E '^\\[init\\.svc\\..*thermal' | "
        "  cut -d: -f1 | "
        "  tr -d '[]' | "
        "  sed 's/init\\.svc\\.//g' | "
        "  while read -r svc; do "
        "      stop \"$svc\"; "
        "  done"
        ") &";

    system(script);
}

void restore_thermal_services() {
    const char *script = 
        "("
        "  getprop | "
        "  grep -E '^\\[init\\.svc\\..*thermal' | "
        "  cut -d: -f1 | "
        "  tr -d '[]' | "
        "  sed 's/init\\.svc\\.//g' | "
        "  while read -r svc; do "
        "      stop \"$svc\" 2>/dev/null; "
        "      resetprop -n \"init.svc.$svc\" \"stopped\" 2>/dev/null; "
        "      start \"$svc\" 2>/dev/null; "
        "  done"
        ") &";

    system(script);
}

// Execution

void resume_frozen_processes() {
    DIR *dir = opendir("/proc");
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        int pid = atoi(ent->d_name);
        if (pid > 0) {
            char path[256];
            char comm[256] = {0};
            char cmdline[1024] = {0};
            int is_thermal = 0;

            snprintf(path, sizeof(path), "/proc/%d/comm", pid);
            FILE *f = fopen(path, "r");
            if (f) {
                if (fgets(comm, sizeof(comm), f)) {
                    comm[strcspn(comm, "\n")] = 0;
                    if (strstr(comm, "thermal") != NULL) {
                        is_thermal = 1;
                    }
                }
                fclose(f);
            }

            if (!is_thermal) {
                snprintf(path, sizeof(path), "/proc/%d/cmdline", pid);
                f = fopen(path, "r");
                if (f) {
                    size_t n = fread(cmdline, 1, sizeof(cmdline) - 1, f);
                    if (n > 0) {
                        cmdline[n] = 0;
                        if (strstr(cmdline, "thermal") != NULL) {
                            is_thermal = 1;
                        }
                    }
                    fclose(f);
                }
            }

            if (is_thermal) {
                kill(pid, SIGCONT);
            }
        }
    }
    closedir(dir);
}

void exec_anya_kawaii() {
    unmount_thermals();
    resume_frozen_processes();
    restore_thermal_services();
    spoof_thermal_props("running");
}

void exec_anya_melfissa() {
    block_kill();
    stop_thermal_services();
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