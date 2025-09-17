#!/system/bin/sh
#
# This script allows enabling or disabling thermal management
# using the volume keys by executing specific scripts.

# A function to print messages to the UI.
ui_print() {
  echo "$1"
}

ui_print " "
ui_print "Anya Control Panel"
ui_print " "
ui_print "Please use the volume keys to make your selection."
ui_print " "
ui_print "Volume UP   = Disable Thermal"
ui_print "Volume DOWN = Enable Thermal"
ui_print " "

while true; do
  EVENT=$(getevent -lqc 1)

  if echo "$EVENT" | grep -q "KEY_VOLUMEUP.*DOWN"; then
    /data/adb/modules/AnyaMelfissa/AnyaMelfissa/AnyaMelfissa.sh
    ui_print "Anya Melfissa!"
    break 

  elif echo "$EVENT" | grep -q "KEY_VOLUMEDOWN.*DOWN"; then
    /data/adb/modules/AnyaMelfissa/AnyaMelfissa/AnyaKawaii.sh
    ui_print "Anya Kawaii!"
    break 
  fi
done

ui_print " "
ui_print "I love Anya Melfissa"
ui_print "Who doesn't want a living weapon as a partner?"
ui_print " "

exit 0