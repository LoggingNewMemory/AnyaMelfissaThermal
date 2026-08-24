package com.kanagawa.yamada.anyathermal

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class AnyaWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
            pendingResult.finish()
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.anya_widget)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_profile, pendingIntent)
        
        var locationText = "Location Unknown"
        
        // 1. Try GPS / Network Location
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) 
                    ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                
                if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val city = address.locality ?: address.subAdminArea ?: address.adminArea
                        val country = address.countryCode ?: address.countryName
                        if (city != null && country != null) {
                            locationText = "$city, $country"
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore and fallthrough to IP lookup
            }
        }

        // 2. Fallback to IP Geolocation if GPS is null or unknown
        if (locationText == "Location Unknown" || locationText.isBlank()) {
            try {
                val city = URL("https://ipinfo.io/city").readText().trim()
                val country = URL("https://ipinfo.io/country").readText().trim()
                if (city.isNotEmpty() && country.isNotEmpty()) {
                    locationText = "$city, $country"
                }
            } catch (e: Exception) {
                locationText = "Location Unavailable"
            }
        }
        
        views.setTextViewText(R.id.widget_location, locationText)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
