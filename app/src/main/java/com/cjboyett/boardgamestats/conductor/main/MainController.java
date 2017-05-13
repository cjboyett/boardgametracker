package com.cjboyett.boardgamestats.conductor.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.SettingsActivity;
import com.cjboyett.boardgamestats.activity.addgame.AddGameActivity;
import com.cjboyett.boardgamestats.activity.extras.AchievementsController;
import com.cjboyett.boardgamestats.activity.statsoverview.StatsTabbedActivity;
import com.cjboyett.boardgamestats.conductor.ConductorActivity;
import com.cjboyett.boardgamestats.conductor.addgameplay.AddGamePlayTabbedController;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.conductor.changehandlers.DirectionalChangeHandler;
import com.cjboyett.boardgamestats.conductor.collection.GameListController;
import com.cjboyett.boardgamestats.conductor.extras.ExtrasController;
import com.cjboyett.boardgamestats.conductor.extras.LoginController;
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

public class MainController extends BaseController implements MainView {
	private View view;

	@BindView(R.id.ticker)
	Ticker ticker;

	@BindView(R.id.textview_welcome_back)
	TextView welcomeBackTextView;

	@BindView(R.id.textview_add_game_play)
	AppCompatButton addGamePlayButton;

	@BindView(R.id.textview_add_game)
	AppCompatButton collectionsButton;

	@BindView(R.id.textview_stats)
	AppCompatButton statsButton;

	@BindView(R.id.textview_extras)
	AppCompatButton extrasButton;

	@BindView(R.id.imageview_settings)
	AppCompatImageView settingsButton;

	@BindView(R.id.imageview_help)
	AppCompatImageView helpButton;

	@BindView(R.id.imageview_achievements)
	AppCompatImageView achievementButton;

	private MainPresenter presenter;

	private GestureDetectorCompat gestureDetector;

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.main_controller, container, false);
		ButterKnife.bind(this, view);
		presenter = new MainPresenter();
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		presenter.attachView(this);
		setColors();
		colorComponents();
		presenter.initializeView();
		gestureDetector = new GestureDetectorCompat(getActivity(), new ScrollGestureListener());
		((ConductorActivity) getActivity()).setGestureDetector(gestureDetector);
	}

	@Override
	protected void onDetach(@NonNull View view) {
		pauseTicker();
		((ConductorActivity) getActivity()).removeGestureDetector();
		presenter.detachView();
		super.onDetach(view);
	}

	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);
		ViewUtilities.tintButtonBackground(collectionsButton, buttonColor);
		ViewUtilities.tintButtonBackground(statsButton, buttonColor);
		ViewUtilities.tintButtonBackground(extrasButton, buttonColor);
		ViewUtilities.tintButtonBackground(addGamePlayButton, buttonColor);

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

	public void setWelcomeMessage(String welcomeMessage) {
		welcomeBackTextView.setText(welcomeMessage);
	}

	public void processFirstVisit() {
		Preferences.setShowPopup(getActivity(), false);
		Preferences.setMetYancey(getActivity(), true);
		ViewUtilities.DialogBuilder syncDialogBuilder = new ViewUtilities.DialogBuilder(getActivity());
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

		final AlertDialog addToCollectionDialog = new ViewUtilities.DialogBuilder(getActivity())
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

		final ViewUtilities.DialogBuilder firstVisitDialog = new ViewUtilities.DialogBuilder(getActivity());
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

		getRouter().pushController(RouterTransaction.with(new AddGamePlayTabbedController())
													.pushChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.TOP))
													.popChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.TOP)));

//		startActivity(new Intent(getActivity(), AddGamePlayTabbedActivity.class).putExtra("EXIT", "UP"));
//		ActivityUtilities.exitDown(getActivity());
	}

	public void openCollections() {
		getRouter().pushController(RouterTransaction.with(new GameListController())
													.pushChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.RIGHT))
													.popChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.RIGHT)));
	}

	public void openStatsOverview() {
		startActivity(new Intent(getActivity(), StatsTabbedActivity.class));
		ActivityUtilities.exitUp(getActivity());
	}

	public void openLogin() {
		getRouter().pushController(RouterTransaction.with(new LoginController())
													.pushChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.LEFT))
													.popChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.LEFT)));
	}

	public void openExtras() {
		getRouter().pushController(RouterTransaction.with(new ExtrasController())
													.pushChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.LEFT))
													.popChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.LEFT)));
	}

	public void openSettings() {
		startActivity(new Intent(getActivity(), SettingsActivity.class));
	}

	public void openAchievements() {
		getRouter().pushController(RouterTransaction.with(new AchievementsController())
													.pushChangeHandler(new FadeChangeHandler(500))
													.popChangeHandler(new FadeChangeHandler(500)));
	}

	public void openHelp() {
		AlertDialog alertDialog = new ViewUtilities.DialogBuilder(getActivity())
				.setTitle("Help")
				.setMessage("This is where help will be placed in the future.")
				.setPositiveButton("Okay", null)
				.create();
		alertDialog.show();
	}


	// OnClick methods

	@OnClick(R.id.textview_add_game_play)
	void processAddGamePlay() {
		presenter.processAddGamePlay();
	}

	@OnClick(R.id.textview_add_game)
	void processCollections() {
		presenter.processCollections();
	}

	@OnClick(R.id.textview_stats)
	void processStatsOverview() {
		presenter.processStatsOverview();
	}

	@OnClick(R.id.textview_extras)
	void processExtras() {
		presenter.processExtras();
	}

	@OnClick(R.id.imageview_settings)
	void processSettings() {
		presenter.processSettings();
	}

	@OnClick(R.id.imageview_achievements)
	void processAchievements() {
		presenter.processAchievements();
	}

	@OnClick(R.id.imageview_help)
	void processHelp() {
		presenter.processHelp();
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (Math.abs(velocityX) > Math.abs(velocityY)) {
				if (Math.abs(e1.getX() - e2.getX()) >= 200) {
					if (velocityX < -2000) {
						processCollections();
						return true;
					} else if (velocityX > 2000) {
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
}
