#!/system/bin/sh
#
# Telegram: @RiProG | Channel: @RiOpSo | Group: @RiOpSoDisc

# RiProG Thermal 2.6.1 (RTN 2.6.1 Low + UnSensor) - Converted to shell by Kanagawa Yamada

get_properties() {
    getprop | grep -E 'logd|thermal' | cut -d '[' -f2 | cut -d ']' -f1 | grep -v 'hal'
}

get_properties | while read -r prop; do
    if [ -n "$prop" ]; then
        status=$(getprop "$prop")
        if [ "$status" = "running" ] || [ "$status" = "restarting" ]; then
            service=${prop:9}
            setprop ctl.stop "$service"
        fi
    fi
done

su -lp 2000 -c "cmd notification post -S bigtext -t 'Anya Melfissa' -i file:///data/local/tmp/Anya.png -I file:///data/local/tmp/Anya.png TagAnya 'Good Day! Thermal Is Dead BTW'"

# Wait for another 5 seconds.
sleep 5

# Script finished. Exit successfully.
exit 0