#define _GNU_SOURCE
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
#include <sys/stat.h>

#define FAKE_TEMP_FILE "/data/adb/modules/AnyaMelfissa/fake_temp"
#define FAKE_TEMP_VALUE "30000\n"
#define THERMAL_DIR "/sys/class/thermal"

FILE *sh_pipe = NULL;

void umount_all(const char *path) {
    if (sh_pipe) {
        fprintf(sh_pipe, "umount -l \"%s\" 2>/dev/null\n", path);
    }
}

void mount_bind(const char *src, const char *dest) {
    if (sh_pipe) {
        fprintf(sh_pipe, "mount --bind \"%s\" \"%s\"\n", src, dest);
    }
}

void create_fake_temp_file() {
    int fd = open(FAKE_TEMP_FILE, O_CREAT | O_WRONLY | O_TRUNC, 0644);
    if (fd >= 0) {
        write(fd, FAKE_TEMP_VALUE, strlen(FAKE_TEMP_VALUE));
        close(fd);
    }
}

void write_sysfs(const char *path, const char *val) {
    int fd = open(path, O_WRONLY);
    if (fd >= 0) {
        write(fd, val, strlen(val));
        close(fd);
    }
}

void spoof_thermal() {
    create_fake_temp_file();

    sh_pipe = popen("sh", "w");
    if (!sh_pipe) return;

    DIR *dir = opendir(THERMAL_DIR);
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strncmp(ent->d_name, "thermal_zone", 12) == 0) {
            char zone_path[512];
            snprintf(zone_path, sizeof(zone_path), "%s/%s", THERMAL_DIR, ent->d_name);
            
            char emul_path[512];
            char temp_path[512];
            char mode_path[512];
            snprintf(emul_path, sizeof(emul_path), "%s/emul_temp", zone_path);
            snprintf(temp_path, sizeof(temp_path), "%s/temp", zone_path);
            snprintf(mode_path, sizeof(mode_path), "%s/mode", zone_path);
            
            // Write to mode native kernel disable
            write_sysfs(mode_path, "disabled\n");

            // Write to emul_temp native kernel spoof
            write_sysfs(emul_path, FAKE_TEMP_VALUE);

            // Unmount first in case it's already mounted to prevent stacking
            umount_all(temp_path);
            // Bind mount the fake files over the real ones
            mount_bind(FAKE_TEMP_FILE, temp_path);
        }
    }
    closedir(dir);
    if (sh_pipe) {
        pclose(sh_pipe);
        sh_pipe = NULL;
    }
}

void restore_thermal() {
    sh_pipe = popen("sh", "w");
    if (!sh_pipe) return;

    DIR *dir = opendir(THERMAL_DIR);
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strncmp(ent->d_name, "thermal_zone", 12) == 0) {
            char zone_path[512];
            snprintf(zone_path, sizeof(zone_path), "%s/%s", THERMAL_DIR, ent->d_name);
            
            char emul_path[512];
            char temp_path[512];
            char mode_path[512];
            snprintf(emul_path, sizeof(emul_path), "%s/emul_temp", zone_path);
            snprintf(temp_path, sizeof(temp_path), "%s/temp", zone_path);
            snprintf(mode_path, sizeof(mode_path), "%s/mode", zone_path);
            
            // Re-enable native kernel thermal monitoring
            write_sysfs(mode_path, "enabled\n");

            // Write 0 to disable emulation
            write_sysfs(emul_path, "0");

            // Unmount the fake files with MNT_DETACH to prevent EBUSY
            umount_all(temp_path);
        }
    }
    closedir(dir);
    if (sh_pipe) {
        pclose(sh_pipe);
        sh_pipe = NULL;
    }
    
    unlink(FAKE_TEMP_FILE);
}

void exec_anya_kawaii() {
    restore_thermal();
    system("sed -i 's/^ANYA .*/ANYA 0/' /data/adb/modules/AnyaMelfissa/AnyaConfig.txt 2>/dev/null");
}

void exec_anya_melfissa() {
    restore_thermal();
    spoof_thermal();
    system("sed -i 's/^ANYA .*/ANYA 1/' /data/adb/modules/AnyaMelfissa/AnyaConfig.txt 2>/dev/null");
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