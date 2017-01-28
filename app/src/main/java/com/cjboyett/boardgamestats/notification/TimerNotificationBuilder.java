package com.cjboyett.boardgamestats.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ImageController;

import java.util.List;

/**
 * Created by Casey on 10/20/2016.
 */
public class TimerNotificationBuilder {
	private static final int NOTIFICATION_ID = 0;

	private RemoteViews timeView;
	private int smallIcon = R.drawable.meeple_play;

	public NotificationCompat.Builder createTimerNotification(Context context, String game, boolean timerRunning,
															  long timerStart) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		Preferences.setTimerGame(context, game);
		Preferences.setTimerStart(context, timerStart);
		Preferences.setTimerRunning(context, timerRunning);

		timeView = new RemoteViews(context.getPackageName(), R.layout.notification_timer);

		timeView.setTextColor(R.id.textview_title, Color.DKGRAY);
		timeView.setTextColor(R.id.textview_game, Color.GRAY);
		timeView.setTextViewText(R.id.textview_game, TextUtils.isEmpty(game) ? "Game in progress" : game);

		timeView.setChronometer(R.id.chronometer, timerStart, null, true);
		timeView.setTextColor(R.id.chronometer, Color.DKGRAY);

		String thumbnailUrl = null;

		GamesDbHelper dbHelper = new GamesDbHelper(context);
		thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, game);
		if (TextUtils.isEmpty(thumbnailUrl)) thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, game);
		if (TextUtils.isEmpty(thumbnailUrl)) thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, game);
		dbHelper.close();

		if (!TextUtils.isEmpty(thumbnailUrl)) {
			ImageController imageController = new ImageController(context).setDirectoryName("thumbnails");
			timeView.setImageViewBitmap(R.id.imageview_thumbnail,
										imageController.setFileName(thumbnailUrl.substring(
												thumbnailUrl.lastIndexOf("/") + 1))
													   .load());
		} else
			timeView.setImageViewBitmap(R.id.imageview_thumbnail,
										BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

		builder.setContentTitle("Timer")
			   .setAutoCancel(true)
			   .setColor(context.getResources().getColor(R.color.colorAccent))
			   .setContentText(TextUtils.isEmpty(game) ? "Game in progress" : game)
			   .setSmallIcon(smallIcon)
			   .setPriority(Notification.PRIORITY_MAX)
			   .setOngoing(true)
			   .setContent(timeView);

		PendingIntent pauseIntent = PendingIntent.getService(context,
															 NOTIFICATION_ID,
															 new Intent("com.cjboyett.boardgamestats.ACTION_PAUSE_TIMER"),
															 PendingIntent.FLAG_UPDATE_CURRENT);

		timeView.setOnClickPendingIntent(R.id.imageview_pause, pauseIntent);

		PendingIntent pendingIntent = PendingIntent.getActivity(context,
																NOTIFICATION_ID,
																new Intent(context, AddGamePlayTabbedActivity.class),
																PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentIntent(pendingIntent);

		return builder;
	}

	public void createTimerNotification(Context context, NotificationCompat.Builder builder) {
		final NotificationManager manager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, builder.build());
	}

	public void toggleTimerNotification(Context context, NotificationCompat.Builder builder) {
		TempDataManager tempDataManager = TempDataManager.getInstance(context);

		List<Long> timer = tempDataManager.getTimer();

		long timerBase = timer.get(0), lastStartTime = timer.get(1), lastStopTime = timer.get(2), diff = timer.get(3);

		boolean timerRunning = Preferences.isTimerRunning(context);

		if (timerRunning) {
			lastStopTime = SystemClock.elapsedRealtime();
			tempDataManager.setTimer(timerBase, lastStartTime, lastStopTime, diff);
			timeView.setImageViewBitmap(R.id.imageview_pause,
										BitmapFactory.decodeResource(context.getResources(),
																	 android.R.drawable.ic_media_play));
			smallIcon = R.drawable.meeple_pause;
		} else {
			diff += (lastStopTime - lastStartTime);
			timerBase = SystemClock.elapsedRealtime() - diff;
			lastStartTime = SystemClock.elapsedRealtime();
			tempDataManager.setTimer(timerBase, lastStartTime, lastStopTime, diff);
			timeView.setImageViewBitmap(R.id.imageview_pause,
										BitmapFactory.decodeResource(context.getResources(),
																	 android.R.drawable.ic_media_pause));
			smallIcon = R.drawable.meeple_play;
		}

		timerRunning = !timerRunning;
		timeView.setChronometer(R.id.chronometer, timerBase, null, timerRunning);

		Preferences.setTimerRunning(context, timerRunning);
		tempDataManager.saveTimer();

		final NotificationManager manager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		builder.setSmallIcon(smallIcon);
		manager.notify(NOTIFICATION_ID, builder.build());
	}
}
