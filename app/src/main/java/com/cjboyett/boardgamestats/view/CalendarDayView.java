package com.cjboyett.boardgamestats.view;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameStatsDbUtility;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ColorUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 3/28/2016.
 */
public class CalendarDayView extends FrameLayout
{
	private CalendarDayView dayView = this;
	private List<Long> boardGamePlayIds, rpgPlayIds, videoGamePlayIds;

	public CalendarDayView(Context context)
	{
		super(context);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.calendar_day_view, this);

		boardGamePlayIds = new ArrayList<>();
		rpgPlayIds = new ArrayList<>();
		videoGamePlayIds = new ArrayList<>();

		((TextView)findViewById(R.id.textview_piechart)).setTextColor(ColorUtilities.mixWithBaseColor(Color.BLACK, 2, Preferences.getForegroundColor(getContext()), 1));
//		((TextView)findViewById(R.id.textview_piechart)).setShadowLayer(24, 1, 1, ColorUtilities.mixWithBaseColor(Color.WHITE, 2, Preferences.getHintTextColor(getContext()), 1));
//		((TextView)findViewById(R.id.textview_piechart)).setBackground(new ShapeDrawable(new S));
	}

	public void setDay(int day)
	{
		((TextView)findViewById(R.id.textview_calendar_day)).setText(day + "");
	}

	public void setDifferentMonth(boolean diff)
	{
		if (diff) ((TextView)findViewById(R.id.textview_calendar_day)).setTextColor(Color.LTGRAY);
		else ((TextView)findViewById(R.id.textview_calendar_day)).setTextColor(Color.BLACK);
	}

	public List<Long> getBoardGamePlayIds()
	{
		return boardGamePlayIds;
	}

	public List<Long> getRPGPlayIds()
	{
		return rpgPlayIds;
	}

	public List<Long> getVideoGamePlayIds()
	{
		return videoGamePlayIds;
	}

	public void addBoardGamePlayId(long id)
	{
		boardGamePlayIds.add(id);
//		adjustCalendarButton();
	}

	public void addRPGPlayId(long id)
	{
		rpgPlayIds.add(id);
//		adjustCalendarButton();
	}

	public void addVideoGamePlayId(long id)
	{
		videoGamePlayIds.add(id);
//		adjustCalendarButton();
	}

	public void adjustCalendarButton()
	{
		if (!boardGamePlayIds.isEmpty() || !rpgPlayIds.isEmpty() || !videoGamePlayIds.isEmpty())
		{
			final View playCountView = findViewById(R.id.layout_piechart);
			final int boardGamePlays = boardGamePlayIds.size();
			final int rpgPlays = rpgPlayIds.size();
			final int videoGamePlays = videoGamePlayIds.size();
			int totalPlays = boardGamePlays + rpgPlays + videoGamePlays;

			if (totalPlays < 100) ((TextView) findViewById(R.id.textview_piechart)).setText(totalPlays + "");
			else ((TextView) findViewById(R.id.textview_piechart)).setText("99+");

			final int backgroundColor = Preferences.getBackgroundColor(getContext());
			final PieChart pieChart = ((PieChart) findViewById(R.id.piechart_calendar_day));
			pieChart.clearItems();
//			pieChart.setHighlightStrength(0.9f);

			final GamesDbHelper dbHelper = new GamesDbHelper(getContext());

			new AsyncTask<String, Void, Void>()
			{
				int boardGameTimePlayed, rpgTimePlayed, videoGameTimePlayed;

				@Override
				protected Void doInBackground(String... params)
				{
					// TODO Do a quicker job of getting time played on a given day.

					if (boardGamePlays > 0)
					{
						for (int i = 0; i < boardGamePlayIds.size(); i++)
							boardGameTimePlayed += BoardGameStatsDbUtility.timePlayed(dbHelper, boardGamePlayIds.get(i));
					}

					if (rpgPlays > 0)
					{
						for (int i = 0; i < rpgPlayIds.size(); i++)
							rpgTimePlayed += RPGStatsDbUtility.timePlayed(dbHelper, rpgPlayIds.get(i));
					}

					if (videoGamePlays > 0)
					{
						for (int i = 0; i < videoGamePlayIds.size(); i++)
							videoGameTimePlayed += VideoGameStatsDbUtility.timePlayed(dbHelper, videoGamePlayIds.get(i));
					}

					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid)
				{
					if (boardGameTimePlayed != 0)
						pieChart.addItem("Board",
						                 1f,
						                 ColorUtilities.mixWithBaseColor(ColorUtilities.pieSliceColor(0, boardGameTimePlayed),
						                                                 4,
						                                                 backgroundColor,
						                                                 1));

					if (rpgTimePlayed != 0)
						pieChart.addItem("Board",
						                 1f,
						                 ColorUtilities.mixWithBaseColor(ColorUtilities.pieSliceColor(1, rpgTimePlayed),
						                                                 4,
						                                                 backgroundColor,
						                                                 1));

					if (videoGameTimePlayed != 0)
						pieChart.addItem("Board",
						                 1f,
						                 ColorUtilities.mixWithBaseColor(ColorUtilities.pieSliceColor(2, videoGameTimePlayed),
						                                                 4,
						                                                 backgroundColor,
						                                                 1));

					playCountView.setVisibility(VISIBLE);

					dbHelper.close();
				}
			}.execute("");
		}
	}

	public void clearIds()
	{
		boardGamePlayIds.clear();
		rpgPlayIds.clear();
		videoGamePlayIds.clear();
		findViewById(R.id.layout_piechart).setVisibility(GONE);
//		findViewById(R.id.button_calendar_day).setVisibility(GONE);
//		pieGraph.setVisibility(GONE);
	}

	@Override
	public void setOnClickListener(OnClickListener l)
	{
		super.setOnClickListener(l);
		findViewById(R.id.layout_piechart).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dayView.callOnClick();
			}
		});
		findViewById(R.id.piechart_calendar_day).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dayView.callOnClick();
			}
		});
		findViewById(R.id.textview_piechart).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dayView.callOnClick();
			}
		});
/*
		findViewById(R.id.button_calendar_day).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dayView.callOnClick();
			}
		});
*/
		findViewById(R.id.textview_calendar_day).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dayView.callOnClick();
			}
		});
	}
}
