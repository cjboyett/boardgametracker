package com.cjboyett.boardgamestats.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.GamesDbUtility;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.PlayerListAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatsListFragment extends Fragment {
	private View view;
	private ListView listView;
	private LinearLayout dummyView;
	private List<String> finalPlayersList;
	private SearchView searchView;
	private int scrollY;

	private GamesDbHelper dbHelper;
	private GestureDetectorCompat gestureDetector;

	private int backgroundColor, foregroundColor, hintTextColor;

	private boolean regenerateLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.activity_player_stats_list, null);
		listView = (ListView) view.findViewById(R.id.listview_player_stats);
		dummyView = (LinearLayout) view.findViewById(R.id.dummyview);
		searchView = (SearchView) view.findViewById(R.id.searchview_players);

		dbHelper = new GamesDbHelper(getActivity());
		gestureDetector = new GestureDetectorCompat(getContext(), new ScrollGestureListener());

		generateLayout();

		backgroundColor = Preferences.getBackgroundColor(getContext());
		foregroundColor = Preferences.getForegroundColor(getContext());
		hintTextColor = Preferences.getHintTextColor(getContext());

		colorComponents();
		;

//		Log.d("MAX MEMORY" ,(int)(Runtime.getRuntime().maxMemory() / 1024) + "");
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		dbHelper = new GamesDbHelper(getActivity());
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

	private void colorComponents() {
		view.setBackgroundColor(backgroundColor);

		searchView.setBackgroundColor(backgroundColor);
		for (TextView textView : ViewUtilities.findChildrenByClass(searchView, TextView.class)) {
//			textView.setBackgroundColor(ColorUtilities.adjustBasedOnHSV(backgroundColor));
			textView.setTextColor(foregroundColor);
			textView.setHintTextColor(hintTextColor);
		}
		for (ImageView imageView : ViewUtilities.findChildrenByClass(searchView, ImageView.class))
			imageView.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_ATOP);

	}

	public void setRegenerateLayout(boolean regenerateLayout) {
		this.regenerateLayout = regenerateLayout;
	}

	private void generateLayout() {
		finalPlayersList = new ArrayList<>();

		new AsyncTask<String, Void, List<String>>() {
			@Override
			protected List<String> doInBackground(String... params) {
				final List<String> playersList = GamesDbUtility.getAllPlayersSorted(dbHelper, new Date(1970, 0, 1));
				playersList.remove("master_user");
				return playersList;
			}

			@Override
			protected void onPostExecute(final List<String> playersList) {
//				listView.setAdapter(new CustomArrayAdapter<String>(getContext(), R.layout.player_list_item, playersList));
				finalPlayersList = new ArrayList<String>(playersList);
				populateListView(playersList);
			}
		}.execute("");

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if (newText != null && !newText.equals("")) {
					List<String> players = new ArrayList<>();
					for (String player : finalPlayersList)
						if (player.toLowerCase().contains(newText.toLowerCase()))
							players.add(player);
//					StringUtilities.sortList(games);
//					StringUtilities.padGamesList(games);
					populateListView(players);
				} else populateListView(finalPlayersList);
				return false;
			}
		});

		ActivityUtilities.setDatabaseChanged(getActivity(), false);
	}

	private void populateListView(final List<String> playersList) {
		listView.setAdapter(new PlayerListAdapter(getActivity(), playersList));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startActivity(new Intent(view.getContext(), PlayerStatsActivity.class).putExtra("NAME",
																								playersList.get(position)));
				ActivityUtilities.exitUp(getActivity());
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
					if (velocityY > 2000 && scrollY <= 0) {
						getActivity().onBackPressed();
						return true;
					}
				}
			}
			return false;
		}
	}

	private class CustomArrayAdapter<String> extends ArrayAdapter<String> {
		public CustomArrayAdapter(Context context, int resource, List<String> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			view.setBackgroundColor(backgroundColor);
			view.findViewById(android.R.id.text1).setBackgroundColor(backgroundColor);
			((TextView) view.findViewById(android.R.id.text1)).setTextColor(foregroundColor);
			return view;
		}
	}
}