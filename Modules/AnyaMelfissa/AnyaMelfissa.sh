#!/system/bin/sh
#
# Telegram: @RiProG | Channel: @RiOpSo | Group: @RiOpSoDisc

# RiProG Thermal 2.6.1 (RTN 2.6.1 Low + UnSensor) - Converted to shell by Kanagawa Yamada

get_properties() {
    getprop | grep 'thermal' | cut -d '[' -f2 | cut -d ']' -f1 | grep -v 'hal'
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

get_properties | while read -r prop; do
    if [ -n "$prop" ]; then
        status=$(getprop "$prop")
        if [ "$status" = "running" ] || [ "$status" = "restarting" ]; then
            service=${prop:9}
            stop "$service"
        fi
    fi
done

find /sys/devices/virtual/thermal/thermal_zone*/mode -type f -exec sh -c 'echo disabled > "$1" && chmod 444 "$1"' _ {} \;