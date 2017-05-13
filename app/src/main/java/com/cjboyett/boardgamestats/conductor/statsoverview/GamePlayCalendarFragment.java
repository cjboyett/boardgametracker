package com.cjboyett.boardgamestats.conductor.statsoverview;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameContract;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.view.CalendarView;
import com.cjboyett.boardgamestats.view.MonthlyGamePlayView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import timber.log.Timber;

public class GamePlayCalendarFragment extends Fragment {
	private Activity activity;
	private View view;
	private ScrollView scrollView;
	private int scrollY;
	private CalendarView calendarView;
	private GamesDbHelper dbHelper;
	private List<Long> gamePlayIds;
	private Map<Long, Date> gamePlayDates;

	GestureDetectorCompat gestureDetector;

	private boolean regenerateLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		activity = this.getActivity();

		view = inflater.inflate(R.layout.activity_game_play_list, null);

		dbHelper = new GamesDbHelper(activity);
		gestureDetector = new GestureDetectorCompat(getContext(), new ScrollGestureListener());

		calendarView = new CalendarView(activity);
//		calendarView.setParent(this);
		LinearLayout.LayoutParams layoutParams =
				new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;
		calendarView.setLayoutParams(layoutParams);
		((LinearLayout) view.findViewById(R.id.linear_layout_game_plays)).addView(calendarView);

		view.setBackgroundColor(Preferences.getBackgroundColor(activity));

		scrollView = (ScrollView) view.findViewById(R.id.scrollview_game_plays);

		scrollView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (Preferences.useSwipes(v.getContext()))
					return gestureDetector.onTouchEvent(event);
				return false;
			}
		});

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
		if (calendarView != null) calendarView.populateCalendar();
		else {
			((LinearLayout) view.findViewById(R.id.linear_layout_game_plays)).removeAllViews();
			generatePlayList();
		}

		ActivityUtilities.setDatabaseChanged(activity, false);
	}

	private void generatePlayList() {
		gamePlayDates = new TreeMap<>();
		gamePlayIds = new ArrayList<>();

		Cursor gamePlayCursor = dbHelper.getReadableDatabase()
										.query(BoardGameContract.GamePlayEntry.TABLE_NAME,
											   new String[]{BoardGameContract.GamePlayEntry.DATE,
															BoardGameContract.GamePlayEntry._ID},
											   null,
											   null,
											   null,
											   null,
											   BoardGameContract.GamePlayEntry.DATE + " ASC");
		while (gamePlayCursor.moveToNext()) {
			gamePlayIds.add(gamePlayCursor.getLong(1));
			gamePlayDates.put(gamePlayCursor.getLong(1), new Date(gamePlayCursor.getString(0)));
		}
		gamePlayCursor.close();

		new MakeCalendarTask().execute();
	}

	private Map<String, List<Long>> sortIds(List<Long> gamePlayIds) {
		Map<String, List<Long>> sortedIds = new TreeMap<>(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return -lhs.compareTo(rhs);
			}
		});

		for (long id : gamePlayIds) {
			Date date = gamePlayDates.get(id);
			if (!sortedIds.containsKey(date.rawYearAndMonth()))
				sortedIds.put(date.rawYearAndMonth(), new ArrayList<Long>());
			sortedIds.get(date.rawYearAndMonth()).add(id);
		}

		for (String date : sortedIds.keySet()) {
			Collections.sort(sortedIds.get(date), new Comparator<Long>() {
				@Override
				public int compare(Long lhs, Long rhs) {
					Date lhsDate = gamePlayDates.get(lhs);
					Date rhsDate = gamePlayDates.get(rhs);
					return lhsDate.rawDate().compareTo(rhsDate.rawDate());
				}
			});
		}

		return sortedIds;
	}

	private class MakeCalendarTask extends AsyncTask<String, Void, String> {
		private Map<String, List<Long>> idsByDate;

		@Override
		protected String doInBackground(String... params) {
			idsByDate = sortIds(gamePlayIds);
			return null;
		}

		@Override
		protected void onPostExecute(String string) {
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
																				   ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER;

			layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
														 ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(5, 5, 5, 5);
			for (String s : idsByDate.keySet()) {
				Date monthAndYear = gamePlayDates.get(idsByDate.get(s).get(0));
				MonthlyGamePlayView monthlyGamePlayView =
						new MonthlyGamePlayView(activity, monthAndYear.getMonthAndYear());
				monthlyGamePlayView.setLayoutParams(layoutParams);

				for (long id : idsByDate.get(s)) monthlyGamePlayView.addBoardGamePlayId(id);

				((LinearLayout) view.findViewById(R.id.linear_layout_game_plays)).addView(monthlyGamePlayView);
			}
		}
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			scrollY = scrollView.getScrollY(); //scrollView.getScrollY();
			Timber.d(scrollY + "");
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Timber.d(scrollY + "");
			if (Math.abs(velocityX) < Math.abs(velocityY)) {
				try {
					if (Math.abs(e1.getY() - e2.getY()) >= 200) {
						if (velocityY > 2000 && scrollY == 0) {
							getActivity().onBackPressed();
							return true;
						}
					}
				} catch (Exception e) {
					Timber.e(e);
				}
			}
			return false;
		}
	}
}
