package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.utility.BitmapCache;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.StringToBitmapBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 4/7/2016.
 */
public class FilteredGameArrayAdapter extends ArrayAdapter<String>
{
	private Activity activity;
	private List<String> items, suggestions;
	private Filter filter;
	private boolean useThumbnails;

	private float SCALE_FACTOR;
	private BitmapCache thumbnails;
//	private Map<String, Bitmap> thumbnails;
	private Map<String, String> thumbnailUrls;

	final ImageController imageController;

	public FilteredGameArrayAdapter(final Activity activity, int resource, List<String> games, boolean useThumbnails)
	{
		super(activity, resource, games);
		this.activity = activity;
		this.useThumbnails = useThumbnails;
		items = new ArrayList<>(games);

		Iterator<String> iterator = items.iterator();
		while(iterator.hasNext())
		{
			if (iterator.next().startsWith("---")) iterator.remove();
		}
		suggestions = new ArrayList<>();
		filter = new StringFilter();

		imageController = new ImageController(activity).setDirectoryName("thumbnails");

		if (useThumbnails)
		{
			SCALE_FACTOR = Preferences.scaleFactor(activity);

			thumbnails = new BitmapCache();
//			thumbnails = new HashMap<>();
			final ImageController imageController = new ImageController(activity).setDirectoryName("thumbnails");

			GamesDbHelper dbHelper = new GamesDbHelper(activity);
			thumbnailUrls = new HashMap<>();
			for (String game : items)
			{
				if (game.startsWith("---")) thumbnailUrls.put(game, game.substring(3));
				else
				{
					int l = game.length();
					String gameType = game.substring(l - 1);
					game = game.substring(0, l - 2);
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


			for (int i = 0; i < thumbnailUrls.size(); i++)
			{
				String game = items.get(i);
				String thumbnailUrl = thumbnailUrls.get(game);
				new BitmapWorkerTask(null).execute(thumbnailUrl, game);
/*
				new AsyncTask<String, Void, Bitmap>()
				{
					@Override
					protected Bitmap doInBackground(String... params)
					{
						Bitmap thumbnail;
						String thumbnailUrl = params[0];
						String game = params[1];
						if (thumbnailUrl.length() > 1)
							thumbnail = imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
							                           .load();
						else
							thumbnail = new StringToBitmapBuilder(activity)
									.setTextSize(90 * SCALE_FACTOR)
									.buildBitmap(thumbnailUrl.charAt(0) + "");

						boolean noThumbnail = (thumbnail == null);

//						if (!noThumbnail) Log.d("THUMBNAIL SIZE", thumbnail.getByteCount() / 1024 + " KB");

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
*/
			}
		}

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = null;

		try
		{
			if (useThumbnails) view = activity.getLayoutInflater().inflate(R.layout.linear_layout_game_play_player_info, null);
			else
			{
				if (convertView == null) view = activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
				else view = convertView;
			}
			String suggestion = suggestions.get(position);
			if (useThumbnails)
			{
				String suggestionUrl = suggestion.replace("<b>", "").replace("</b>", "");
				view.findViewById(R.id.textview_score).setVisibility(View.GONE);
				view.findViewById(R.id.imageview_win_icon).setVisibility(View.GONE);
				((TextView)view.findViewById(R.id.textview_name)).setText(Html.fromHtml(suggestion.substring(0, suggestion.length()-2)));
				((ImageView) view.findViewById(R.id.imageview_avatar)).setImageBitmap(thumbnails.get(suggestionUrl));
			}
			else
			{
				((TextView)view).setText(Html.fromHtml(suggestion));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return view;
//		return super.getView(position, convertView, parent);
	}

	@Override
	public Filter getFilter()
	{
		return filter;
	}

	private class StringFilter extends Filter
	{
		@Override
		protected FilterResults performFiltering(CharSequence constraint)
		{
			if (constraint != null)
			{
				suggestions.clear();
				for (String s : items)
					if (s.toLowerCase().contains(constraint.toString().toLowerCase()))
					{
						int index = s.toLowerCase().indexOf(constraint.toString().toLowerCase());
						String boldedString = s.substring(0, index) + "<b>" +
								s.substring(index, index + constraint.length()) + "</b>" +
								s.substring(index + constraint.length());
						suggestions.add(boldedString);
					}

				FilterResults filterResults = new FilterResults();
				filterResults.values = suggestions;
				filterResults.count = suggestions.size();
				return filterResults;
			}
			else return new FilterResults();
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results)
		{
			if (results != null && results.count > 0)
			{
				List<String> filterList = (ArrayList<String>) results.values;
				clear();
				for (String s : filterList)
				{
					if (useThumbnails)
						add(s.substring(0, s.length() - 2).replaceAll("<b>", "").replaceAll("</b>", ""));
					else
						add(s.replaceAll("<b>", "").replaceAll("</b>", ""));

					notifyDataSetChanged();
				}
			}
		}
	}

	private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>
	{
		private final WeakReference<ImageView> imageViewReference;

		public BitmapWorkerTask(ImageView imageView)
		{
			imageViewReference = new WeakReference<>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... params)
		{
			Bitmap thumbnail;
			String thumbnailUrl = params[0];
			String game = params[1];
			if (thumbnailUrl.length() > 1)
				thumbnail = imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
				                           .load();
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
			if (imageViewReference != null && bitmap != null)
			{
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) imageView.setImageBitmap(bitmap);
				notifyDataSetChanged();
			}
		}
	}

}