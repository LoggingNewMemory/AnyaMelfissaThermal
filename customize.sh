LATESTARTSERVICE=true

ui_print "🗡--------------------------------🗡"
ui_print "    ANYA MELFISSA DISABLE THERMAL   "
ui_print "🗡--------------------------------🗡"
ui_print "         By: Kanagawa Yamada        "
ui_print "------------------------------------"
ui_print "      READ THE TELEGRAM MESSAGE     "
ui_print "------------------------------------"
ui_print " "
sleep 1.5

ui_print "-----------------📱-----------------"
ui_print "            DEVICE INFO             "
ui_print "-----------------📱-----------------"
ui_print "DEVICE : $(getprop ro.build.product) "
ui_print "MODEL : $(getprop ro.product.model) "
ui_print "MANUFACTURE : $(getprop ro.product.system.manufacturer) "
ui_print "PROC : $(getprop ro.product.board) "
ui_print "CPU : $(getprop ro.hardware) "
ui_print "ANDROID VER : $(getprop ro.build.version.release) "
ui_print "KERNEL : $(uname -r) "
ui_print "RAM : $(free | grep Mem |  awk '{print $2}') "
ui_print " "
sleep 1.5

ui_print "-----------------🗡-----------------"
ui_print "            MODULE INFO             "
ui_print "-----------------🗡-----------------"
ui_print "Name : Anya Melfissa Disable Thermal"
ui_print "Version : Keris 1.0"
ui_print "Support Root : Magisk / KernelS / APatch"
ui_print " "
sleep 1.5

ui_print "🗡--------------------------------🗡"
ui_print "         INSTALLING MODULES         "
ui_print "🗡--------------------------------🗡"
ui_print " "
sleep 1.5

unzip -o "$ZIPFILE" 'AnyaMelfissa/*' -d $MODPATH >&2
set_perm_recursive $MODPATH/AnyaMelfissa 0 0 0755 0644

ui_print "🗡--------------------------------🗡"
ui_print "        INSTALLING MODULES OK       "
ui_print "🗡--------------------------------🗡"
sleep 1.5

am start -a android.intent.action.VIEW -d https://t.me/KanagawaLabAnnouncement/289 >/dev/null 2>&1
