package com.cjboyett.boardgamestats.utility.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.cjboyett.boardgamestats.data.games.HotnessXmlParser;
import com.cjboyett.boardgamestats.data.games.board.BoardGameXmlParser;
import com.cjboyett.boardgamestats.data.games.video.VideoGameXmlParser;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 4/10/2016.
 */
public class UrlUtilities
{
	public static Map<Integer, String> parseRPGsFromBGGSearch(String rpgName)
	{
		Map<Integer, String> rpgs = new HashMap<>();
		InputStream inputStream = UrlUtilities.downloadUrl("https://rpggeek.com/geeksearch.php?action=search&objecttype=rpg&q=" +
		                                                   URLEncoder.encode(rpgName) + "&B1=Go");
		try
		{
			String result = IOUtils.toString(inputStream);
			int index = 0;
			while ((index = result.indexOf("href=\"/rpg/", index+11)) > -1)
			{
				if (result.charAt(index+11) == 'r') continue;
				else if (result.charAt(result.indexOf("<", index) + 1) != '/') continue;
				else
				{
					int id = Integer.parseInt(result.substring(index + 11, result.indexOf("/", index + 11)));
					String name = result.substring(result.indexOf(">", index) + 1, result.indexOf("<", index));
					rpgs.put(id, name);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return rpgs;
	}

	public static List<BoardGameXmlParser.Item> loadBoardGameXmlFromNetwork(String urlString)
	{
		InputStream in = null;
		BoardGameXmlParser parser = new BoardGameXmlParser();
		List<BoardGameXmlParser.Item> items = null;

		try
		{
			in = UrlUtilities.downloadUrl(urlString);
			items = parser.parse(in);
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (Exception e)
			{
			}
		}
		return items;
	}

	public static List<VideoGameXmlParser.Item> loadVideoGameXmlFromNetwork(String urlString)
	{
		InputStream in = null;
		VideoGameXmlParser parser = new VideoGameXmlParser();
		List<VideoGameXmlParser.Item> items = null;

		try
		{
			in = UrlUtilities.downloadUrl(urlString);
			items = parser.parse(in);
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (Exception e)
			{
			}
		}
		return items;
	}

	public static InputStream downloadUrl(String urlString)
	{
		try
		{
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setReadTimeout(15000);
			connection.setConnectTimeout(15000);
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.connect();
			return connection.getInputStream();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static List<HotnessXmlParser.Item> loadHotnessXmlFromNetwork(String urlString)
	{
		InputStream in = null;
		HotnessXmlParser parser = new HotnessXmlParser();
		List<HotnessXmlParser.Item> items = null;

		try
		{
			in = UrlUtilities.downloadUrl(urlString);
			items = parser.parse(in);
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (Exception e)
			{
			}
		}
		return items;
	}

	public static void openWebPage(Context context, String url)
	{
		Uri webpage = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
		if (intent.resolveActivity(context.getPackageManager()) != null)
			context.startActivity(intent);
	}
}
