package com.cjboyett.boardgamestats.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;
import com.cjboyett.boardgamestats.utility.Preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Casey on 3/23/2016.
 */
public class MonthlyGamePlayView extends LinearLayout
{
	private Activity activity;
	List<Long> boardGamePlayIds, rpgPlayIds, videoGamePlayIds;
	private Map<String, DailyGamePlayView> days;
	private boolean showingDays = false;

	private int backgroundColor, foregroundColor;

	public MonthlyGamePlayView(Activity activity, String month)
	{
		super(activity);
		this.activity = activity;
		LayoutInflater inflater = LayoutInflater.from(activity);
		inflater.inflate(R.layout.monthly_game_play_view, this);

		boardGamePlayIds = new ArrayList<>();
		rpgPlayIds = new ArrayList<>();
		videoGamePlayIds = new ArrayList<>();

		days = new TreeMap<>();
		((TextView) findViewById(R.id.textview_month)).setText(month);

		findViewById(R.id.textview_month).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (showingDays) clearViews();
				else drawViews();
				showingDays = !showingDays;
			}
		});

		setColors();
		colorComponents();
	}

	private void setColors()
	{
		backgroundColor = Preferences.getBackgroundColor(getContext());
		foregroundColor = Preferences.getForegroundColor(getContext());
	}

	private void colorComponents()
	{
		setBackground(Preferences.getBackgroundDrawable(activity));
		((TextView)findViewById(R.id.textview_month)).setTextColor(foregroundColor);
		findViewById(R.id.textview_month).setBackgroundColor(backgroundColor);
	}

	public void addBoardGamePlayId(long id)
	{
		boardGamePlayIds.add(id);
	}

	public void addRPGPlayId(long id)
	{
		rpgPlayIds.add(id);
	}

	public void addVideoGamePlayId(long id)
	{
		videoGamePlayIds.add(id);
	}

	private void drawViews()
	{
		GamesDbHelper dbHelper = new GamesDbHelper(activity);
		String yearAndMonth = "";
		if (!boardGamePlayIds.isEmpty())
			yearAndMonth = BoardGameDbUtility.getDateById(dbHelper, boardGamePlayIds.get(0)).rawYearAndMonth().replace(" ", "");
		else if (!rpgPlayIds.isEmpty())
			yearAndMonth = RPGDbUtility.getDateById(dbHelper, rpgPlayIds.get(0)).rawYearAndMonth().replace(" ", "");
		if (!videoGamePlayIds.isEmpty())
			yearAndMonth = VideoGameDbUtility.getDateById(dbHelper, videoGamePlayIds.get(0)).rawYearAndMonth().replace(" ", "");

		Map<Long, BoardGamePlayData> boardGamePlayDataMap = BoardGameDbUtility.getGamePlay(dbHelper, yearAndMonth + "00", yearAndMonth + "99");
		Map<Long, RPGPlayData> rpgPlayDataMap = RPGDbUtility.getGamePlay(dbHelper, yearAndMonth + "00", yearAndMonth + "99");
		Map<Long, VideoGamePlayData> videoGamePlayDataMap = VideoGameDbUtility.getGamePlay(dbHelper, yearAndMonth + "00", yearAndMonth + "99");

		for (long id : boardGamePlayDataMap.keySet())
		{
			BoardGamePlayData boardGamePlayData = boardGamePlayDataMap.get(id);
			Date date = boardGamePlayData.getDate();
			if (!days.containsKey(date.rawDate()))
			{
				days.put(date.rawDate(),
						new DailyGamePlayView(activity, date.getDayOfWeek() + " " + date.getDayOfMonth()));
			}
			DailyGamePlayView dailyGamePlayView = days.get(date.rawDate());
			dailyGamePlayView.addBoardGamePlay(boardGamePlayData, id);
		}

		for (long id : rpgPlayDataMap.keySet())
		{
			RPGPlayData rpgPlayData = rpgPlayDataMap.get(id);
			Date date = rpgPlayData.getDate();
			if (!days.containsKey(date.rawDate()))
			{
				days.put(date.rawDate(),
						new DailyGamePlayView(activity, date.getDayOfWeek() + " " + date.getDayOfMonth()));
			}
			DailyGamePlayView dailyGamePlayView = days.get(date.rawDate());
			dailyGamePlayView.addBoardGamePlay(rpgPlayData, id);
		}

		for (long id : videoGamePlayDataMap.keySet())
		{
			VideoGamePlayData videoGamePlayData = videoGamePlayDataMap.get(id);
			Date date = videoGamePlayData.getDate();
			if (!days.containsKey(date.rawDate()))
			{
				days.put(date.rawDate(),
						new DailyGamePlayView(activity, date.getDayOfWeek() + " " + date.getDayOfMonth()));
			}
			DailyGamePlayView dailyGamePlayView = days.get(date.rawDate());
			dailyGamePlayView.addBoardGamePlay(videoGamePlayData, id);
		}

		for (DailyGamePlayView view : days.values()) addView(view);
		for (DailyGamePlayView view : days.values()) view.drawViews();
		dbHelper.close();
	}

	private void clearViews()
	{
		for (View view : days.values())
			((LinearLayout)findViewById(R.id.linear_layout_monthly_game_plays)).removeView(view);
		days = new TreeMap<>();
	}

	@Override
	public void addView(View child)
	{
		((LinearLayout)findViewById(R.id.linear_layout_monthly_game_plays)).addView(child);
	}
}
