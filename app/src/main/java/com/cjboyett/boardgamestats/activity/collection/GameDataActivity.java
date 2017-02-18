package com.cjboyett.boardgamestats.activity.collection;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.addgame.AddGameActivity;
import com.cjboyett.boardgamestats.activity.addgameplay.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.activity.base.BaseAdActivity;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.GameDownloadUtilities;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.GameExtrasAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

@SuppressWarnings("ResourceType")
public class GameDataActivity extends BaseAdActivity implements GameDataView {
	private Activity activity = this;

	private GamesDbHelper dbHelper;
	private View view;

	@BindView(R.id.dummyview)
	protected View dummyView;

	@BindView(R.id.scrollview_description)
	ScrollView scrollView;

	@BindView(R.id.linearlayout_button_bar)
	protected LinearLayout buttonBar;

	@BindView(R.id.textview_submit)
	protected TextView doneButton;

	@BindView(R.id.textview_game_description)
	protected TextView descriptionTextView;

	@BindView(R.id.listview_game_extras)
	protected ListView extrasListView;

	@BindView(R.id.edittext_game_name)
	protected EditText nameEditText;

	@BindView(R.id.edittext_game_description)
	protected EditText descriptionEditText;

	@BindView(R.id.imageview_thumbnail)
	protected ImageView thumbnail;

	@BindView(R.id.floating_menu)
	protected FloatingActionMenu fabMenu;

	@BindView(R.id.fab_add)
	protected FloatingActionButton addFab;

	@BindView(R.id.fab_delete)
	protected FloatingActionButton deleteFab;

	@BindView(R.id.fab_edit)
	protected FloatingActionButton editFab;

	@BindView(R.id.fab_bgg)
	protected FloatingActionButton syncFab;

	private String gameName, gameType;
	private Game game;

	private int backgroundColor, foregroundColor, hintTextColor;

	private AnimatorSet setRightOut, setLeftIn;
	private boolean editing;

	private GestureDetectorCompat gestureDetector;

	public GameDataActivity() {
		super("ca-app-pub-1437859753538305/9571180678");
	}


	// Lifecycle methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_game_data, null);
		setContentView(view);
		ButterKnife.bind(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		gameName = getIntent().getStringExtra("GAME");
		gameType = getIntent().getStringExtra("TYPE");
		dbHelper = new GamesDbHelper(this);
		if (StringUtilities.isBoardGame(gameType)) game = BoardGameDbUtility.getBoardGame(dbHelper, gameName);
		else if (StringUtilities.isRPG(gameType)) game = RPGDbUtility.getRPG(dbHelper, gameName);
		else if (StringUtilities.isVideoGame(gameType)) game = VideoGameDbUtility.getVideoGame(dbHelper, gameName);
		else game = new BoardGame("Farts");

		extrasListView.setAdapter(new GameExtrasAdapter(this, game));

		gestureDetector = new GestureDetectorCompat(this, new ScrollGestureListener());

		try {
			setRightOut =
					(AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.anim.card_flip_right_out);
			setLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.anim.card_flip_left_in);
		} catch (Exception e) {
			// Why do we catch here?
		}

		generateLayout();
		setColors();
		colorComponents();

		dummyView.requestFocus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		dbHelper = new GamesDbHelper(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		dbHelper.close();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbHelper != null) dbHelper.close();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ActivityUtilities.exitRight(this);
	}


	// BaseActivity methods

	@Override
	protected void setColors() {
		if (Preferences.generatePalette(this)) {
			foregroundColor = Preferences.getGeneratedForegroundColor(this);
			backgroundColor = Preferences.getGeneratedBackgroundColor(this);
			hintTextColor = Preferences.getHintTextColor(this);
		} else {
			foregroundColor = Preferences.getForegroundColor(this);
			backgroundColor = Preferences.getBackgroundColor(this);
			hintTextColor = Preferences.getHintTextColor(this);
		}
	}

	protected void colorComponents() {
		nameEditText.setTextColor(foregroundColor);
		nameEditText.setHintTextColor(hintTextColor);

		descriptionEditText.setTextColor(foregroundColor);
		descriptionEditText.setHintTextColor(hintTextColor);

		descriptionTextView.setTextColor(foregroundColor);
		descriptionTextView.setBackgroundColor(backgroundColor);

		view.setBackgroundColor(backgroundColor);

		doneButton.setBackgroundColor(backgroundColor);
		doneButton.setTextColor(foregroundColor);

		((TextView) view.findViewById(R.id.textview_description_label)).setTextColor(foregroundColor);
		((TextView) view.findViewById(R.id.textview_extras_label)).setTextColor(foregroundColor);

		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.relativelayout_header), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.listview_game_extras), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.relativelayout_description), foregroundColor);
	}

	protected void generateLayout() {
		String gameString = game.getName();
		if (game.getYearPublished() > 0) gameString += " (" + game.getYearPublished() + ")";

		nameEditText.setText(gameString);
		descriptionEditText.setText(game.getDescription());
		descriptionTextView.setText(game.getDescription() + "\n\n\n");

		try {
			Bitmap thumbnailBitmap = getIntent().getParcelableExtra("BITMAP");
			if (thumbnailBitmap != null) {
				thumbnail.setImageBitmap(thumbnailBitmap);
			} else {
				thumbnail.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			thumbnail.setVisibility(View.GONE);
		}

//		final TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, buttonBar.getTop(), buttonBar.getBottom());
//		buttonBar.startAnimation(translateAnimation);
	}


	// Interface methods


	// Binding methods

	@OnClick(R.id.fab_add)
	protected void addGamePlay() {
		fabMenu.close(true);
		TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
		tempDataManager.initialize();
		startActivity(new Intent(view.getContext(), AddGamePlayTabbedActivity.class)
							  .putExtra("GAME", gameName)
							  .putExtra("TYPE", gameType)
							  .putExtra("ID", -1L)
							  .putExtra("EXIT", "UP"));
		ActivityUtilities.exitDown(activity);
	}

	@OnClick(R.id.fab_delete)
	protected void deleteGame() {
		fabMenu.close(true);
		final View finalView = view;
		AlertDialog dialog = new ViewUtilities.DialogBuilder(view.getContext())
				.setTitle("Delete Game")
				.setMessage(
						"Are you sure you want to delete this game?  This will remove all data associated with it.  This action cannot be undone.")
				.setPositiveButton("Delete", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String thumbnailUrl = "http://" + game.getThumbnailUrl();
						new ImageController(finalView.getContext())
								.setDirectoryName("thumbnails")
								.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
								.delete();
						switch (gameType) {
							case "b":
								BoardGameDbUtility.deleteBoardGame(dbHelper,
																   new BoardGame(getIntent().getStringExtra(
																		   "GAME")));
								break;
							case "r":
								RPGDbUtility.deleteRPG(dbHelper,
													   new RolePlayingGame(getIntent().getStringExtra("GAME")));
								break;
							case "v":
								VideoGameDbUtility.deleteVideoGame(dbHelper,
																   new VideoGame(getIntent().getStringExtra(
																		   "GAME")));
								break;
						}
						ActivityUtilities.setDatabaseChanged(activity, true);
						startActivity(new Intent(finalView.getContext(), GameListActivity.class)
											  .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
						ActivityUtilities.exitRight(activity);
					}
				})
				.setNegativeButton("Cancel", null)
				.create();
		dialog.show();
	}

	@OnClick(R.id.fab_edit)
	protected void editGame() {
		fabMenu.close(true);
		if (!editing) {
			editing = true;
			flipView(scrollView, descriptionEditText);

			nameEditText.setText(game.getName());
			nameEditText.setEnabled(true);
			descriptionEditText.setEnabled(true);

			nameEditText.setHint("Name");
			descriptionEditText.setHint("Description");
		}
	}

	@OnClick(R.id.fab_bgg)
	protected void syncWithBgg() {
		long id = 0;
		switch (gameType) {
			case "b":
				id = BoardGameDbUtility.getGameId(dbHelper, gameName);
				break;
			case "r":
				id = RPGDbUtility.getGameId(dbHelper, gameName);
				break;
			case "v":
				id = VideoGameDbUtility.getGameId(dbHelper, gameName);
				break;
		}

		if (id > 0) {
			syncWithBggWithId(id, gameType);
		} else {
			searchBgg(gameName, gameType);
		}
	}

	@OnClick(R.id.textview_submit)
	protected void updateGame() {
		if (editing) {
			editing = false;
			flipView(descriptionEditText, scrollView);

			nameEditText.setEnabled(false);
			descriptionEditText.setEnabled(false);

			nameEditText.setHint("");
			descriptionEditText.setHint("");

			String oldGameName = game.getName();

			game.setName(nameEditText.getText()
									 .toString());
			game.setDescription(descriptionEditText.getText()
												   .toString());

			try {
				boolean success = false;
				switch (gameType) {
					case "b":
						success = BoardGameDbUtility.updateBoardGame(dbHelper, oldGameName, (BoardGame) game);
						break;
					case "r":
						success = RPGDbUtility.updateRPG(dbHelper, oldGameName, (RolePlayingGame) game);
						break;
					case "v":
						success = VideoGameDbUtility.updateVideoGame(dbHelper, oldGameName, (VideoGame) game);
						break;
				}
				if (success) {
					ActivityUtilities.setDatabaseChanged(activity, true);

					String gameString = game.getName();
					if (game.getYearPublished() > 0)
						gameString += " (" + game.getYearPublished() + ")";

					nameEditText.setText(gameString);
					descriptionTextView.setText(game.getDescription() + "\n\n\n");
				} else showError("Could not update game.");
			} catch (Exception e) {
				e.printStackTrace();
			}

			dummyView.requestFocus();
		}
	}

	@OnTouch(R.id.scrollview_description)
	protected boolean touchScrollView(MotionEvent event) {
		return Preferences.useSwipes(activity) && gestureDetector.onTouchEvent(event);
	}


	// Private methods

	private void flipView(final View fromView, final View toView) {
		try {
			setRightOut.setTarget(fromView);
			setLeftIn.setTarget(toView);

			setRightOut.removeAllListeners();

			// Flip views then reveal button bar
			if (editing) {
				final TranslateAnimation translateAnimation =
						new TranslateAnimation(0, 0, buttonBar.getBottom(), buttonBar.getTop());
				translateAnimation.setDuration(300);
				translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

				setRightOut.addListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						extrasListView.setVisibility(View.GONE);
						toView.setAlpha(0f);
						toView.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						fromView.setVisibility(View.GONE);
						buttonBar.setVisibility(View.VISIBLE);
						buttonBar.startAnimation(translateAnimation);
						descriptionEditText.requestFocus();
					}

					@Override
					public void onAnimationCancel(Animator animation) {

					}

					@Override
					public void onAnimationRepeat(Animator animation) {

					}
				});
				setRightOut.start();
				setLeftIn.start();
			}
			// Remove button bar then flip views
			else {
				final TranslateAnimation translateAnimation =
						new TranslateAnimation(0, 0, buttonBar.getTop(), buttonBar.getBottom());
				translateAnimation.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						extrasListView.setVisibility(View.VISIBLE);
						buttonBar.setVisibility(View.GONE);
						setRightOut.start();
						setLeftIn.start();
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}
				});
				translateAnimation.setDuration(300);
				translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

				setRightOut.addListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						toView.setAlpha(0f);
						toView.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						fromView.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationCancel(Animator animation) {

					}

					@Override
					public void onAnimationRepeat(Animator animation) {

					}
				});

				buttonBar.startAnimation(translateAnimation);
			}
		} catch (Exception e) {
			fromView.setVisibility(View.GONE);
			toView.setVisibility(View.VISIBLE);
			buttonBar.setVisibility(editing ? View.VISIBLE : View.GONE);
		}
	}

	private void syncWithBggWithId(final long id, final String gameType) {
		fabMenu.close(true);

		Game newGame = null;
		switch (gameType) {
			case "b":
				try {
					newGame = new GameDownloadUtilities.DownloadXmlGameTask(Game.GameType.BOARD)
							.execute("https://www.boardgamegeek.com/xmlapi2/thing?id=" + id)
							.get();
					if (newGame != null) {
						if (!BoardGameDbUtility.updateBoardGame(dbHelper, game.getName(),
																(BoardGame) newGame))
							showError("Could not sync with Board Game Geek");
						else {
							String thumbnailUrl = newGame.getThumbnailUrl();
							GameDownloadUtilities.downloadThumbnail("http://" + thumbnailUrl, activity);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					showError("Could not sync with Board Game Geek");
				}
				break;
			case "r":
				try {
					newGame = new GameDownloadUtilities.DownloadXmlGameTask(Game.GameType.RPG)
							.execute("https://www.boardgamegeek.com/xmlapi2/family?id=" + id)
							.get();
					if (newGame != null) {
						if (!RPGDbUtility.updateRPG(dbHelper, game.getName(),
													(RolePlayingGame) newGame))
							showError("Could not sync with Board Game Geek");
						else {
							String thumbnailUrl = newGame.getThumbnailUrl();
							GameDownloadUtilities.downloadThumbnail("http://" + thumbnailUrl, activity);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					showError("Could not sync with Board Game Geek");
				}
				break;
			case "v":
				try {
					newGame = new GameDownloadUtilities.DownloadXmlGameTask(Game.GameType.VIDEO)
							.execute("https://www.boardgamegeek.com/xmlapi2/thing?id=" + id)
							.get();
					if (newGame != null) {
						if (!VideoGameDbUtility.updateVideoGame(dbHelper, game.getName(),
																(VideoGame) newGame))
							showError("Could not sync with Board Game Geek");
						else {
							String thumbnailUrl = newGame.getThumbnailUrl();
							GameDownloadUtilities.downloadThumbnail("http://" + thumbnailUrl, activity);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					showError("Could not sync with Board Game Geek");
				}
				break;
		}

		if (editing) {
			editing = false;
			flipView(descriptionEditText, scrollView);

			nameEditText.setEnabled(false);
			descriptionEditText.setEnabled(false);

			nameEditText.setHint("");
			descriptionEditText.setHint("");
		}

		ActivityUtilities.setDatabaseChanged(activity, true);

		if (newGame != null) game = newGame;

		String gameString = game.getName();
		if (game.getYearPublished() > 0)
			gameString += " (" + game.getYearPublished() + ")";

		nameEditText.setText(gameString);
		descriptionTextView.setText(game.getDescription() + "\n\n\n");

		if (newGame != null) {
			AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Success")
					.setMessage("I have successfully updated the information of " + newGame.getName() + ".")
					.withYancey(true)
					.setPositiveButton("Close", null)
					.create();
			alertDialog.show();
		}
	}

	private void searchBgg(final String gameName, final String gameType) {
		AlertDialog alertDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Sync with BGG")
				.setMessage("In order to sync the data of this game I need you to search for it again.")
				.withYancey(true)
				.setPositiveButton("Search", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivityForResult(
								new Intent(view.getContext(), AddGameActivity.class)
										.putExtra("GAME", gameName)
										.putExtra("TYPE", gameType)
										.putExtra("SYNC", true),
								100);
						ActivityUtilities.exitUp(activity);
					}
				})
				.setNegativeButton("Cancel", null)
				.create();
		alertDialog.show();
	}

	private void showError(String errorMessage) {
		AlertDialog errorDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Error")
				.setMessage(errorMessage)
				.setPositiveButton("Close", null)
				.create();
		errorDialog.show();
	}

	@Override
	public Activity getActivity() {
		return activity;
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			if (fabMenu.isOpened()) fabMenu.close(true);
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.d("FLING", "fling");
			if (Math.abs(velocityX) > Math.abs(velocityY)) {
				if (Math.abs(e1.getX() - e2.getX()) >= 200) {
					if (velocityX > 2000) {
						onBackPressed();
						return true;
					}
				}
			}
			return false;
		}
	}
}
