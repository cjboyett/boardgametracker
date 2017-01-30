package com.cjboyett.boardgamestats.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.statsdetail.GamePlayDetailsTabbedActivity;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.StringToBitmapBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Casey on 3/23/2016.
 */
public class DailyGamePlayView extends LinearLayout {
	private Activity activity;
	private float ratio;
	private int index = 0;
	private List<TableRow> rows;

	private Map<Long, GamePlayData> boardGamePlayDatas, rpgPlayDatas, videoGamePlayDatas;

	private int backgroundColor, foregroundColor;

	private long[] ids;
	private List<String> gameTypes;
	private List<String> gameNames;

	private int NUM_COLUMNS;
	private float SCALE_FACTOR;

	public DailyGamePlayView(Activity activity, String day) {
		super(activity);
		LayoutInflater inflater = LayoutInflater.from(activity);
		inflater.inflate(R.layout.daily_game_play_view, this);
		this.activity = activity;

		NUM_COLUMNS = Preferences.numberOfGridColumns(activity);
		SCALE_FACTOR = Preferences.scaleFactor(activity);

		rows = new ArrayList<>();
		addTableRow();

		setDay(day);

		boardGamePlayDatas = new TreeMap<>();
		rpgPlayDatas = new TreeMap<>();
		videoGamePlayDatas = new TreeMap<>();

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		ratio = metrics.density;

		setColors();
		colorComponents();
	}

	public void setDay(String day) {
		((TextView) findViewById(R.id.textview_day)).setText(day);
	}

	public void addBoardGamePlay(final GamePlayData gamePlayData, final long gamePlayId) {
		boardGamePlayDatas.put(gamePlayId, gamePlayData);
	}

	public void addRPGPlay(final GamePlayData gamePlayData, final long gamePlayId) {
		rpgPlayDatas.put(gamePlayId, gamePlayData);
	}

	public void addVideoGamePlay(final GamePlayData gamePlayData, final long gamePlayId) {
		videoGamePlayDatas.put(gamePlayId, gamePlayData);
	}

	public void drawViews() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			TransitionManager.beginDelayedTransition(this);
		}

		ids = new long[boardGamePlayDatas.size() + rpgPlayDatas.size() + videoGamePlayDatas.size()];
		gameNames = new ArrayList<>();
		gameTypes = new ArrayList<>();

		int index = 0;

		for (final long gamePlayId : boardGamePlayDatas.keySet()) {
			ids[index] = gamePlayId;
			gameTypes.add("b");
			gameNames.add(boardGamePlayDatas.get(gamePlayId).getGame().getName());
			addGamePlayToView(gamePlayId, Game.GameType.BOARD, index);
			index++;
		}
		for (final long gamePlayId : rpgPlayDatas.keySet()) {
			ids[index] = gamePlayId;
			gameTypes.add("r");
			gameNames.add(rpgPlayDatas.get(gamePlayId).getGame().getName());
			addGamePlayToView(gamePlayId, Game.GameType.RPG, index);
			index++;
		}
		for (final long gamePlayId : videoGamePlayDatas.keySet()) {
			ids[index] = gamePlayId;
			gameTypes.add("v");
			gameNames.add(videoGamePlayDatas.get(gamePlayId).getGame().getName());
			addGamePlayToView(gamePlayId, Game.GameType.VIDEO, index);
			index++;
		}
	}

	private void addGamePlayToView(final long gamePlayId, Game.GameType gameType, int index) {
		TableRow.LayoutParams layoutParams =
				new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(5, 5, 5, 5);
		layoutParams.gravity = Gravity.CENTER;

		final GamePlayData gamePlayData;
		if (gameType == Game.GameType.BOARD) gamePlayData = boardGamePlayDatas.get(gamePlayId);
		else if (gameType == Game.GameType.RPG) gamePlayData = rpgPlayDatas.get(gamePlayId);
		else if (gameType == Game.GameType.VIDEO) gamePlayData = videoGamePlayDatas.get(gamePlayId);
		else gamePlayData = null;

		final String thumbnailUrl = "http://" + gamePlayData.getGame().getThumbnailUrl();
		Bitmap thumbnail = new ImageController(activity)
				.setDirectoryName("thumbnails")
				.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1)).load();

		ImageView imageView = new ImageView(activity);
		imageView.setLayoutParams(layoutParams);
		imageView.setMinimumWidth((int) (90 * ratio * SCALE_FACTOR));
		imageView.setMinimumHeight((int) (128 * ratio * SCALE_FACTOR));
		if (!StringUtilities.isRPG(gameType.getType()))
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		else
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setPadding(8, 8, 8, 8);

		// TODO Get this to scale and fit properly
		if (thumbnail == null) {
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			thumbnail = new StringToBitmapBuilder(activity)
					.setTextSize(8)
//								.setTextWidth(10)
//								.setAlign(Paint.Align.CENTER)
					.buildBitmap(gamePlayData.getGame().getName());
		}

		imageView.setImageBitmap(thumbnail);

		final int finalIndex = index;
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				ActivityUtilities.generatePaletteAndOpenActivity(activity,
																 new Intent(activity,
																			GamePlayDetailsTabbedActivity.class)
																		 .putExtra("IDS", ids)
																		 .putStringArrayListExtra("TYPES",
																								  (ArrayList<String>) gameTypes)
																		 .putStringArrayListExtra("NAMES",
																								  (ArrayList<String>) gameNames)
																		 .putExtra("COUNT", ids.length)
																		 .putExtra("POSITION", finalIndex),
																 "http://" + gamePlayData.getGame().getThumbnailUrl(),
																 "UP");
				ActivityUtilities.exitUp(activity);
			}
		});

		if (index == NUM_COLUMNS) {
			addTableRow();
			index = 0;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			TransitionManager.beginDelayedTransition(this);
		}

		rows.get(rows.size() - 1).addView(imageView);
		index++;

		gamePlayData.getDate().getDayOfWeek();

	}

	public void clearViews() {
		for (TableRow row : rows) ((TableLayout) findViewById(R.id.table_daily_game_plays)).removeView(row);
		rows = new ArrayList<>();
		addTableRow();
		index = 0;
	}

	private void setColors() {
		backgroundColor = Preferences.getBackgroundColor(getContext());
		foregroundColor = Preferences.getForegroundColor(getContext());
	}

	private void colorComponents() {
		setBackgroundColor(backgroundColor);
		((TextView) findViewById(R.id.textview_day)).setTextColor(foregroundColor);
		findViewById(R.id.textview_day).setBackgroundColor(backgroundColor);
		findViewById(R.id.table_daily_game_plays).setBackgroundColor(backgroundColor);
	}

	private void addTableRow() {
		TableRow row = new TableRow(activity);
		row.setPadding(5, 5, 5, 5);
		row.setBackgroundColor(backgroundColor);
		TableRow.LayoutParams layoutParams =
				new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;
		row.setLayoutParams(layoutParams);
		rows.add(row);
		((TableLayout) findViewById(R.id.table_daily_game_plays)).addView(row);
	}

	public void clearIds() {
		boardGamePlayDatas.clear();
		rpgPlayDatas.clear();
		videoGamePlayDatas.clear();
	}

	public void setChildrenBackrground(int color) {
//		setBackgroundColor(color);
		backgroundColor = color;
		((TextView) findViewById(R.id.textview_day)).setTextColor(Color.BLACK);
		findViewById(R.id.textview_day).setBackgroundColor(color);
		findViewById(R.id.table_daily_game_plays).setBackgroundColor(color);
	}
}