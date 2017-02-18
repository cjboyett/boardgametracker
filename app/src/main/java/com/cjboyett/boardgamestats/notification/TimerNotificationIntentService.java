package com.cjboyett.boardgamestats.notification;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.cjboyett.boardgamestats.utility.Preferences;

/**
 * Created by Casey on 10/20/2016.
 */
public class TimerNotificationIntentService extends IntentService {
	private static final int NOTIFICATION_ID = 0;
	private static final String ACTION_PAUSE_TIMER = "com.cjboyett.boardgamestats.ACTION_PAUSE_TIMER";

	public TimerNotificationIntentService() {
		super(TimerNotificationIntentService.class.getSimpleName());
	}

/*
	public static Intent createIntentPauseTimerNotification(Context context)
	{
		Intent intent = new Intent(context, TimerNotificationIntentService.class);
		intent.setAction(ACTION_PAUSE_TIMER);
		return intent;
	}
*/

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(getClass().getSimpleName(), "onHandleIntent, started handling a notification event");
		try {
			String action = intent.getAction();
			Log.d("ACTION", action);
			if (ACTION_PAUSE_TIMER.equals(action))
				processPauseTimerNotification(getApplicationContext());
		} finally {
			WakefulBroadcastReceiver.completeWakefulIntent(intent);
		}
	}

	public static void processPauseTimerNotification(Context context) {
		String game = Preferences.getTimerGame(context);
		long timerStart = Preferences.getTimerStart(context);
		boolean timerRunning = Preferences.isTimerRunning(context);

		Log.d("SERVICE", "Pausing " + game + " " + timerStart + " " + timerRunning);

		TimerNotificationBuilder builder = new TimerNotificationBuilder();
		builder.toggleTimerNotification(context,
										builder.createTimerNotification(context, game, timerRunning, timerStart));
	}
}
