package com.cjboyett.boardgamestats.data.games.rpg;

import android.util.Log;
import android.util.Xml;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 4/10/2016.
 */
public class RPGXmlParser
{
	private static final String namespace = null;

	public List<Item> parse(InputStream in)
	{
		try
		{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
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
		return new ArrayList<>();
	}

	private List<Item> readFeed(XmlPullParser parser)
	{
		List<Item> entries = new ArrayList<>();

		try
		{
			parser.require(XmlPullParser.START_TAG, namespace, "items");
			while (parser.next() != XmlPullParser.END_TAG)
			{
				if (parser.getEventType() != XmlPullParser.START_TAG) continue;
				String name = parser.getName();
				if (name.equals("item")) entries.add(readItem(parser));
				else skip(parser);
			}
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}
		return entries;
	}

	private Item readItem(XmlPullParser parser)
	{
		int id = 0, yearPublished = 0;
		String name = null, thumbnailUrl = null, description = null;
		List<String[]> links = new ArrayList<>();
		try
		{
			parser.require(XmlPullParser.START_TAG, namespace, "item");
			id = Integer.parseInt(parser.getAttributeValue(null, "id"));

			while (parser.next() != XmlPullParser.END_TAG)
			{
				if (parser.getEventType() != XmlPullParser.START_TAG) continue;
				String entryName = parser.getName();

				switch (entryName)
				{
					case "name":
						if (parser.getAttributeValue(null, "type").equals("primary")) name = readName(parser);
						else skip(parser);
						break;
					case "thumbnail":
						thumbnailUrl = readThumbnailUrl(parser);
						break;
					case "description":
						description = readDescription(parser);
						break;
					case "link":
						links.add(readLink(parser));
						break;
					default:
						skip(parser);
						break;
				}
			}
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}
		return new Item(id, name, thumbnailUrl, description, links);
	}

	private String readName(XmlPullParser parser)
	{
		String name = null;

		try
		{
			parser.require(XmlPullParser.START_TAG, namespace, "name");
			name = parser.getAttributeValue(null, "value");
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, namespace, "name");
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}

		return name;
	}

	private String readThumbnailUrl(XmlPullParser parser)
	{
		String thumbnailUrl = null;

		try
		{
			parser.require(XmlPullParser.START_TAG, namespace, "thumbnail");
			parser.next();
			thumbnailUrl = parser.getText();
			while (thumbnailUrl.startsWith("/")) thumbnailUrl = thumbnailUrl.substring(1);
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, namespace, "thumbnail");
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}

		return thumbnailUrl;
	}

	private String readDescription(XmlPullParser parser)
	{
		String description = null;

		try
		{
			parser.require(XmlPullParser.START_TAG, namespace, "description");
			parser.next();
			description = StringEscapeUtils.unescapeHtml4(parser.getText());
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, namespace, "description");
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}

		return description;
	}

	private String[] readLink(XmlPullParser parser)
	{
		String[] link = new String[3];

		try
		{
			parser.require(XmlPullParser.START_TAG, namespace, "link");
			link[0] = parser.getAttributeValue(null, "type");
			link[1] = parser.getAttributeValue(null, "id");
			link[2] = parser.getAttributeValue(null, "value");
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, namespace, "link");
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}

		return link;
	}

	private void skip(XmlPullParser parser)
	{
		try
		{
			if (parser.getEventType() != XmlPullParser.START_TAG)
			{
				throw new IllegalStateException();
			}
			int depth = 1;
			while (depth != 0)
			{
				switch (parser.next())
				{
					case XmlPullParser.END_TAG:
						depth--;
						break;
					case XmlPullParser.START_TAG:
						depth++;
						break;
				}
			}
		}
		catch (Exception e)
		{
			Log.e("PARSER", e.getMessage());
		}
	}

	public static class Item
	{
		public final int id;
		public final String name, thumbnailUrl, description;
		public final List<String[]> links;

		public Item(int id, String name, String thumbnailUrl, String description, List<String[]> links)
		{
			this.id = id;
			this.name = name;
			this.thumbnailUrl = thumbnailUrl;
			this.description = description;
			this.links = links;
		}

		@Override
		public String toString()
		{
			return name + " " + id;
		}
	}


}
