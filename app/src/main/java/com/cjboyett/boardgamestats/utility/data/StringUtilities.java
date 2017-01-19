package com.cjboyett.boardgamestats.utility.data;

import com.cjboyett.boardgamestats.model.games.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Casey on 3/17/2016.
 */
public class StringUtilities
{
	private final static String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

	public static String dateToString(String date)
	{
		return months[Integer.parseInt(date.substring(4, 6))] + " " + Integer.parseInt(date.substring(6)) + ", " + Integer.parseInt(date.substring(0, 4));
	}

	public static String stringToDate(String string)
	{
		return null;
	}

	public static String convertMinutes(int minutes)
	{
		int years = minutes / (60 * 24 * 365);
		minutes = minutes - (60 * 24 * 365) * years;
		int days = minutes / (60 * 24);
		minutes = minutes - (60 * 24) * days;
		int hours = minutes / 60;
		minutes = minutes - 60 * hours;
		String time = "";
		if (years > 0) time += years + " year" + (years > 1 ? "s" : "") + ", ";
		if (days > 0) time += days + " day" + (days > 1 ? "s" : "") + ", ";
		if (hours > 0) time += hours + " hour" + (hours > 1 ? "s" : "") + ", ";
		if (minutes > 0) time += minutes + " minute"  + (minutes > 1 ? "s" : "");
		if (time.endsWith(", ")) time = time.substring(0, time.lastIndexOf(","));
		return time;
	}

	public static boolean isBoardGame(String gameType)
	{
		return gameType.equalsIgnoreCase("b") || gameType.equalsIgnoreCase(Game.GameType.BOARD.getType());
	}

	public static boolean isRPG(String gameType)
	{
		return gameType.equalsIgnoreCase("r") || gameType.equalsIgnoreCase(Game.GameType.RPG.getType());
	}

	public static boolean isVideoGame(String gameType)
	{
		return gameType.equalsIgnoreCase("v") || gameType.equalsIgnoreCase(Game.GameType.VIDEO.getType());
	}

	public static boolean isArticle(String s)
	{
		return (s.equalsIgnoreCase("the") || s.equalsIgnoreCase("a") || s.equalsIgnoreCase("an"));
	}

	public static int compareIgnoreArticles(String lhs, String rhs)
	{
		String[] lhsParts = lhs.split(" ");
		String[] rhsParts = rhs.split(" ");
		String lhsToCompare = "";
		String rhsToCompare = "";
		if (!StringUtilities.isArticle(lhsParts[0])) lhsToCompare += lhsParts[0] + " ";
		if (!StringUtilities.isArticle(rhsParts[0])) rhsToCompare += rhsParts[0] + " ";
		for (int i = 1; i < lhsParts.length; i++) lhsToCompare += lhsParts[i] + " ";
		for (int i = 1; i < rhsParts.length; i++) rhsToCompare += rhsParts[i] + " ";
		return lhsToCompare.compareTo(rhsToCompare);
	}

	public static void sortList(List<String> list)
	{
		Collections.sort(list, new Comparator<String>()
		{
			@Override
			public int compare(String lhs, String rhs)
			{
				return StringUtilities.compareIgnoreArticles(lhs, rhs);
			}
		});
	}

	public static void padGamesList(List<String> gamesList)
	{
		try
		{
			char currentChar;
			if ((gamesList.get(0).charAt(0) + "").matches("\\d")) currentChar = '#';
			else currentChar = gamesList.get(0).toUpperCase().charAt(0);
			gamesList.add(0, "---" + currentChar);
			for (int i = 1; i < gamesList.size(); i++)
			{
				String[] gameParts = gamesList.get(i).toUpperCase().split(" ");
				String toMatch;
				if (isArticle(gameParts[0])) toMatch = gameParts[1];
				else toMatch = gameParts[0];
				char nextChar;
				if ((toMatch.charAt(0) + "").matches("\\d")) nextChar = '#';
				else nextChar = toMatch.charAt(0);
				if (currentChar != nextChar)
				{
					int offset = 0;//(3-i%3)%3;
					for (int j=0;j<offset;j++) gamesList.add(i, "--- ");
					currentChar = nextChar;
					gamesList.add(i+offset, "---" + currentChar);
					i += offset;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static List<String> combinesLists(List<String> boardGames, List<String> rpgs, List<String> videoGames)
	{
		List<String> games = new ArrayList<>();
		if (boardGames != null && boardGames.size() > 0)
			for (String game : boardGames) games.add(game + ":b");
		if (rpgs != null && rpgs.size() > 0)
			for (String game : rpgs) games.add(game + ":r");
		if (videoGames != null && videoGames.size() > 0)
			for (String game : videoGames) games.add(game + ":v");

		StringUtilities.sortList(games);
		StringUtilities.padGamesList(games);

		return games;
	}

	public static String blankIfNull(String string)
	{
		return string == null ? "" : string;
	}
}
