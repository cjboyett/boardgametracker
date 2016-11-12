package com.cjboyett.boardgamestats.activity;

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
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GameExtra;
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

@SuppressWarnings("ResourceType")
public class BoardGameDataActivity extends BaseAdActivity
{
	private Activity activity = this;

	private GamesDbHelper dbHelper;
	private Bitmap thumbnailBitmap = null;
	private View view, dummyView;
	private LinearLayout buttonBar;
	private TextView doneButton, syncButton, descriptionTextView;
	private ListView extrasListView;
	private EditText nameEditText, descriptionEditText;
	private ImageView thumbnail;

	private FloatingActionMenu fabMenu;
	private FloatingActionButton addFab, deleteFab, editFab;

	private String gameName, gameType;
	private Game game;

	private int backgroundColor, foregroundColor, hintTextColor;

	private AnimatorSet setRightOut, setLeftIn;
	private boolean editing;

	private GestureDetectorCompat gestureDetector;

	public BoardGameDataActivity()
	{
		super("ca-app-pub-1437859753538305/9571180678");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_board_game_data, null);
		setContentView(view);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		gameName = getIntent().getStringExtra("GAME");
		gameType = getIntent().getStringExtra("TYPE");
		dbHelper = new GamesDbHelper(this);
		if (StringUtilities.isBoardGame(gameType)) game = BoardGameDbUtility.getBoardGame(dbHelper, gameName);
		else if (StringUtilities.isRPG(gameType)) game = RPGDbUtility.getRPG(dbHelper, gameName);
		else if (StringUtilities.isVideoGame(gameType)) game = VideoGameDbUtility.getVideoGame(dbHelper, gameName);
		else game = new BoardGame("Farts");

		extrasListView = (ListView)view.findViewById(R.id.listview_game_extras);
		extrasListView.setAdapter(new GameExtrasAdapter(this, game));

		dummyView = findViewById(R.id.dummyview);
		buttonBar = (LinearLayout)findViewById(R.id.linearlayout_button_bar);
		doneButton = (TextView)findViewById(R.id.textview_submit);
		syncButton = (TextView)findViewById(R.id.textview_sync);

		fabMenu = (FloatingActionMenu)findViewById(R.id.floating_menu);

		addFab = (FloatingActionButton) findViewById(R.id.fab_add);
		addFab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				fabMenu.close(true);
				TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
				tempDataManager.initialize();
				startActivity(new Intent(view.getContext(), AddGamePlayTabbedActivity.class)
						.putExtra("GAME", gameName)
						.putExtra("TYPE", gameType)
						.putExtra("ID", -1l)
						.putExtra("EXIT", "UP"));
				ActivityUtilities.exitDown(activity);
			}
		});

		deleteFab = (FloatingActionButton) findViewById(R.id.fab_delete);
		deleteFab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				fabMenu.close(true);
				final View finalView = view;
				AlertDialog dialog = new ViewUtilities.DialogBuilder(view.getContext())
						.setTitle("Delete Game")
						.setMessage("Are you sure you want to delete this game?  This will remove all data associated with it.  This action cannot be undone.")
						.setPositiveButton("Delete", new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								String thumbnailUrl = "http://" + game.getThumbnailUrl();
								new ImageController(finalView.getContext())
										.setDirectoryName("thumbnails")
										.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
										.delete();
								if (gameType.equals("b"))
									BoardGameDbUtility.deleteBoardGame(dbHelper, new BoardGame(getIntent().getStringExtra("GAME")));
								else if (gameType.equals("r"))
									RPGDbUtility.deleteRPG(dbHelper, new RolePlayingGame(getIntent().getStringExtra("GAME")));
								else if (gameType.equals("v"))
									VideoGameDbUtility.deleteVideoGame(dbHelper, new VideoGame(getIntent().getStringExtra("GAME")));
								ActivityUtilities.setDatabaseChanged(activity, true);
								startActivity(new Intent(finalView.getContext(), BoardGameListActivity.class)
										              .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
								ActivityUtilities.exitRight(activity);
							}
						})
						.setNegativeButton("Cancel", null)
						.create();
				dialog.show();
			}
		});

		editFab = (FloatingActionButton)findViewById(R.id.fab_edit);
		editFab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				fabMenu.close(true);
				if (!editing)
				{
					editing = true;
					flipView(view.findViewById(R.id.scrollview_description), descriptionEditText);

					nameEditText.setText(game.getName());
					nameEditText.setEnabled(true);
					descriptionEditText.setEnabled(true);

					nameEditText.setHint("Name");
					descriptionEditText.setHint("Description");
				}
			}
		});

		findViewById(R.id.fab_bgg).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				syncWithBgg();
			}
		});

		doneButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (editing)
				{
					editing = false;
					flipView(descriptionEditText, view.findViewById(R.id.scrollview_description));

					nameEditText.setEnabled(false);
					descriptionEditText.setEnabled(false);

					nameEditText.setHint("");
					descriptionEditText.setHint("");

					String oldGameName = game.getName();

					game.setName(nameEditText.getText()
											 .toString());
					game.setDescription(descriptionEditText.getText()
														   .toString());

					try
					{
						boolean success = false;
						if (gameType.equals("b"))
							success = BoardGameDbUtility.updateBoardGame(dbHelper, oldGameName, (BoardGame) game);
						else if (gameType.equals("r"))
							success = RPGDbUtility.updateRPG(dbHelper, oldGameName, (RolePlayingGame) game);
						else if (gameType.equals("v"))
							success = VideoGameDbUtility.updateVideoGame(dbHelper, oldGameName, (VideoGame) game);
						if (success)
						{
							ActivityUtilities.setDatabaseChanged(activity, true);

							String gameString = game.getName();
							if (game.getYearPublished() > 0)
								gameString += " (" + game.getYearPublished() + ")";

							nameEditText.setText(gameString);
							descriptionTextView.setText(game.getDescription() + "\n\n\n");
						}
						else showError("Could not update game.");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					dummyView.requestFocus();
				}
			}
		});

		nameEditText = (EditText)findViewById(R.id.edittext_boardgame_name);
		descriptionEditText = (EditText)findViewById(R.id.edittext_boardgame_description);
		descriptionTextView = (TextView)findViewById(R.id.textview_boardgame_description);
		thumbnail = (ImageView)findViewById(R.id.imageview_avatar);

		gestureDetector = new GestureDetectorCompat(this, new ScrollGestureListener());

		try
		{
			setRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.anim.card_flip_right_out);
			setLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(), R.anim.card_flip_left_in);
		}
		catch (Exception e)
		{

		}

		findViewById(R.id.scrollview_description).setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (Preferences.useSwipes(activity)) return gestureDetector.onTouchEvent(event);
				return false;
			}
		});

		generateLayout();
		setColors();
		colorComponents();

		if (game instanceof BoardGame)
		{
			for (GameExtra mechanic : ((BoardGame) game).getMechanics()) Log.d("MECHANIC", mechanic.toString());
		}

		dummyView.requestFocus();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		dbHelper = new GamesDbHelper(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		dbHelper.close();
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
		super.onBackPressed();
		ActivityUtilities.exitRight(this);
	}

	@Override
	protected void setColors()
	{
		if (Preferences.generatePalette(this))
		{
			foregroundColor = Preferences.getGeneratedForegroundColor(this);
			backgroundColor = Preferences.getGeneratedBackgroundColor(this);
			hintTextColor = Preferences.getHintTextColor(this);
		}
		else
		{
			foregroundColor = Preferences.getForegroundColor(this);
			backgroundColor = Preferences.getBackgroundColor(this);
			hintTextColor = Preferences.getHintTextColor(this);
		}
	}

	void colorComponents()
	{
		nameEditText.setTextColor(foregroundColor);
//		nameEditText.setBackgroundColor(backgroundColor);
		nameEditText.setHintTextColor(hintTextColor);

		descriptionEditText.setTextColor(foregroundColor);
//		descriptionEditText.setBackgroundColor(backgroundColor);
		descriptionEditText.setHintTextColor(hintTextColor);

		descriptionTextView.setTextColor(foregroundColor);
		descriptionTextView.setBackgroundColor(backgroundColor);

		view.setBackgroundColor(backgroundColor);

		doneButton.setBackgroundColor(backgroundColor);
		doneButton.setTextColor(foregroundColor);
		syncButton.setBackgroundColor(backgroundColor);
		syncButton.setTextColor(foregroundColor);

		findViewById(R.id.imageview_spacer).setBackgroundColor(hintTextColor);
	}

	void generateLayout()
	{
		String gameString = game.getName();
		if (game.getYearPublished() > 0) gameString += " (" + game.getYearPublished() + ")";

		nameEditText.setText(gameString);
		descriptionEditText.setText(game.getDescription());
		descriptionTextView.setText(game.getDescription() + "\n\n\n");

		try
		{
			thumbnailBitmap = getIntent().getParcelableExtra("BITMAP");
			if (thumbnailBitmap != null)
			{
				thumbnail.setImageBitmap(thumbnailBitmap);
			}
			else
			{
				thumbnail.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{
			thumbnail.setVisibility(View.GONE);
		}

//		final TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, buttonBar.getTop(), buttonBar.getBottom());
//		buttonBar.startAnimation(translateAnimation);
	}

	private void flipView(final View fromView, final View toView)
	{
		try
		{
			setRightOut.setTarget(fromView);
			setLeftIn.setTarget(toView);

			setRightOut.removeAllListeners();

			// Flip views then reveal button bar
			if (editing)
			{
				final TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, buttonBar.getBottom(), buttonBar.getTop());
				translateAnimation.setDuration(300);
				translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

				setRightOut.addListener(new Animator.AnimatorListener()
				{
					@Override
					public void onAnimationStart(Animator animation)
					{
						extrasListView.setVisibility(View.GONE);
						toView.setAlpha(0f);
						toView.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationEnd(Animator animation)
					{
						fromView.setVisibility(View.GONE);
						buttonBar.setVisibility(View.VISIBLE);
						buttonBar.startAnimation(translateAnimation);
						descriptionEditText.requestFocus();
					}

					@Override
					public void onAnimationCancel(Animator animation)
					{

					}

					@Override
					public void onAnimationRepeat(Animator animation)
					{

					}
				});
				setRightOut.start();
				setLeftIn.start();
			}
			// Remove button bar then flip views
			else
			{
				final TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, buttonBar.getTop(), buttonBar.getBottom());
				translateAnimation.setAnimationListener(new Animation.AnimationListener()
				{
					@Override
					public void onAnimationStart(Animation animation)
					{

					}

					@Override
					public void onAnimationEnd(Animation animation)
					{
						extrasListView.setVisibility(View.VISIBLE);
						buttonBar.setVisibility(View.GONE);
						setRightOut.start();
						setLeftIn.start();
					}

					@Override
					public void onAnimationRepeat(Animation animation)
					{

					}
				});
				translateAnimation.setDuration(300);
				translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

				setRightOut.addListener(new Animator.AnimatorListener()
				{
					@Override
					public void onAnimationStart(Animator animation)
					{
						toView.setAlpha(0f);
						toView.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationEnd(Animator animation)
					{
						fromView.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationCancel(Animator animation)
					{

					}

					@Override
					public void onAnimationRepeat(Animator animation)
					{

					}
				});

				buttonBar.startAnimation(translateAnimation);
			}
		}
		catch (Exception e)
		{
			fromView.setVisibility(View.GONE);
			toView.setVisibility(View.VISIBLE);
			buttonBar.setVisibility(editing ? View.VISIBLE : View.GONE);
		}
	}

	private void syncWithBgg()
	{
		long id = 0;
		switch (gameType)
		{
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

		if (id > 0)
		{
			syncWithBggWithId(id, gameType);
		}

		else
		{
			searchBgg(gameName, gameType);
		}
	}

	private void syncWithBggWithId(final long id, final String gameType)
	{
		fabMenu.close(true);

		Game newGame = null;
		switch (gameType)
		{
			case "b":
				try
				{
					newGame = new GameDownloadUtilities.DownloadXmlGameTask(Game.GameType.BOARD)
							.execute("https://www.boardgamegeek.com/xmlapi2/thing?id=" + id)
							.get();
					if (newGame != null)
					{
						if (!BoardGameDbUtility.updateBoardGame(dbHelper, game.getName(),
						                                        (BoardGame) newGame))
							showError("Could not sync with Board Game Geek");
						else
						{
							String thumbnailUrl = newGame.getThumbnailUrl();
							GameDownloadUtilities.downloadThumbnail("http://" + thumbnailUrl, activity);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					showError("Could not sync with Board Game Geek");
				}
				break;
			case "r":
				try
				{
					newGame = new GameDownloadUtilities.DownloadXmlGameTask(Game.GameType.RPG)
							.execute("https://www.boardgamegeek.com/xmlapi2/family?id=" + id)
							.get();
					if (newGame != null)
					{
						if (!RPGDbUtility.updateRPG(dbHelper, game.getName(),
						                            (RolePlayingGame) newGame))
							showError("Could not sync with Board Game Geek");
						else
						{
							String thumbnailUrl = newGame.getThumbnailUrl();
							GameDownloadUtilities.downloadThumbnail("http://" + thumbnailUrl, activity);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					showError("Could not sync with Board Game Geek");
				}
				break;
			case "v":
				try
				{
					newGame = new GameDownloadUtilities.DownloadXmlGameTask(Game.GameType.VIDEO)
							.execute("https://www.boardgamegeek.com/xmlapi2/thing?id=" + id)
							.get();
					if (newGame != null)
					{
						if (!VideoGameDbUtility.updateVideoGame(dbHelper, game.getName(),
						                                        (VideoGame) newGame))
							showError("Could not sync with Board Game Geek");
						else
						{
							String thumbnailUrl = newGame.getThumbnailUrl();
							GameDownloadUtilities.downloadThumbnail("http://" + thumbnailUrl, activity);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					showError("Could not sync with Board Game Geek");
				}
				break;
		}

		if (editing)
		{
			editing = false;
			flipView(descriptionEditText, view.findViewById(R.id.scrollview_description));

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

		if (newGame != null)
		{
			AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Success")
					.setMessage("I have successfully updated the information of " + newGame.getName() + ".")
					.withYancey(true)
					.setPositiveButton("Close", null)
					.create();
			alertDialog.show();
		}
	}

	private void searchBgg(final String gameName, final String gameType)
	{
		AlertDialog alertDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Sync with BGG")
				.setMessage("In order to sync the data of this game I need you to search for it again.")
				.withYancey(true)
				.setPositiveButton("Search", new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						startActivityForResult(
								new Intent(view.getContext(), AddBoardGameActivity.class)
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

	private void showError(String errorMessage)
	{
		AlertDialog errorDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Error")
				.setMessage(errorMessage)
				.setPositiveButton("Close", null)
				.create();
		errorDialog.show();
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onDown(MotionEvent e)
		{
			if (fabMenu.isOpened()) fabMenu.close(true);
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			Log.d("FLING", "fling");
			if (Math.abs(velocityX) > Math.abs(velocityY))
			{
				if (Math.abs(e1.getX() - e2.getX()) >= 200)
				{
					if (velocityX > 2000)
					{
						onBackPressed();
						return true;
					}
				}
			}
			return false;
		}
	}
}
