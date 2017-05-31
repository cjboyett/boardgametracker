package com.cjboyett.boardgamestats.view.ticker;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.HotnessXmlParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

/**
 * Ticker Item for BGG Hot List items
 * Created by Casey on 5/8/2016.
 */
public class BGGTickerItem extends TickerItem {
	HotnessXmlParser.Item item;
	Bitmap image;

	public BGGTickerItem(Context context) {
		super(context);

		List<HotnessXmlParser.Item> items =
				DataManager.getInstance((Application) context.getApplicationContext()).getAllHotnessItems();
		if (items != null && !items.isEmpty()) {
			// Get a random Hot List Item!!!
			item = items.get(new Random().nextInt(items.size()));
			try {
				// AsyncTasks are used to run code in the background
				// This keeps the screen from locking on the phone
				// They are bulky, but ultimately are necessary evils
				// This one simply attempts to download a bitmap image
				image = new AsyncTask<String, Void, Bitmap>() {
					@Override
					protected Bitmap doInBackground(String... url) {
						Bitmap bitmap = null;
						InputStream in = null;
						try {
							if (!url[0].startsWith("http://") && !url[0].startsWith("https://")) {
								url[0] = "https://" + url[0];
							}
							url[0] = url[0].replaceAll("http://http://", "http://")
										   .replaceAll("https://https://", "https://");
							URL thumbnailUrl = new URL(url[0]);
							HttpURLConnection connection = (HttpURLConnection) thumbnailUrl.openConnection();
							connection.setReadTimeout(10000);
							connection.setConnectTimeout(15000);
							connection.setDoInput(true);
							connection.connect();
							in = connection.getInputStream();
							bitmap = BitmapFactory.decodeStream(in);
						} catch (Exception e) {
							Timber.e(e);
						} finally {
							try {
								in.close();
							} catch (Exception e) {
								Timber.e(e);
							}
						}

						return bitmap;
					}
				}.execute(item.thumbnailUrl)
				 .get();
			} catch (InterruptedException e) {
				Timber.e(e);
			} catch (ExecutionException e) {
				Timber.e(e);
			}
		}
	}

	public int getId() {
		return item.id;
	}

	@Override
	public String getID() {
		String ID = "";
		try {
			ID = item.name;
		} catch (Exception e) {
			Timber.e(e);
		}
		return ID;
	}

	@Override
	public Bitmap getImage() {
		return image;
	}

	@Override
	public String getBlurb() {
		String blurb = "";
		try {
			blurb = "<b>Currently Hot #" + item.rank + ":</b><br/>" + item.name;
		} catch (Exception e) {
			Timber.e(e);
		}
		return blurb;
	}
}
