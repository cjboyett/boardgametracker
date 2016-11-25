package com.cjboyett.boardgamestats.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cjboyett.boardgamestats.MyApp;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameXmlParser;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGXmlParser;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameXmlParser;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.data.GameDownloadUtilities;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.data.UrlUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AddBoardGameActivity extends BaseActivity
{
	private Activity activity = this;
	private View view, dummyView;
	private Spinner gameTypeSpinner;
	private SearchView gameSearchView;
	private LinearLayout progressBarLayout;
	private ProgressBar progressBar;
	private AppCompatCheckBox exactMatch;
	private ListView listView;
	private List<BoardGameXmlParser.Item> boardGamesList;
	private List<VideoGameXmlParser.Item> videoGamesList;
	private List<Integer> ids;
	private List<String> games;
	private boolean exact;
	private Game currentGame = null;
	private Game.GameType gameType = Game.GameType.BOARD;

	private boolean sync;
	private String oldGameName;

	private GamesDbHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_add_board_game, null);
		setContentView(view);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		dbHelper = new GamesDbHelper(this);
		games = new ArrayList<>();
		sync = getIntent().getBooleanExtra("SYNC", false);
		if (sync) oldGameName = getIntent().getStringExtra("GAME");

		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		dbHelper.close();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		dbHelper = new GamesDbHelper(this);
		if (getIntent().hasExtra("TYPE"))
		{
			String type = getIntent().getStringExtra("TYPE");
			if (StringUtilities.isBoardGame(type))
			{
				gameType = Game.GameType.BOARD;
				gameTypeSpinner.setSelection(0);
			}
			else if (StringUtilities.isRPG(type))
			{
				gameType = Game.GameType.RPG;
				gameTypeSpinner.setSelection(1);
			}
			else if (StringUtilities.isVideoGame(type))
			{
				gameType = Game.GameType.VIDEO;
				gameTypeSpinner.setSelection(2);
			}

			if (sync) gameTypeSpinner.setEnabled(false);

			if (getIntent().hasExtra("GAME"))
			{
				gameSearchView.setQuery(getIntent().getStringExtra("GAME"), true);
			}
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (dbHelper != null) dbHelper.close();
	}

	private void setRPGListAdapter(Map<Integer, String> rpgs)
	{
		if (rpgs.isEmpty())
		{
			showSearchError();
		}
		else
		{
			games = new ArrayList<>();
			List<Long> idList = RPGDbUtility.getGameIds(dbHelper);
			for (int id : rpgs.keySet()) games.add(rpgs.get(id));

			Collections.sort(games);

			ids = new ArrayList<>();
			for (int i = 0; i < games.size(); i++)
			{
				String rpg = games.get(i);
				for (int id : rpgs.keySet())
				{
					if (rpgs.get(id)
					        .equals(rpg))
					{
						ids.add(id);
						if (idList.contains((long) id))
							games.set(i, "<i>" + rpg + " (In your collection)</i>");
						continue;
					}
				}
			}
		}

		listView.setAdapter(new CustomArrayAdapter(this, android.R.layout.simple_list_item_1, games));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				new DownloadXmlGameTask().execute(
						"https://www.boardgamegeek.com/xmlapi2/family?id=" + ids.get(position));
			}
		});
	}

	private void setBoardGameListAdapter(List<BoardGameXmlParser.Item> items)
	{
		boardGamesList = items;
		Iterator<BoardGameXmlParser.Item> iter = boardGamesList.iterator();
		while (iter.hasNext())
		{
			if (iter.next().name == null) iter.remove();
		}
		if (boardGamesList.isEmpty())
		{
			showSearchError();
		}
		else
		{
			listView.setAdapter(new CustomArrayAdapter(this, android.R.layout.simple_list_item_1, makeList()));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					new DownloadXmlGameTask().execute(
							"https://www.boardgamegeek.com/xmlapi2/thing?id=" +
							boardGamesList.get(position).id);
				}
			});
		}
	}

	private void setVideoGameListAdapter(List<VideoGameXmlParser.Item> items)
	{
		videoGamesList = items;
		Iterator<VideoGameXmlParser.Item> iter = videoGamesList.iterator();
		while (iter.hasNext())
		{
			if (iter.next().name == null) iter.remove();
		}
		if (videoGamesList.isEmpty())
		{
			showSearchError();
		}
		else
		{
			listView.setAdapter(new CustomArrayAdapter(this, android.R.layout.simple_list_item_1, makeList()));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					new DownloadXmlGameTask().execute(
							"https://www.boardgamegeek.com/xmlapi2/thing?id=" +
							videoGamesList.get(position).id);
				}
			});
		}
	}

	private List<String> makeList()
	{
		List<String> gameNames = new ArrayList<>();
		if (gameType == Game.GameType.BOARD)
		{
			List<Long> idList = BoardGameDbUtility.getGameIds(dbHelper);
			for (BoardGameXmlParser.Item item : boardGamesList)
			{
				if (idList.contains((long) item.id))
					gameNames.add("<i>" + item.name +
					              (item.yearPublished > 0 ? " (" + item.yearPublished + ")" : "") +
					              " (In your collection)</i>");
				else
					gameNames.add(item.name +
					              (item.yearPublished > 0 ? " (" + item.yearPublished + ")" : ""));
			}
		}
		else if (gameType == Game.GameType.VIDEO)
		{
			List<Long> idList = VideoGameDbUtility.getGameIds(dbHelper);
			for (VideoGameXmlParser.Item item : videoGamesList)
			{
				if (idList.contains((long) item.id))
					gameNames.add("<i>" + item.name +
					              ((item.releaseDate != null && !item.releaseDate.equals("")) ?
					               " (" + item.releaseDate + ")" : "") +
					              " (In your collection)</i>");
				else
					gameNames.add(item.name +
					              ((item.releaseDate != null && !item.releaseDate.equals("")) ?
					               " (" + item.releaseDate + ")" : ""));
			}
		}
		return gameNames;
	}

	private void showSearchError()
	{
		if (sync)
		{
			AlertDialog errorDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Error")
					.setMessage("I apologize, but I cannot find your game.")
					.withYancey(true)
					.setPositiveButton("Close", null)
					.create();
			errorDialog.show();
		}
		else
		{
			AlertDialog errorDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Error")
					.setMessage(
							"Something went wrong while searching.  Either Board Game Geek is not responding, or their database does not contain your game." +
							"  You can try again later or manually add your game.")
					.setPositiveButton("Try again later", null)
					.setNegativeButton("Add game", new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							showAddManualDialog();
						}
					})
					.create();
			errorDialog.show();
		}
	}

	private AlertDialog gameDialog;

	private void showAddManualDialog()
	{
		final Game.GameType[] type = {gameType};
		final View view = activity.getLayoutInflater()
		                          .inflate(R.layout.dialog_add_game_manual, null);
		final EditText gameNameEditText = (EditText) view.findViewById(R.id.edittext_game_name);
		final EditText gameDescriptionEditText =
				(EditText) view.findViewById(R.id.edittext_game_details);

		gameDialog = new ViewUtilities.DialogBuilder(activity)
				.setView(view)
				.setPositiveButton("Add Game to Collection", new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						gameDialog.dismiss();
						if (!TextUtils.isEmpty(gameNameEditText.getText()
						                                       .toString()))
						{
							String name = gameNameEditText.getText()
							                              .toString();
							String description = gameDescriptionEditText.getText()
							                                            .toString();

							Log.d("MANUAL", name + " " + type);

							try
							{
								if (type[0] == Game.GameType.BOARD)
									BoardGameDbUtility.addBoardGame(dbHelper, new BoardGame(name, description, -10000));
								else if (type[0] == Game.GameType.RPG)
									RPGDbUtility.addRPG(dbHelper, new RolePlayingGame(name, description, -10000));
								else if (type[0] == Game.GameType.VIDEO)
									VideoGameDbUtility.addVideoGame(dbHelper, new VideoGame(name, description, -10000));
								ActivityUtilities.setDatabaseChanged(activity, true);
								showSnack("Added " + name);
							}
							catch (Exception e)
							{
								Log.e("GAME", e.getMessage());
							}
						}
					}
				})
				.setNegativeButton("Cancel", null)
				.create();

		view.setBackgroundColor(backgroundColor);

		gameNameEditText.setTextColor(foregroundColor);
		gameNameEditText.setHintTextColor(hintTextColor);
		gameNameEditText.setText(gameSearchView.getQuery());

		gameDescriptionEditText.setTextColor(foregroundColor);
		gameDescriptionEditText.setHintTextColor(hintTextColor);

		((TextView) view.findViewById(R.id.textview_note)).setTextColor(hintTextColor);

		final Spinner gameTypeSpinner = (Spinner) view.findViewById(R.id.spinner_types);
		ViewUtilities.tintLayoutBackground(gameTypeSpinner, foregroundColor);
		final List<String> gameTypeList = new ArrayList<>();
		gameTypeList.add("Board Game");
		gameTypeList.add("RPG");
		gameTypeList.add("Video Game");
		gameTypeSpinner.setAdapter(new CustomArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gameTypeList));
		gameTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position)
				{
					case 0:
						type[0] = Game.GameType.BOARD;
						break;
					case 1:
						type[0] = Game.GameType.RPG;
						break;
					case 2:
						type[0] = Game.GameType.VIDEO;
						break;
					default:
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		if (type[0] == Game.GameType.RPG)
			gameTypeSpinner.setSelection(1);
		else if (type[0] == Game.GameType.VIDEO)
			gameTypeSpinner.setSelection(2);

		view.findViewById(R.id.textview_add_game)
		    .setBackgroundColor(backgroundColor);
		((TextView) view.findViewById(R.id.textview_add_game)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_add_game)
		    .setOnClickListener(new View.OnClickListener()
		    {
			    @Override
			    public void onClick(View v)
			    {
				    gameDialog.dismiss();
				    if (!TextUtils.isEmpty(gameNameEditText.getText()
				                                           .toString()))
				    {
					    String name = gameNameEditText.getText()
					                                  .toString();
					    String description = gameDescriptionEditText.getText()
					                                                .toString();

					    Log.d("MANUAL", name + " " + type);

					    try
					    {
						    if (type[0] == Game.GameType.BOARD)
							    BoardGameDbUtility.addBoardGame(dbHelper, new BoardGame(name, description, -10000));
						    else if (type[0] == Game.GameType.RPG)
							    RPGDbUtility.addRPG(dbHelper, new RolePlayingGame(name, description, -10000));
						    else if (type[0] == Game.GameType.VIDEO)
							    VideoGameDbUtility.addVideoGame(dbHelper, new VideoGame(name, description, -10000));
						    ActivityUtilities.setDatabaseChanged(activity, true);
						    showSnack("Added " + name);
					    }
					    catch (Exception e)
					    {
						    Log.e("GAME", e.getMessage());
					    }
				    }
			    }
		    });

		view.findViewById(R.id.button_cancel)
		    .setBackgroundColor(backgroundColor);
		((TextView) view.findViewById(R.id.button_cancel)).setTextColor(foregroundColor);
		view.findViewById(R.id.button_cancel)
		    .setOnClickListener(new View.OnClickListener()
		    {
			    @Override
			    public void onClick(View v)
			    {
				    gameDialog.cancel();
			    }
		    });

		gameDialog.show();
	}

	private void showSnack(String message)
	{
		Snackbar.make(view, message, Snackbar.LENGTH_LONG)
		        .setAction("Action", null)
		        .show();
	}


	@Override
	void generateLayout()
	{
		dummyView = findViewById(R.id.dummyview);
		gameTypeSpinner = (Spinner) findViewById(R.id.spinner_game_type);
		gameSearchView = (SearchView) findViewById(R.id.searchview_boardgame);
		exactMatch = (AppCompatCheckBox) findViewById(R.id.checkbox_exact_match);
		listView = (ListView) findViewById(R.id.listview_boardgames);
		progressBarLayout = (LinearLayout) findViewById(R.id.linearlayout_progress_bar);
		progressBar = (ProgressBar) findViewById(R.id.progressbar_add_game);

		gameSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				if (((MyApp)getApplication()).isConnectedToInternet())
				{
					try
					{
						dummyView.requestFocus();
						InputMethodManager inputManager = (InputMethodManager)
								getSystemService(Context.INPUT_METHOD_SERVICE);

						inputManager.hideSoftInputFromWindow(gameSearchView.getWindowToken(),
						                                     InputMethodManager.HIDE_NOT_ALWAYS);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					listView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1));

					if (gameType != Game.GameType.RPG)
					{
						try
						{
							exact = exactMatch.isChecked();
							String url = "https://www.boardgamegeek.com/xmlapi2/search?query=" +
							             URLEncoder.encode(query) + "&type=" + gameType.getType();
							if (exact) url += "&exact=1";

							if (gameType == Game.GameType.BOARD)
								new DownloadBoardGameXmlItemsTask().execute(url);
							else if (gameType == Game.GameType.VIDEO)
								new DownloadVideoGameXmlItemsTask().execute(url);
						}
						catch (Exception e)
						{
						}
					}
					else
					{
						new DownloadRPGsTask().execute(query);
					}
				}
				else
					ViewUtilities.errorDialog(activity).show();

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				return false;
			}
		});

		if (getIntent().hasExtra("SEARCH")) gameSearchView.setQuery(getIntent().getStringExtra("SEARCH"), true);

		final List<String> gameTypeList = new ArrayList<>();
		gameTypeList.add("Board Game");
		gameTypeList.add("RPG");
		gameTypeList.add("Video Game");
		gameTypeSpinner.setAdapter(new CustomArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gameTypeList));
		gameTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position)
				{
					case 0:
						gameType = Game.GameType.BOARD;
						break;
					case 1:
						gameType = Game.GameType.RPG;
						break;
					case 2:
						gameType = Game.GameType.VIDEO;
						break;
					default:
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		boardGamesList = new ArrayList<>();
		videoGamesList = new ArrayList<>();

		ArrayAdapter<String> adapter =
				new CustomArrayAdapter(this, android.R.layout.simple_list_item_1, makeList());

		listView.setAdapter(adapter);
	}

	void colorComponents()
	{
		view.setBackgroundColor(backgroundColor);
		gameSearchView.setBackgroundColor(backgroundColor);
		for (TextView textView : ViewUtilities.findChildrenByClass(gameSearchView, TextView.class))
		{
			textView.setTextColor(foregroundColor);
			textView.setHintTextColor(hintTextColor);
		}
		for (ImageView imageView : ViewUtilities.findChildrenByClass(gameSearchView, ImageView.class))
			imageView.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_ATOP);
		exactMatch.setBackgroundColor(backgroundColor);
		exactMatch.setTextColor(foregroundColor);
		ViewUtilities.tintCheckBox(exactMatch);

		ViewUtilities.tintLayoutBackground(gameTypeSpinner, foregroundColor);
		listView.setBackgroundColor(backgroundColor);

		((TextView)view.findViewById(R.id.textview_game_search)).setTextColor(foregroundColor);
		((TextView)view.findViewById(R.id.textview_game_search_results)).setTextColor(foregroundColor);

		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.layout_game_search), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.layout_game_search_results), foregroundColor);

		findViewById(R.id.imageview_spacer).setBackgroundColor(hintTextColor);

		ViewUtilities.tintProgressBar(progressBar, foregroundColor);
	}

	@Override
	public void onBackPressed()
	{
		if (getIntent().hasExtra("GAME"))
		{
			if (currentGame != null)
			{
				if (getIntent().hasExtra("SYNC"))
				{
					String thumbnailUrl = currentGame.getThumbnailUrl();
					Intent returnIntent = new Intent(view.getContext(), BoardGameDataActivity.class)
							.putExtra("GAME", currentGame.getName())
							.putExtra("TYPE", gameType.getType())
							.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					setResult(RESULT_OK, returnIntent);
					if (thumbnailUrl != null && !thumbnailUrl.equals(""))
					{
						ActivityUtilities.generatePaletteAndOpenActivity(activity,
						                                                 returnIntent,
						                                                 "http://" + thumbnailUrl,
						                                                 "DOWN");
						ActivityUtilities.exitDown(activity);
						finish();
					}
					else
					{
						startActivity(returnIntent);
						ActivityUtilities.exitDown(activity);
						finish();
					}
				}
				else
				{
					Intent returnIntent = new Intent();
					returnIntent.putExtra("GAME", currentGame.getName())
					            .putExtra("TYPE", gameType.getType());
					setResult(RESULT_OK, returnIntent);
					ActivityUtilities.exitRight(activity);
					finish();
				}
			}
			else
			{
				finish();
				if (getIntent().hasExtra("SYNC"))
					ActivityUtilities.exitDown(activity);
				else
					ActivityUtilities.exitRight(activity);
			}
		}
		else
		{
			finish();
			ActivityUtilities.exitDown(this);
		}
	}

	private class DownloadRPGsTask extends AsyncTask<String, Void, Map<Integer, String>>
	{
		@Override
		protected void onPreExecute()
		{
			progressBarLayout.setVisibility(View.VISIBLE);
		}

		@Override
		protected Map<Integer, String> doInBackground(String... rpgName)
		{
			return UrlUtilities.parseRPGsFromBGGSearch(rpgName[0]);
		}

		@Override
		protected void onPostExecute(Map<Integer, String> integerStringMap)
		{
			setRPGListAdapter(integerStringMap);
			progressBarLayout.setVisibility(View.GONE);
		}
	}

	private class DownloadBoardGameXmlItemsTask extends AsyncTask<String, Void, List<BoardGameXmlParser.Item>>
	{
		@Override
		protected void onPreExecute()
		{
			progressBarLayout.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<BoardGameXmlParser.Item> doInBackground(String... queries)
		{
			String url = queries[0];
			try
			{
				return UrlUtilities.loadBoardGameXmlFromNetwork(url);
			}
			catch (Exception e)
			{
				Log.e("PARSER", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<BoardGameXmlParser.Item> result)
		{
			setBoardGameListAdapter(result);
			progressBarLayout.setVisibility(View.GONE);
		}
	}

	private class DownloadVideoGameXmlItemsTask extends AsyncTask<String, Void, List<VideoGameXmlParser.Item>>
	{
		@Override
		protected void onPreExecute()
		{
			progressBarLayout.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<VideoGameXmlParser.Item> doInBackground(String... queries)
		{
			String url = queries[0];
			try
			{
				return UrlUtilities.loadVideoGameXmlFromNetwork(url);
			}
			catch (Exception e)
			{
				Log.e("PARSER", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<VideoGameXmlParser.Item> result)
		{
			setVideoGameListAdapter(result);
			progressBarLayout.setVisibility(View.GONE);
		}
	}

	private class DownloadXmlGameTask extends AsyncTask<String, Void, Game>
	{
		private AlertDialog gameDialog;

		@Override
		protected void onPreExecute()
		{
			progressBarLayout.setVisibility(View.VISIBLE);
		}

		@Override
		protected Game doInBackground(String... urls)
		{
			if (gameType == Game.GameType.BOARD)
			{
				try
				{
					List<BoardGameXmlParser.Item> items =
							UrlUtilities.loadBoardGameXmlFromNetwork(urls[0]);
					return BoardGame.createGame(items.get(0));
				}
				catch (Exception e)
				{
					Log.e("PARSER", e.getMessage());
				}
			}
			else if (gameType == Game.GameType.RPG)
			{
				InputStream inputStream = null;
				try
				{
					inputStream = UrlUtilities.downloadUrl(urls[0]);
					List<RPGXmlParser.Item> items = new RPGXmlParser().parse(inputStream);
					return RolePlayingGame.createGame(items.get(0));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						inputStream.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else if (gameType == Game.GameType.VIDEO)
			{
				try
				{
					List<VideoGameXmlParser.Item> items =
							UrlUtilities.loadVideoGameXmlFromNetwork(urls[0]);
					return VideoGame.createGame(items.get(0));
				}
				catch (Exception e)
				{
					Log.e("PARSER", e.getMessage());
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Game game)
		{
			if (game != null)
			{
				Bitmap thumbnail = null;
				if (game.getThumbnailUrl() != null)
				{
					try
					{
						thumbnail = new DownloadThumbnailTask().execute(
								"http://" + game.getThumbnailUrl())
						                                       .get();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					catch (ExecutionException e)
					{
						e.printStackTrace();
					}
				}
				progressBarLayout.setVisibility(View.GONE);

				currentGame = game;

				final View view = activity.getLayoutInflater()
				                          .inflate(R.layout.dialog_fragment_add_board_game, null);
				gameDialog = new ViewUtilities.DialogBuilder(activity)
						.setView(view)
						.setPositiveButton(
								sync ? "Sync" : "Add Game to Collection", new View.OnClickListener()
								{
									@Override
									public void onClick(View v)
									{
										gameDialog.dismiss();
										if (currentGame != null)
										{
											try
											{
												if (gameType == Game.GameType.BOARD)
												{
													if (sync)
														BoardGameDbUtility.updateBoardGame(dbHelper,
														                                   oldGameName,
														                                   (BoardGame) currentGame);
													else
														BoardGameDbUtility.addBoardGame(dbHelper,
														                                (BoardGame) currentGame);
												}
												else if (gameType == Game.GameType.RPG)
												{
													if (sync)
														RPGDbUtility.updateRPG(dbHelper,
														                       oldGameName,
														                       (RolePlayingGame) currentGame);
													else
														RPGDbUtility.addRPG(dbHelper,
														                    (RolePlayingGame) currentGame);
												}
												else if (gameType == Game.GameType.VIDEO)
												{
													if (sync)
														VideoGameDbUtility.updateVideoGame(dbHelper,
														                                   oldGameName,
														                                   (VideoGame) currentGame);
													else
														VideoGameDbUtility.addVideoGame(dbHelper,
														                                (VideoGame) currentGame);
												}
												String thumbnailUrl = currentGame.getThumbnailUrl();
												GameDownloadUtilities.downloadThumbnail(
														"http://" + thumbnailUrl, activity);
												ActivityUtilities.setDatabaseChanged(activity, true);
												if (activity.getIntent()
												            .hasExtra("GAME"))
													onBackPressed();
												else
													showSnack("Added " + currentGame.getName());
											}
											catch (Exception e)
											{
												Log.e("GAME", e.getMessage());
											}
										}
									}
								})
						.setNegativeButton("Cancel", new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								gameDialog.cancel();
							}
						})
						.create();

				view.setBackgroundColor(backgroundColor);
				if (thumbnail != null)
					((ImageView) view.findViewById(R.id.imageview_avatar)).setImageBitmap(thumbnail);
				else view.findViewById(R.id.imageview_avatar)
				         .setVisibility(View.GONE);

				((TextView) view.findViewById(R.id.textview_game_title)).setText(game.getName());
				((TextView) view.findViewById(R.id.textview_game_title)).setTextColor(foregroundColor);
				view.findViewById(R.id.textview_game_title)
				    .setBackgroundColor(backgroundColor);

				((TextView) view.findViewById(R.id.textview_game_details)).setText(game.getDescription());
				view.findViewById(R.id.textview_game_details)
				    .setBackgroundColor(backgroundColor);
				((TextView) view.findViewById(R.id.textview_game_details)).setTextColor(foregroundColor);

				gameDialog.show();
			}
			else
			{
				progressBarLayout.setVisibility(View.GONE);
				AlertDialog errorDialog = new ViewUtilities.DialogBuilder(activity)
						.setTitle("Error")
						.setMessage("Something went wrong in finding your game.  Please try again later.")
						.setPositiveButton("Okay", null)
						.create();
				errorDialog.show();
			}
		}
	}

	private class DownloadThumbnailTask extends AsyncTask<String, Void, Bitmap>
	{

		@Override
		protected Bitmap doInBackground(String... url)
		{
			Bitmap bitmap = null;
			InputStream in = null;
			try
			{
				URL thumbnailUrl = new URL(url[0]);
				HttpURLConnection connection = (HttpURLConnection) thumbnailUrl.openConnection();
				connection.setReadTimeout(10000);
				connection.setConnectTimeout(15000);
//				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.connect();
				in = connection.getInputStream();
				bitmap = BitmapFactory.decodeStream(in);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					in.close();
				}
				catch (Exception e)
				{
				}
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap)
		{
			//thumbnail.setImageBitmap(bitmap);
		}
	}

	private class CustomArrayAdapter extends ArrayAdapter<String>
	{
		public CustomArrayAdapter(Context context, int resource, List<String> objects)
		{
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			TextView view = (TextView) super.getView(position, convertView, parent);
			view.setText(Html.fromHtml(view.getText()
			                               .toString()));
//			view.setBackgroundColor(backgroundColor);
			view.setTextColor(foregroundColor);
			return view;
		}


	}
}
