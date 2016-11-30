package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;

import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.utility.BitmapCache;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringMatcher;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.StringToBitmapBuilder;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 3/19/2016.
 */
public class ImageAdapter extends BaseAdapter implements SectionIndexer
{
	private Activity activity;
	private Map<String, String> thumbnailUrls;
	private BitmapCache thumbnails;
//	private Map<String, Bitmap> thumbnails;
	private List<String> games;
	private ImageController imageController;
	private float ratio = 1f;

	private float SCALE_FACTOR;

	public ImageAdapter(final Activity activity, final List<String> games)
	{
		this.activity = activity;
		this.games = games;

		SCALE_FACTOR = Preferences.scaleFactor(activity);

		thumbnails = new BitmapCache();
//		thumbnails = new HashMap<>();
		imageController = new ImageController(activity).setDirectoryName("thumbnails");
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		ratio = metrics.density;

		GamesDbHelper dbHelper = new GamesDbHelper(activity);
		thumbnailUrls = new HashMap<>();
		for (String game : games)
		{
			if (game.startsWith("---")) thumbnailUrls.put(game, game.substring(3));
			else
			{
				int l = game.length();
				String gameType = game.substring(l-1);
				game = game.substring(0, l-2);
				switch (gameType)
				{
					case "b":
						thumbnailUrls.put(game + ":b", "http://" + BoardGameDbUtility.getThumbnailUrl(dbHelper, game));
						break;
					case "r":
						thumbnailUrls.put(game + ":r", "http://" + RPGDbUtility.getThumbnailUrl(dbHelper, game));
						break;
					case "v":
						thumbnailUrls.put(game + ":v", "http://" + VideoGameDbUtility.getThumbnailUrl(dbHelper, game));
						break;
				}
			}
		}

		try
		{
			for (int i = 0; i <= Math.min(30, thumbnailUrls.size()); i++)
				new BitmapWorkerTask(null, games.get(i)).execute(thumbnailUrls.get(games.get(i)));
			for (int i = thumbnailUrls.size() - 1; i > 30; i--) new BitmapWorkerTask(null, games.get(i)).execute(thumbnailUrls.get(games.get(i)));
		}
		catch (Exception e){}

/*
		for (int i=0;i<thumbnailUrls.size();i++)
		{
			String game = games.get(i);
			String thumbnailUrl = thumbnailUrls.get(game);
			new AsyncTask<String, Void, Bitmap>()
			{
				@Override
				protected Bitmap doInBackground(String... params)
				{
					Bitmap thumbnail;
					String thumbnailUrl = params[0];
					String game = params[1];
					if (thumbnailUrl.length() > 1)
						thumbnail = imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1)).load();
					else
						thumbnail = new StringToBitmapBuilder(activity)
								.setTextSize(90 * SCALE_FACTOR)
								.buildBitmap(thumbnailUrl.charAt(0) + "");

					boolean noThumbnail = (thumbnail == null);

					if (noThumbnail)
						thumbnail = new StringToBitmapBuilder(activity)
								.setTextSize(16)
//								.setTextWidth(10)
//								.setAlign(Paint.Align.CENTER)
								.buildBitmap(game.substring(0, game.length() - 2));
					thumbnails.put(game, thumbnail);
					return thumbnail;
				}

				@Override
				protected void onPostExecute(Bitmap bitmap)
				{
					notifyDataSetChanged();
				}
			}.execute(thumbnailUrl, game);
		}
*/
	}

	@Override
	public int getCount()
	{
		return games.size();
	}

	@Override
	public Object getItem(int position)
	{
		return thumbnails.get(games.get(position));
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ImageView imageView;
		if (convertView == null)
		{
			imageView = new ImageView(activity);
			// Can scale here
			imageView.setMinimumWidth((int) (128 * ratio * SCALE_FACTOR));
			imageView.setMinimumHeight((int) (128 * ratio * SCALE_FACTOR));
			imageView.setPadding(8, 8, 8, 8);
		}
		else imageView = (ImageView)convertView;

		final String game = games.get(position);
		Bitmap thumbnail = thumbnails.get(game);

		if (thumbnail != null) setImage(imageView, thumbnail, game);
		else new BitmapWorkerTask(imageView, game).execute(thumbnailUrls.get(game));

		return imageView;
	}

	private void setImage(ImageView imageView, Bitmap thumbnail, String game)
	{
		boolean noThumbnail = (thumbnailUrls.get(game).length() <= 12);

		if (game.startsWith("---"))
			imageView.setScaleType(ImageView.ScaleType.CENTER);
		else if (game.substring(game.length() - 1).equals("r") || noThumbnail)
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		else
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

		imageView.setImageBitmap(thumbnail);
	}

	private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	@Override
	public int getPositionForSection(int section)
	{
		// If there is no item for current section, previous section will be selected
		for (int i = section; i >= 0; i--)
		{
			for (int j = 0; j < getCount(); j++)
			{
				char toCompare = ' ';
				String[] game = games.get(j).split(" ");
				if (game[0].equalsIgnoreCase("the") || game[0].equalsIgnoreCase("a") || game[0].equalsIgnoreCase("an"))
					toCompare = game[1].charAt(0);
				else toCompare = game[0].charAt(0);
				if (i == 0)
				{
					// For numeric section
					for (int k = 0; k <= 9; k++)
					{
						if (StringMatcher.match(String.valueOf(toCompare), String.valueOf(k)))
							return j;
					}
				}
				else
				{
					if (StringMatcher.match(String.valueOf(toCompare), String.valueOf(mSections.charAt(i))))
						return j;
				}
			}
		}
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		String[] sections = new String[mSections.length()];
		for (int i = 0; i < mSections.length(); i++)
			sections[i] = String.valueOf(mSections.charAt(i));
		return sections;
	}

	private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>
	{
		private final WeakReference<ImageView> imageViewReference;
		private final String game;

		public BitmapWorkerTask(ImageView imageView, String game)
		{
			imageViewReference = new WeakReference<>(imageView);
			this.game = game;
		}

		@Override
		protected Bitmap doInBackground(String... params)
		{
			Bitmap thumbnail;
			String thumbnailUrl = params[0];
			if (thumbnailUrl.length() > 1)
				thumbnail = imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1)).load();
			else
				thumbnail = new StringToBitmapBuilder(activity)
						.setTextSize(90 * SCALE_FACTOR)
						.buildBitmap(thumbnailUrl.charAt(0) + "");

			boolean noThumbnail = (thumbnail == null);

			if (noThumbnail)
				thumbnail = new StringToBitmapBuilder(activity)
						.setTextSize(16)
//								.setTextWidth(10)
//								.setAlign(Paint.Align.CENTER)
						.buildBitmap(game.substring(0, game.length() - 2));
			thumbnails.addBitmapToCache(game, thumbnail);
			return thumbnail;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap)
		{
			if (imageViewReference != null && bitmap != null)
			{
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) setImage(imageView, bitmap, game);
				notifyDataSetChanged();
			}
		}
	}
}
