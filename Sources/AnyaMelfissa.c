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

#define FAKE_TEMP_FILE "/data/adb/modules/AnyaMelfissa/fake_temp"
#define FAKE_TRIP_FILE "/data/adb/modules/AnyaMelfissa/fake_trip"
#define FAKE_ZERO_FILE "/data/adb/modules/AnyaMelfissa/fake_zero"
#define FAKE_TEMP_VALUE "30000\n"
#define FAKE_TRIP_VALUE "200000\n"
#define FAKE_ZERO_VALUE "0\n"
#define THERMAL_DIR "/sys/class/thermal"

void create_fake_temp_file() {
    int fd = open(FAKE_TEMP_FILE, O_CREAT | O_WRONLY | O_TRUNC, 0644);
    if (fd >= 0) {
        write(fd, FAKE_TEMP_VALUE, strlen(FAKE_TEMP_VALUE));
        close(fd);
    }
}

void create_fake_trip_file() {
    int fd = open(FAKE_TRIP_FILE, O_CREAT | O_WRONLY | O_TRUNC, 0644);
    if (fd >= 0) {
        write(fd, FAKE_TRIP_VALUE, strlen(FAKE_TRIP_VALUE));
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
    create_fake_trip_file();
    create_fake_zero_file();

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
            umount2(temp_path, MNT_DETACH);
            // Bind mount the fake files over the real ones
            mount(FAKE_TEMP_FILE, temp_path, NULL, MS_BIND, NULL);
            
            DIR *zdir = opendir(zone_path);
            if (zdir) {
                struct dirent *zent;
                while ((zent = readdir(zdir)) != NULL) {
                    if (strncmp(zent->d_name, "trip_point_", 11) == 0 && strstr(zent->d_name, "_temp") != NULL) {
                        char trip_path[512];
                        snprintf(trip_path, sizeof(trip_path), "%s/%s", zone_path, zent->d_name);
                        umount2(trip_path, MNT_DETACH);
                        mount(FAKE_TRIP_FILE, trip_path, NULL, MS_BIND, NULL);
                    } else if (strncmp(zent->d_name, "trip_point_", 11) == 0 && strstr(zent->d_name, "_hyst") != NULL) {
                        char hyst_path[512];
                        snprintf(hyst_path, sizeof(hyst_path), "%s/%s", zone_path, zent->d_name);
                        umount2(hyst_path, MNT_DETACH);
                        mount(FAKE_ZERO_FILE, hyst_path, NULL, MS_BIND, NULL);
                    } else if (strncmp(zent->d_name, "cdev", 4) == 0 && strstr(zent->d_name, "_trip_point") != NULL) {
                        char cdev_path[512];
                        snprintf(cdev_path, sizeof(cdev_path), "%s/%s", zone_path, zent->d_name);
                        umount2(cdev_path, MNT_DETACH);
                        mount(FAKE_ZERO_FILE, cdev_path, NULL, MS_BIND, NULL);
                    } else if (strncmp(zent->d_name, "cdev", 4) == 0 && strstr(zent->d_name, "_weight") != NULL) {
                        char weight_path[512];
                        snprintf(weight_path, sizeof(weight_path), "%s/%s", zone_path, zent->d_name);
                        umount2(weight_path, MNT_DETACH);
                        mount(FAKE_ZERO_FILE, weight_path, NULL, MS_BIND, NULL);
                    }
                }
                closedir(zdir);
            }
        } else if (strncmp(ent->d_name, "cooling_device", 14) == 0) {
            char cdev_dir[512];
            snprintf(cdev_dir, sizeof(cdev_dir), "%s/%s", THERMAL_DIR, ent->d_name);
            
            char cur_state_path[512];
            snprintf(cur_state_path, sizeof(cur_state_path), "%s/cur_state", cdev_dir);
            
            umount2(cur_state_path, MNT_DETACH);
            
            mount(FAKE_ZERO_FILE, cur_state_path, NULL, MS_BIND, NULL);
        }
        
        // Sleep for 5ms to prevent flooding system_server with mount events (prevents UI freeze)
        usleep(5000);
    }
    closedir(dir);
}

void restore_thermal() {
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
            umount2(temp_path, MNT_DETACH);
            
            DIR *zdir = opendir(zone_path);
            if (zdir) {
                struct dirent *zent;
                while ((zent = readdir(zdir)) != NULL) {
                    if (strncmp(zent->d_name, "trip_point_", 11) == 0 && strstr(zent->d_name, "_temp") != NULL) {
                        char trip_path[512];
                        snprintf(trip_path, sizeof(trip_path), "%s/%s", zone_path, zent->d_name);
                        umount2(trip_path, MNT_DETACH);
                    } else if (strncmp(zent->d_name, "trip_point_", 11) == 0 && strstr(zent->d_name, "_hyst") != NULL) {
                        char hyst_path[512];
                        snprintf(hyst_path, sizeof(hyst_path), "%s/%s", zone_path, zent->d_name);
                        umount2(hyst_path, MNT_DETACH);
                    } else if (strncmp(zent->d_name, "cdev", 4) == 0 && strstr(zent->d_name, "_trip_point") != NULL) {
                        char cdev_path[512];
                        snprintf(cdev_path, sizeof(cdev_path), "%s/%s", zone_path, zent->d_name);
                        umount2(cdev_path, MNT_DETACH);
                    } else if (strncmp(zent->d_name, "cdev", 4) == 0 && strstr(zent->d_name, "_weight") != NULL) {
                        char weight_path[512];
                        snprintf(weight_path, sizeof(weight_path), "%s/%s", zone_path, zent->d_name);
                        umount2(weight_path, MNT_DETACH);
                    }
                }
                closedir(zdir);
            }
        } else if (strncmp(ent->d_name, "cooling_device", 14) == 0) {
            char cdev_dir[512];
            snprintf(cdev_dir, sizeof(cdev_dir), "%s/%s", THERMAL_DIR, ent->d_name);
            
            char cur_state_path[512];
            snprintf(cur_state_path, sizeof(cur_state_path), "%s/cur_state", cdev_dir);
            
            umount2(cur_state_path, MNT_DETACH);
        }
        
        // Sleep for 5ms to prevent flooding system_server with mount events (prevents UI freeze)
        usleep(5000);
    }
    closedir(dir);
    
    unlink(FAKE_TEMP_FILE);
    unlink(FAKE_TRIP_FILE);
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