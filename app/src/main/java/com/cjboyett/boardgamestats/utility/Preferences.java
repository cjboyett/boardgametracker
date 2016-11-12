package com.cjboyett.boardgamestats.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.view.ColorUtilities;

/**
 * Created by Casey on 3/19/2016.
 */
public class Preferences
{
	public static final int THUMBNAIL_STUPID_SMALL = 4;
	public static final int THUMBNAIL_TINY = 3;
	public static final int THUMBNAIL_SMALL = 2;
	public static final int THUMBNAIL_NORMAL = 1;
	public static final int THUMBNAIL_LARGE = 0;

	private static final float[] SCALE_FACTOR = {3f/2f, 1f, 3f/4f, 3f/5f, 3f/10f};
	private static final int[] GRID_COLUMNS = {2, 3, 4, 5, 10};

	public static boolean lightUI(Context context)
	{
		int defaultBackground = context.getResources().getColor(R.color.colorMainLight);
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		int background = sharedPreferences
				.getInt(context.getString(R.string.theme_background_preference), defaultBackground);
		float[] hsv = new float[3];
		Color.colorToHSV(background, hsv);
		return hsv[2] > 0.5f;

/*
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.light_UI_preference), true);
*/
	}

	public static void setLightUIPreference(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.light_UI_preference), preference)
				.apply();
	}

	public static boolean showCalendar(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.calendar_view_preference), true);
	}

	public static void setCalendarPreference(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.calendar_view_preference), preference)
				.apply();
	}

	public static void setThemeBackgroundPreference(Context context, int preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putInt(context.getString(R.string.theme_background_preference), preference)
				.apply();
	}

	public static void setThemeForegroundPreference(Context context, int preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putInt(context.getString(R.string.theme_foreground_preference), preference)
				.apply();
	}

	public static int getBackgroundColor(Context context)
	{
		int defaultBackground;
		if (lightUI(context)) defaultBackground = context.getResources().getColor(R.color.colorMainLight);
		else defaultBackground = context.getResources().getColor(R.color.colorMainDark);

		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences
				.getInt(context.getString(R.string.theme_background_preference), defaultBackground);
	}

	public static int getForegroundColor(Context context)
	{
		int defaultForeground;
		if (lightUI(context)) defaultForeground = context.getResources().getColor(R.color.colorMainDark);
		else defaultForeground = context.getResources().getColor(R.color.colorMainLight);

		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences
				.getInt(context.getString(R.string.theme_foreground_preference), defaultForeground);
	}

	public static int getHintTextColor(Context context)
	{
		if (lightUI(context)) return ColorUtilities.lighten(getForegroundColor(context));
		else return ColorUtilities.darken(getForegroundColor(context));
	}

	public static void setGeneratedPaletteColors(Context context, int backgroundColor, int foregroundColor)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putInt(context.getString(R.string.generated_background), backgroundColor)
				.putInt(context.getString(R.string.generated_foreground), foregroundColor)
				.apply();
	}

	public static int getGeneratedBackgroundColor(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getInt(context.getString(R.string.generated_background), getBackgroundColor(context));
	}

	public static int getGeneratedForegroundColor(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getInt(context.getString(R.string.generated_foreground), getForegroundColor(context));
	}

	public static Drawable getBackgroundDrawable(Context context)
	{
		if (lightUI(context)) return context.getResources().getDrawable(R.drawable.month_background_light);
		else return context.getResources().getDrawable(R.drawable.month_background_dark);
	}

	public static boolean generatePalette(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.generate_palette_preference), false);
	}

	public static void setGeneratePalettePreference(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.generate_palette_preference), preference)
				.apply();
	}


	public static float scaleFactor(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return SCALE_FACTOR[getThumbnailSize(context)];
	}

	public static int numberOfGridColumns(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return GRID_COLUMNS[getThumbnailSize(context)];
	}

	public static int getThumbnailSize(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getInt(context.getString(R.string.thumbnail_size_preference), THUMBNAIL_NORMAL);
	}

	public static void setThumbnailSizePreference(Context context, int preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putInt(context.getString(R.string.thumbnail_size_preference), preference)
				.apply();
	}


	public static boolean sortWinnersFirst(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.sort_winners_first_preference), false);
	}

	public static void setSortWinnersFirstPreference(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.sort_winners_first_preference), preference)
				.apply();
	}

	public static boolean useActivityTransitions(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.activity_transition_preference), true);
	}

	public static void setActivityTransitionPreference(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.activity_transition_preference), preference)
				.apply();
	}

	public static boolean useSwipes(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.swipe_preference), true);
	}

	public static void setUseSwipesPreference(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.swipe_preference), preference)
				.apply();
	}

	public static boolean isFirstVisit(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.first_visit), true);
	}

	public static void setFirstVisit(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.first_visit), preference)
				.apply();
	}


	public static String getTimerGame(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.timer_game), "");
	}

	public static void setTimerGame(Context context, String game)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putString(context.getString(R.string.timer_game), game)
		                 .apply();
	}

	public static long getTimerStart(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getLong(context.getString(R.string.timer_started), 0);
	}

	public static void setTimerStart(Context context, long time)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putLong(context.getString(R.string.timer_started), time)
		                 .apply();
	}

	public static boolean isTimerRunning(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.is_timer_running), false);
	}

	public static void setTimerRunning(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.is_timer_running), preference)
		                 .apply();
	}


	public static boolean useBoardGamesForStats(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.use_board_games_for_stats_preference), true);
	}

	public static void setUseBoardGamesForStats(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.use_board_games_for_stats_preference), preference)
		                 .apply();

		ActivityUtilities.setDatabaseChanged(context, true);
	}

	public static boolean useRPGsForStats(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.use_rpgs_for_stats_preference), true);
	}

	public static void setUseRPGsForStats(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.use_rpgs_for_stats_preference), preference)
		                 .apply();

		ActivityUtilities.setDatabaseChanged(context, true);
	}

	public static boolean useVideoGamesForStats(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.use_video_games_for_stats_preference), true);
	}

	public static void setUseVideoGamesForStats(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.use_video_games_for_stats_preference), preference)
		                 .apply();

		ActivityUtilities.setDatabaseChanged(context, true);
	}


	public static boolean showAds(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.show_ads), true) && !isSuperUser(context);
	}

	public static void setShowAds(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.show_ads), preference)
		                 .apply();
	}

	// TODO Add way to change this
	public static boolean showNotifications(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.show_notification), true);// && !isSuperUser(context);
	}

	public static void setShowNotifications(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.show_notification), preference)
		                 .apply();
	}

	public static boolean showReminder(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.show_reminder), true);// && !isSuperUser(context);
	}

	public static void setShowReminder(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.show_reminder), preference)
		                 .apply();
	}

	public static boolean isSuperUser(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.super_user), false);
	}

	public static void setSuperUser(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.super_user), preference)
				.apply();
	}

	public static boolean canUseCamera(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.use_camera_preference), false);
	}

	public static void setCanUseCameraPreference(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.use_camera_preference), preference)
				.apply();
	}

	public static boolean canAccessStorage(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.access_storage_preference), false);
	}

	public static void setCanAccessStoragePreference(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.access_storage_preference), preference)
				.apply();
	}

	public static boolean hasAskedPermission(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.asked_permission), false);
	}

	public static void setHasAskedPermission(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.asked_permission), preference)
				.apply();
	}

	public static String getUsername(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.display_name_preference), "User");
	}

	public static void setUsername(Context context, String username)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putString(context.getString(R.string.display_name_preference), username)
				.apply();
	}


	public static boolean showPopup(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.show_popup), true);
	}

	public static void setShowPopup(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.show_popup), preference)
				.apply();
	}

	public static boolean metYancey(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.met_yancey), false);
	}

	public static void setMetYancey(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.met_yancey), preference)
		                 .apply();
	}

	public static int gamePlayThreshold(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getInt(context.getString(R.string.threshold_preference), 3);
	}

	public static void setGamePlayThreshold(Context context, int preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putInt(context.getString(R.string.threshold_preference), preference)
		                 .apply();
	}

	public static long getLastHotnessDownload(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getLong(context.getString(R.string.last_hotness_download), 0l);
	}

	public static void setLastHotnessDownload(Context context, long preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putLong(context.getString(R.string.last_hotness_download), preference)
		                 .apply();
	}

	public static boolean needAllPlayerTableUpgrade(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.need_all_player_table_upgrade), false);
	}

	public static void setNeedAllPlayerTableUpgrade(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putBoolean(context.getString(R.string.need_all_player_table_upgrade), preference)
		                 .apply();
	}


	public static String getAuthId(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.auth_id), "");
	}

	public static void setAuthId(Context context, String authId)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putString(context.getString(R.string.auth_id), authId)
		                 .apply();
	}

	public static String getCurrentAuthProvider(Context context)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.auth_provider), "");
	}

	public static void setCurrentAuthProvider(Context context, String provider)
	{
		SharedPreferences sharedPreferences = getSharedPreferences(context);
		sharedPreferences.edit()
		                 .putString(context.getString(R.string.auth_provider), provider)
		                 .apply();
	}



	public static SharedPreferences getSharedPreferences(Context context)
	{
		return context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
	}

}
