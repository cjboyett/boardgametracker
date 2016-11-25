package com.cjboyett.boardgamestats;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.model.stats.StatisticsManager;
import com.cjboyett.boardgamestats.notification.NotificationEventReceiver;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Casey on 4/14/2016.
 */
public class MyApp extends Application
{
	/**
	 * The Analytics singleton. The field is set in onCreate method override when the application
	 * class is initially created.
	 */
	private static GoogleAnalytics analytics;

	/**
	 * The default app tracker. The field is from onCreate callback when the application is
	 * initially created.
	 */
	private static Tracker tracker;

	/**
	 * Access to the global Analytics singleton. If this method returns null you forgot to either
	 * set android:name="&lt;this.class.name&gt;" attribute on your application element in
	 * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
	 */
	public static GoogleAnalytics analytics() {
		return analytics;
	}

	/**
	 * The default app tracker. If this method returns null you forgot to either set
	 * android:name="&lt;this.class.name&gt;" attribute on your application element in
	 * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
	 */
	public static Tracker tracker() {
		return tracker;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (!Preferences.isSuperUser(this))
		{
			Log.d("SUPER USER", "You are not.");
			analytics = GoogleAnalytics.getInstance(this);

			// Replaced the tracker-id with my app from https://www.google.com/analytics/web/
			tracker = analytics.newTracker("UA-76449004-2");

			// Provide unhandled exceptions reports. Do that first after creating the tracker
			tracker.enableExceptionReporting(true);

			// Enable Remarketing, Demographics & Interests reports
			// https://developers.google.com/analytics/devguides/collection/android/display-features
			tracker.enableAdvertisingIdCollection(true);

			// Enable automatic activity tracking for your app
			tracker.enableAutoActivityTracking(true);
		}

		// Initialize data managers for quickly navigation
		final DataManager dataManager = DataManager.getInstance(this);
		new AsyncTask<String, Void, Void>()
		{
			@Override
			protected Void doInBackground(String... params)
			{
				dataManager.initialize();
				return null;
			}
		}.execute("");

		final StatisticsManager statisticsManager = StatisticsManager.getInstance(this);
		new AsyncTask<String, Void, Void>()
		{
			@Override
			protected Void doInBackground(String... params)
			{
				statisticsManager.initialize();
				return null;
			}
		}.execute("");

		final TempDataManager tempDataManager = TempDataManager.getInstance(this);

		// Register reminder to play in 5 days.
		if (Preferences.showNotifications(this) && Preferences.showReminder(this))
			NotificationEventReceiver.setupReminder(this);

		Preferences.setShowReminder(this, true);

		// Init Facebook SDK
		FacebookSdk.sdkInitialize(this);
	}

	public boolean isConnectedToInternet()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnected();
	}
}
