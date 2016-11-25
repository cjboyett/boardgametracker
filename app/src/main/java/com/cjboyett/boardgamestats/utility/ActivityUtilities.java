package com.cjboyett.boardgamestats.utility;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.model.stats.StatisticsManager;
import com.cjboyett.boardgamestats.utility.view.ImageController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 3/16/2016.
 */
public class ActivityUtilities
{
	public static void openActivity(Activity activity, Intent intent, String exit)
	{
		activity.startActivity(intent);
		exit(activity, exit);
	}

	public static void generatePaletteAndOpenActivity(final Activity activity, final Intent intent, final String thumbnailUrl, final String exit)
	{
		final Bitmap thumbnail = new ImageController(activity)
				.setDirectoryName("thumbnails")
				.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/")+1))
				.load();
		if (Preferences.generatePalette(activity) && thumbnail != null)
		{
			Palette.from(thumbnail).generate(new Palette.PaletteAsyncListener()
			{
				@Override
				public void onGenerated(Palette palette)
				{
					List<Palette.Swatch> swatchList = new ArrayList<>();
					if (palette.getDarkVibrantSwatch() != null)
						swatchList.add(palette.getDarkVibrantSwatch());
					if (palette.getDarkMutedSwatch() != null)
						swatchList.add(palette.getDarkMutedSwatch());
					if (palette.getMutedSwatch() != null)
						swatchList.add(palette.getMutedSwatch());
					if (palette.getVibrantSwatch() != null)
						swatchList.add(palette.getVibrantSwatch());
					if (palette.getLightMutedSwatch() != null)
						swatchList.add(palette.getLightMutedSwatch());
					if (palette.getLightVibrantSwatch() != null)
						swatchList.add(palette.getLightVibrantSwatch());

					Palette.Swatch swatch;
					if (Preferences.lightUI(activity))
						swatch = swatchList.get(swatchList.size() - 1);
					else swatch = swatchList.get(0);

					Preferences.setGeneratedPaletteColors(activity, swatch.getRgb(), swatch.getBodyTextColor());

					openActivity(activity, intent
									.putExtra("BITMAP", thumbnail)
									.putExtra("BACKGROUND", swatch.getRgb())
									.putExtra("TEXT", swatch.getBodyTextColor()),
							exit);
				}
			});
		}
		else
		{
			if (thumbnail != null) intent.putExtra("BITMAP", thumbnail);
			if (Preferences.lightUI(activity))
				intent.putExtra("BACKGROUND", activity.getResources().getColor(R.color.colorMainLight))
						.putExtra("TEXT", activity.getResources().getColor(R.color.colorMainDark));
			else
			intent.putExtra("BACKGROUND", activity.getResources().getColor(R.color.colorMainDark))
					.putExtra("TEXT", activity.getResources().getColor(R.color.colorMainLight));
			openActivity(activity, intent, exit);
		}
	}

	public static boolean databaseChanged(Context context)
	{
		SharedPreferences sharedPreferences = Preferences.getSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.database_changed), true);
	}

	public static void setDatabaseChanged(Context context, boolean preference)
	{
		SharedPreferences sharedPreferences = Preferences.getSharedPreferences(context);
		sharedPreferences.edit()
				.putBoolean(context.getString(R.string.database_changed), preference)
				.apply();

		if (preference)
		{
			DataManager.getInstance((Application)context.getApplicationContext())
			           .initialize();
			StatisticsManager.getInstance(context)
			                 .reset();
		}
	}

	public static void exit(Activity activity, String direction)
	{
		switch (direction)
		{
			case "UP":
				ActivityUtilities.exitUp(activity);
				break;
			case "DOWN":
				ActivityUtilities.exitDown(activity);
				break;
			case "LEFT":
				ActivityUtilities.exitLeft(activity);
				break;
			case "RIGHT":
				ActivityUtilities.exitRight(activity);
				break;
		}
	}

	public static void exitUp(Activity activity)
	{
		if (Preferences.useActivityTransitions(activity))
			activity.overridePendingTransition(R.anim.shrink_up_enter, R.anim.shrink_up_exit);
	}

	public static void exitDown(Activity activity)
	{
		if (Preferences.useActivityTransitions(activity))
			activity.overridePendingTransition(R.anim.shrink_down_enter, R.anim.shrink_down_exit);
	}

	public static void exitLeft(Activity activity)
	{
		if (Preferences.useActivityTransitions(activity))
			activity.overridePendingTransition(R.anim.shrink_left_enter, R.anim.shrink_left_exit);
	}

	public static void exitRight(Activity activity)
	{
		if (Preferences.useActivityTransitions(activity))
			activity.overridePendingTransition(R.anim.shrink_right_enter, R.anim.shrink_right_exit);
	}

	public static int getLastStatsPage(Activity activity)
	{
		SharedPreferences sharedPreferences = Preferences.getSharedPreferences(activity);
		return sharedPreferences.getInt(activity.getString(R.string.last_stats_page), 1);
	}

	public static void setLastStatsPage(Activity activity, int page)
	{
		SharedPreferences sharedPreferences = Preferences.getSharedPreferences(activity);
		sharedPreferences.edit()
				.putInt(activity.getString(R.string.last_stats_page), page)
				.apply();
	}
}
