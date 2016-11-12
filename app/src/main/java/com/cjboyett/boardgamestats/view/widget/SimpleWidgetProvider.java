package com.cjboyett.boardgamestats.view.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.cjboyett.boardgamestats.R;

/**
 * Created by Casey on 4/19/2016.
 */
public class SimpleWidgetProvider extends AppWidgetProvider
{
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		final int count = appWidgetIds.length;

		for (int i = 0; i < count; i++)
		{
			int widgetId = appWidgetIds[i];

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_simple);

			Intent intent = new Intent(context, WidgetAddGamePlayActivity.class);
			intent.putExtra("WIDGET", true);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

			remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}
}
