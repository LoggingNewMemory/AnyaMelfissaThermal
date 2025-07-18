while [ -z "$(getprop sys.boot_completed)" ]; do
sleep 10
sh /data/adb/modules/AnyaMelfissa/AnyaMelfissa/AnyaMelfissa.sh
done

