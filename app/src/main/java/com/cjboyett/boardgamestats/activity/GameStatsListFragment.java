package com.cjboyett.boardgamestats.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.ImageAdapter;

import java.util.ArrayList;
import java.util.List;

public class GameStatsListFragment extends Fragment
{
	private Activity activity;
	private View view;
	private LinearLayout dummyView;
	private TextView textView;
	private GridView gridView;
	private SearchView gamesSearchView;
	private int scrollY;

	private int backgroundColor, foregroundColor, hintTextColor;

	private GamesDbHelper dbHelper;
	private GestureDetectorCompat gestureDetector;

	private boolean regenerateLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.activity_game_stats_list, null);
		activity = this.getActivity();

		dbHelper = new GamesDbHelper(activity);
		gestureDetector = new GestureDetectorCompat(activity, new ScrollGestureListener());

		generateLayout();

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		dbHelper = new GamesDbHelper(activity);

		if (regenerateLayout)
		{
			generateLayout();
			regenerateLayout = false;
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		dbHelper.close();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (dbHelper != null) dbHelper.close();
	}

	public void setRegenerateLayout(boolean regenerateLayout)
	{
		this.regenerateLayout = regenerateLayout;
	}

	private void generateLayout()
	{
		dummyView = (LinearLayout)view.findViewById(R.id.dummyview);
		textView = (TextView)view.findViewById(R.id.textview_game_play);
		gridView = (GridView)view.findViewById(R.id.listview_game_stats);
		gamesSearchView = (SearchView)view.findViewById(R.id.searchview_games_stats);

		DataManager dataManager = DataManager.getInstance(activity.getApplication());
		List<String> gamesList = dataManager.getAllPlayedGamesCombined();

		final List<String> finalGamesList = gamesList;

		populateGrid(finalGamesList);

		gamesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				if (newText != null && !newText.equals(""))
				{
					List<String> games = new ArrayList<>();
					for (String game : finalGamesList)
						if (!game.startsWith("---") && game.toLowerCase().substring(0, game.length() - 2).contains(newText.toLowerCase()))
							games.add(game);
					StringUtilities.sortList(games);
					StringUtilities.padGamesList(games);
					populateGrid(games);
				} else populateGrid(finalGamesList);
				return false;
			}
		});

		setColors();
		colorComponents();

		ActivityUtilities.setDatabaseChanged(activity, false);
	}

	private void setColors()
	{
		backgroundColor = Preferences.getBackgroundColor(activity);
		foregroundColor = Preferences.getForegroundColor(activity);
		hintTextColor = Preferences.getHintTextColor(activity);
	}

	private void colorComponents()
	{
		view.setBackgroundColor(backgroundColor);

		textView.setBackgroundColor(backgroundColor);
		textView.setTextColor(foregroundColor);

		gridView.setBackgroundColor(backgroundColor);

		gamesSearchView.setBackgroundColor(backgroundColor);
		for (TextView textView : ViewUtilities.findChildrenByClass(gamesSearchView, TextView.class))
		{
//			textView.setBackgroundColor(ColorUtilities.adjustBasedOnHSV(backgroundColor));
			textView.setTextColor(foregroundColor);
			textView.setHintTextColor(hintTextColor);
		}
		for (ImageView imageView : ViewUtilities.findChildrenByClass(gamesSearchView, ImageView.class))
			imageView.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_ATOP);
	}

	private void populateGrid(final List<String> games)
	{
		gridView.setNumColumns(Preferences.numberOfGridColumns(activity));
		gridView.setAdapter(new ImageAdapter(activity, games));

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				dummyView.requestFocus();
				if (!games.get(position).startsWith("---"))
				{
					textView.requestFocus();
					String game = games.get(position);
					String gameType = game.substring(game.length() - 1);
					game = game.substring(0, game.length() - 2);

					String thumbnailUrl = "";
					if (gameType.equals("b"))
						thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, game);
					else if (gameType.equals("r"))
						thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, game);
					else if (gameType.equals("v"))
						thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, game);
					ActivityUtilities.generatePaletteAndOpenActivity(activity,
							new Intent(view.getContext(), GameStatsActivity.class)
									.putExtra("GAME", game)
									.putExtra("TYPE", gameType),
							"http://" + thumbnailUrl,
							"UP");
					ActivityUtilities.exitUp(activity);
				}
			}
		});
		gridView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (Preferences.useSwipes(v.getContext()))
					return gestureDetector.onTouchEvent(event);
				return false;
			}
		});
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onDown(MotionEvent e)
		{
			View c = gridView.getChildAt(0);
			scrollY = -c.getTop() + gridView.getFirstVisiblePosition() * c.getHeight();
			Log.d("DOWN", scrollY + "");
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			Log.d("FLING", scrollY + "");
			if (Math.abs(velocityX) < Math.abs(velocityY))
			{
				if (Math.abs(e1.getY() - e2.getY()) >= 200)
				{
					if (velocityY > 2000 && scrollY == 0)
					{
						getActivity().onBackPressed();
						return true;
					}
				}
			}
			return false;
		}
	}

}
