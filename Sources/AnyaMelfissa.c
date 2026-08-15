/*
Anya Melfissa Thermal - Anya Thermal Kernel Level Like Logic
Copyright (C) 2026 Kanagawa Yamada
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/mount.h>
#include <sys/stat.h>
#include <sys/system_properties.h>

#define FAKE_TEMP_FILE "/data/local/tmp/fake_temp"
#define FAKE_TEMP_VALUE "30000\n"
#define THERMAL_DIR "/sys/class/thermal"

void create_fake_temp_file() {
    int fd = open(FAKE_TEMP_FILE, O_CREAT | O_WRONLY | O_TRUNC, 0644);
    if (fd >= 0) {
        write(fd, FAKE_TEMP_VALUE, strlen(FAKE_TEMP_VALUE));
        close(fd);
    }
}

void spoof_temperatures() {
    create_fake_temp_file();

    DIR *dir = opendir(THERMAL_DIR);
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strncmp(ent->d_name, "thermal_zone", 12) == 0) {
            char temp_path[512];
            snprintf(temp_path, sizeof(temp_path), "%s/%s/temp", THERMAL_DIR, ent->d_name);
            
            // Bind mount the fake temperature file over the real one
            mount(FAKE_TEMP_FILE, temp_path, NULL, MS_BIND, NULL);
        }
    }
    closedir(dir);
}

void restore_temperatures() {
    DIR *dir = opendir(THERMAL_DIR);
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strncmp(ent->d_name, "thermal_zone", 12) == 0) {
            char temp_path[512];
            snprintf(temp_path, sizeof(temp_path), "%s/%s/temp", THERMAL_DIR, ent->d_name);
            
            // Unmount the fake temperature file
            umount2(temp_path, 0);
        }
    }
    closedir(dir);
    
    unlink(FAKE_TEMP_FILE);
}

#define MAX_SERVICES 50
char services_to_restart[MAX_SERVICES][128];
int num_services = 0;

static void read_prop_callback(void *cookie, const char *name, const char *value, uint32_t serial) {
    (void)value;
    (void)serial;
    
    if (strncmp(name, "init.svc.", 9) == 0 && strstr(name, "thermal") != NULL) {
        const char *svc_name = name + 9;
        for (int i = 0; i < num_services; i++) {
            if (strcmp(services_to_restart[i], svc_name) == 0) return;
        }
        if (num_services < MAX_SERVICES) {
            strncpy(services_to_restart[num_services], svc_name, 127);
            services_to_restart[num_services][127] = '\0';
            num_services++;
        }
    }
}

static void iterate_prop_callback(const prop_info *pi, void *cookie) {
    __system_property_read_callback(pi, read_prop_callback, cookie);
}

void restart_init_services() {
    num_services = 0;
    __system_property_foreach(iterate_prop_callback, NULL);
    for (int i = 0; i < num_services; i++) {
        char cmd[256];
        snprintf(cmd, sizeof(cmd), "stop \"%s\"; start \"%s\" >/dev/null 2>&1", services_to_restart[i], services_to_restart[i]);
        system(cmd);
    }
}

void exec_anya_kawaii() {
    restore_temperatures();
    restart_init_services();
}

void exec_anya_melfissa() {
    spoof_temperatures();
    restart_init_services();
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