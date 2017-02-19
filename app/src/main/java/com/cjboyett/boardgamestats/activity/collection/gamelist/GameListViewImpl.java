package com.cjboyett.boardgamestats.activity.collection.gamelist;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.addgame.AddGameScreen;
import com.cjboyett.boardgamestats.activity.base.BaseViewController;
import com.cjboyett.boardgamestats.activity.base.ScoopActivity;
import com.cjboyett.boardgamestats.activity.collection.gamedata.GameDataScreen;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.ImageAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class GameListViewImpl extends BaseViewController implements GameListView {
	private GridView gamesGridView;
	private SearchView gamesSearchView;
	private View view, dummyView;

	private List<String> gamesList;

	private String game;

	private GamesDbHelper dbHelper;
	private GestureDetectorCompat gestureDetector;

/*
	public GameListActivity() {
		super("ca-app-pub-1437859753538305/9571180678");
	}
*/

	@Override
	public void onAttach() {
		super.onAttach();
		view = getView();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		dbHelper = new GamesDbHelper(getActivity());
		gestureDetector = new GestureDetectorCompat(getActivity(), new GameListViewImpl.ScrollGestureListener());
		getActivity().setGestureDetector(gestureDetector);

		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	public void onDetach() {
		if (dbHelper != null) dbHelper.close();
		super.onDetach();
	}

/*
	@Override
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
		DataManager dataManager = DataManager.getInstance(getActivity().getApplication());
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

		ActivityUtilities.setDatabaseChanged(getActivity(), false);
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
/*
		GridLayoutManager layoutManager = new GridLayoutManager(this, Preferences.numberOfGridColumns(this));
		gamesGridView.setLayoutManager(layoutManager);

		ImageRecyclerAdapter adapter = new ImageRecyclerAdapter(this, games);
		gamesGridView.setAdapter(adapter);
*/


		gamesGridView.setNumColumns(Preferences.numberOfGridColumns(getActivity()));
		gamesGridView.setAdapter(new ImageAdapter(getActivity(), games));

		gamesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dummyView.requestFocus();
				game = games.get(position);
				if (!game.startsWith("---")) {
					String gameType = game.substring(game.length() - 1);
					game = game.substring(0, game.length() - 2);

					String thumbnailUrl = "";
					if (gameType.equals("b"))
						thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, game);
					else if (gameType.equals("r"))
						thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, game);
					else if (gameType.equals("v"))
						thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, game);

//					if (thumbnailUrl != null && !thumbnailUrl.equals("")) {
/*
						ActivityUtilities.generatePaletteAndOpenActivity(getActivity(),
																		 new Intent(view.getContext(),
																					GameDataActivity.class)
																				 .putExtra("GAME", game)
																				 .putExtra("TYPE", gameType),
																		 "http://" + thumbnailUrl,
																		 "LEFT");
						ActivityUtilities.exitLeft(getActivity());
*/

//					} else {
//					}
/*
						startActivity(new Intent(view.getContext(), GameDataActivity.class)
											  .putExtra("GAME", game)
											  .putExtra("TYPE", gameType));
					ActivityUtilities.exitLeft(activity);
*/
					appRouter.goTo(new GameDataScreen(new Intent().putExtra("GAME", game)
																  .putExtra("TYPE", gameType)
					));
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

	@OnClick(R.id.fab)
	protected void onFabClick(View view) {
		//					startActivity(new Intent(view.getContext(), AddGameActivity.class));
		//					ActivityUtilities.exitUp(activity);
		appRouter.goTo(new AddGameScreen(new Intent().putExtra("GAME", "King of New York").putExtra("TYPE", "b")));
		Toast.makeText(getActivity(), "AHHHHH", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected int layoutId() {
		return R.layout.activity_game_list;
	}

	@Override
	public ScoopActivity getActivity() {
		return getScoop().findService(ScoopActivity.ACTIVITY_SERVICE);
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
//						onBackPressed();
						appRouter.goBack();
						return true;
					}
				}
			}
			return false;
		}
	}

}
