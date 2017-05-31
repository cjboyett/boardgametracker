package com.cjboyett.boardgamestats.conductor.addgameplay;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.support.RouterPagerAdapter;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.addgame.AddGameActivity;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.conductor.changehandlers.DirectionalChangeHandler;
import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.GamesDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.Timer;
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
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class AddGamePlayTabbedController extends BaseController {
	private Activity activity;
	private AddGamePlayDetailsController addGamePlayDetailsController;
	private AddGamePlayPlayersController addGamePlayPlayersController;
	private AddGamePlaySubmitController addGamePlaySubmitController;

	private ViewPager mViewPager;
	private int currentPage;

	private GamesDbHelper dbHelper;
	private String gameName = null, date, location, notes, gameType;
	private int timePlayed;

	private BoardGamePlayData boardGamePlayData;
	private RPGPlayData rpgPlayData;
	private VideoGamePlayData videoGamePlayData;
	private long gamePlayId = -1L;
	private boolean fromWidget = false;

	private View view;

	private CallbackManager callbackManager;

	private boolean override = false, submitted = false;

	public AddGamePlayTabbedController() {
		//Empty Constructor
	}

	public AddGamePlayTabbedController(String gameName, String gameType, long gamePlayId, boolean fromWidget) {
		this.gameName = gameName;
		this.gameType = gameType;
		this.gamePlayId = gamePlayId;
		this.fromWidget = fromWidget;
	}

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.add_game_play_tabbed_controller, container, false);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		activity = getActivity();
		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(this);

		mViewPager = (ViewPager) view.findViewById(R.id.container);
		TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);
		tabLayout.setBackgroundColor(buttonColor);
		tabLayout.setSelectedTabIndicatorColor(ColorUtilities.darken(buttonColor));
		tabLayout.setTabTextColors(ColorUtilities.lighten(foregroundColor), foregroundColor);

		dbHelper = new GamesDbHelper(activity);
		if (gameType == null) gameType = "";
		else {
			switch (gameType) {
				case "b":
					gameType = Game.GameType.BOARD.getType();
					break;
				case "r":
					gameType = Game.GameType.RPG.getType();
					break;
				case "v":
					gameType = Game.GameType.VIDEO.getType();
					break;
			}
		}

		Timber.d(gameName + " " + gameType + " " + gamePlayId);
		if (gameType != null && !gameType.equals("") && gamePlayId != -1L) {
			if (StringUtilities.isBoardGame(gameType))
				boardGamePlayData = BoardGameDbUtility.getGamePlay(dbHelper, gamePlayId);
			else if (StringUtilities.isRPG(gameType))
				rpgPlayData = RPGDbUtility.getGamePlay(dbHelper, gamePlayId);
			else if (StringUtilities.isVideoGame(gameType))
				videoGamePlayData = VideoGameDbUtility.getGamePlay(dbHelper, gamePlayId);
		}

		addGamePlayDetailsController = new AddGamePlayDetailsController();
		addGamePlayPlayersController = new AddGamePlayPlayersController();
		addGamePlaySubmitController = new AddGamePlaySubmitController();

		addGamePlayDetailsController.setParent(this);
		addGamePlaySubmitController.setParent(this);

		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

		if (StringUtilities.isBoardGame(gameType) && boardGamePlayData != null) {
			addGamePlayDetailsController.setData(boardGamePlayData.getTimePlayed(),
												 boardGamePlayData.getDate()
																  .rawDate(),
												 boardGamePlayData.getLocation(),
												 boardGamePlayData.getNotes());
			addGamePlayPlayersController.setPlayers(boardGamePlayData.getOtherPlayers()
																	 .values());
			addGamePlaySubmitController.setIgnoreCheckBox(!boardGamePlayData.isCountForStats());
		} else if (StringUtilities.isRPG(gameType) && rpgPlayData != null) {
			addGamePlayDetailsController.setData(rpgPlayData.getTimePlayed(),
												 rpgPlayData.getDate()
															.rawDate(),
												 rpgPlayData.getLocation(),
												 rpgPlayData.getNotes());
			addGamePlayPlayersController.setPlayers(rpgPlayData.getOtherPlayers()
															   .values());
			addGamePlaySubmitController.setIgnoreCheckBox(!rpgPlayData.isCountForStats());
		} else if (StringUtilities.isVideoGame(gameType) && videoGamePlayData != null) {
			addGamePlayDetailsController.setData(videoGamePlayData.getTimePlayed(),
												 videoGamePlayData.getDate()
																  .rawDate(),
												 videoGamePlayData.getLocation(),
												 videoGamePlayData.getNotes());
			addGamePlayPlayersController.setPlayers(videoGamePlayData.getOtherPlayers()
																	 .values());
			addGamePlaySubmitController.setIgnoreCheckBox(!videoGamePlayData.isCountForStats());
		}

		if (fromWidget) {
			TempDataManager tempDataManager = TempDataManager.getInstance(activity.getApplication());
			tempDataManager.initialize();

			if (fromWidget) {
				final Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);
				String date = year + "" + (month > 10 ? month : "0" + month) + "" + (day > 10 ? day : "0" + day);

				addGamePlayDetailsController.setData(0, date, "", "");
			}
		}

		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//				Timber.d(position + "");
			}

			@Override
			public void onPageSelected(int position) {
				// FIXME
//				Timber.d(position + "");
				addGamePlayDetailsController.updateData();
				addGamePlayPlayersController.addTempPlayers();
				currentPage = position;
			}

			@Override
			public void onPageScrollStateChanged(int state) {
//				Timber.d(state + "");
			}
		});

		setColors();
		colorComponents();
	}

	@Override
	protected void onDetach(@NonNull View view) {
		if (dbHelper != null) dbHelper.close();
		super.onDetach(view);
	}

	@Override
	protected void onActivityPaused(@NonNull Activity a) {
		dbHelper.close();

		GamePlayDetails gamePlayDetails = addGamePlayDetailsController.getGamePlayDetails();
		Timer timer = gamePlayDetails.getTimer();

		TempDataManager tempDataManager = TempDataManager.getInstance();
		tempDataManager.clearTempGamePlayData();
		tempDataManager.setTempGamePlayData(gamePlayDetails);
		tempDataManager.saveTempGamePlayData();

		tempDataManager.setTimer(timer);
		tempDataManager.saveTimer();

		List<Long> times = timer.toList();
		if (!submitted && !times.isEmpty() && times.get(0) > 0 && times.get(1) > times.get(2)) {
			String game =
					gamePlayDetails.getGameName(); //TempDataManager.getInstance(activity.getApplication()).getTempGamePlayData().get(0);
			TimerNotificationBuilder timerNotificationBuilder = new TimerNotificationBuilder();
			NotificationCompat.Builder builder =
					timerNotificationBuilder.createTimerNotification(activity, game, true, times.get(0));
			timerNotificationBuilder.createTimerNotification(activity, builder);
		}
	}

	@Override
	protected void onActivityResumed(@NonNull Activity a) {
		dbHelper = new GamesDbHelper(activity);
		final NotificationManager manager =
				(NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		int NOTIFICATION_ID = 0;
		manager.cancel(NOTIFICATION_ID);

		GamePlayDetails gamePlayDetails = new GamePlayDetails();
		TempDataManager tempDataManager = TempDataManager.getInstance();
		List<String> gameData = tempDataManager.getTempGamePlayData();
		Timer timer = tempDataManager.getTimer();
		gamePlayDetails.setTimer(timer);

		if (gameData != null && gameData.size() >= 6) {
			if (!TextUtils.isEmpty(gameData.get(0)))
				gamePlayDetails.setGameName(gameData.get(0));
			if (!TextUtils.isEmpty(gameData.get(1)))
				gamePlayDetails.setGameType(gameData.get(1));
			if (!TextUtils.isEmpty(gameData.get(2)))
				gamePlayDetails.setTimePlayed(gameData.get(2));
			if (!TextUtils.isEmpty(gameData.get(3)))
				gamePlayDetails.setDate(StringUtilities.dateToString(gameData.get(3)));
			if (!TextUtils.isEmpty(gameData.get(4)))
				gamePlayDetails.setLocation(gameData.get(4));
			if (!TextUtils.isEmpty(gameData.get(5)))
				gamePlayDetails.setNotes(gameData.get(5));
		}

		gameName = gamePlayDetails.getGameName();
		gameType = gamePlayDetails.getGameType();

		addGamePlayDetailsController.setGamePlayDetails(gamePlayDetails);
	}


	@Override
	public boolean handleBack() {
		GamesDbUtility.clearTempTables(dbHelper);

		if (!override && addGamePlayDetailsController.isTimerRunning()) {
			AlertDialog dialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Woah!")
					.setMessage(
							"I see you have the timer running.  Would you like to discard this game play?  Or I can keep the timer going for you.")
					.setPositiveButton("Stay", null)
					.setNeutralButton("Discard", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							submitted = true;
							Preferences.setTimerRunning(activity, false);
							goBack();
						}
					})
					.setNegativeButton("Leave", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Preferences.setTimerRunning(activity, true);
							goBack();
						}
					})
					.create();
			dialog.show();
		} else {
			goBack();
		}

		return true;
	}

	private void goBack() {
		getRouter().popToRoot(DirectionalChangeHandler.from(DirectionalChangeHandler.TOP));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 100 && resultCode == RESULT_OK) {
			mViewPager.setCurrentItem(0);
			gameName = data.getStringExtra("GAME");
			gameType = data.getStringExtra("TYPE");
			addGamePlayDetailsController.setGame(gameName, gameType);
		} else if (requestCode == 201 && resultCode == RESULT_OK) {
			List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
			for (String path : paths) Timber.d(path);
		} else callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	private void colorComponents() {
		view.setBackgroundColor(ColorUtilities.darken(backgroundColor));
	}

	public Game getGame() {
		TempDataManager tempDataManager = TempDataManager.getInstance(activity.getApplication());
		if (tempDataManager.getTempGamePlayData().size() >= 6) {
			gameName = tempDataManager.getTempGamePlayData().get(0);
			gameType = tempDataManager.getTempGamePlayData().get(1);
			if (tempDataManager.getTimer().getTimerBase() != 0 &&
					tempDataManager.getTimer().isTimerRunning())
				timePlayed = getMinutesFromTimeString(TimerUtility.getElapsedTime(activity));
			else
				timePlayed = getMinutesFromTimeString(tempDataManager.getTempGamePlayData().get(2));
			date = tempDataManager.getTempGamePlayData().get(3);
			location = tempDataManager.getTempGamePlayData().get(4);
			notes = tempDataManager.getTempGamePlayData().get(5);
		}
		Timber.d(gameName + " " + gameType + " " + timePlayed + " " + date + " " + location + " " + notes);

		if (gameName != null && gameType != null) {
			if (StringUtilities.isBoardGame(gameType))
				return BoardGameDbUtility.getBoardGame(dbHelper, gameName);
			else if (StringUtilities.isRPG(gameType))
				return RPGDbUtility.getRPG(dbHelper, gameName);
			else if (StringUtilities.isVideoGame(gameType))
				return VideoGameDbUtility.getVideoGame(dbHelper, gameName);
		}
		return null;
	}

	public void makeGamePlay(boolean exit, boolean ignore) {
		TempDataManager tempDataManager = TempDataManager.getInstance(activity.getApplication());
		if (dbHelper == null) dbHelper = new GamesDbHelper(activity);
		try {
			Game game = getGame();
			if (game != null) {
				List<GamePlayerData> players;
				try {
					players = tempDataManager.getTempPlayers();
				} catch (Exception e) {
					Timber.e(e);
					players = addGamePlayPlayersController.getPlayerData();
				}

				if (StringUtilities.isBoardGame(gameType)) {
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

					if (gamePlayId == -1L)
						BoardGameDbUtility.addGamePlay(dbHelper, boardGamePlayData);
					else BoardGameDbUtility.updateGamePlay(dbHelper, gamePlayId, boardGamePlayData);
				} else if (StringUtilities.isRPG(gameType)) {
					rpgPlayData = new RPGPlayData((RolePlayingGame) game,
												  timePlayed,
												  new Date(date),
												  notes,
												  gamePlayId);
					rpgPlayData.setLocation(location);
					rpgPlayData.setCountForStats(!ignore);
					for (GamePlayerData player : players)
						rpgPlayData.addOtherPlayer(player.getPlayerName(), player);

					if (gamePlayId == -1L) RPGDbUtility.addGamePlay(dbHelper, rpgPlayData);
					else RPGDbUtility.updateGamePlay(dbHelper, gamePlayId, rpgPlayData);
				} else if (StringUtilities.isVideoGame(gameType)) {
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

					if (gamePlayId == -1L)
						VideoGameDbUtility.addGamePlay(dbHelper, videoGamePlayData);
					else VideoGameDbUtility.updateGamePlay(dbHelper, gamePlayId, videoGamePlayData);
				}

				for (GamePlayerData player : players) {
					if (!player.getPlayerName().equalsIgnoreCase("master_user"))
						PlayersDbUtility.generateNewPlayer(dbHelper, player.getPlayerName());
				}

				ActivityUtilities.setDatabaseChanged(activity, true);

				Toast.makeText(activity, "Game play recorded", Toast.LENGTH_SHORT)
					 .show();
				override = true;
				submitted = true;
				Preferences.setTimerRunning(activity, false);
				if (exit) handleBack();
			} else {
				final AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
						.setTitle("Game is missing")
						.setMessage("A game is required.")
						.setPositiveButton("Okay", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mViewPager.setCurrentItem(0);
							}
						})
						.setNegativeButton("Add New Game", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								startActivityForResult(
										new Intent(view.getContext(), AddGameActivity.class)
												.putExtra("GAME", gameName),
										100);
								ActivityUtilities.exitLeft(activity);
							}
						})
						.create();
				alertDialog.show();
			}
		} catch (Exception e) {
			Timber.e(e);
			if (Preferences.isSuperUser(activity)) {
				AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
						.setTitle("Error")
						.withYancey(true)
						.setMessage(e.getMessage())
						.setPositiveButton("Okay", null)
						.create();
				alertDialog.show();
			} else {
				AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
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

	public void makeGamePlayAndShare(boolean ignore) {
		makeGamePlay(false, ignore);
		shareGamePlay();
	}

	private void shareGamePlay() {
		ShareDialog shareDialog = new ShareDialog(activity);
		callbackManager = CallbackManager.Factory.create();
		shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
			@Override
			public void onSuccess(Sharer.Result result) {
				handleBack();
			}

			@Override
			public void onCancel() {
			}

			@Override
			public void onError(FacebookException error) {
			}
		});
		if (ShareDialog.canShow(ShareLinkContent.class)) {
			ShareLinkContent feedContent = ViewUtilities.createShareLinkContent(activity,
																				gameName,
																				gameType,
																				location,
																				TempDataManager.getInstance(activity)
																							   .getTempPlayers(),
																				true);
			shareDialog.show(feedContent, ShareDialog.Mode.AUTOMATIC);
		}
	}


	public void setData(String gameName, String gameType, String timePlayed, String date, String location,
						String notes) {
		this.gameName = gameName;
		this.gameType = gameType;
		this.timePlayed = getMinutesFromTimeString(timePlayed);

		this.date = date;
		this.location = location;
		this.notes = notes;

		Timber.d(gameType);
	}

	private int getMinutesFromTimeString(String time) {
		Timber.d("Getting time from " + time);
		if (NumberUtils.isParsable(time)) return Integer.parseInt(time);
		else if (time.contains(":")) {
			String[] parts = time.split(":");
			Timber.d(Arrays.toString(parts));
			if (parts.length == 1 && NumberUtils.isParsable(parts[0])) return 1;
			else if (parts.length == 2 && NumberUtils.isParsable(parts[0]) && NumberUtils.isParsable(parts[1]))
				return Integer.parseInt(parts[0]) + Integer.parseInt(parts[1]) / 30;
			else if (parts.length == 3 && NumberUtils.isParsable(parts[0]) && NumberUtils.isParsable(parts[1])
					&& NumberUtils.isParsable(parts[2]))
				return 60 * Integer.parseInt(parts[0]) + Integer.parseInt(parts[1]) + Integer.parseInt(parts[2]) / 30;
		}
		return 0;
	}

	private class SectionsPagerAdapter extends RouterPagerAdapter {
		SectionsPagerAdapter(@NonNull Controller host) {
			super(host);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "DETAILS";
				case 1:
					return "PLAYERS";
				case 2:
					return "SUBMIT";
			}
			return null;
		}

		@Override
		public void configureRouter(@NonNull Router router, int position) {
			switch (position) {
				case 0:
					router.pushController(RouterTransaction.with(addGamePlayDetailsController));
					break;
				case 1:
					router.pushController(RouterTransaction.with(addGamePlayPlayersController));
					break;
				case 2:
					router.pushController(RouterTransaction.with(addGamePlaySubmitController));
					break;
			}
		}
	}
}