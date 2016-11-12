package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.activity.PlayerStatsActivity;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 4/16/2016.
 */
public class PlayerDataAdapter extends BaseAdapter
{
	private Activity activity;
	private List<GamePlayerData> gamePlayerDataList;
	private Map<String, Bitmap> playerAvatars;

	private boolean countWins;

	private int backgroundColor, foregroundColor;

	public PlayerDataAdapter(final Activity activity, Collection<GamePlayerData> gamePlayerData, boolean countWins)
	{
		this(activity,
		     gamePlayerData,
		     countWins,
		     (Preferences.generatePalette(activity) ? Preferences.getGeneratedBackgroundColor(activity) : Preferences.getBackgroundColor(activity)),
		     (Preferences.generatePalette(activity) ? Preferences.getGeneratedForegroundColor(activity) : Preferences.getForegroundColor(activity)));
	}

	public PlayerDataAdapter(final Activity activity, Collection<GamePlayerData> gamePlayerData, boolean countWins, int backgroundColor, int foregroundColor)
	{
		this.activity = activity;
		gamePlayerDataList = new ArrayList<>(gamePlayerData);
		this.countWins = countWins;

		for (GamePlayerData data : gamePlayerDataList)
			if (data.getPlayerName().equals("master_user")) data.setPlayerName(Preferences.getUsername(activity));

		final boolean sortWinnersFirst = Preferences.sortWinnersFirst(activity);
		Collections.sort(gamePlayerDataList, new Comparator<GamePlayerData>()
		{
			@Override
			public int compare(GamePlayerData lhs, GamePlayerData rhs)
			{
				if (sortWinnersFirst)
				{
					if (lhs.isWin() && !rhs.isWin()) return -1;
					else if (!lhs.isWin() && rhs.isWin()) return 1;
					else return lhs.getPlayerName().compareTo(rhs.getPlayerName());
				}
				else
					return lhs.getPlayerName().compareTo(rhs.getPlayerName());
			}
		});

		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;

		playerAvatars = new HashMap<>();

		for (GamePlayerData data : gamePlayerData)
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
					playerAvatars.put(params[0], ViewUtilities.createAvatar(activity, name, true));
					return null;
				}

				@Override
				protected void onPostExecute(Bitmap bitmap)
				{
					notifyDataSetChanged();
				}
			}.execute(data.getPlayerName());
		}
	}

	@Override
	public int getCount()
	{
		return gamePlayerDataList.size();
	}

	@Override
	public Object getItem(int position)
	{
		return gamePlayerDataList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = activity.getLayoutInflater().inflate(R.layout.linear_layout_game_play_player_info, null);

		view.setBackgroundColor(backgroundColor);
		view.findViewById(R.id.textview_name).setBackgroundColor(backgroundColor);
		((TextView)view.findViewById(R.id.textview_name)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_score).setBackgroundColor(backgroundColor);
		((TextView)view.findViewById(R.id.textview_score)).setTextColor(foregroundColor);
		view.findViewById(R.id.imageview_win_icon).setBackgroundColor(backgroundColor);

		final GamePlayerData gamePlayerData = (GamePlayerData)getItem(position);

		try
		{
			((ImageView)view.findViewById(R.id.imageview_avatar)).setImageBitmap(playerAvatars.get(gamePlayerData.getPlayerName()));
		}
		catch (Exception e)
		{
		}
		((TextView)view.findViewById(R.id.textview_name)).setText(gamePlayerData.getPlayerName());
		if (gamePlayerData.getScore() > -10000)
			if ((int) gamePlayerData.getScore() == gamePlayerData.getScore())
				((TextView)view.findViewById(R.id.textview_score)).setText((int) gamePlayerData.getScore() + "");
			else
				((TextView)view.findViewById(R.id.textview_score)).setText(gamePlayerData.getScore() + "");
		if (countWins)
		{
			if (gamePlayerData.isWin())
			{
				((ImageView) view.findViewById(R.id.imageview_win_icon)).setImageResource(R.drawable.ic_action_achievement);
				ViewUtilities.tintImageView((AppCompatImageView) view.findViewById(R.id.imageview_win_icon), foregroundColor);
			}
			else
				view.findViewById(R.id.imageview_win_icon).setVisibility(View.INVISIBLE);
		}
		else view.findViewById(R.id.imageview_win_icon).setVisibility(View.GONE);

		view.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
//				Log.d("CLICK", gamePlayerData.getPlayerName());
				if (!gamePlayerData.getPlayerName().equals(Preferences.getUsername(activity)))
					activity.startActivity(new Intent(activity, PlayerStatsActivity.class).putExtra("NAME", gamePlayerData.getPlayerName()));
				ActivityUtilities.exitUp(activity);
			}
		});

		return view;
	}
}
