package com.cjboyett.boardgamestats.activity.main;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.SettingsActivity;
import com.cjboyett.boardgamestats.activity.addgame.AddGameActivity;
import com.cjboyett.boardgamestats.activity.addgameplay.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.activity.base.BaseActivity;
import com.cjboyett.boardgamestats.activity.collection.GameListActivity;
import com.cjboyett.boardgamestats.activity.extras.AchievementsActivity;
import com.cjboyett.boardgamestats.activity.extras.ExtrasActivity;
import com.cjboyett.boardgamestats.activity.extras.LoginActivity;
import com.cjboyett.boardgamestats.activity.statsoverview.StatsTabbedActivity;
import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.GameDownloadUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.ticker.Ticker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements MainView, GestureDetector.OnGestureListener {
	private View view;

	@BindView(R.id.ticker)
	protected Ticker ticker;

	@BindView(R.id.textview_welcome_back)
	protected TextView welcomeBackTextView;

	@BindView(R.id.textview_add_game_play)
	protected TextView addGamePlayButton;

	@BindView(R.id.textview_add_game)
	protected TextView collectionsButton;

	@BindView(R.id.textview_stats)
	protected TextView statsButton;

	@BindView(R.id.textview_extras)
	protected TextView extrasButton;

	@BindView(R.id.imageview_settings)
	protected AppCompatImageView settingsButton;

	@BindView(R.id.imageview_help)
	protected AppCompatImageView helpButton;

	@BindView(R.id.imageview_achievements)
	protected AppCompatImageView achievementButton;
	private boolean animatingButton;

	private GestureDetectorCompat gestureDetector;

	private MainPresenter presenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		view = getLayoutInflater().inflate(R.layout.activity_main, null);
		setContentView(view);
		ButterKnife.bind(this);

		presenter = new MainPresenter();
		presenter.attachView(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}

		generateLayout();

		gestureDetector = new GestureDetectorCompat(this, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setColors();
		colorComponents();

		presenter.initializeView();

		if (!animatingButton) {
			animatingButton = true;

//			animateAchievementButton();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		pauseTicker();
	}

	@Override
	protected void onDestroy() {
		presenter.detachView();
		super.onDestroy();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Preferences.useSwipes(this)) gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}


	// BaseActivity methods

	@Override
	protected void generateLayout() {
	}

	@Override
	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);
		if (Preferences.lightUI(this)) {
			collectionsButton.setBackgroundResource(R.drawable.main_button_background_dark);
			statsButton.setBackgroundResource(R.drawable.main_button_background_dark);
			extrasButton.setBackgroundResource(R.drawable.main_button_background_dark);
			addGamePlayButton.setBackgroundResource(R.drawable.main_button_background_dark);
			settingsButton.setBackgroundResource(R.drawable.main_button_background_dark);
			achievementButton.setBackgroundResource(R.drawable.main_button_background_dark);
			helpButton.setBackgroundResource(R.drawable.main_button_background_dark);
		} else {
			collectionsButton.setBackgroundResource(R.drawable.main_button_background_light);
			statsButton.setBackgroundResource(R.drawable.main_button_background_light);
			extrasButton.setBackgroundResource(R.drawable.main_button_background_light);
			collectionsButton.setBackgroundResource(R.drawable.main_button_background_light);
			settingsButton.setBackgroundResource(R.drawable.main_button_background_light);
			achievementButton.setBackgroundResource(R.drawable.main_button_background_light);
			helpButton.setBackgroundResource(R.drawable.main_button_background_light);
		}

		collectionsButton.setTextColor(foregroundColor);
		statsButton.setTextColor(foregroundColor);
		extrasButton.setTextColor(foregroundColor);
		addGamePlayButton.setTextColor(foregroundColor);

		welcomeBackTextView.setTextColor(foregroundColor);

		ViewUtilities.tintImageView(settingsButton, foregroundColor);
		ViewUtilities.tintImageView(achievementButton, foregroundColor);
		ViewUtilities.tintImageView(helpButton, foregroundColor);

		ticker.setColors(foregroundColor);
	}


	// Interface methods

	public Activity getActivity() {
		return this;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		welcomeBackTextView.setText(welcomeMessage);
	}

	public void processFirstVisit() {
		Preferences.setShowPopup(this, false);
		Preferences.setMetYancey(this, true);
		ViewUtilities.DialogBuilder syncDialogBuilder = new ViewUtilities.DialogBuilder(this);
		final EditText usernameEditText = syncDialogBuilder.getInput();
		final AlertDialog syncDialog = syncDialogBuilder.setTitle("Sync With BGG")
														.setMessage(
																"Would you like me to download your collection and game plays from your Board Game Geek account?  This can also be done at any time through the Settings.")
														.setHintText("BGG Username")
														.withYancey(true)
														.setPositiveButton("Okay",
																		   new View.OnClickListener() {
																			   @Override
																			   public void onClick(View v) {
																				   String username =
																						   usernameEditText.getText()
																										   .toString();
																				   GameDownloadUtilities.syncWithBgg(
																						   v.getContext(),
																						   username);
																			   }
																		   })
														.setNegativeButton("Cancel", null)
														.create();

		final AlertDialog addToCollectionDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Add a New Game?")
				.setMessage("If you have a Board Game Geek account I can download your game information.\n\n" +
									"Or you can manually add a new game.")
				.withYancey(true)
				.setPositiveButton("Sync", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						syncDialog.show();
					}
				})
				.setNeutralButton("Add a Game", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(v.getContext(), AddGameActivity.class));
						ActivityUtilities.exitDown(getActivity());
					}
				})
				.setNegativeButton("Later", null)
				.create();

		final ViewUtilities.DialogBuilder firstVisitDialog = new ViewUtilities.DialogBuilder(this);
		final EditText firstVisitInput = firstVisitDialog.getInput();
		firstVisitDialog.setTitle("Welcome!")
						.setMessage(
								"You may call me Yancey.  Would you like to tell me your name?  This will help me customize your experience.")
						.setHintText("Name")
						.withYancey(true)
						.setPositiveButton("Okay", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								String name = firstVisitInput.getText()
															 .toString();
								if (!TextUtils.isEmpty(name))
									Preferences.setUsername(v.getContext(), name);
								else
									Preferences.setUsername(v.getContext(), "User");
								Preferences.setFirstVisit(v.getContext(), false);
								addToCollectionDialog.show();
							}
						});
		firstVisitDialog.create()
						.show();
	}

	public void processNeedAllPlayerTableUpgrade() {
		new AsyncTask<String, Void, Void>() {
			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute() {
				progressDialog = new ProgressDialog(getActivity());
				progressDialog.setMessage("Updating database");
				progressDialog.setCancelable(false);
				progressDialog.show();
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				progressDialog.dismiss();
			}

			@Override
			protected Void doInBackground(String... params) {
				GamesDbHelper dbHelper = new GamesDbHelper(getActivity());
				PlayersDbUtility.populateAllPlayersTable(dbHelper);
				Preferences.setNeedAllPlayerTableUpgrade(getActivity(), false);
				dbHelper.close();

				return null;
			}
		}.execute("");
	}

	public void startTicker() {
		ticker.start();
	}

	public void pauseTicker() {
		ticker.pause();
	}

	public void stopTicker() {
		ticker.stop();
	}

	public void openAddGamePlay() {
		startActivity(new Intent(this, AddGamePlayTabbedActivity.class).putExtra("EXIT", "UP"));
		ActivityUtilities.exitDown(this);
	}

	public void openCollections() {
		startActivity(new Intent(this, GameListActivity.class));
		ActivityUtilities.exitLeft(this);
	}

	public void openStatsOverview() {
		startActivity(new Intent(this, StatsTabbedActivity.class));
		ActivityUtilities.exitUp(this);
	}

	public void openLogin() {
		startActivity(new Intent(this, LoginActivity.class));
		ActivityUtilities.exitRight(this);
	}

	public void openExtras() {
		startActivity(new Intent(this, ExtrasActivity.class));
		ActivityUtilities.exitRight(this);
	}

	public void openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void openAchievements() {
		startActivity(new Intent(this, AchievementsActivity.class));
	}

	public void openHelp() {
		AlertDialog alertDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Help")
				.setMessage("This is where help will be placed in the future.")
				.setPositiveButton("Okay", null)
				.create();
		alertDialog.show();
	}


	// OnClick methods

	@OnClick(R.id.textview_add_game_play)
	protected void processAddGamePlay() {
		presenter.processAddGamePlay();
	}

	@OnClick(R.id.textview_add_game)
	protected void processCollections() {
		presenter.processCollections();
	}

	@OnClick(R.id.textview_stats)
	protected void processStatsOverview() {
		presenter.processStatsOverview();
	}

	@OnClick(R.id.textview_extras)
	protected void processExtras() {
		presenter.processExtras();
	}

	@OnClick(R.id.imageview_settings)
	protected void processSettings() {
		presenter.processSettings();
	}

	@OnClick(R.id.imageview_achievements)
	protected void processAchievements() {
		presenter.processAchievements();
	}

	@OnClick(R.id.imageview_help)
	protected void processHelp() {
		presenter.processHelp();
	}

	private void animateAchievementButton() {
		achievementButton.animate()
						 .scaleX(1.2f)
						 .scaleY(1.2f)
						 .setDuration(1000)
						 .setListener(new Animator.AnimatorListener() {
							 @Override
							 public void onAnimationStart(Animator animation) {

							 }

							 @Override
							 public void onAnimationEnd(Animator animation) {
								 achievementButton.animate()
												  .scaleX(1f)
												  .scaleY(1f)
												  .setDuration(1000)
												  .setListener(new Animator.AnimatorListener() {
													  @Override
													  public void onAnimationStart(Animator animation) {

													  }

													  @Override
													  public void onAnimationEnd(Animator animation) {
														  animateAchievementButton();
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

							 @Override
							 public void onAnimationCancel(Animator animation) {

							 }

							 @Override
							 public void onAnimationRepeat(Animator animation) {

							 }
						 })
						 .start();
	}


	// Gesture methods

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (Math.abs(velocityX) > Math.abs(velocityY)) {
			if (Math.abs(e1.getX() - e2.getX()) >= 200) {
				if (velocityX < -2000) {
					processCollections();
					return true;
				} else if (velocityX > 2000) {
/*
					startActivity(new Intent(this, TestActivity.class)); //SettingsActivity.class));
					              ActivityUtilities.exitRight(activity);
*/
					processExtras();
					return true;
				}
			}
		} else {
			if (Math.abs(e1.getY() - e2.getY()) >= 200) {
				if (velocityY < -2000) {
					processStatsOverview();
					return true;
				} else if (velocityY > 2000) {
					processAddGamePlay();
					return true;
				}
			}
		}

		return false;
	}
}
