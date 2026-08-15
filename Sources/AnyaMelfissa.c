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
            
            // Unmount first in case it's already mounted to prevent stacking
            umount2(temp_path, MNT_DETACH);
            
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
            
            // Unmount the fake temperature file with MNT_DETACH to prevent EBUSY
            umount2(temp_path, MNT_DETACH);
        }
    }
    closedir(dir);
    
    unlink(FAKE_TEMP_FILE);
}

void exec_anya_kawaii() {
    restore_temperatures();
}

void exec_anya_melfissa() {
    spoof_temperatures();
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