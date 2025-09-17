while [ -z "$(getprop sys.boot_completed)" ]; do
sleep 10
done

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

find /sys/devices/virtual/thermal -type f -exec chmod 444 {} +

sh /data/adb/modules/AnyaMelfissa/AnyaMelfissa/AnyaMelfissa.sh