package com.example.lucimanager.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.lucimanager.R
import com.example.lucimanager.model.LoginCredentials
import com.example.lucimanager.network.LuciApiClient
import com.example.lucimanager.repository.NetworkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RouterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_router_status)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = NetworkRepository(context)
                val credentials = repository.getSavedCredentials()

                if (credentials != null) {
                    // Login if needed
                    if (LuciApiClient.getCurrentSession() == null ||
                        LuciApiClient.getCurrentSession()?.isExpired() == true) {
                        LuciApiClient.login(credentials)
                    }

                    // Get router info
                    val infoResult = LuciApiClient.getRouterInfo()
                    infoResult.getOrNull()?.let { info ->
                        val days = info.uptime / 86400
                        val hours = (info.uptime % 86400) / 3600
                        val uptimeText = "$days${context.getString(R.string.dashboard_days)} " +
                                "$hours${context.getString(R.string.dashboard_hours)}"
                        views.setTextViewText(R.id.widget_uptime, uptimeText)
                        views.setTextViewText(R.id.widget_memory, "${info.memUsagePercent}%")
                    }

                    // Get connected devices count
                    val devicesResult = LuciApiClient.getConnectedDevices()
                    devicesResult.getOrNull()?.let { devices ->
                        views.setTextViewText(R.id.widget_clients, "${devices.size}")
                    }
                } else {
                    views.setTextViewText(R.id.widget_uptime, context.getString(R.string.widget_no_data))
                    views.setTextViewText(R.id.widget_memory, context.getString(R.string.widget_no_data))
                    views.setTextViewText(R.id.widget_clients, context.getString(R.string.widget_no_data))
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_uptime, context.getString(R.string.widget_no_data))
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
