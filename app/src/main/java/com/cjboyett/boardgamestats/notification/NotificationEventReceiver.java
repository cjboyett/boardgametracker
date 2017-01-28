package com.cjboyett.boardgamestats.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by Casey on 5/3/2016.
 */
public class NotificationEventReceiver extends WakefulBroadcastReceiver {
	public static final String ACTION_START_TIMER = "com.cjboyett.boardgamestats.ACTION_START_TIMER";
	public static final String ACTION_DELETE_TIMER = "com.cjboyett.boardgamestats.ACTION_DELETE_TIMER";
	private static final String ACTION_PAUSE_TIMER = "com.cjboyett.boardgamestats.ACTION_PAUSE_TIMER";
	public static final String ACTION_REMINDER = "com.cjboyett.boardgamestats.ACTION_REMINDER";

	private static final int NOTIFICATIONS_REMINDER_INTERVAL_IN_DAYS = 4;
	private static final int NOTIFICATIONS_TIMER_INTERVAL_IN_MILLISECONDS = 60;

	public static void setupTimer(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = getStartTimerPendingIntent(context);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
								  getTriggerAt(new Date()),
								  NOTIFICATIONS_TIMER_INTERVAL_IN_MILLISECONDS * 1000,
								  alarmIntent);
	}

	public static void deleteTimer(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = getStartTimerPendingIntent(context);
		alarmManager.cancel(alarmIntent);
	}

	public static void setupReminder(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = getSetReminderPendingIntent(context);
		alarmManager.cancel(alarmIntent);
		alarmManager.set(AlarmManager.RTC_WAKEUP,
						 getTriggerAt(new Date()) +
								 NOTIFICATIONS_REMINDER_INTERVAL_IN_DAYS * AlarmManager.INTERVAL_DAY +
								 (17 + new Random().nextInt(2) - Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) *
										 AlarmManager.INTERVAL_HOUR,
						 alarmIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Intent serviceIntent = null;

		if (action.equals(ACTION_START_TIMER)) {
			Log.d("INTENT", "Start timer");
			serviceIntent = NotificationIntentService.createIntentStartTimerNotificationService(context);
		} else if (action.equals(ACTION_DELETE_TIMER))
			serviceIntent = NotificationIntentService.createIntentDeleteTimerNotification(context);
		else if (action.equals(ACTION_PAUSE_TIMER))
			serviceIntent = NotificationIntentService.createIntentPauseTimerNotification(context);
		else if (action.equals(ACTION_REMINDER))
			serviceIntent = NotificationIntentService.createIntentReminderNotificationService(context);

		if (serviceIntent != null) {
			startWakefulService(context, serviceIntent);
		}
	}

	private static long getTriggerAt(Date now) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		//calendar.add(Calendar.HOUR, NOTIFICATIONS_INTERVAL_IN_HOURS);
		return calendar.getTimeInMillis();
	}

	private static PendingIntent getStartTimerPendingIntent(Context context) {
		Intent intent = new Intent(context, NotificationEventReceiver.class);
		intent.setAction(ACTION_START_TIMER);
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static PendingIntent getDeleteTimerIntent(Context context) {
		Intent intent = new Intent(context, NotificationEventReceiver.class);
		intent.setAction(ACTION_DELETE_TIMER);
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static PendingIntent getPauseTimerIntent(Context context) {
		Intent intent = new Intent(context, NotificationEventReceiver.class);
		intent.setAction(ACTION_PAUSE_TIMER);
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private static PendingIntent getSetReminderPendingIntent(Context context) {
		Intent intent = new Intent(context, NotificationEventReceiver.class);
		intent.setAction(ACTION_REMINDER);
		return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
