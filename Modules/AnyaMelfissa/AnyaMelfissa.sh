#!/system/bin/sh
#
# Telegram: @RiProG | Channel: @RiOpSo | Group: @RiOpSoDisc
# This is a shell script conversion of the original C code.
# Do not use or modify this code without permission.
#

# RiProG Thermal 2.2 (RTN 2.2 Normal + GamingFix R2) - Converted to shell by Kanagawa Yamada

# --- Step 1: Stop Running Thermal Services ---
# Find all thermal-related services (except 'hal' services),
# check if they are running, and stop them.
getprop | grep 'thermal' | cut -d '[' -f2 | cut -d ']' -f1 | grep -v 'hal' | while read -r prop; do
    # Get the current status of the service property (e.g., "running").
    status=$(getprop "$prop")

    # Check if the service is running or restarting.
    if [ "$status" = "running" ] || [ "$status" = "restarting" ]; then
        # Extract the service name from the property (e.g., 'init.svc.thermal-engine' -> 'thermal-engine').
        service_name=$(echo "$prop" | sed 's/^init\.svc\.//')
        # Stop the service.
        stop "$service_name"
    fi
done

# --- Step 2: Suspend Thermal Processes ---
# Find the Process IDs (PIDs) of all thermal-related processes (except for a specific Mediatek service)
# and send a SIGSTOP signal to suspend them.
ps -e -o pid,comm | grep 'thermal' | grep -v 'android.hardware.thermal-service.mediatek' | awk '{print $1}' | while read -r pid; do
    # Suspend the process using its PID.
    kill -STOP "$pid"
done

# Wait for 5 seconds to ensure processes are suspended.
sleep 5

# --- Step 3: Mark Thermal Services as Paused ---
# Find all thermal-related services again.
# If they are still listed as running, change their property status to 'paused'.
getprop | grep 'thermal' | cut -d '[' -f2 | cut -d ']' -f1 | while read -r prop; do
    # Get the current status of the service property.
    status=$(getprop "$prop")

    # Check if the service is running or restarting.
    if [ "$status" = "running" ] || [ "$status" = "restarting" ]; then
        # Set the property to 'paused' to prevent it from restarting.
        setprop "$prop" paused
    fi
done

# --- Step 4: Ensure Vendor Thermal HAL is Running ---
# The primary hardware abstraction layer for thermal management should be running.
# This sets its state to 'running', starting it if it was stopped.
setprop init.svc.vendor.thermal-hal running

# Original Stripped Anya Thermal

tweak() {
	if [ -f "$2" ]; then
		chmod 644 "$2" >/dev/null 2>&1
		echo "$1" >"$2" 2>/dev/null
		chmod 444 "$2" >/dev/null 2>&1
	fi
}

for trip in /sys/class/thermal/thermal_zone*/trip_point*; do
    tweak 999999999 "$trip"
done

su -lp 2000 -c "cmd notification post -S bigtext -t 'Anya Melfissa' -i file:///data/local/tmp/Anya.png -I file:///data/local/tmp/Anya.png TagAnya 'Good Day! Thermal Is Dead BTW'"

# Wait for another 5 seconds.
sleep 5

# Script finished. Exit successfully.
exit 0