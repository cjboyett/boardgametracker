package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.Preferences;

import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 9/6/2016.
 */
public class RecommendedGamesAdapter extends BaseAdapter
{
	private Activity activity;
	private List<Integer> keys;
	private Map<Integer, String> gameNames;
	private Map<Integer, Bitmap> thumbnails;

	private int foregroundColor;

	public RecommendedGamesAdapter(Activity activity, Map<Integer, String> gameNames, Map<Integer, Bitmap> thumbnails, List<Integer> order)
	{
		this.activity = activity;
		this.gameNames = gameNames;
		keys = order;
		this.thumbnails = thumbnails;

		foregroundColor = Preferences.getForegroundColor(activity);
	}

	@Override
	public int getCount()
	{
		return gameNames.size();
	}

	@Override
	public Integer getItem(int position)
	{
		return keys.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = activity.getLayoutInflater().inflate(R.layout.linear_layout_game_play_player_info, null);
		Integer key = getItem(position);
		view.findViewById(R.id.textview_score).setVisibility(View.GONE);
		view.findViewById(R.id.imageview_win_icon).setVisibility(View.GONE);
		((TextView)view.findViewById(R.id.textview_name)).setTextColor(foregroundColor);
		((TextView)view.findViewById(R.id.textview_name)).setText(gameNames.get(key));
		((ImageView) view.findViewById(R.id.imageview_avatar)).setImageBitmap(thumbnails.get(key));
		return view;
	}

}
