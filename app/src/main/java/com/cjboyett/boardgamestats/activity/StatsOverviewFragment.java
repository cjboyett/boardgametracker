package com.cjboyett.boardgamestats.activity;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.model.stats.BlankStatistic;
import com.cjboyett.boardgamestats.model.stats.GamePlayStatistics;
import com.cjboyett.boardgamestats.model.stats.HScoreStatistic;
import com.cjboyett.boardgamestats.model.stats.MostLostPlayersStatistic;
import com.cjboyett.boardgamestats.model.stats.MostPlayTimeGamesStatistic;
import com.cjboyett.boardgamestats.model.stats.MostPlayTimePlayersStatistic;
import com.cjboyett.boardgamestats.model.stats.MostPlayedGamesStatistic;
import com.cjboyett.boardgamestats.model.stats.MostPlayedPlayersStatistic;
import com.cjboyett.boardgamestats.model.stats.MostWonGamesStatistic;
import com.cjboyett.boardgamestats.model.stats.Statistic;
import com.cjboyett.boardgamestats.model.stats.StatisticsManager;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ColorUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.StatsRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;


public class StatsOverviewFragment extends Fragment {
	private View view;
	private RecyclerView recyclerView;
	private ImageView boardGamesLightLens, rpgsLightLens, videoGamesLightLens;
	private AppCompatImageView boardGamesLight, rpgsLight, videoGamesLight;
	private int backgroundColor, foregroundColor, hintTextColor;
	private int boardGamesLightOnColor, boardGamesLightOffColor, rpgsLightOnColor, rpgsLightOffColor,
			videoGamesLightOnColor, videoGamesLightOffColor;
	private boolean boardGamesLightOn, rpgsLightOn, videoGamesLightOn;

	private GamesDbHelper dbHelper;
	private StatisticsManager statisticsManager;

	private boolean regenerateLayout;

	private GestureDetectorCompat gestureDetector;
	private int scrollY;

	public StatsOverviewFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		dbHelper = new GamesDbHelper(getContext());
		view = inflater.inflate(R.layout.fragment_stats_overview, container, false);
		recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_stats_overview);
		gestureDetector = new GestureDetectorCompat(getActivity(), new ScrollGestureListener());

		recyclerView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (Preferences.useSwipes(v.getContext()))
					return gestureDetector.onTouchEvent(event);
				return false;
			}
		});

		generateLayout();
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

	private void generateLayout() {
		setColors();

		statisticsManager = StatisticsManager.getInstance(getActivity());

		resetLayout();

		boardGamesLightOn = Preferences.useBoardGamesForStats(getActivity());
		rpgsLightOn = Preferences.useRPGsForStats(getActivity());
		videoGamesLightOn = Preferences.useVideoGamesForStats(getActivity());

		boardGamesLightLens = (ImageView) view.findViewById(R.id.imageview_board_games_light_lens);
		rpgsLightLens = (ImageView) view.findViewById(R.id.imageview_rpgs_light_lens);
		videoGamesLightLens = (ImageView) view.findViewById(R.id.imageview_video_games_light_lens);

		boardGamesLight = (AppCompatImageView) view.findViewById(R.id.imageview_board_games_light);
		rpgsLight = (AppCompatImageView) view.findViewById(R.id.imageview_rpgs_light);
		videoGamesLight = (AppCompatImageView) view.findViewById(R.id.imageview_video_games_light);

		boardGamesLightLens.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (boardGamesLightOn)
					ViewUtilities.tintImageView(boardGamesLight, boardGamesLightOffColor);
				else
					ViewUtilities.tintImageView(boardGamesLight, boardGamesLightOnColor);

				boardGamesLightOn = !boardGamesLightOn;
				Preferences.setUseBoardGamesForStats(getActivity(), boardGamesLightOn);

				resetLayout();
			}
		});

		rpgsLightLens.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (rpgsLightOn)
					ViewUtilities.tintImageView(rpgsLight, rpgsLightOffColor);
				else
					ViewUtilities.tintImageView(rpgsLight, rpgsLightOnColor);

				rpgsLightOn = !rpgsLightOn;
				Preferences.setUseRPGsForStats(getActivity(), rpgsLightOn);

				resetLayout();
			}
		});

		videoGamesLightLens.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (videoGamesLightOn)
					ViewUtilities.tintImageView(videoGamesLight, videoGamesLightOffColor);
				else
					ViewUtilities.tintImageView(videoGamesLight, videoGamesLightOnColor);

				videoGamesLightOn = !videoGamesLightOn;
				Preferences.setUseVideoGamesForStats(getActivity(), videoGamesLightOn);

				resetLayout();
			}
		});

		colorComponents();
	}

	private void resetLayout() {
		List<Statistic> statisticList = new ArrayList<>();
		statisticList.add(new BlankStatistic(getActivity()));
		statisticList.add(new GamePlayStatistics(getActivity(), statisticsManager));
		if (statisticsManager.getGamesTimesPlayed() > 0) {
//		statisticList.add(new GameMetricStatistic(getActivity(), statisticsManager));
			statisticList.add(new HScoreStatistic(getActivity(), statisticsManager));
			statisticList.add(new MostPlayedGamesStatistic(getActivity(), statisticsManager));
			statisticList.add(new MostPlayTimeGamesStatistic(getActivity(), statisticsManager));
			statisticList.add(new MostWonGamesStatistic(getActivity(), statisticsManager));
			statisticList.add(new MostPlayedPlayersStatistic(getActivity(), statisticsManager));
			statisticList.add(new MostPlayTimePlayersStatistic(getActivity(), statisticsManager));
			statisticList.add(new MostLostPlayersStatistic(getActivity(), statisticsManager));
		}
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(layoutManager);

		StatsRecyclerAdapter adapter = new StatsRecyclerAdapter(statisticList,
																ColorUtilities.adjustBasedOnHSV(backgroundColor),
																foregroundColor,
																hintTextColor);
		recyclerView.setAdapter(adapter);
	}

	public void setRegenerateLayout(boolean regenerateLayout) {
		this.regenerateLayout = regenerateLayout;
	}

	private void setColors() {
		backgroundColor = Preferences.getBackgroundColor(getActivity());
		foregroundColor = Preferences.getForegroundColor(getActivity());
		hintTextColor = Preferences.getHintTextColor(getActivity());

		boardGamesLightOnColor = ColorUtilities.mixWithBaseColor(Color.GREEN, 5, backgroundColor, 1);
		rpgsLightOnColor = ColorUtilities.mixWithBaseColor(Color.RED, 5, backgroundColor, 1);
		videoGamesLightOnColor = ColorUtilities.mixWithBaseColor(Color.BLUE, 5, backgroundColor, 1);

		boardGamesLightOffColor = ColorUtilities.mixWithBaseColor(boardGamesLightOnColor, 2, Color.BLACK, 3);
		rpgsLightOffColor = ColorUtilities.mixWithBaseColor(rpgsLightOnColor, 2, Color.BLACK, 3);
		videoGamesLightOffColor = ColorUtilities.mixWithBaseColor(videoGamesLightOnColor, 2, Color.BLACK, 3);
	}

	private void colorComponents() {

		view.setBackgroundColor(backgroundColor);

		if (boardGamesLightOn)
			ViewUtilities.tintImageView(boardGamesLight, boardGamesLightOnColor);
		else
			ViewUtilities.tintImageView(boardGamesLight, boardGamesLightOffColor);

		if (rpgsLightOn)
			ViewUtilities.tintImageView(rpgsLight, rpgsLightOnColor);
		else
			ViewUtilities.tintImageView(rpgsLight, rpgsLightOffColor);

		if (videoGamesLightOn)
			ViewUtilities.tintImageView(videoGamesLight, videoGamesLightOnColor);
		else
			ViewUtilities.tintImageView(videoGamesLight, videoGamesLightOffColor);
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			LinearLayout l = (LinearLayout) recyclerView.getChildAt(0);

			// Works here because we set the first stat as blank in adapter.
			scrollY =
					l.getChildCount(); //recyclerView.getScrollY(); //-c.getTop() + recyclerView.getFirstVisiblePosition() * c.getHeight();
			Log.d("DOWN", scrollY + "");
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.d("FLING", scrollY + "");
			try {
				if (Math.abs(velocityX) < Math.abs(velocityY)) {
					if (Math.abs(e1.getY() - e2.getY()) >= 200) {
						if (velocityY > 2000 && scrollY == 0) {
							getActivity().onBackPressed();
							return true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

}
