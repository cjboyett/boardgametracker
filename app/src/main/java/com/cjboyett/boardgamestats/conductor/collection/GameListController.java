package com.cjboyett.boardgamestats.conductor.collection;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.conductor.addgame.AddGameController;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.conductor.changehandlers.DirectionalChangeHandler;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.ImageRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class GameListController extends BaseController implements ImageRecyclerAdapter.GamesGridView {
	private Activity activity;
	private RecyclerView gamesGridView;
	private SearchView gamesSearchView;
	private View view, dummyView;

	private List<String> gamesList;

	private String game;

	private GamesDbHelper dbHelper;

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

		getToolbar().setTitle("Collection");

		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	protected void onDetach(@NonNull View view) {
		dbHelper.close();
		super.onDetach(view);
	}

	@Override
	protected void onDestroy() {
		if (dbHelper != null) dbHelper.close();
		super.onDestroy();
	}

	protected void generateLayout() {
		dummyView = view.findViewById(R.id.dummyview);
		gamesSearchView = (SearchView) view.findViewById(R.id.searchview_games_list);
		gamesGridView = (RecyclerView) view.findViewById(R.id.listview_games);
		Timber.d((gamesList == null) + "");
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

		gamesSearchView.setQuery("", true);

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
		gamesGridView.setAdapter(new ImageRecyclerAdapter(this, games, foregroundColor));
		gamesGridView.setLayoutManager(new GridLayoutManager(activity, Preferences.numberOfGridColumns(activity)));
	}

	public void navigateToGameDetails(String game, String gameType, String thumbnailUrl, String bggId) {
		getRouter().pushController(RouterTransaction.with(new GameDataController(game,
																				 gameType,
																				 thumbnailUrl))
													.pushChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.RIGHT))
													.popChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.RIGHT)));
/*
		getRouter().pushController(RouterTransaction.with(new GameDataController2(game,
																				  gameType,
																				  thumbnailUrl, bggId))
													.pushChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.RIGHT))
													.popChangeHandler(DirectionalChangeHandler.from(
															DirectionalChangeHandler.RIGHT)));
*/
	}

	public void navigateToGameDetails(String gameType, String thumbnailUrl, String bggId) {
		navigateToGameDetails(game, gameType, thumbnailUrl, bggId);
	}
}