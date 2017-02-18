package com.cjboyett.boardgamestats.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cjboyett.boardgamestats.utility.Preferences;

/**
 * Created by Casey on 10/20/2016.
 */
public class TimerNotificationEventReceiver extends WakefulBroadcastReceiver {
	private static final String ACTION_PAUSE_TIMER = "com.cjboyett.boardgamestats.ACTION_PAUSE_TIMER";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Intent serviceIntent = null;

		String game = Preferences.getTimerGame(context);
		long timerStart = Preferences.getTimerStart(context);
		boolean timerRunning = Preferences.isTimerRunning(context);

		if (action.equals(ACTION_PAUSE_TIMER))
			TimerNotificationIntentService.processPauseTimerNotification(context);
	}

	public static PendingIntent getPauseTimerIntent(Context context) {
		Intent intent = new Intent(context, TimerNotificationEventReceiver.class);
		intent.setAction(ACTION_PAUSE_TIMER);
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
