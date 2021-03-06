package com.cjboyett.boardgamestats.data.games;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 5/9/2016.
 */
public class HotnessXmlParser {
	private static final String namespace = null;

	public List<Item> parse(InputStream in) {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		return new ArrayList<>();
	}

	private List<Item> readFeed(XmlPullParser parser) {
		List<Item> entries = new ArrayList<>();

		try {
			parser.require(XmlPullParser.START_TAG, namespace, "items");
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) continue;
				String name = parser.getName();
				if (name.equals("item")) entries.add(readItem(parser));
				else skip(parser);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entries;
	}

	private Item readItem(XmlPullParser parser) {
		int id = 0, rank = 0;
		String name = null, thumbnailUrl = null;
		try {
			parser.require(XmlPullParser.START_TAG, namespace, "item");
			id = Integer.parseInt(parser.getAttributeValue(null, "id"));
			rank = Integer.parseInt(parser.getAttributeValue(null, "rank"));

			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) continue;
				String entryName = parser.getName();

				switch (entryName) {
					case "name":
						name = readName(parser);
						break;
					case "thumbnail":
						thumbnailUrl = readThumbnailUrl(parser);
						break;
					default:
						skip(parser);
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Item(name, id, rank, thumbnailUrl);
	}

	private String readName(XmlPullParser parser) {
		String name = null;

		try {
			parser.require(XmlPullParser.START_TAG, namespace, "name");
			name = parser.getAttributeValue(null, "value");
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, namespace, "name");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return name;
	}

	private String readThumbnailUrl(XmlPullParser parser) {
		String thumbnailUrl = null;

		try {
			parser.require(XmlPullParser.START_TAG, namespace, "thumbnail");
			thumbnailUrl = parser.getAttributeValue(null, "value");
			while (thumbnailUrl.startsWith("/")) thumbnailUrl = thumbnailUrl.substring(1);
			parser.nextTag();
			parser.require(XmlPullParser.END_TAG, namespace, "thumbnail");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "http://" + thumbnailUrl;
	}

	private void skip(XmlPullParser parser) {
		try {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				throw new IllegalStateException();
			}
			int depth = 1;
			while (depth != 0) {
				switch (parser.next()) {
					case XmlPullParser.END_TAG:
						depth--;
						break;
					case XmlPullParser.START_TAG:
						depth++;
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class Item {
		public final int id, rank;
		public final String name, thumbnailUrl;

		public Item(String name, int id, int rank, String thumbnailUrl) {
			this.id = id;
			this.rank = rank;
			this.name = name;
			this.thumbnailUrl = thumbnailUrl;
		}

		@Override
		public String toString() {
			return name + " " + id;
		}
	}

}
