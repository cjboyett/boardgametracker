package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 11/26/2016.
 */
public class FilteredPlayerArrayAdapter extends ArrayAdapter<String>
{
	private Activity activity;
	private List<String> items, suggestions;
	private Filter filter;
	private boolean useThumbnails;

	private float SCALE_FACTOR;
	private Map<String, Bitmap> thumbnails;
	private Map<String, String> thumbnailUrls;

	public FilteredPlayerArrayAdapter(final Activity activity, int resource, List<String> players, boolean useThumbnails)
	{
		super(activity, resource, players);
		this.activity = activity;
		this.useThumbnails = useThumbnails;
		items = new ArrayList<>(players);

		Iterator<String> iterator = items.iterator();
		while(iterator.hasNext())
		{
			if (iterator.next().startsWith("---")) iterator.remove();
		}
		suggestions = new ArrayList<>();
		filter = new StringFilter();

		if (useThumbnails)
		{
			SCALE_FACTOR = Preferences.scaleFactor(activity);

			thumbnails = new HashMap<>();

			for (String player : players)
			{
//			Log.d("PLAYER", data.toString());
				new AsyncTask<String, Void, Bitmap>()
				{
					@Override
					protected Bitmap doInBackground(String... params)
					{
						String name = params[0];
						if (name.equals(Preferences.getUsername(activity))) name = "master_user";
						Log.d("PLAYER", name);
						thumbnails.put(params[0], ViewUtilities.createAvatar(activity, name, true));
						return null;
					}

					@Override
					protected void onPostExecute(Bitmap bitmap)
					{
						notifyDataSetChanged();
					}
				}.execute(player);
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
				((TextView)view.findViewById(R.id.textview_name)).setText(Html.fromHtml(suggestion));
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
/*
					if (useThumbnails)
						add(s.substring(0, s.length() - 2).replaceAll("<b>", "").replaceAll("</b>", ""));
					else
*/
						add(s.replaceAll("<b>", "").replaceAll("</b>", ""));

					notifyDataSetChanged();
				}
			}
		}
	}
}