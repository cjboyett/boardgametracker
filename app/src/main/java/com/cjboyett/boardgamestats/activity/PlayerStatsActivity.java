package com.cjboyett.boardgamestats.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.games.GameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameContract;
import com.cjboyett.boardgamestats.data.games.rpg.RPGContract;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.PlayerData;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.StatsView;
import com.cjboyett.boardgamestats.view.adapter.GamePlayAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class PlayerStatsActivity extends BaseAdActivity {
	private Activity activity = this;
	private View view;
	private ImageView avatarImageView, editAvatarImageView, deleteAvatarImageView;
	private TextView nameTextView, playerNotesTextView;//, timePlayedTextView;
	private ListView listView;

	private FloatingActionMenu fabMenu;
	private FloatingActionButton editNameFab, addNotesFab, linkToFacebookFab;

	private List<StatsView> statsViews;

	private final int REQUEST_IMAGE = 200, REQUEST_PERMISSIONS = 250;

	private int scrollY;

	private GamesDbHelper dbHelper;
	private GestureDetectorCompat gestureDetector;

	public PlayerStatsActivity() {
		super("ca-app-pub-1437859753538305/2047913877");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_player_stats, null);
		setContentView(view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		dbHelper = new GamesDbHelper(this);

		generateLayout();
		setColors();
		colorComponents();

		gestureDetector = new GestureDetectorCompat(this, new ScrollGestureListener());
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_IMAGE) {
			if (resultCode == RESULT_OK) {
				// Get the result list of select image paths
				List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
//				for (String path : paths) Log.d("PICTURE", path);

				new AsyncTask<String, Void, Bitmap>() {
					@Override
					protected Bitmap doInBackground(String... params) {
						Bitmap bitmap = BitmapFactory.decodeFile(params[0]);
						BitmapFactory.Options options = new BitmapFactory.Options();

						if (bitmap.getByteCount() > 256 * 1024) {
							options.inSampleSize = (int) Math.ceil(Math.sqrt((bitmap.getByteCount() / 1024) / 256));
							bitmap = BitmapFactory.decodeFile(params[0], options);
						}

						PlayersDbUtility.setPlayerImageFilePath(dbHelper, getIntent().getStringExtra("NAME"), true);

						ImageController imageController = new ImageController(activity);
						imageController.setDirectoryName("avatars")
									   .setFileName(PlayersDbUtility.getPlayerImageFilePath(dbHelper,
																							getIntent().getStringExtra(
																									"NAME")) + ".jpg")
									   .setFileType("JPG")
									   .save(bitmap);

						return bitmap;
					}

					@Override
					protected void onPostExecute(Bitmap bitmap) {
						avatarImageView.setImageBitmap(bitmap);
						ActivityUtilities.setDatabaseChanged(activity, true);
					}
				}.execute(paths.get(0));

				// do your logic ....
			}
		}
	}

	@Override
	void colorComponents() {
		view.setBackgroundColor(backgroundColor);

		nameTextView.setTextColor(foregroundColor);
		playerNotesTextView.setTextColor(foregroundColor);

		for (StatsView statsView : statsViews)
			statsView.colorComponents(backgroundColor, foregroundColor);

		((TextView) view.findViewById(R.id.textview_stats)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_stats).setBackgroundColor(backgroundColor);

		((TextView) view.findViewById(R.id.textview_gameplays)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_gameplays).setBackgroundColor(backgroundColor);

		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.relativelayout_info), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.listview_gameplay), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.scrollView), foregroundColor);
	}

	@Override
	void generateLayout() {
		avatarImageView = (ImageView) view.findViewById(R.id.imageview_avatar);
		editAvatarImageView = (ImageView) view.findViewById(R.id.imageview_edit_avatar);
		deleteAvatarImageView = (ImageView) view.findViewById(R.id.imageview_delete_avatar);
		nameTextView = (TextView) view.findViewById(R.id.textview_player_name);
		playerNotesTextView = (TextView) view.findViewById(R.id.textview_player_notes);
		listView = (ListView) view.findViewById(R.id.listview_gameplay);

		fabMenu = (FloatingActionMenu) view.findViewById(R.id.floating_menu);
		editNameFab = (FloatingActionButton) view.findViewById(R.id.fab_edit_name);
		addNotesFab = (FloatingActionButton) view.findViewById(R.id.fab_add_notes);
		linkToFacebookFab = (FloatingActionButton) view.findViewById(R.id.fab_link_with_facebook);

		statsViews = new ArrayList<>();

		try {
			final String name = getIntent().getStringExtra("NAME");
			PlayerData playerData = PlayersDbUtility.getPlayerData(dbHelper, name);

			avatarImageView.setImageBitmap(ViewUtilities.createAvatar(this, name, false));
			nameTextView.setText(name);
			playerNotesTextView.setText(playerData.getNotes());

			statsViews.add(new StatsView(activity, "Play count", playerData.getTimesPlayedWith() + ""));
			statsViews.add(new StatsView(activity,
										 "Play time",
										 StringUtilities.convertMinutes(playerData.getTimePlayedWith())));
			statsViews.add(new StatsView(activity, "Most played game by count", playerData.getMostPlayedGameByTimes()));
			statsViews.add(new StatsView(activity, "Most played game by time", playerData.getMostPlayedGameByTime()));

			if (!TextUtils.isEmpty(playerData.getMostWonGame()))
				statsViews.add(new StatsView(activity, "Most won game", playerData.getMostWonGame()));
			if (!TextUtils.isEmpty(playerData.getMostLostGame()))
				statsViews.add(new StatsView(activity, "Most lost game", playerData.getMostLostGame()));

			statsViews.add(new StatsView(activity,
										 "Win percentage",
										 NumberFormat.getPercentInstance()
													 .format(playerData.getWinPercentage() / 100)));
			statsViews.add(new StatsView(activity,
										 "Lose percentage",
										 NumberFormat.getPercentInstance()
													 .format(playerData.getLosePercentage() / 100)));

			for (StatsView statsView : statsViews)
				((LinearLayout) view.findViewById(R.id.linearlayout_stats)).addView(statsView);

			boolean useBoardGamesForStats = Preferences.useBoardGamesForStats(activity);
			boolean useRPGsForStats = Preferences.useRPGsForStats(activity);
			boolean useVideoGamesForStats = Preferences.useVideoGamesForStats(activity);

			final List<GamePlayData> gamePlayDataList = GameStatsDbUtility.getGamePlaysWithPlayer(dbHelper,
																								  name,
																								  useBoardGamesForStats,
																								  useRPGsForStats,
																								  useVideoGamesForStats);

			final long[] ids = new long[gamePlayDataList.size()];
			final List<String> gameTypes = new ArrayList<>();
			final List<String> gameNames = new ArrayList<>();
			for (int i = 0; i < ids.length; i++) {
				ids[i] = gamePlayDataList.get(i).getId();

				GamePlayData gamePlayData = gamePlayDataList.get(i);
				String gameType = "b";
				if (gamePlayData instanceof BoardGamePlayData) gameType = "b";
				else if (gamePlayData instanceof RPGPlayData) gameType = "r";
				else if (gamePlayData instanceof VideoGamePlayData) gameType = "v";
				gameTypes.add(gameType);

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


			listView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (Preferences.useSwipes(v.getContext()))
						return gestureDetector.onTouchEvent(event);
					return false;
				}
			});

			editNameFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					fabMenu.close(true);
					ViewUtilities.DialogBuilder editDialog = new ViewUtilities.DialogBuilder(activity);
					final EditText editText = editDialog.getInput();
					editText.setSingleLine(true);
					editText.setText(name);
					editDialog.setTitle("Edit name")
							  .setHintText("Edit name")
							  .setPositiveButton("Okay", new View.OnClickListener() {
								  @Override
								  public void onClick(View v) {
									  String newName = editText.getText().toString();
									  if (!TextUtils.isEmpty(newName) && !name.equals(newName)) {
										  new AsyncTask<String, Void, Void>() {
											  String newName, oldName;

											  @Override
											  protected Void doInBackground(String... params) {
												  newName = params[1];
												  oldName = params[0];

												  SQLiteDatabase db = dbHelper.getWritableDatabase();

												  ContentValues values = new ContentValues();
												  values.put(BoardGameContract.PlayerEntry.NAME, newName);
												  db.update(BoardGameContract.PlayerEntry.TABLE_NAME,
															values,
															BoardGameContract.PlayerEntry.NAME + " = ?",
															new String[]{oldName});

												  values = new ContentValues();
												  values.put(RPGContract.PlayerEntry.NAME, newName);
												  db.update(RPGContract.PlayerEntry.TABLE_NAME,
															values,
															RPGContract.PlayerEntry.NAME + " = ?",
															new String[]{oldName});

												  values = new ContentValues();
												  values.put(VideoGameContract.PlayerEntry.NAME, newName);
												  db.update(VideoGameContract.PlayerEntry.TABLE_NAME,
															values,
															VideoGameContract.PlayerEntry.NAME + " = ?",
															new String[]{oldName});

												  db.close();
												  return null;
											  }

											  @Override
											  protected void onPostExecute(Void aVoid) {
												  PlayersDbUtility.combinePlayers(dbHelper, oldName, newName);

												  ActivityUtilities.setDatabaseChanged(activity, true);
												  onBackPressed();
											  }
										  }.execute(name, newName);
									  }
								  }
							  })
							  .setNegativeButton("Cancel", null);
					editDialog.create().show();
				}
			});

			addNotesFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					fabMenu.close(true);
					ViewUtilities.DialogBuilder editDialog = new ViewUtilities.DialogBuilder(activity);
					final EditText editText = editDialog.getInput();
					editText.setText(PlayersDbUtility.getPlayerNotes(dbHelper, name));
					editDialog.setTitle("Add player notes")
							  .setHintText("Notes")
							  .setLinesOfInput(8)
							  .setPositiveButton("Okay", new View.OnClickListener() {
								  @Override
								  public void onClick(View v) {
									  final String notes = editText.getText().toString();
									  if (!TextUtils.isEmpty(notes)) {
										  new AsyncTask<String, Void, Void>() {
											  @Override
											  protected Void doInBackground(String... params) {
												  PlayersDbUtility.setPlayerNotes(dbHelper, params[0], params[1]);
												  return null;
											  }

											  @Override
											  protected void onPostExecute(Void aVoid) {
												  ActivityUtilities.setDatabaseChanged(activity, true);
												  playerNotesTextView.setText(notes);
											  }
										  }.execute(name, notes);
									  }
								  }
							  })
							  .setNegativeButton("Cancel", null);
					editDialog.create().show();
				}
			});

			avatarImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (editAvatarImageView.getVisibility() == View.GONE) {
						editAvatarImageView.setVisibility(View.VISIBLE);
						deleteAvatarImageView.setVisibility(View.VISIBLE);
					} else {
						editAvatarImageView.setVisibility(View.GONE);
						deleteAvatarImageView.setVisibility(View.GONE);
					}
				}
			});

			editAvatarImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					editAvatarImageView.setVisibility(View.GONE);
					deleteAvatarImageView.setVisibility(View.GONE);

					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
						String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};
						requestPermissions(perms, REQUEST_PERMISSIONS);
					} else
						MultiImageSelector.create()
										  .single()
										  .start(activity, REQUEST_IMAGE);
				}
			});

			deleteAvatarImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
							.setTitle("Delete avatar")
							.setMessage("Would you like to delete the avatar for this person?")
							.setPositiveButton("Yes", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									String avatarFilePath = PlayersDbUtility.getPlayerImageFilePath(dbHelper, name);
									ImageController imageController = new ImageController(activity);
									imageController.setDirectoryName("avatars")
												   .setFileName(avatarFilePath + ".jpg")
												   .createFile()
												   .delete();

									PlayersDbUtility.setPlayerImageFilePath(dbHelper, name, false);
									ActivityUtilities.setDatabaseChanged(activity, true);
									avatarImageView.setImageBitmap(ViewUtilities.createAvatar(activity, name, true));
								}
							})
							.setNegativeButton("No", null)
							.create();

					editAvatarImageView.setVisibility(View.GONE);
					deleteAvatarImageView.setVisibility(View.GONE);
					alertDialog.show();
				}
			});

			ActivityUtilities.setDatabaseChanged(this, false);
		} catch (Exception e) {
			onBackPressed();
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == REQUEST_PERMISSIONS) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
					grantResults[1] == PackageManager.PERMISSION_GRANTED)
				MultiImageSelector.create()
								  .single()
								  .start(activity, REQUEST_IMAGE);
		}
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
					if (velocityY > 2000 && scrollY == 0) {
						onBackPressed();
						return true;
					}
				}
			}
			return false;
		}
	}

}
