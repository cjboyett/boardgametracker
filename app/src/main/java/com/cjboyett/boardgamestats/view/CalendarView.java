package com.cjboyett.boardgamestats.view;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cjboyett.boardgamestats.activity.GamePlayCalendarFragment;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.Date;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 3/28/2016.
 */
public class CalendarView extends TableLayout
{
	private GamePlayCalendarFragment parent;
	private int month, year, firstDayOfMonth;
	private Date currentDate;

	private GamesDbHelper dbHelper;
	private List<DailyGamePlayView> dailyGamePlayViews;
	private List<Long> boardGamePlayIds, rpgPlayIds, videoGamePlayIds;

	CalendarDayView[] days;

	private Map<Long, Date> boardGamePlayDates, rpgPlayDates, videoGamePlayDates;

	public CalendarView(Activity activity)
	{
		super(activity);
		LayoutInflater inflater = LayoutInflater.from(activity);
		inflater.inflate(R.layout.calendar_view, this);

		TableLayout calendar = (TableLayout)findViewById(R.id.table_layout_calendar);
		dbHelper = new GamesDbHelper(getContext());

		boardGamePlayIds = new ArrayList<>();
		rpgPlayIds = new ArrayList<>();
		videoGamePlayIds = new ArrayList<>();
		dailyGamePlayViews = new ArrayList<>();
		days = new CalendarDayView[7*6];

		TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.span = 7;

		for (int i=0;i<6;i++)
		{
			TableRow row = new TableRow(activity);
			row.setBackgroundColor(Color.WHITE);
			calendar.addView(row);

			TableRow dailyGamePlayRow = new TableRow(activity);
			DailyGamePlayView dailyGamePlayView = new DailyGamePlayView(activity, "");
			dailyGamePlayView.setBackground(activity.getResources().getDrawable(R.drawable.calendar_day_backround));
			dailyGamePlayView.setChildrenBackrground(Color.WHITE);
			dailyGamePlayView.setLayoutParams(layoutParams);
			dailyGamePlayView.setVisibility(GONE);
			dailyGamePlayViews.add(dailyGamePlayView);
			dailyGamePlayRow.addView(dailyGamePlayView);
			calendar.addView(dailyGamePlayRow);

			for (int j=0;j<7;j++)
			{
				CalendarDayView dayView = new CalendarDayView(activity);
				row.addView(dayView, j);
				days[7*i + j] = dayView;
			}
		}

		month = Calendar.getInstance().get(Calendar.MONTH);
		year = Calendar.getInstance().get(Calendar.YEAR);
		currentDate = new Date("19700001");

		populateCalendar();

		findViewById(R.id.imageview_next_month).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (month != 11) month++;
				else
				{
					month = 0;
					year++;
				}
				populateCalendar();
			}
		});

		findViewById(R.id.imageview_previous_month).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (month != 0) month--;
				else
				{
					month = 11;
					year--;
				}
				populateCalendar();
			}
		});
	}

	public void setParent(GamePlayCalendarFragment parent)
	{
		this.parent = parent;
	}

	private void setDailyGamePlayView(List<Long> dailyBoardGamePlayIds, List<Long> dailyRPGPlayIds, List<Long> dailyVideoGamePlayIds, int week)
	{
		if (!dailyBoardGamePlayIds.isEmpty() || !dailyRPGPlayIds.isEmpty() || !dailyVideoGamePlayIds.isEmpty())
		{
			Date newDate = null;
			if (!dailyBoardGamePlayIds.isEmpty())
				newDate = BoardGameDbUtility.getDateById(dbHelper, dailyBoardGamePlayIds.get(0));
			else if (!dailyRPGPlayIds.isEmpty() && newDate == null)
				newDate = RPGDbUtility.getDateById(dbHelper, dailyRPGPlayIds.get(0));
			else if (!dailyVideoGamePlayIds.isEmpty() && newDate == null)
				newDate = VideoGameDbUtility.getDateById(dbHelper, dailyVideoGamePlayIds.get(0));

			DailyGamePlayView dailyGamePlayView = dailyGamePlayViews.get(week);
			dailyGamePlayView.clearViews();
			dailyGamePlayView.clearIds();
			dailyGamePlayView.setDay(newDate.getDayOfWeek() + " " + newDate.getDayOfMonth());
			for (long id : dailyBoardGamePlayIds)
				dailyGamePlayView.addBoardGamePlay(BoardGameDbUtility.getGamePlay(dbHelper, id), id);
			for (long id : dailyRPGPlayIds)
				dailyGamePlayView.addRPGPlay(RPGDbUtility.getGamePlay(dbHelper, id), id);
			for (long id : dailyVideoGamePlayIds)
				dailyGamePlayView.addVideoGamePlay(VideoGameDbUtility.getGamePlay(dbHelper, id), id);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
				TransitionManager.beginDelayedTransition(this);
			}

			dailyGamePlayView.drawViews();
			for (int i = 0; i < dailyGamePlayViews.size(); i++)
			{
				if (i != week) dailyGamePlayViews.get(i).setVisibility(GONE);
			}
			if (dailyGamePlayView.getVisibility() == GONE) dailyGamePlayView.setVisibility(VISIBLE);
			else if (currentDate.equals(newDate)) dailyGamePlayView.setVisibility(GONE);

			currentDate = newDate;
		}
	}

	public void populateCalendar()
	{
		Calendar c = Calendar.getInstance();
		c.set(year, month, 1);
		firstDayOfMonth = (c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek()) % 7;
		int daysInMonth = Date.numberOfDaysInMonth[month];
		if (month == 1 && year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) daysInMonth++;
		int daysInLastMonth = Date.numberOfDaysInMonth[(month+11)%12];
		if (month-1 == 1 && year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) daysInLastMonth++;

		for (int i=0;i<daysInMonth;i++)
		{
			days[i+firstDayOfMonth].setDay(i+1);
			days[i+firstDayOfMonth].setDifferentMonth(false);

			final int finalI = i;
			days[i+firstDayOfMonth].setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (v instanceof CalendarDayView)
					{
						List<Long> boardGamePlayIds = new ArrayList<>();
						List<Long> rpgPlayIds = new ArrayList<>();
						List<Long> videoGamePlayIds = new ArrayList<>();
						for (long id : ((CalendarDayView)v).getBoardGamePlayIds()) boardGamePlayIds.add(id);
						for (long id : ((CalendarDayView)v).getRPGPlayIds()) rpgPlayIds.add(id);
						for (long id : ((CalendarDayView)v).getVideoGamePlayIds()) videoGamePlayIds.add(id);
						setDailyGamePlayView(boardGamePlayIds, rpgPlayIds, videoGamePlayIds, (finalI +firstDayOfMonth)/7);
					}
				}
			});

		}
		for (int i=0;i<firstDayOfMonth;i++)
		{
			days[i].setDay(daysInLastMonth-firstDayOfMonth+i+1);
			days[i].setDifferentMonth(true);
		}
		for (int i=daysInMonth+firstDayOfMonth;i<days.length;i++)
		{
			days[i].setDay(i-daysInMonth-firstDayOfMonth+1);
			days[i].setDifferentMonth(true);
		}

		for (CalendarDayView day : days) day.clearIds();

		for (int i = 0; i < dailyGamePlayViews.size(); i++)	dailyGamePlayViews.get(i).setVisibility(GONE);

		((TextView) findViewById(R.id.textview_month_and_year)).setText(Date.months[month] + " " + year);

		boardGamePlayIds.clear();
		rpgPlayIds.clear();
		videoGamePlayIds.clear();

		String yearAndMonth = year + (month < 10 ? "0" : "") + month;
		boardGamePlayDates = BoardGameDbUtility.getGamePlayDates(dbHelper, yearAndMonth + "00", yearAndMonth + "99");
		for (long id : boardGamePlayDates.keySet()) addBoardGamePlayId(boardGamePlayDates.get(id), id);

		rpgPlayDates = RPGDbUtility.getGamePlayDates(dbHelper, yearAndMonth + "00", yearAndMonth + "99");
		for (long id : rpgPlayDates.keySet()) addRPGPlayId(rpgPlayDates.get(id), id);

		videoGamePlayDates = VideoGameDbUtility.getGamePlayDates(dbHelper, yearAndMonth + "00", yearAndMonth + "99");
		for (long id : videoGamePlayDates.keySet()) addVideoGamePlayId(videoGamePlayDates.get(id), id);

		for (CalendarDayView dayView : days) dayView.adjustCalendarButton();
		dbHelper.close();
	}

	public void addBoardGamePlayId(Date date, long id)
	{
		boardGamePlayIds.add(id);
		days[Integer.parseInt(date.getDayOfMonth()) + firstDayOfMonth - 1].addBoardGamePlayId(id);
	}

	public void addRPGPlayId(Date date, long id)
	{
		boardGamePlayIds.add(id);
		days[Integer.parseInt(date.getDayOfMonth()) + firstDayOfMonth - 1].addRPGPlayId(id);
	}

	public void addVideoGamePlayId(Date date, long id)
	{
		boardGamePlayIds.add(id);
		days[Integer.parseInt(date.getDayOfMonth()) + firstDayOfMonth - 1].addVideoGamePlayId(id);
	}

}
