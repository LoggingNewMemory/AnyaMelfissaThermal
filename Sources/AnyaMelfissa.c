/*
Anya Melfissa Thermal - Anya Thermal Kernel Level Like Logic
Copyright (C) 2026 Kanagawa Yamada
*/

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/stat.h>

#define THERMAL_DIR "/sys/class/thermal"
#define BACKUP_DIR "/data/local/tmp/anya_thermal_backup"
#define MAX_PATH 512
#define MAX_VAL 128

// Spoofed values
#define SPOOF_TRIP "200000"

static void write_sysfs(const char *path, const char *value) {
    int fd = open(path, O_WRONLY | O_TRUNC);
    if (fd >= 0) {
        write(fd, value, strlen(value));
        close(fd);
    }
}

static int read_sysfs(const char *path, char *buf, size_t len) {
    int fd = open(path, O_RDONLY);
    if (fd < 0) return -1;
    ssize_t n = read(fd, buf, len - 1);
    close(fd);
    if (n <= 0) return -1;
    // Strip trailing newline
    if (buf[n - 1] == '\n') n--;
    buf[n] = '\0';
    return 0;
}

static void backup_file(const char *zone, const char *filename) {
    char src[MAX_PATH], dst[MAX_PATH], val[MAX_VAL];
    snprintf(src, sizeof(src), "%s/%s/%s", THERMAL_DIR, zone, filename);
    if (read_sysfs(src, val, sizeof(val)) != 0) return;

    char backup_zone_dir[MAX_PATH];
    snprintf(backup_zone_dir, sizeof(backup_zone_dir), "%s/%s", BACKUP_DIR, zone);
    mkdir(backup_zone_dir, 0755);

    snprintf(dst, sizeof(dst), "%s/%s/%s", BACKUP_DIR, zone, filename);
    int fd = open(dst, O_CREAT | O_WRONLY | O_TRUNC, 0644);
    if (fd >= 0) {
        write(fd, val, strlen(val));
        close(fd);
    }
}

static void restore_file(const char *zone, const char *filename) {
    char backup_path[MAX_PATH], sysfs_path[MAX_PATH], val[MAX_VAL];
    snprintf(backup_path, sizeof(backup_path), "%s/%s/%s", BACKUP_DIR, zone, filename);
    if (read_sysfs(backup_path, val, sizeof(val)) != 0) return;

    snprintf(sysfs_path, sizeof(sysfs_path), "%s/%s/%s", THERMAL_DIR, zone, filename);
    write_sysfs(sysfs_path, val);
}

void spoof_temperatures() {
    mkdir(BACKUP_DIR, 0755);

    DIR *dir = opendir(THERMAL_DIR);
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strncmp(ent->d_name, "thermal_zone", 12) != 0)
            continue;

        char path[MAX_PATH];

        // Backup and disable the thermal zone mode
        backup_file(ent->d_name, "mode");
        snprintf(path, sizeof(path), "%s/%s/mode", THERMAL_DIR, ent->d_name);
        write_sysfs(path, "disabled");

        // Backup and override trip points to very high values
        for (int i = 0; i < 20; i++) {
            char trip_name[64];
            snprintf(trip_name, sizeof(trip_name), "trip_point_%d_temp", i);
            snprintf(path, sizeof(path), "%s/%s/%s", THERMAL_DIR, ent->d_name, trip_name);
            if (access(path, F_OK) != 0) break;

            backup_file(ent->d_name, trip_name);
            write_sysfs(path, SPOOF_TRIP);
        }

    }
    closedir(dir);
}

static void remove_dir_recursive(const char *path) {
    DIR *dir = opendir(path);
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strcmp(ent->d_name, ".") == 0 || strcmp(ent->d_name, "..") == 0)
            continue;

        char child[MAX_PATH];
        snprintf(child, sizeof(child), "%s/%s", path, ent->d_name);

        struct stat st;
        if (stat(child, &st) != 0) continue;

        if (S_ISDIR(st.st_mode)) {
            remove_dir_recursive(child);
        } else {
            unlink(child);
        }
    }
    closedir(dir);
    rmdir(path);
}

void restore_temperatures() {
    DIR *dir = opendir(THERMAL_DIR);
    if (!dir) return;

    struct dirent *ent;
    while ((ent = readdir(dir)) != NULL) {
        if (strncmp(ent->d_name, "thermal_zone", 12) != 0)
            continue;

        char path[MAX_PATH];

        // Restore trip points first (before re-enabling the zone)
        for (int i = 0; i < 20; i++) {
            char trip_name[64];
            snprintf(trip_name, sizeof(trip_name), "trip_point_%d_temp", i);
            snprintf(path, sizeof(path), "%s/%s/%s", THERMAL_DIR, ent->d_name, trip_name);
            if (access(path, F_OK) != 0) break;

            restore_file(ent->d_name, trip_name);
        }

        // Restore thermal zone mode last
        restore_file(ent->d_name, "mode");
    }
    closedir(dir);

    // Clean up backup directory
    remove_dir_recursive(BACKUP_DIR);
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