package com.cjboyett.boardgamestats.view.ticker;

import android.animation.Animator;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cjboyett.boardgamestats.MyApp;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.addgame.AddGameActivity;
import com.cjboyett.boardgamestats.activity.addgameplay.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.activity.statsdetail.GameStatsActivity;
import com.cjboyett.boardgamestats.activity.statsdetail.PlayerStatsActivity;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.stats.StatisticsManager;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.data.UrlUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * View that cycles through TickerItems.
 * Created by Casey on 5/8/2016.
 */
public class Ticker extends RelativeLayout {
	private static final long DURATION = 8000;
	private final int THRESHOLD, TICKER_ITEM_TRANSLATION_X;
	private TickerItemView[] tickerItemViews;
	private TickerItem[] tickerItems;
	private int currentTickerItem = 0;

	private Handler tickerHandler;
	private Runnable tickerRunnable;
	private boolean paused, failedToLoad;

	private Queue<String> previousTickerItems;

	private int foregroundColor;

	public Ticker(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.layout_ticker, this);

		// THRESHOLD is used to keep ticker from using the same items too often.
		THRESHOLD = Math.min(StatisticsManager.getInstance(context).getNumberGamesPlayed(), 6);

		// Used with moving TickerItemViews on and off the screen
		TICKER_ITEM_TRANSLATION_X = ViewUtilities.dpToPx(context, 240);

		// Buffers two TickerItemViews in memory for ease of loading to screen
		tickerItemViews = new TickerItemView[2];
		tickerItemViews[0] = (TickerItemView) findViewById(R.id.ticker_view_1);
		tickerItemViews[1] = (TickerItemView) findViewById(R.id.ticker_view_2);
		// Sets second View off screen
		tickerItemViews[1].setTranslationX(TICKER_ITEM_TRANSLATION_X);

		initializeTickerItems();

		// Keeps ticker running in the background
		tickerHandler = new Handler();
		tickerRunnable = new Runnable() {
			@Override
			public void run() {
				changeTickerItem();
				if (!paused && !failedToLoad) tickerHandler.postDelayed(tickerRunnable, DURATION);
			}
		};
	}

	public void setColors(int foregroundColor) {
		this.foregroundColor = foregroundColor;
		colorComponents();
	}

	private void colorComponents() {
		// TODO Get this damn thing to tint correctly!!!
//		ViewUtilities.tintLayoutBackground(this, foregroundColor);
		tickerItemViews[0].colorComponents(foregroundColor);
		tickerItemViews[1].colorComponents(foregroundColor);
	}

	// Animation for moving TickerItemViews
	public void changeTickerItem() {
		tickerItemViews[currentTickerItem].animate()
										  .translationX(-TICKER_ITEM_TRANSLATION_X)
										  .setStartDelay(0)
										  .setDuration(1500)
										  .setInterpolator(new AnticipateOvershootInterpolator())
										  .setListener(new Animator.AnimatorListener() {
											  @Override
											  public void onAnimationStart(Animator animation) {
											  }

											  @Override
											  public void onAnimationEnd(Animator animation) {
												  tickerItemViews[currentTickerItem].setTranslationX(
														  TICKER_ITEM_TRANSLATION_X);
												  getNewTickerItem(currentTickerItem);
												  tickerItemViews[currentTickerItem = 1 - currentTickerItem].animate()
																											.translationX(
																													0)
																											.setStartDelay(
																													0)
																											.setDuration(
																													1000)
																											.setInterpolator(
																													new OvershootInterpolator())
																											.setListener(
																													null)
																											.start();
											  }

											  @Override
											  public void onAnimationCancel(Animator animation) {

											  }

											  @Override
											  public void onAnimationRepeat(Animator animation) {

											  }
										  })
										  .start();
	}

	// Starts...
	public void start() {
		paused = false;
		stop();
		if (!failedToLoad) tickerHandler.postDelayed(tickerRunnable, DURATION);
	}

	// Pauses...
	public void pause() {
		paused = true;
	}

	// And stops the ticker
	public void stop() {
		tickerHandler.removeCallbacks(tickerRunnable);
	}

	// Loads first 2 TickerItemViews at creation
	private void initializeTickerItems() {
		tickerItems = new TickerItem[2];
		// A queue used to keep TickerItems from being overused
		previousTickerItems = new LinkedList<>();

		// If no games have been played, set the first TickerItem to easily
		// allow the user to add game / game play
		if (StatisticsManager.getInstance(getContext()).getNumberGamesPlayed() == 0) {
			final Intent intent;
			final String direction;

			// If no games in collection then first TickerItem is an AddGameTickerItem
			if (StatisticsManager.getInstance(getContext()).getNumberOfGames() == 0) {
				tickerItems[0] = new AddBoardGameTickerItem(getContext());
				intent = new Intent(getContext(), AddGameActivity.class);
				direction = "UP";
			}
			// Otherwise it is an AddGamePlayTickerItem
			else {
				tickerItems[0] = new AddGamePlayTickerItem(getContext());
				intent = new Intent(getContext(), AddGamePlayTabbedActivity.class);
				direction = "DOWN";
			}
			tickerItemViews[0].setBlurb(tickerItems[0].getBlurb());
			tickerItemViews[0].setImage(tickerItems[0].getImage());
			tickerItemViews[0].setOnImageClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ActivityUtilities.openActivity((Activity) getContext(), intent, direction);
				}
			});
		}

		// If there are games and game plays, then just generate 2 TickerItems
		else
			getNewTickerItem(0);
		getNewTickerItem(1);
	}

	// Randomly generates a new TickerItem under certain constraints
	private void getNewTickerItem(final int position) {
		int count = 0;

		// Annoying loop used in case loading of a TickerItem fails
		// Usually caused by the BGG Hot List not being loaded,
		// Occasionally because of duplicate TickerItems in the queue
		do {
			int r = new Random().nextInt(THRESHOLD + 3);
			if (r == 0 &&
					((MyApp) getContext().getApplicationContext()).isConnectedToInternet() &&
					DataManager.getInstance((Application) getContext().getApplicationContext()).getAllHotnessItems() !=
							null)
				tickerItems[position] = new BGGTickerItem(getContext());
			else if (r <= 2 &&
					DataManager.getInstance((Application) getContext().getApplicationContext()).getAllPlayers().size() >
							1) {
				List<String> players =
						new ArrayList<>(DataManager.getInstance((Application) getContext().getApplicationContext())
												   .getAllPlayers());
				players.remove("master_user");
				tickerItems[position] =
						new PlayerTickerItem(getContext(), players.get(new Random().nextInt(players.size())));
			} else if (StatisticsManager.getInstance(getContext()).getNumberGamesPlayed() > 0)
				tickerItems[position] = new LocalTickerItem(getContext());
		} while (++count <= 10 &&
				(tickerItems[position] == null || previousTickerItems.contains(tickerItems[position].getID())));

		// If previous loop failed, then pause ticker
		if (count >= 10) failedToLoad = true;

			// Ugly if/elses depending on what type of TickerItem was generated.
			// I should clean this up...but I'm lazy
		else {
			previousTickerItems.add(tickerItems[position].getID());
			if (previousTickerItems.size() >= THRESHOLD) previousTickerItems.poll();

			if (tickerItems[position] instanceof LocalTickerItem &&
					StringUtilities.isRPG(((LocalTickerItem) tickerItems[position]).getGameType()))
				tickerItemViews[position].setImageScaleType(ImageView.ScaleType.FIT_CENTER);
			else if (tickerItems[position] instanceof PlayerTickerItem)
				tickerItemViews[position].setImageScaleType(ImageView.ScaleType.FIT_CENTER);
			else
				tickerItemViews[position].setImageScaleType(ImageView.ScaleType.CENTER_CROP);

			tickerItemViews[position].setBlurb(tickerItems[position].getBlurb());
			tickerItemViews[position].setImage(tickerItems[position].getImage());
			if (tickerItems[position] instanceof BGGTickerItem)
				tickerItemViews[position].setOnImageClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						UrlUtilities.openWebPage(getContext(),
												 "https://www.boardgamegeek.com/boardgame/" +
														 ((BGGTickerItem) tickerItems[position]).getId());
					}
				});
			else if (tickerItems[position] instanceof PlayerTickerItem)
				tickerItemViews[position].setOnImageClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ActivityUtilities.openActivity((Activity) getContext(),
													   new Intent(getContext(), PlayerStatsActivity.class).putExtra(
															   "NAME",
															   tickerItems[position].getID()),
													   "UP");
						ActivityUtilities.exitUp((Activity) getContext());
					}
				});
			else if (tickerItems[position] instanceof LocalTickerItem)
				tickerItemViews[position].setOnImageClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String game = tickerItems[position].getID();
						String gameType = ((LocalTickerItem) tickerItems[position]).getGameType();

						String thumbnailUrl = "";
						GamesDbHelper dbHelper = new GamesDbHelper(getContext());
						if (StringUtilities.isBoardGame(gameType))
							thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, game);
						else if (StringUtilities.isRPG(gameType))
							thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, game);
						else if (StringUtilities.isVideoGame(gameType))
							thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, game);
						dbHelper.close();
						ActivityUtilities.generatePaletteAndOpenActivity((Activity) getContext(),
																		 new Intent(getContext(),
																					GameStatsActivity.class)
																				 .putExtra("GAME", game)
																				 .putExtra("TYPE", gameType),
																		 "http://" + thumbnailUrl,
																		 "UP");
						ActivityUtilities.exitUp((Activity) getContext());
					}
				});
		}
	}
}
