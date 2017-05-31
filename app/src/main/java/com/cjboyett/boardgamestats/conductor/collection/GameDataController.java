package com.cjboyett.boardgamestats.conductor.collection;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.board.SimpleGame;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.GameDownloadUtilities;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ColorUtilities;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.GameExtrasAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

@SuppressWarnings("ResourceType")
public class GameDataController extends BaseController implements GameDataView {

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

	private String gameName, gameType, thumbnailUrl;
	private Game game;

	private int backgroundColor, foregroundColor, hintTextColor;

	private AnimatorSet setRightOut, setLeftIn;
	private boolean editing;

/*
	public GameDataActivity() {
		super("ca-app-pub-1437859753538305/9571180678");
	}
*/

	public GameDataController() {
	}

	public GameDataController(String gameName, String gameType, String thumbnailUrl) {
		this.gameName = gameName;
		this.gameType = gameType;
		this.thumbnailUrl = thumbnailUrl;
	}

	// Lifecycle methods

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.activity_game_data, container, false);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		ButterKnife.bind(this, view);

		dbHelper = new GamesDbHelper(getActivity());
		if (StringUtilities.isBoardGame(gameType)) game = BoardGameDbUtility.getBoardGame(dbHelper, gameName);
		else if (StringUtilities.isRPG(gameType)) game = RPGDbUtility.getRPG(dbHelper, gameName);
		else if (StringUtilities.isVideoGame(gameType)) game = VideoGameDbUtility.getVideoGame(dbHelper, gameName);
		else game = new BoardGame("Farts");

		extrasListView.setAdapter(new GameExtrasAdapter(getActivity(), game));

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

		getToolbar().setTitleTextColor(foregroundColor);
		getToolbar().setBackgroundColor(Preferences.generatePalette(getActivity()) ? ColorUtilities.darken(
				backgroundColor) : buttonColor);

		dummyView.requestFocus();
	}

	@Override
	protected void onDetach(@NonNull View view) {
		if (dbHelper != null) dbHelper.close();
		getToolbarImage().setImageBitmap(null);
		super.onDetach(view);
	}


	// BaseActivity methods

	@Override
	protected void setColors() {
		if (Preferences.generatePalette(getActivity())) {
			foregroundColor = Preferences.getGeneratedForegroundColor(getActivity());
			backgroundColor = Preferences.getGeneratedBackgroundColor(getActivity());
			hintTextColor = Preferences.getHintTextColor(getActivity());
		} else {
			foregroundColor = Preferences.getForegroundColor(getActivity());
			backgroundColor = Preferences.getBackgroundColor(getActivity());
			hintTextColor = Preferences.getHintTextColor(getActivity());
		}
		buttonColor = Preferences.getButtonColor(getActivity());
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
		getToolbar().setTitle(game.getName());

		if (game.getYearPublished() > 0) gameString += " (" + game.getYearPublished() + ")";

		nameEditText.setText(gameString);
		descriptionEditText.setText(game.getDescription());
		descriptionTextView.setText(game.getDescription());

		if (!TextUtils.isEmpty(thumbnailUrl)) {
			final Bitmap thumbnailBitmap = new ImageController(getActivity())
					.setDirectoryName("thumbnails")
					.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
					.load();
			if (thumbnailBitmap != null) {
				thumbnail.setImageBitmap(thumbnailBitmap);
				//getToolbarImage().setImageBitmap(thumbnailBitmap);
				getToolbar().setTitleTextColor(foregroundColor);
//				getCollapsingToolbarLayout().setTitleEnabled(true);
//				getCollapsingToolbarLayout().setTitle(game.getName());
			} else {
				thumbnail.setVisibility(View.GONE);
			}
		}

//		final TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, buttonBar.getTop(), buttonBar.getBottom());
//		buttonBar.startAnimation(translateAnimation);
	}


	// Interface methods


	// Binding methods

	@OnClick(R.id.fab_add)
	protected void addGamePlay() {
		fabMenu.close(true);
		TempDataManager tempDataManager = TempDataManager.getInstance(getActivity().getApplication());
		tempDataManager.initialize();
		startActivity(new Intent(view.getContext(), AddGamePlayTabbedActivity.class)
							  .putExtra("GAME", gameName)
							  .putExtra("TYPE", gameType)
							  .putExtra("ID", -1L)
							  .putExtra("EXIT", "UP"));
		ActivityUtilities.exitDown(getActivity());
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
																   new BoardGame(gameName));
								break;
							case "r":
								RPGDbUtility.deleteRPG(dbHelper,
													   new RolePlayingGame(gameName));
								break;
							case "v":
								VideoGameDbUtility.deleteVideoGame(dbHelper,
																   new VideoGame(gameName));
								break;
						}
						ActivityUtilities.setDatabaseChanged(getActivity(), true);
						getRouter().popCurrentController();
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
					ActivityUtilities.setDatabaseChanged(getActivity(), true);

					String gameString = game.getName();
					if (game.getYearPublished() > 0)
						gameString += " (" + game.getYearPublished() + ")";

					nameEditText.setText(gameString);
					descriptionTextView.setText(game.getDescription());
				} else showError("Could not update game.");
			} catch (Exception e) {
				Timber.e(e);
			}

			dummyView.requestFocus();
		}
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
							GameDownloadUtilities.downloadThumbnail(thumbnailUrl, getActivity());
						}
					}
				} catch (Exception e) {
					Timber.e(e);
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
							GameDownloadUtilities.downloadThumbnail(thumbnailUrl, getActivity());
						}
					}
				} catch (Exception e) {
					Timber.e(e);
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
							GameDownloadUtilities.downloadThumbnail(thumbnailUrl, getActivity());
						}
					}
				} catch (Exception e) {
					Timber.e(e);
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

		ActivityUtilities.setDatabaseChanged(getActivity(), true);

		if (newGame != null) game = newGame;

		String gameString = game.getName();
		if (game.getYearPublished() > 0)
			gameString += " (" + game.getYearPublished() + ")";

		nameEditText.setText(gameString);
		descriptionTextView.setText(game.getDescription());

		if (newGame != null) {
			AlertDialog alertDialog = new ViewUtilities.DialogBuilder(getActivity())
					.setTitle("Success")
					.setMessage("I have successfully updated the information of " + newGame.getName() + ".")
					.withYancey(true)
					.setPositiveButton("Close", null)
					.create();
			alertDialog.show();
		}
	}

	private void searchBgg(final String gameName, final String gameType) {
		AlertDialog alertDialog = new ViewUtilities.DialogBuilder(getActivity())
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
						ActivityUtilities.exitUp(getActivity());
					}
				})
				.setNegativeButton("Cancel", null)
				.create();
		alertDialog.show();
	}

	private void showError(String errorMessage) {
		AlertDialog errorDialog = new ViewUtilities.DialogBuilder(getActivity())
				.setTitle("Error")
				.setMessage(errorMessage)
				.setPositiveButton("Close", null)
				.create();
		errorDialog.show();
	}

	@Override
	public void populateView(SimpleGame simpleGame) {

	}

	@Override
	public void populateThumbnail(Bitmap thumbnail) {

	}

	@Override
	public void populateToolbarImage(Bitmap image) {

	}
}
