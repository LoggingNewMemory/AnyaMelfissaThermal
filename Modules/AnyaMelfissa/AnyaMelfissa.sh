#!/system/bin/sh
#
# Telegram: @RiProG | Channel: @RiOpSo | Group: @RiOpSoDisc
# This is a shell script conversion of the original C code.
# Do not use or modify this code without permission.
#

# RiProG Thermal 2.6.1 (RTN 2.6.1 Low + UnSensor) - Converted to shell by Kanagawa Yamada

# Define the list of properties to check
get_properties() {
    getprop | grep -E 'logd|thermal' | cut -d '[' -f2 | cut -d ']' -f1 | grep -v 'hal'
}

# --- First Attempt to Stop Services (using setprop) ---
# This loop runs twice, attempting to stop services using the 'setprop ctl.stop' method.
for i in 1 2; do
    get_properties | while read -r prop; do
        # Ensure the property name is not empty
        if [ -n "$prop" ]; then
            status=$(getprop "$prop")
            # Check if the service is running or restarting
            if [ "$status" = "running" ] || [ "$status" = "restarting" ]; then
                # Extract service name (e.g., 'thermal-engine' from 'init.svc.thermal-engine')
                service=${prop:9}
                setprop ctl.stop "$service"
            fi
        fi
    done
    sleep 5
done

# --- Second Attempt to Stop Services (using stop) ---
# This loop also runs twice, using the 'stop' command as an alternative method.
for i in 1 2; do
    get_properties | while read -r prop; do
        if [ -n "$prop" ]; then
            status=$(getprop "$prop")
            if [ "$status" = "running" ] || [ "$status" = "restarting" ]; then
                service=${prop:9}
                stop "$service"
                # This getprop was in the original C code, likely to re-check status
                getprop "$prop"
            fi
        fi
    done
    sleep 5
done

# Wait for services to fully stop
sleep 10

# --- Final Step: Lock Thermal Files ---
# Find all files (not directories) in the virtual thermal path
# and remove all read/write/execute permissions for all users.
find /sys/devices/virtual/thermal -type f -exec chmod 000 {} +

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