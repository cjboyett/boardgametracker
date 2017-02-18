package com.cjboyett.boardgamestats.activity.statsdetail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.base.BaseAdActivity;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameStatsDbUtility;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.StringToBitmapBuilder;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.StatsView;
import com.cjboyett.boardgamestats.view.adapter.GamePlayAdapter;

import java.util.ArrayList;
import java.util.List;

public class GameStatsActivity extends BaseAdActivity {
	private Activity activity = this;
	private View view;
	private ListView listView;
	private ScrollView scrollView;
	private int scrollY;

	private List<StatsView> statsViews;

	private GestureDetectorCompat gestureDetector;
	private GamesDbHelper dbHelper;

	public GameStatsActivity() {
		super("ca-app-pub-1437859753538305/2047913877");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_game_stats, null);
		setContentView(view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		dbHelper = new GamesDbHelper(this);

		gestureDetector = new GestureDetectorCompat(this, new ScrollGestureListener());

		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	protected void onResume() {
		super.onResume();
		dbHelper = new GamesDbHelper(this);
		if (ActivityUtilities.databaseChanged(this)) generateLayout();
	}

	@Override
	protected void onDestroy() {
		if (dbHelper != null) dbHelper.close();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		dbHelper.close();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ActivityUtilities.exitDown(this);
	}

	protected void generateLayout() {
		try {
			final List<GamePlayData> gamePlayDataList;
			Game game = null;
			List<String[]> stats = new ArrayList<>();

			if (getIntent().getStringExtra("TYPE").equals("b")) {
				gamePlayDataList =
						BoardGameStatsDbUtility.getGamePlaysFromGame(dbHelper, getIntent().getStringExtra("GAME"));
				game = BoardGameDbUtility.getBoardGame(dbHelper, getIntent().getStringExtra("GAME"));
				stats.add(new String[]{"Last Played",
									   BoardGameStatsDbUtility.lastTimePlayed(dbHelper, game.getName()).toString()});
				stats.add(new String[]{"Times Played",
									   BoardGameStatsDbUtility.timesPlayed(dbHelper, game.getName()) + ""});
				stats.add(new String[]{"Time Played",
									   StringUtilities.convertMinutes(BoardGameStatsDbUtility.totalTimePlayed(dbHelper,
																											  game.getName()))});
				stats.add(new String[]{"Average Game Length",
									   StringUtilities.convertMinutes(BoardGameStatsDbUtility.averageTimePlayed(dbHelper,
																												game.getName()))});
				stats.add(new String[]{"Times Won",
									   BoardGameStatsDbUtility.getTimesWon(dbHelper, game.getName()) + ""});
				stats.add(new String[]{"Most Played With",
									   BoardGameStatsDbUtility.getMostPlayedWithPlayer(dbHelper, game.getName())});
				stats.add(new String[]{"Most Lost Against",
									   BoardGameStatsDbUtility.getMostLostToPlayer(dbHelper, game.getName())});
			} else if (getIntent().getStringExtra("TYPE").equals("r")) {
				gamePlayDataList = RPGStatsDbUtility.getGamePlaysFromGame(dbHelper, getIntent().getStringExtra("GAME"));
				game = RPGDbUtility.getRPG(dbHelper, getIntent().getStringExtra("GAME"));
				stats.add(new String[]{"Last Played",
									   RPGStatsDbUtility.lastTimePlayed(dbHelper, game.getName()).toString()});
				stats.add(new String[]{"Times Played", RPGStatsDbUtility.timesPlayed(dbHelper, game.getName()) + ""});
				stats.add(new String[]{"Time Played",
									   StringUtilities.convertMinutes(RPGStatsDbUtility.totalTimePlayed(dbHelper,
																										game.getName()))});
				stats.add(new String[]{"Average Game Length",
									   StringUtilities.convertMinutes(RPGStatsDbUtility.averageTimePlayed(dbHelper,
																										  game.getName()))});
				stats.add(new String[]{"Most Played With",
									   RPGStatsDbUtility.getMostPlayedWithPlayer(dbHelper, game.getName())});
			} else if (getIntent().getStringExtra("TYPE").equals("v")) {
				gamePlayDataList =
						VideoGameStatsDbUtility.getGamePlaysFromGame(dbHelper, getIntent().getStringExtra("GAME"));
				game = VideoGameDbUtility.getVideoGame(dbHelper, getIntent().getStringExtra("GAME"));
				stats.add(new String[]{"Last Played",
									   VideoGameStatsDbUtility.lastTimePlayed(dbHelper, game.getName()).toString()});
				stats.add(new String[]{"Times Played",
									   VideoGameStatsDbUtility.timesPlayed(dbHelper, game.getName()) + ""});
				stats.add(new String[]{"Time Played",
									   StringUtilities.convertMinutes(VideoGameStatsDbUtility.totalTimePlayed(dbHelper,
																											  game.getName()))});
				stats.add(new String[]{"Average Game Length",
									   StringUtilities.convertMinutes(VideoGameStatsDbUtility.averageTimePlayed(dbHelper,
																												game.getName()))});
				stats.add(new String[]{"Most Played With",
									   VideoGameStatsDbUtility.getMostPlayedWithPlayer(dbHelper, game.getName())});
			} else gamePlayDataList = new ArrayList<>();

			//TODO Fix this!  Add the scrollview back.
/*
			scrollView = (ScrollView) findViewById(R.id.scrollview_game_stats);
			scrollView.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					if (Preferences.useSwipes(v.getContext())) return gestureDetector.onTouchEvent(event);
					return false;
				}
			});
*/
			((TextView) view.findViewById(R.id.textview_game_name)).setText(game.getName());

			String thumbnailUrl = game.getThumbnailUrl();

			Bitmap thumbnail = null;
			try {
				ImageController imageController = new ImageController(activity).setDirectoryName("thumbnails");
				thumbnail =
						imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1)).load();
				imageController.close();
			} catch (Exception e) {
			}

			if (thumbnail == null) //view.findViewById(R.id.imageview_thumbnail).setVisibility(View.GONE);
				thumbnail = new StringToBitmapBuilder(this).buildBitmap(game.getName());
			/*else*/
			((ImageView) view.findViewById(R.id.imageview_thumbnail)).setImageBitmap(thumbnail);

			statsViews = new ArrayList<>();

			for (String[] stat : stats)
				if (!TextUtils.isEmpty(stat[1]))
					statsViews.add(new StatsView(this, stat[0], stat[1]));

			for (StatsView statsView : statsViews)
				((LinearLayout) view.findViewById(R.id.linearlayout_stats)).addView(statsView);

			//RatingBar ratingBar = ((RatingBar)view.findViewById(R.id.ratingBar));

			listView = (ListView) findViewById(R.id.listview_gameplay);
			listView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (Preferences.useSwipes(v.getContext()))
						return gestureDetector.onTouchEvent(event);
					return false;
				}
			});

			final long[] ids = new long[gamePlayDataList.size()];
			final List<String> gameTypes = new ArrayList<>();
			final List<String> gameNames = new ArrayList<>();
			for (int i = 0; i < ids.length; i++) {
				ids[i] = gamePlayDataList.get(i).getId();
				gameTypes.add(getIntent().getStringExtra("TYPE"));
				gameNames.add(gamePlayDataList.get(i).getGame().getName());
			}

			listView.setAdapter(new GamePlayAdapter(this, gamePlayDataList));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					GamePlayData gamePlayData = gamePlayDataList.get(position);
					ActivityUtilities.generatePaletteAndOpenActivity(activity,
																	 new Intent(activity,
																				GamePlayDetailsTabbedActivity.class)
																			 .putExtra("IDS", ids)
																			 .putStringArrayListExtra("TYPES",
																									  (ArrayList<String>) gameTypes)
																			 .putStringArrayListExtra("NAMES",
																									  (ArrayList<String>) gameNames)
																			 .putExtra("COUNT", ids.length)
																			 .putExtra("POSITION", position),
																	 "http://" +
																			 gamePlayData.getGame().getThumbnailUrl(),
																	 "UP");
					ActivityUtilities.exitUp(activity);
				}
			});

			ActivityUtilities.setDatabaseChanged(this, false);
		} catch (Exception e) {
			e.printStackTrace();
			onBackPressed();
		}
	}

	@Override
	protected void setColors() {
		if (Preferences.generatePalette(this)) {
			backgroundColor = Preferences.getGeneratedBackgroundColor(this);
			foregroundColor = Preferences.getGeneratedForegroundColor(this);
		} else {
			backgroundColor = Preferences.getBackgroundColor(this);
			foregroundColor = Preferences.getForegroundColor(this);
		}
	}

	@Override
	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);

		((TextView) view.findViewById(R.id.textview_game_name)).setTextColor(foregroundColor);

		if (statsViews != null)
			for (StatsView statsView : statsViews)
				if (statsView != null) statsView.colorComponents(backgroundColor, foregroundColor);

		((TextView) view.findViewById(R.id.textview_stats)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_stats).setBackgroundColor(backgroundColor);

		((TextView) view.findViewById(R.id.textview_gameplays)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_gameplays).setBackgroundColor(backgroundColor);

		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.relativelayout_game_data_top), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.listview_gameplay), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.scrollView), foregroundColor);

//		ViewUtilities.tintRatingBar(((RatingBar)view.findViewById(R.id.ratingBar)), foregroundColor, Preferences.getHintTextColor(this));
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			View c = listView.getChildAt(0);
			scrollY = -c.getTop() + listView.getFirstVisiblePosition() * c.getHeight();
			Log.d("DOWN", scrollY + "");
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.d("FLING", scrollY + "");
			if (Math.abs(velocityX) < Math.abs(velocityY)) {
				if (Math.abs(e1.getY() - e2.getY()) >= 200) {
					if (velocityY > 2000 && scrollY <= 0) {
						onBackPressed();
						return true;
					}
				}
			}
			return false;
		}
	}
}
