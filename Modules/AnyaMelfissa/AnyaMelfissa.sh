#!/system/bin/sh
#
# Telegram: @RiProG | Channel: @RiOpSo | Group: @RiOpSoDisc
# This is a shell script conversion of the original C code.
# Do not use or modify this code without permission.
#

# RiProG Thermal 2.6.1 (RTN 2.6.1 Low + UnSensor) - Converted to shell by Kanagawa Yamada
# Loop 1: Stop services using 'setprop ctl.stop' (runs twice)
for i in 1 2; do
    get_services | while read -r prop; do
        # Ensure the property name is not empty
        if [ -n "$prop" ]; then
            status=$(getprop "$prop")
            # Check if the service is currently 'running' or 'restarting'
            if [ "$status" = "running" ] || [ "$status" = "restarting" ]; then
                # Extract the service name from the property (e.g., 'init.svc.thermal-engine' -> 'thermal-engine')
                service=${prop:9}
                # Stop the service using the setprop method
                setprop ctl.stop "$service"
            fi
        fi
    done
    # Wait for 5 seconds before the next attempt
    sleep 5
done

# Loop 2: Stop services using the 'stop' command (runs twice)
for i in 1 2; do
    get_services | while read -r prop; do
        if [ -n "$prop" ]; then
            status=$(getprop "$prop")
            if [ "$status" = "running" ] || [ "$status" = "restarting" ]; then
                service=${prop:9}
                # Stop the service using the direct 'stop' command
                stop "$service"
                # This getprop call was in the C code, preserved for functional equivalence
                getprop "$prop"
            fi
        fi
    done
    sleep 5
done

# --- Final Actions ---
# Wait for 10 seconds to allow services to fully stop.
sleep 10

# Find all files in the virtual thermal driver directory and remove all their permissions (r, w, x).
# This effectively prevents the system or any app from reading temperature sensors.
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