package com.cjboyett.boardgamestats.notification;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.addgameplay.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.activity.main.ClearStackMainActivity;
import com.cjboyett.boardgamestats.model.stats.StatisticsManager;
import com.cjboyett.boardgamestats.utility.Preferences;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Casey on 5/3/2016.
 */
public class NotificationIntentService extends IntentService {
	private static final int NOTIFICATION_ID = 1;
	private static final String ACTION_START_TIMER = "com.cjboyett.boardgamestats.ACTION_START_TIMER";
	private static final String ACTION_DELETE_TIMER = "com.cjboyett.boardgamestats.ACTION_DELETE_TIMER";
	private static final String ACTION_PAUSE_TIMER = "com.cjboyett.boardgamestats.ACTION_PAUSE_TIMER";
	private static final String ACTION_REMINDER = "com.cjboyett.boardgamestats.ACTION_REMINDER";

	public NotificationIntentService() {
		super(NotificationIntentService.class.getSimpleName());
	}

	public static Intent createIntentStartTimerNotificationService(Context context) {
		Intent intent = new Intent(context, NotificationIntentService.class);
		intent.setAction(ACTION_START_TIMER);
		return intent;
	}

	public static Intent createIntentDeleteTimerNotification(Context context) {
		Intent intent = new Intent(context, NotificationIntentService.class);
		intent.setAction(ACTION_DELETE_TIMER);
		return intent;
	}

	public static Intent createIntentPauseTimerNotification(Context context) {
		Intent intent = new Intent(context, NotificationIntentService.class);
		intent.setAction(ACTION_PAUSE_TIMER);
		return intent;
	}

	public static Intent createIntentReminderNotificationService(Context context) {
		Intent intent = new Intent(context, NotificationIntentService.class);
		intent.setAction(ACTION_REMINDER);
		return intent;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(getClass().getSimpleName(), "onHandleIntent, started handling a notification event");
		try {
			String action = intent.getAction();
			Log.d("ACTION", action);
			if (ACTION_START_TIMER.equals(action))
				processStartTimerNotification();
			else if (ACTION_DELETE_TIMER.equals(action))
				processDeleteTimerNotification(intent);
			else if (ACTION_PAUSE_TIMER.equals(action))
				processDeleteTimerNotification(intent);
			else if (ACTION_REMINDER.equals(action))
				processReminderNotification(intent);
		} finally {
			WakefulBroadcastReceiver.completeWakefulIntent(intent);
		}
	}

	private void processReminderNotification(Intent intent) {
		StatisticsManager statisticsManager = StatisticsManager.getInstance(this);
		Map<Integer, List<String>> games = statisticsManager.getAllGamesByAverageTimePlayedWithType();

		String notificationText = "";
		PendingIntent pendingIntent = null;

		if (!games.isEmpty()) {
			int total = 0;
			for (Integer count : games.keySet())
				total += count * games.get(count)
									  .size();

			int choice = new Random().nextInt(total);
			String game = "";

			for (Integer count : games.keySet()) {
				if (count * games.get(count)
								 .size() <= choice)
					choice -= count * games.get(count)
										   .size();
				else {
					game = games.get(count)
								.get(choice / count);
					break;
				}
			}

			notificationText = "Maybe it's time for a game of " + game.substring(0, game.lastIndexOf(":"));
			pendingIntent = PendingIntent.getActivity(this,
													  NOTIFICATION_ID,
													  new Intent(this, ClearStackMainActivity.class),
													  PendingIntent.FLAG_CANCEL_CURRENT);
		} else {
			notificationText = "Would you like to add some games to your collection?";
			pendingIntent = PendingIntent.getActivity(this,
													  NOTIFICATION_ID,
													  new Intent(this, ClearStackMainActivity.class),
													  PendingIntent.FLAG_CANCEL_CURRENT);

		}

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle("Played any games lately?")
			   .setAutoCancel(true)
			   .setColor(getResources().getColor(R.color.colorAccent))
			   .setContentText(notificationText)
			   .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(notificationText))
			   .setSmallIcon(R.mipmap.ic_launcher)
			   .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

		builder.setContentIntent(pendingIntent);
		final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());

		Preferences.setShowReminder(this, false);
	}

	private void processStartTimerNotification() {
		// TODO Add game and timer data

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle("Timer")
			   .setAutoCancel(true)
			   .setColor(getResources().getColor(R.color.colorAccent))
			   .setContentText("Game is running: " + SystemClock.elapsedRealtime())
			   .setSmallIcon(R.mipmap.ic_launcher)
			   .setPriority(Notification.PRIORITY_HIGH)
			   .setUsesChronometer(true);

		PendingIntent pendingIntent = PendingIntent.getActivity(this,
																NOTIFICATION_ID,
																new Intent(this, AddGamePlayTabbedActivity.class),
																PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		builder.setDeleteIntent(NotificationEventReceiver.getDeleteTimerIntent(this));

		final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());
	}

	private void processDeleteTimerNotification(Intent intent) {
		NotificationEventReceiver.deleteTimer(this);
	}

	private void processPauseTimerNotification(Intent intent) {

	}

}
