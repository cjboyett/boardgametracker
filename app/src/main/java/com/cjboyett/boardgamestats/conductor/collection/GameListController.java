package com.cjboyett.boardgamestats.conductor.collection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.graphics.Palette;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.conductor.ConductorActivity;
import com.cjboyett.boardgamestats.conductor.addgame.AddGameController;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.conductor.changehandlers.DirectionalChangeHandler;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.ImageAdapter;

import java.util.ArrayList;
import java.util.List;

public class GameListController extends BaseController {
	private Activity activity;
	private GridView gamesGridView;
	private SearchView gamesSearchView;
	private View view, dummyView;

	private List<String> gamesList;

	private String game;

	private GamesDbHelper dbHelper;
	private GestureDetectorCompat gestureDetector;

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.activity_game_list, container, false);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		activity = getActivity();

		view.findViewById(R.id.fab)
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
/*
					startActivity(new Intent(view.getContext(), AddGameActivity.class));
					ActivityUtilities.exitUp(activity);
*/
					getRouter().pushController(RouterTransaction.with(new AddGameController())
																.pushChangeHandler(DirectionalChangeHandler.from(
																		DirectionalChangeHandler.TOP))
																.popChangeHandler(DirectionalChangeHandler.from(
																		DirectionalChangeHandler.TOP)));
				}
			});

		dbHelper = new GamesDbHelper(activity);
		gestureDetector = new GestureDetectorCompat(activity, new ScrollGestureListener());
		((ConductorActivity) getActivity()).setGestureDetector(null);

		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	protected void onDetach(@NonNull View view) {
		((ConductorActivity) getActivity()).removeGestureDetector();
		dbHelper.close();
		super.onDetach(view);
	}

	@Override
	protected void onDestroy() {
		if (dbHelper != null) dbHelper.close();
		super.onDestroy();
	}

/*	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ActivityUtilities.exitRight(this);
	}
*/

	protected void generateLayout() {
		dummyView = view.findViewById(R.id.dummyview);
		gamesSearchView = (SearchView) view.findViewById(R.id.searchview_games_list);
		gamesGridView = (GridView) view.findViewById(R.id.listview_games);
		gamesGridView.setFastScrollEnabled(true);
		gamesList = new ArrayList<>();

		// TODO Move to a background thread
		DataManager dataManager = DataManager.getInstance(activity.getApplication());
		gamesList = dataManager.getAllGamesCombined();

		populateGrid(gamesList);

		gamesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if (newText != null && !newText.equals("")) {
					List<String> games = new ArrayList<>();
					for (String game : gamesList)
						if (!game.startsWith("---") &&
								game.toLowerCase().substring(0, game.length() - 2).contains(newText.toLowerCase()))
							games.add(game);
					StringUtilities.sortList(games);
					StringUtilities.padGamesList(games);
					populateGrid(games);
				} else populateGrid(gamesList);
				return false;
			}
		});

		setColors();
		colorComponents();

		ActivityUtilities.setDatabaseChanged(activity, false);

	}

	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);
		gamesSearchView.setBackgroundColor(backgroundColor);
		for (TextView textView : ViewUtilities.findChildrenByClass(gamesSearchView, TextView.class)) {
//			textView.setBackgroundColor(ColorUtilities.adjustBasedOnHSV(backgroundColor));
			textView.setTextColor(foregroundColor);
			textView.setHintTextColor(hintTextColor);
		}
		for (ImageView imageView : ViewUtilities.findChildrenByClass(gamesSearchView, ImageView.class))
			imageView.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_ATOP);
	}

	private void populateGrid(final List<String> games) {
		gamesGridView.setNumColumns(Preferences.numberOfGridColumns(activity));
		gamesGridView.setAdapter(new ImageAdapter(activity, games));

		gamesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dummyView.requestFocus();
				game = games.get(position);
				if (!game.startsWith("---")) {
					final String gameType = game.substring(game.length() - 1);
					game = game.substring(0, game.length() - 2);

					String thumbnailUrl = "";
					switch (gameType) {
						case "b":
							thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, game);
							break;
						case "r":
							thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, game);
							break;
						case "v":
							thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, game);
							break;
					}

					final Bitmap thumbnail = new ImageController(activity)
							.setDirectoryName("thumbnails")
							.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
							.load();
					if (Preferences.generatePalette(activity) && thumbnail != null) {
						final String finalThumbnailUrl = thumbnailUrl;
						Palette.from(thumbnail).generate(new Palette.PaletteAsyncListener() {
							@Override
							public void onGenerated(Palette palette) {
								List<Palette.Swatch> swatchList = new ArrayList<>();
								if (palette.getDarkVibrantSwatch() != null)
									swatchList.add(palette.getDarkVibrantSwatch());
								if (palette.getDarkMutedSwatch() != null)
									swatchList.add(palette.getDarkMutedSwatch());
								if (palette.getMutedSwatch() != null)
									swatchList.add(palette.getMutedSwatch());
								if (palette.getVibrantSwatch() != null)
									swatchList.add(palette.getVibrantSwatch());
								if (palette.getLightMutedSwatch() != null)
									swatchList.add(palette.getLightMutedSwatch());
								if (palette.getLightVibrantSwatch() != null)
									swatchList.add(palette.getLightVibrantSwatch());

								Palette.Swatch swatch;
								if (Preferences.lightUI(activity))
									swatch = swatchList.get(swatchList.size() - 1);
								else swatch = swatchList.get(0);

								Preferences.setGeneratedPaletteColors(activity,
																	  swatch.getRgb(),
																	  swatch.getBodyTextColor());

								navigateToGameDetails(gameType, finalThumbnailUrl);
							}
						});
					} else navigateToGameDetails(gameType, thumbnailUrl);
				}
			}
		});


		gamesGridView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (Preferences.useSwipes(v.getContext()))
					return gestureDetector.onTouchEvent(event);
				return false;
			}
		});

	}

	private void navigateToGameDetails(String gameType, String thumbnailUrl) {
		getRouter().pushController(RouterTransaction.with(new GameDataController(game,
																				 gameType,
																				 thumbnailUrl))
													.pushChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.RIGHT))
													.popChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.RIGHT)));
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (Math.abs(velocityX) > Math.abs(velocityY)) {
				if (Math.abs(e1.getX() - e2.getX()) >= 200) {
					if (velocityX > 2000) {
						getRouter().popCurrentController();
						return true;
					}
				}
			}
			return false;
		}
	}

}