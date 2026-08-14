/*
Anya Melfissa - Disable Thermal
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

// Struct to pass arguments into the property callback
struct thermal_callback_data {
    int action;
    const char *state;
};

// Callback to read the actual name and value of a property
static void read_prop_callback(void *cookie, const char *name, const char *value, uint32_t serial) {
    struct thermal_callback_data *data = (struct thermal_callback_data *)cookie;
    
    // Look for init services with "thermal" in the name
    if (strncmp(name, "init.svc.", 9) == 0 && strstr(name, "thermal") != NULL) {
        const char *svc_name = name + 9; // Skip "init.svc." prefix
        char cmd[256];

        if (data->action == 0 && data->state != NULL) { // Spoof
            snprintf(cmd, sizeof(cmd), "resetprop -n \"init.svc.%s\" \"%s\"", svc_name, data->state);
            system(cmd);
        } else if (data->action == 1) { // Stop
            snprintf(cmd, sizeof(cmd), "stop \"%s\"", svc_name);
            system(cmd);
        } else if (data->action == 2) { // Restore
            snprintf(cmd, sizeof(cmd), "stop \"%s\" >/dev/null 2>&1", svc_name);
            system(cmd);
            snprintf(cmd, sizeof(cmd), "resetprop -n \"init.svc.%s\" \"stopped\" >/dev/null 2>&1", svc_name);
            system(cmd);
            snprintf(cmd, sizeof(cmd), "start \"%s\" >/dev/null 2>&1", svc_name);
            system(cmd);
        }
    }
}

// Callback for iterating through all properties
static void iterate_prop_callback(const prop_info *pi, void *cookie) {
    __system_property_read_callback(pi, read_prop_callback, cookie);
}

// Helper to manage thermal services using Android's native property shared memory
void process_thermal_services(int action, const char *state) {
    struct thermal_callback_data data = { action, state };
    __system_property_foreach(iterate_prop_callback, &data);
}

void spoof_thermal_props(const char *state) {
    process_thermal_services(0, state);
}

void stop_thermal_services() {
    process_thermal_services(1, NULL);
}

void restore_thermal_services() {
    process_thermal_services(2, NULL);
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