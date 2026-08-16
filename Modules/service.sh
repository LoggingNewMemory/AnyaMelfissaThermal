while [ -z "$(getprop sys.boot_completed)" ]; do
sleep 10
done

START_ON_BOOT=$(grep "^START_ON_BOOT=" /data/adb/modules/AnyaMelfissa/AnyaConfig.txt | cut -d= -f2)
if [ "$START_ON_BOOT" = "1" ]; then
  su -c /data/adb/modules/AnyaMelfissa/AnyaMelfissa 1
  su -lp 2000 -c "cmd notification post -S bigtext -t 'Anya Melfissa' -i file:///data/local/tmp/Anya.png -I file:///data/local/tmp/Anya.png TagAnya 'Good Day! Thermal modification is done. Now let me sleep'"
fi