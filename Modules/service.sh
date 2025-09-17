while [ -z "$(getprop sys.boot_completed)" ]; do
sleep 10
done

sh /data/adb/modules/AnyaMelfissa/AnyaMelfissa/AnyaMelfissa.sh

find /sys/devices/virtual/thermal/thermal_zone*/mode -type f -exec sh -c 'echo disabled > "$1" && chmod 444 "$1"' _ {} \;

su -lp 2000 -c "cmd notification post -S bigtext -t 'Anya Melfissa' -i file:///data/local/tmp/Anya.png -I file:///data/local/tmp/Anya.png TagAnya 'Good Day! Thermal Is Dead BTW'"