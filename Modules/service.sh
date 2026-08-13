while [ -z "$(getprop sys.boot_completed)" ]; do
sleep 10
done

/data/adb/modules/AnyaMelfissa/Compiled/AnyaMelfissa 1
su -lp 2000 -c "cmd notification post -S bigtext -t 'Anya Melfissa' -i file:///data/local/tmp/Anya.png -I file:///data/local/tmp/Anya.png TagAnya 'Good Day! Thermal Is Dead BTW'"