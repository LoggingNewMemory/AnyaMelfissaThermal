#!/system/bin/sh

# Function to find all relevant thermal and logging properties
get_properties() {
    getprop | grep 'thermal' | cut -d '[' -f2 | cut -d ']' -f1 | grep -v 'hal'
}

get_properties | while read -r prop; do
    if [[ -n "$prop" && "$prop" == init.svc.* ]]; then
        service=${prop:9}
        setprop ctl.start "$service"
    fi
done

get_properties | while read -r prop; do
    if [[ -n "$prop" && "$prop" == init.svc.* ]]; then
        service=${prop:9}
        start "$service"
    fi
done

find /sys/devices/virtual/thermal/thermal_zone*/mode -type f -exec sh -c 'chmod 644 "$1" && echo enabled > "$1"' _ {} \;