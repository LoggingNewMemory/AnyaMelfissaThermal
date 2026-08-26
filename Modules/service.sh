while [ -z "$(getprop sys.boot_completed)" ]; do
sleep 10
done

sed -i 's/^ANYA .*/ANYA 0/' /data/adb/modules/AnyaMelfissa/AnyaConfig.txt

START_ON_BOOT=$(grep "^START_ON_BOOT " /data/adb/modules/AnyaMelfissa/AnyaConfig.txt | awk '{print $2}')
if [ "$START_ON_BOOT" = "1" ]; then
  sh /data/adb/modules/AnyaMelfissa/AnyaOn.sh
  su -lp 2000 -c "cmd notification post -S bigtext -t 'Anya Melfissa' -i file:///data/local/tmp/Anya.png -I file:///data/local/tmp/Anya.png TagAnya 'Good Day! Thermal modification is done. Now let me sleep'"
fi