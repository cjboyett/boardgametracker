package com.cjboyett.boardgamestats.activity.statsoverview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.statsdetail.GameStatsActivity;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.ImageRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class GameStatsListFragment extends Fragment implements ImageRecyclerAdapter.GamesGridView {
	private Activity activity;
	private View view;
	private LinearLayout dummyView;
	private TextView textView;
	private RecyclerView gridView;
	private SearchView gamesSearchView;
	private int scrollY;

	private int backgroundColor, foregroundColor, hintTextColor;

	private GamesDbHelper dbHelper;

	private boolean regenerateLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.activity_game_stats_list, null);
		activity = this.getActivity();

		dbHelper = new GamesDbHelper(activity);

		generateLayout();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		dbHelper = new GamesDbHelper(activity);

		if (regenerateLayout) {
			generateLayout();
			regenerateLayout = false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		dbHelper.close();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (dbHelper != null) dbHelper.close();
	}

	public void setRegenerateLayout(boolean regenerateLayout) {
		this.regenerateLayout = regenerateLayout;
	}

	private void generateLayout() {
		dummyView = (LinearLayout) view.findViewById(R.id.dummyview);
		textView = (TextView) view.findViewById(R.id.textview_game_play);
		gridView = (RecyclerView) view.findViewById(R.id.listview_game_stats);
		gamesSearchView = (SearchView) view.findViewById(R.id.searchview_games_stats);

		DataManager dataManager = DataManager.getInstance(activity.getApplication());
		List<String> gamesList = dataManager.getAllPlayedGamesCombined();

		final List<String> finalGamesList = gamesList;

		setColors();
		populateGrid(finalGamesList);

		gamesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if (newText != null && !newText.equals("")) {
					List<String> games = new ArrayList<>();
					for (String game : finalGamesList)
						if (!game.startsWith("---") &&
								game.toLowerCase().substring(0, game.length() - 2).contains(newText.toLowerCase()))
							games.add(game);
					StringUtilities.sortList(games);
					StringUtilities.padGamesList(games);
					populateGrid(games);
				} else populateGrid(finalGamesList);
				return false;
			}
		});

		colorComponents();

		ActivityUtilities.setDatabaseChanged(activity, false);
	}

	private void setColors() {
		backgroundColor = Preferences.getBackgroundColor(activity);
		foregroundColor = Preferences.getForegroundColor(activity);
		hintTextColor = Preferences.getHintTextColor(activity);
	}

	private void colorComponents() {
		view.setBackgroundColor(backgroundColor);

		textView.setBackgroundColor(backgroundColor);
		textView.setTextColor(foregroundColor);

		gridView.setBackgroundColor(backgroundColor);

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
		gridView.setAdapter(new ImageRecyclerAdapter(this, games, foregroundColor));
		gridView.setLayoutManager(new GridLayoutManager(activity, Preferences.numberOfGridColumns(activity)));

	}

	@Override
	public void navigateToGameDetails(String game, String gameType, String thumbnailUrl, String bggId) {
		ActivityUtilities.generatePaletteAndOpenActivity(activity,
														 new Intent(view.getContext(),
																	GameStatsActivity.class)
																 .putExtra("GAME", game)
																 .putExtra("TYPE", gameType),
														 "http://" + thumbnailUrl,
														 "UP");
		ActivityUtilities.exitUp(activity);
	}
}
