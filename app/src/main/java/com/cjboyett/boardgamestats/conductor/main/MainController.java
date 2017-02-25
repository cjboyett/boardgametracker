package com.cjboyett.boardgamestats.conductor.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.SettingsActivity;
import com.cjboyett.boardgamestats.activity.addgame.AddGameActivity;
import com.cjboyett.boardgamestats.activity.addgameplay.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.activity.collection.GameListActivity;
import com.cjboyett.boardgamestats.activity.extras.AchievementsActivity;
import com.cjboyett.boardgamestats.activity.extras.ExtrasActivity;
import com.cjboyett.boardgamestats.activity.extras.LoginActivity;
import com.cjboyett.boardgamestats.activity.statsoverview.StatsTabbedActivity;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
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

	private MainPresenter presenter;

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
	}

	@Override
	protected void onDetach(@NonNull View view) {
		pauseTicker();
		presenter.detachView();
		super.onDetach(view);
	}

	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);
		if (Preferences.lightUI(getActivity())) {
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
			addGamePlayButton.setBackgroundResource(R.drawable.main_button_background_light);
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
		startActivity(new Intent(getActivity(), AddGamePlayTabbedActivity.class).putExtra("EXIT", "UP"));
		ActivityUtilities.exitDown(getActivity());
	}

	public void openCollections() {
		startActivity(new Intent(getActivity(), GameListActivity.class));
		ActivityUtilities.exitLeft(getActivity());
	}

	public void openStatsOverview() {
		startActivity(new Intent(getActivity(), StatsTabbedActivity.class));
		ActivityUtilities.exitUp(getActivity());
	}

	public void openLogin() {
		startActivity(new Intent(getActivity(), LoginActivity.class));
		ActivityUtilities.exitRight(getActivity());
	}

	public void openExtras() {
		startActivity(new Intent(getActivity(), ExtrasActivity.class));
		ActivityUtilities.exitRight(getActivity());
	}

	public void openSettings() {
		startActivity(new Intent(getActivity(), SettingsActivity.class));
	}

	public void openAchievements() {
		startActivity(new Intent(getActivity(), AchievementsActivity.class));
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
}
