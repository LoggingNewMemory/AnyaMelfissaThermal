package com.kanagawa.yamada.anyathermal

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnyaToggleWidgetProvider : AppWidgetProvider() {
    
    companion object {
        const val ACTION_TOGGLE_THERMAL = "com.kanagawa.yamada.anyathermal.ACTION_TOGGLE_THERMAL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_TOGGLE_THERMAL) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                val isCurrentlyDisabled = CheckRoot.isThermalDisabled()
                val arg = if (isCurrentlyDisabled) "0" else "1"
                val newState = arg == "1"
                
                val scriptCommand = if (arg == "1") "su -c '/data/adb/modules/AnyaMelfissa/AnyaMelfissa 1'" else "su -c '/data/adb/modules/AnyaMelfissa/AnyaMelfissa 0'"
                Shell.cmd(scriptCommand).exec()
                
                // Update the state globally so MainActivity and Tiles also update
                CheckRoot.thermalStateFlow.tryEmit(newState)
                
                // Refresh all toggle widgets
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, AnyaToggleWidgetProvider::class.java))
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, newState)
                }
                
                pendingResult.finish()
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val isCurrentlyDisabled = CheckRoot.isThermalDisabled()
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, isCurrentlyDisabled)
            }
            pendingResult.finish()
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        isDisabled: Boolean
    ) {
        val views = RemoteViews(context.packageName, R.layout.anya_widget_toggle)
        
        // Setup intent to open app on profile click
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget2_profile, appPendingIntent)
        
        // Setup toggle intent for the button
        val intent = Intent(context, AnyaToggleWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_THERMAL
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget2_button, pendingIntent)
        
        if (isDisabled) {
            views.setTextViewText(R.id.widget2_button, "ON")
            views.setTextColor(R.id.widget2_button, android.graphics.Color.parseColor("#FFFFFF"))
            views.setInt(R.id.widget2_button, "setBackgroundResource", R.drawable.bg_btn_on)
        } else {
            views.setTextViewText(R.id.widget2_button, "OFF")
            views.setTextColor(R.id.widget2_button, android.graphics.Color.parseColor("#6D4C41"))
            views.setInt(R.id.widget2_button, "setBackgroundResource", R.drawable.bg_btn_off)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
