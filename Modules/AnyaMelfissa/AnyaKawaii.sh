#!/system/bin/sh
#
# AnyaKawaii - Restore/Enable Thermal
# Reverses the actions of AnyaMelfissa to re-enable thermal throttling and management.

# Helper function to write values safely
write_val() {
    local file="$1"
    local value="$2"
    if [ -e "$file" ]; then
        chmod 644 "$file" 2>/dev/null
        echo "$value" > "$file"
    fi
}

# 1. Unmount blocked binaries (CRITICAL)
# AnyaMelfissa binds /dev/null to these to prevent them from running. 
# We must unmount them so the real binaries are visible again.
umount /vendor/bin/hw/thermal-hal-2-0 2>/dev/null
umount /vendor/bin/thermald 2>/dev/null

# 2. Restore Permissions (Reverse chmod 000)
# We restore standard read/write permissions so the system can read temps again.
restore_perms() {
    find "$1" -name '*temp*' -o -name '*trip_point_*' -o -name '*type*' -o -name '*thermal*' | while read -r file; do
        if [ -d "$file" ]; then
            chmod 755 "$file"
        else
            chmod 644 "$file"
        fi
    done
}

# Restore permissions in key thermal directories
restore_perms "/sys/devices/virtual/thermal/thermal_zone*/"
restore_perms "/sys/firmware/devicetree/base/soc/*/"
chmod -R 755 /sys/devices/virtual/hwmon/hwmon* 2>/dev/null
chmod -R 644 /sys/devices/virtual/hwmon/hwmon*/* 2>/dev/null

# 3. Re-enable Thermal Modes
# Set all thermal zones back to "enabled"
for thermmode in /sys/devices/virtual/thermal/thermal_zone*/mode; do
    write_val "$thermmode" "enabled"
done

# 4. Re-enable Kernel & CPU Thermal Controls
# Re-enable core_ctl (Hotplugging)
for cpu in /sys/devices/system/cpu/cpu[0,4,7]/core_ctl; do
    write_val "$cpu/enable" "1"
done

# Re-enable MSM Thermal if present
find /sys/ -name enabled | grep 'msm_thermal' | while read -r msm_thermal_status; do
    write_val "$msm_thermal_status" "Y"
    write_val "$msm_thermal_status" "1"
done

# Re-enable GPU Throttling/Governance
for kgsl in /sys/class/kgsl/kgsl-3d0; do
    if [ -d "$kgsl" ]; then
        write_val "$kgsl/throttling" "1"
        write_val "$kgsl/thermal_pwrlevel" "1" 
    fi
done

# 5. Restore Android Thermal Service
# Reverse "cmd thermalservice override-status 0"
cmd thermalservice override-status 1 2>/dev/null
cmd thermalservice reset 2>/dev/null

# 6. Restart Thermal Services (The Real Fix)
# We find all thermal services, reset their status from "stopped" to allow them to boot,
# and then trigger a real start.

# Function to get service names from properties
get_thermal_services() {
    getprop | grep -E 'init.svc(\.vendor)?\.thermal' | cut -d: -f1 | sed 's/init.svc.//g' | tr -d '[]'
}

for svc in $(get_thermal_services); do
    # 1. Reset the "running/stopped" property so init thinks it's fresh
    resetprop -n "init.svc.$svc" "stopped"
    
    # 2. Actually start the service using the control property
    start "$svc"
    
    # Optional: If 'start' command isn't in path, use setprop
    setprop ctl.start "$svc"
done