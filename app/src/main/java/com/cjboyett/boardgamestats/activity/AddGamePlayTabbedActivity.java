package com.cjboyett.boardgamestats.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.GamesDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;
import com.cjboyett.boardgamestats.notification.TimerNotificationBuilder;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.data.TimerUtility;
import com.cjboyett.boardgamestats.utility.view.ColorUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.animation.ZoomOutPageTransformer;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class AddGamePlayTabbedActivity extends AppCompatActivity
{
	private Activity activity = this;
	private AddGamePlayDetailsFragment addGamePlayDetailsFragment;
	private AddGamePlayPlayersFragment addGamePlayPlayersFragment;
	private AddGamePlaySubmitFragment addGamePlaySubmitFragment;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private int currentPage;

	private GamesDbHelper dbHelper;
	private String gameName = null, date, location, notes, gameType;
	private int timePlayed;

	private BoardGamePlayData boardGamePlayData;
	private RPGPlayData rpgPlayData;
	private VideoGamePlayData videoGamePlayData;
	private long gamePlayId;

	private View view;
	private int backgroundColor, foregroundColor;

	private CallbackManager callbackManager;

	private boolean override = false, submitted = false;
	private static int NOTIFICATION_ID = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_add_game_play_tabbed, null);
		setContentView(view);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		dbHelper = new GamesDbHelper(this);
		gameName = getIntent().getStringExtra("GAME");
		gameType = getIntent().getStringExtra("TYPE");
		if (gameType == null) gameType = "";
		else
		{
			if (gameType.equals("b")) gameType = Game.GameType.BOARD.getType();
			else if (gameType.equals("r")) gameType = Game.GameType.RPG.getType();
			else if (gameType.equals("v")) gameType = Game.GameType.VIDEO.getType();
		}

		gamePlayId = getIntent().getLongExtra("ID", -1l);

		Log.d("GAME", gameName + " " + gameType + " " + gamePlayId);
		if (gameType != null && !gameType.equals("") && gamePlayId != -1l)
		{
			if (StringUtilities.isBoardGame(gameType))
				boardGamePlayData = BoardGameDbUtility.getGamePlay(dbHelper, gamePlayId);
			else if (StringUtilities.isRPG(gameType))
				rpgPlayData = RPGDbUtility.getGamePlay(dbHelper, gamePlayId);
			else if (StringUtilities.isVideoGame(gameType))
				videoGamePlayData = VideoGameDbUtility.getGamePlay(dbHelper, gamePlayId);
		}

		addGamePlayDetailsFragment = new AddGamePlayDetailsFragment();
		addGamePlayPlayersFragment = new AddGamePlayPlayersFragment();
		addGamePlaySubmitFragment = new AddGamePlaySubmitFragment();

		if (StringUtilities.isBoardGame(gameType) && boardGamePlayData != null)
		{
			addGamePlayDetailsFragment.setData(boardGamePlayData.getTimePlayed(),
			                                   boardGamePlayData.getDate()
			                                                    .rawDate(),
			                                   boardGamePlayData.getLocation(),
			                                   boardGamePlayData.getNotes());
			addGamePlayPlayersFragment.setPlayers(boardGamePlayData.getOtherPlayers()
			                                                       .values());
			addGamePlaySubmitFragment.setIgnoreCheckBox(!boardGamePlayData.isCountForStats());
		}
		else if (StringUtilities.isRPG(gameType) && rpgPlayData != null)
		{
			addGamePlayDetailsFragment.setData(rpgPlayData.getTimePlayed(),
			                                   rpgPlayData.getDate()
			                                              .rawDate(),
			                                   rpgPlayData.getLocation(),
			                                   rpgPlayData.getNotes());
			addGamePlayPlayersFragment.setPlayers(rpgPlayData.getOtherPlayers()
			                                                 .values());
			addGamePlaySubmitFragment.setIgnoreCheckBox(!rpgPlayData.isCountForStats());
		}
		else if (StringUtilities.isVideoGame(gameType) && videoGamePlayData != null)
		{
			addGamePlayDetailsFragment.setData(videoGamePlayData.getTimePlayed(),
			                                   videoGamePlayData.getDate()
			                                                    .rawDate(),
			                                   videoGamePlayData.getLocation(),
			                                   videoGamePlayData.getNotes());
			addGamePlayPlayersFragment.setPlayers(videoGamePlayData.getOtherPlayers()
			                                                       .values());
			addGamePlaySubmitFragment.setIgnoreCheckBox(!videoGamePlayData.isCountForStats());
		}

		if (getIntent().hasExtra("WIDGET"))
		{
			TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
			tempDataManager.initialize();

			if (getIntent().hasExtra("WIDGET"))
			{
				final Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);
				String date = year + "" + (month > 10 ? month : "0" + month) + "" + (day > 10 ? day : "0" + day);

				addGamePlayDetailsFragment.setData(0, date, "", "");
			}
		}

		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{

			}

			@Override
			public void onPageSelected(int position)
			{
				// FIXME
				if (currentPage == 0) addGamePlayDetailsFragment.updateData();
				else if (currentPage == 1) addGamePlayPlayersFragment.addTempPlayers();
				currentPage = position;
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{

			}
		});

		setColors();
		colorComponents();
	}

	private void setColors()
	{
		backgroundColor = Preferences.getBackgroundColor(this);
		foregroundColor = Preferences.getForegroundColor(this);
	}

	private void colorComponents()
	{
		view.setBackgroundColor(ColorUtilities.darken(backgroundColor));
	}

	public Game getGame()
	{
		TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
		if (tempDataManager.getTempGamePlayData().size() >= 6)
		{
			gameName = tempDataManager.getTempGamePlayData().get(0);
			gameType = tempDataManager.getTempGamePlayData().get(1);
			if (tempDataManager.getTimer().get(0) != 0 && tempDataManager.getTimer().get(1) > tempDataManager.getTimer().get(2))
				timePlayed = getMinutesFromTimeString(TimerUtility.getElapsedTime(this));
			else
				timePlayed = getMinutesFromTimeString(tempDataManager.getTempGamePlayData().get(2));
			date = tempDataManager.getTempGamePlayData().get(3);
			location = tempDataManager.getTempGamePlayData().get(4);
			notes = tempDataManager.getTempGamePlayData().get(5);
		}
		Log.d("GAME", gameName + " " + gameType + " " + timePlayed + " " + date + " " + location + " " + notes);

		if (gameName != null && gameType != null)
		{
			if (StringUtilities.isBoardGame(gameType))
				return BoardGameDbUtility.getBoardGame(dbHelper, gameName);
			else if (StringUtilities.isRPG(gameType))
				return RPGDbUtility.getRPG(dbHelper, gameName);
			else if (StringUtilities.isVideoGame(gameType))
				return VideoGameDbUtility.getVideoGame(dbHelper, gameName);
		}
		return null;
	}

	public void makeGamePlay(boolean exit, boolean ignore)
	{
		TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
		if (dbHelper == null) dbHelper = new GamesDbHelper(this);
		try
		{
			Game game = getGame();
			if (game != null)
			{
				List<GamePlayerData> players = null;
				try
				{
					players = tempDataManager.getTempPlayers();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					players = addGamePlayPlayersFragment.getPlayerData();
				}

				if (StringUtilities.isBoardGame(gameType))
				{
					boardGamePlayData = new BoardGamePlayData((BoardGame) game,
					                                          players.get(0).getScore(),
					                                          players.get(0).isWin(),
					                                          timePlayed,
					                                          new Date(date),
					                                          notes,
					                                          gamePlayId);
					boardGamePlayData.setLocation(location);
					boardGamePlayData.setCountForStats(!ignore);
					for (GamePlayerData player : players)
						boardGamePlayData.addOtherPlayer(player.getPlayerName(), player);

					if (gamePlayId == -1l)
						BoardGameDbUtility.addGamePlay(dbHelper, boardGamePlayData);
					else BoardGameDbUtility.updateGamePlay(dbHelper, gamePlayId, boardGamePlayData);
				}
				else if (StringUtilities.isRPG(gameType))
				{
					rpgPlayData = new RPGPlayData((RolePlayingGame) game,
					                              timePlayed,
					                              new Date(date),
					                              notes,
					                              gamePlayId);
					rpgPlayData.setLocation(location);
					rpgPlayData.setCountForStats(!ignore);
					for (GamePlayerData player : players)
						rpgPlayData.addOtherPlayer(player.getPlayerName(), player);

					if (gamePlayId == -1l) RPGDbUtility.addGamePlay(dbHelper, rpgPlayData);
					else RPGDbUtility.updateGamePlay(dbHelper, gamePlayId, rpgPlayData);
				}
				else if (StringUtilities.isVideoGame(gameType))
				{
					videoGamePlayData = new VideoGamePlayData((VideoGame) game,
					                                          players.get(0).getScore(),
					                                          players.get(0).isWin(),
					                                          timePlayed,
					                                          new Date(date),
					                                          notes,
					                                          gamePlayId);
					videoGamePlayData.setLocation(location);
					videoGamePlayData.setCountForStats(!ignore);
					for (GamePlayerData player : players)
						videoGamePlayData.addOtherPlayer(player.getPlayerName(), player);

					if (gamePlayId == -1l)
						VideoGameDbUtility.addGamePlay(dbHelper, videoGamePlayData);
					else VideoGameDbUtility.updateGamePlay(dbHelper, gamePlayId, videoGamePlayData);
				}

				for (GamePlayerData player : players)
				{
					if (!player.getPlayerName().equalsIgnoreCase("master_user")) PlayersDbUtility.generateNewPlayer(dbHelper, player.getPlayerName());
				}

				ActivityUtilities.setDatabaseChanged(this, true);

				Toast.makeText(this, "Game play recorded", Toast.LENGTH_SHORT)
				     .show();
				override = true;
				submitted = true;
				Preferences.setTimerRunning(activity, false);
				if (exit) onBackPressed();
			}
			else
			{
				final AlertDialog alertDialog = new ViewUtilities.DialogBuilder(this)
						.setTitle("Game is missing")
						.setMessage("A game is required.")
						.setPositiveButton("Okay", new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								mViewPager.setCurrentItem(0);
							}
						})
						.setNegativeButton("Add New Game", new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								startActivityForResult(
										new Intent(view.getContext(), AddBoardGameActivity.class)
												.putExtra("GAME", gameName),
										100);
								ActivityUtilities.exitLeft(activity);
							}
						})
						.create();
				alertDialog.show();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (Preferences.isSuperUser(activity))
			{
				AlertDialog alertDialog = new ViewUtilities.DialogBuilder(this)
						.setTitle("Error")
						.withYancey(true)
						.setMessage(e.getMessage())
						.setPositiveButton("Okay", null)
						.create();
				alertDialog.show();
			}
			else
			{
				AlertDialog alertDialog = new ViewUtilities.DialogBuilder(this)
						.setTitle("Error")
						.withYancey(true)
						.setMessage("I apologize, but something seems to have gone wrong.  Please try again.\n\n" +
						            "If this problem continues, please let my creator know at casey@cjboyett.com.")
						.setPositiveButton("Okay", null)
						.create();
				alertDialog.show();
			}
		}
	}

	public void makeGamePlayAndShare(boolean ignore)
	{
		makeGamePlay(false, ignore);
		shareGamePlay();
	}

	private void shareGamePlay()
	{
		ShareDialog shareDialog = new ShareDialog(activity);
		callbackManager = CallbackManager.Factory.create();
		shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>()
		{
			@Override
			public void onSuccess(Sharer.Result result)
			{
				onBackPressed();
			}

			@Override
			public void onCancel()
			{
			}

			@Override
			public void onError(FacebookException error)
			{
			}
		});
		if (ShareDialog.canShow(ShareLinkContent.class))
		{
			ShareLinkContent feedContent = ViewUtilities.createShareLinkContent(activity, gameName, gameType, location, TempDataManager.getInstance(activity).getTempPlayers(), true);
			shareDialog.show(feedContent, ShareDialog.Mode.AUTOMATIC);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		dbHelper.close();

		List<Long> times = TempDataManager.getInstance(getApplication()).getTimer();
		if (!submitted && !times.isEmpty() && times.get(0) > 0 && times.get(1) > times.get(2))
		{
			String game = TempDataManager.getInstance(getApplication()).getTempGamePlayData().get(0);
/*			final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

*//*
			builder.setContentTitle("Timer")
			       .setAutoCancel(true)
			       .setColor(getResources().getColor(R.color.colorAccent))
			       .setContentText(TextUtils.isEmpty(game) ? "Game is running" : game)
			       .setSmallIcon(R.mipmap.ic_launcher)
			       .setPriority(Notification.PRIORITY_MAX)
			       .setOngoing(true)
			       .setUsesChronometer(true)
			       .setWhen(times.get(0) - SystemClock.elapsedRealtime() + System.currentTimeMillis())
			       .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle()
					                 .setBigContentTitle("Timer")
					                 .bigText(TextUtils.isEmpty(game) ? "Game is running" : game))
			       .addAction(android.R.drawable.ic_media_pause, "Pause", null);
*//*
			RemoteViews timeView = new RemoteViews(this.getPackageName(), R.layout.notification_timer);

//			timeView = new RemoteViews()

			builder.setContentTitle("Timer")
			       .setAutoCancel(true)
			       .setColor(getResources().getColor(R.color.colorAccent))
			       .setContentText(TextUtils.isEmpty(game) ? "Game is running" : game)
			       .setSmallIcon(R.mipmap.ic_launcher)
			       .setPriority(Notification.PRIORITY_MAX)
			       .setOngoing(true)
//			       .setUsesChronometer(true)
//			       .setWhen(times.get(0) - SystemClock.elapsedRealtime() + System.currentTimeMillis())
//			       .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle()
//					                 .setBigContentTitle("Timer")
//					                 .bigText(TextUtils.isEmpty(game) ? "Game is running" : game))
			       .setContent(timeView);
//			       .addAction(android.R.drawable.ic_media_pause, "Pause", null);

			timeView.setTextColor(R.id.textview_title, Color.DKGRAY);
			timeView.setTextColor(R.id.textview_game, Color.GRAY);
			timeView.setTextViewText(R.id.textview_game, TextUtils.isEmpty(game) ? "Game is running" : game);

			timeView.setChronometer(R.id.chronometer, times.get(0), null, true);
			timeView.setTextColor(R.id.chronometer, Color.DKGRAY);*/

/*
			PendingIntent pauseIntent = PendingIntent.getService(this,
			                                                     0,
			                                                     new Intent("com.cjboyett.boardgamestats.ACTION_PAUSE_TIMER"),
			                                                     PendingIntent.FLAG_UPDATE_CURRENT);

			timeView.setOnClickPendingIntent(R.id.imageview_pause, pauseIntent);

			String thumbnailUrl = null;

			thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, game);
			if (TextUtils.isEmpty(thumbnailUrl)) thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, game);
			if (TextUtils.isEmpty(thumbnailUrl)) thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, game);

			if (!TextUtils.isEmpty(thumbnailUrl))
			{
				ImageController imageController = new ImageController(activity).setDirectoryName("thumbnails");
				timeView.setImageViewBitmap(R.id.imageview_thumbnail, imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
				                                                                     .load());
			}
*/

/*
			PendingIntent pendingIntent = PendingIntent.getActivity(this,
			                                                        NOTIFICATION_ID,
			                                                        new Intent(this, AddGamePlayTabbedActivity.class),
			                                                        PendingIntent.FLAG_UPDATE_CURRENT);
*/

			TimerNotificationBuilder timerNotificationBuilder = new TimerNotificationBuilder();
			NotificationCompat.Builder builder = timerNotificationBuilder.createTimerNotification(this, game, true, times.get(0));
//			builder.setContentIntent(pendingIntent);
			timerNotificationBuilder.createTimerNotification(this, builder);

/*
			builder.setContentIntent(pendingIntent);

			final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(NOTIFICATION_ID, builder.build());

			try
			{
				Thread.sleep(5000);
				builder.setUsesChronometer(false);
				manager.notify(NOTIFICATION_ID, builder.build());
			}
			catch (Exception e)
			{}
*/
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		dbHelper = new GamesDbHelper(this);
		final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (dbHelper != null) dbHelper.close();
	}

	@Override
	public void onBackPressed()
	{
		GamesDbUtility.clearTempTables(dbHelper);

		if (!override && addGamePlayDetailsFragment.isTimerRunning())
		{
			AlertDialog dialog = new ViewUtilities.DialogBuilder(this)
					.setTitle("Woah!")
					.setMessage("I see you have the timer running.  Would you like to discard this game play?  Or I can keep the timer going for you.")
					.setPositiveButton("Stay", null)
					.setNeutralButton("Discard", new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							submitted = true;
							Preferences.setTimerRunning(activity, false);
							goBack();
						}
					})
					.setNegativeButton("Leave", new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Preferences.setTimerRunning(activity, true);
							goBack();
						}
					})
					.create();
			dialog.show();
		}
		else goBack();
	}

	private void goBack()
	{
		if (getIntent().getBooleanExtra("WIDGET", false) || getIntent().getStringExtra("EXIT") == null)
		{
			startActivity(new Intent(this, ClearStackMainActivity.class));
			ActivityUtilities.exitUp(this);
		}
		else
		{
			super.onBackPressed();
			String exitActivity = getIntent().getStringExtra("EXIT");
			ActivityUtilities.exit(this, exitActivity);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 100 && resultCode == RESULT_OK)
		{
			mViewPager.setCurrentItem(0);
			gameName = data.getStringExtra("GAME");
			gameType = data.getStringExtra("TYPE");
			addGamePlayDetailsFragment.setGame(gameName, gameType);
		}

		else if (requestCode == 201 && requestCode == RESULT_OK)
		{
			List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
			for (String path : paths) Log.d("PATH", path);
		}

		else callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	public void setData(String gameName, String gameType, String timePlayed, String date, String location, String notes)
	{
		this.gameName = gameName;
		this.gameType = gameType;
		this.timePlayed = getMinutesFromTimeString(timePlayed);

		this.date = date;
		this.location = location;
		this.notes = notes;

		Log.d("TYPE", gameType);
	}

	private int getMinutesFromTimeString(String time)
	{
		Log.d("TIME", "Getting time from " + time);
		if (NumberUtils.isParsable(time)) return Integer.parseInt(time);
		else if (time.contains(":"))
		{
			String[] parts = time.split(":");
			Log.d("TIME", Arrays.toString(parts));
			if (parts.length == 1 && NumberUtils.isParsable(parts[0])) return 1;
			else if (parts.length == 2 && NumberUtils.isParsable(parts[0]) && NumberUtils.isParsable(parts[1]))
				return Integer.parseInt(parts[0]) + Integer.parseInt(parts[1]) / 30;
			else if (parts.length == 3 && NumberUtils.isParsable(parts[0]) && NumberUtils.isParsable(parts[1])
			         && NumberUtils.isParsable(parts[2]))
				return 60 * Integer.parseInt(parts[0]) + Integer.parseInt(parts[1]) + Integer.parseInt(parts[2]) / 30;
		}
		return 0;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{

		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			switch (position)
			{
				case 0:
					return addGamePlayDetailsFragment;
				case 1:
					return addGamePlayPlayersFragment;
				case 2:
					return addGamePlaySubmitFragment;
			}
			return null;
		}

		@Override
		public int getCount()
		{
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			switch (position)
			{
				case 0:
					return "DETAILS";
				case 1:
					return "PLAYERS";
				case 2:
					return "SUBMIT";
			}
			return null;
		}
	}
}