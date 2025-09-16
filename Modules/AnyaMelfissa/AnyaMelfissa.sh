#!/system/bin/sh
#
# Telegram: @RiProG | Channel: @RiOpSo | Group: @RiOpSoDisc
# This is a shell script conversion of the original C code.
# Do not use or modify this code without permission.
#

# RiProG Thermal 2.6.1 (RTN 2.6.1 Low + UnSensor) - Converted to shell by Kanagawa Yamada

get_properties() {
    getprop | grep -E 'logd|thermal' | cut -d '[' -f2 | cut -d ']' -f1 | grep -v 'hal'
}

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
    sleep 1
done

su -lp 2000 -c "cmd notification post -S bigtext -t 'Anya Melfissa' -i file:///data/local/tmp/Anya.png -I file:///data/local/tmp/Anya.png TagAnya 'Good Day! Thermal Is Dead BTW'"

# Wait for another 5 seconds.
sleep 5

# Script finished. Exit successfully.
exit 0