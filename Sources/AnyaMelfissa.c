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
#define FAKE_ZERO_FILE "/data/adb/modules/AnyaMelfissa/fake_zero"
#define FAKE_TEMP_VALUE "30000\n"
#define FAKE_ZERO_VALUE "0\n"
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

void create_fake_zero_file() {
    int fd = open(FAKE_ZERO_FILE, O_CREAT | O_WRONLY | O_TRUNC, 0644);
    if (fd >= 0) {
        write(fd, FAKE_ZERO_VALUE, strlen(FAKE_ZERO_VALUE));
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
    create_fake_zero_file();

    sh_pipe = popen("sh", "w");
    if (!sh_pipe) return;

    DIR *dir = opendir(THERMAL_DIR);
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strncmp(ent->d_name, "thermal_zone", 12) == 0) {
            char zone_path[512];
            snprintf(zone_path, sizeof(zone_path), "%s/%s", THERMAL_DIR, ent->d_name);
            
            char temp_path[512];
            snprintf(temp_path, sizeof(temp_path), "%s/temp", zone_path);

            // Unmount first in case it's already mounted to prevent stacking
            umount_all(temp_path);
            // Bind mount the fake files over the real ones
            mount_bind(FAKE_TEMP_FILE, temp_path);
            
            DIR *zdir = opendir(zone_path);
            if (zdir) {
                struct dirent *zent;
                while ((zent = readdir(zdir)) != NULL) {
                    if (strncmp(zent->d_name, "cdev", 4) == 0 && strstr(zent->d_name, "_trip_point") != NULL) {
                        char cdev_path[512];
                        snprintf(cdev_path, sizeof(cdev_path), "%s/%s", zone_path, zent->d_name);
                        umount_all(cdev_path);
                        mount_bind(FAKE_ZERO_FILE, cdev_path);
                    } else if (strncmp(zent->d_name, "cdev", 4) == 0 && strstr(zent->d_name, "_weight") != NULL) {
                        char weight_path[512];
                        snprintf(weight_path, sizeof(weight_path), "%s/%s", zone_path, zent->d_name);
                        umount_all(weight_path);
                        mount_bind(FAKE_ZERO_FILE, weight_path);
                    }
                }
                closedir(zdir);
            }
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
            
            char temp_path[512];
            snprintf(temp_path, sizeof(temp_path), "%s/temp", zone_path);

            // Unmount the fake files with MNT_DETACH to prevent EBUSY
            umount_all(temp_path);
            
            DIR *zdir = opendir(zone_path);
            if (zdir) {
                struct dirent *zent;
                while ((zent = readdir(zdir)) != NULL) {
                    if (strncmp(zent->d_name, "cdev", 4) == 0 && strstr(zent->d_name, "_trip_point") != NULL) {
                        char cdev_path[512];
                        snprintf(cdev_path, sizeof(cdev_path), "%s/%s", zone_path, zent->d_name);
                        umount_all(cdev_path);
                    } else if (strncmp(zent->d_name, "cdev", 4) == 0 && strstr(zent->d_name, "_weight") != NULL) {
                        char weight_path[512];
                        snprintf(weight_path, sizeof(weight_path), "%s/%s", zone_path, zent->d_name);
                        umount_all(weight_path);
                    }
                }
                closedir(zdir);
            }
        }
    }
    closedir(dir);
    if (sh_pipe) {
        pclose(sh_pipe);
        sh_pipe = NULL;
    }
    
    unlink(FAKE_TEMP_FILE);
    unlink(FAKE_ZERO_FILE);
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