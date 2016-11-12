package com.cjboyett.boardgamestats.model;

import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by Casey on 3/24/2016.
 */
public class Date implements Comparator<Date>
{
	private String date, year, month, day;
	public final static String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	public final static String[] shortMonths = {"Jan.", "Feb.", "March", "April", "May", "June", "July", "Aug.", "Sept.", "Oct.", "Nov.", "Dec."};
	public final static int[] numberOfDaysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	public final static String[] daysInWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

	private boolean useShortMonth;

	public Date(String date)
	{
		this.date = date;
		year = date.substring(0, 4);
		month = date.substring(4, 6);
		day = date.substring(6);
	}

	public Date(int year, int month, int day)
	{
		this.year = year + "";
		this.month = month < 10 ? "0" + month : month + "";
		this.day = day < 10 ? "0" + day : day + "";
		date = this.year + "" + this.month + "" + this.day;
	}

	public Date useShortMonth(boolean useShortMonth)
	{
		this.useShortMonth = useShortMonth;
		return this;
	}

	public String getYear()
	{
		return year;
	}

	public String getMonth()
	{
		if (useShortMonth) return shortMonths[Integer.parseInt(month)];
		else return months[Integer.parseInt(month)];
	}

	public String getDayOfMonth()
	{
		return Integer.parseInt(day) + "";
	}

	public String getDayOfWeek()
	{
		Calendar c = Calendar.getInstance();
		c.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
		return daysInWeek[c.get(Calendar.DAY_OF_WEEK)-c.getFirstDayOfWeek()];
	}

	public String getMonthAndYear()
	{
		return getMonth() + " " + getYear();
	}

	public String getMonthDayAndYear()
	{
		return getMonth() + " " + getDayOfMonth() + ", " + getYear();
	}

	public String rawDate()
	{
		return date;
	}

	public String rawYearAndMonth()
	{
		return year + " " + month;
	}

	@Override
	public String toString()
	{
		return getDayOfWeek() + ", " + getMonth() + " " + getDayOfMonth() + ", " + getYear();
	}

	@Override
	public int compare(Date lhs, Date rhs)
	{
		return -lhs.rawDate().compareTo(rhs.rawDate());
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Date) return o.toString().equals(toString());
		return false;
	}
}
