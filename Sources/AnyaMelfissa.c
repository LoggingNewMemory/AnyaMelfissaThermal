/*
Anya Melfissa - Disable Thermal (Remake)
Copyright (C) 2026 Kanagawa Yamada
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
#include <sys/system_properties.h>

#define DUMMY_FILE "/data/local/tmp/empty_thermal"

void create_dummy_file() {
    int fd = open(DUMMY_FILE, O_CREAT | O_WRONLY, 0644);
    if (fd >= 0) close(fd);
}

// Check if it's a vendor or odm executable/script
int is_vendor_target(const char *path) {
    if (strncmp(path, "/vendor/", 8) == 0) return 1;
    if (strncmp(path, "/odm/", 5) == 0) return 1;
    return 0;
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
            char exe_link[256];
            char exe_target[512] = {0};
            char cmdline[1024] = {0};
            int is_target = 0;
            int is_script = 0;

            snprintf(exe_link, sizeof(exe_link), "/proc/%d/exe", pid);
            ssize_t len = readlink(exe_link, exe_target, sizeof(exe_target) - 1);
            
            if (len != -1) {
                exe_target[len] = '\0';
                
                // Ignore all Android apps entirely (fixes vendor bridge display dead)
                if (strstr(exe_target, "app_process") != NULL) continue;
                
                // Target native binaries in /vendor/ or /odm/ with "thermal" in the name
                if (strstr(exe_target, "thermal") != NULL && is_vendor_target(exe_target)) {
                    // Critical fix: NEVER kill the thermal HAL, doing so causes system_server to deadlock and the device to freeze!
                    if (strstr(exe_target, "hardware.thermal") == NULL) {
                        is_target = 1;
                    }
                }
                
                // If it's a shell script, check the command line
                if (strcmp(exe_target, "/system/bin/sh") == 0 || strcmp(exe_target, "/vendor/bin/sh") == 0) {
                    is_script = 1;
                }
            }

            if (!is_target && is_script) {
                snprintf(path, sizeof(path), "/proc/%d/cmdline", pid);
                FILE *f = fopen(path, "r");
                if (f) {
                    size_t n = fread(cmdline, 1, sizeof(cmdline) - 1, f);
                    if (n > 0) {
                        cmdline[n] = 0; // cmdline arguments are null-separated, first arg is usually the script path
                        
                        // Check if the script path contains thermal and is from vendor/odm
                        // E.g., /vendor/bin/thermal_script.sh 
                        
                        // Sometimes the first arg is 'sh', second is script. Let's just search the block.
                        // For simplicity, we just check if any part of the cmdline matches our criteria
                        // To be safe and avoid matching arguments like `--thermal`, we ensure the match is an absolute vendor path.
                        if (strstr(cmdline, "thermal") != NULL && (strstr(cmdline, "/vendor/") != NULL || strstr(cmdline, "/odm/") != NULL)) {
                            if (strstr(cmdline, "hardware.thermal") == NULL) {
                                is_target = 1;
                            }
                        }
                    }
                    fclose(f);
                }
            }

            if (is_target) {
                if (!is_script) {
                    // Bind mount dummy over the executable to prevent init from restarting it
                    mount(DUMMY_FILE, exe_target, NULL, MS_BIND, NULL);
                }
                
                // Kill the native vendor service
                kill(pid, SIGKILL);
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
            char dev[256] = {0};
            char mnt[256] = {0};
            
            if (sscanf(line, "%255s %255s", dev, mnt) == 2) {
                // Only unmount if it's a vendor/odm thermal mount (which means it's our dummy mount)
                if (strstr(mnt, "thermal") != NULL && is_vendor_target(mnt)) {
                    umount2(mnt, 0); // Native Unmount
                }
            }
        }
    }
    fclose(f);
    
    unlink(DUMMY_FILE); // Clean up dummy file
}

struct thermal_callback_data {
    int action;
};

static void read_prop_callback(void *cookie, const char *name, const char *value, uint32_t serial) {
    struct thermal_callback_data *data = (struct thermal_callback_data *)cookie;
    
    // Look for init services with "thermal" in the name
    if (strncmp(name, "init.svc.", 9) == 0 && strstr(name, "thermal") != NULL) {
        const char *svc_name = name + 9;
        
        // Exclude Android's native thermalserviced explicitly to prevent games getting stuck
        if (strcmp(svc_name, "thermalserviced") == 0) return;
        
        // Exclude thermal HAL to prevent system freezes
        if (strstr(svc_name, "hardware.thermal") != NULL) return;

        char cmd[256];
        if (data->action == 1) { // Stop
            snprintf(cmd, sizeof(cmd), "stop \"%s\"", svc_name);
            system(cmd);
        } else if (data->action == 2) { // Restore
            snprintf(cmd, sizeof(cmd), "start \"%s\" >/dev/null 2>&1", svc_name);
            system(cmd);
        }
    }
}

static void iterate_prop_callback(const prop_info *pi, void *cookie) {
    __system_property_read_callback(pi, read_prop_callback, cookie);
}

void process_thermal_services(int action) {
    struct thermal_callback_data data = { action };
    __system_property_foreach(iterate_prop_callback, &data);
}

void exec_anya_kawaii() {
    unmount_thermals();
    process_thermal_services(2); // Restore
}

void exec_anya_melfissa() {
    block_kill();
    process_thermal_services(1); // Stop
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