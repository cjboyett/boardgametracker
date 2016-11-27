package com.cjboyett.boardgamestats.activity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.view.AddPlayerView;
import com.cjboyett.boardgamestats.view.DatedTextView;
import com.cjboyett.boardgamestats.view.adapter.FilteredPlayerArrayAdapter;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AddGamePlayPlayersFragment extends Fragment
{
	private View view;
	private LinearLayout linearLayout;
	private List<String> playersInAdpater, allPlayers;
	private AddPlayerView addPlayerView;
	private List<GamePlayerData> gamePlayerDatas;
	private Map<DatedTextView, AddPlayerView> playerViews;
	private ArrayAdapter<String> arrayAdapter;

	private boolean initializing = true;
	private TempDataManager tempDataManager;

	private int backgroundColor, foregroundColor, hintTextColor;

	public AddGamePlayPlayersFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		if (playerViews == null) playerViews = new TreeMap<>(new Comparator<DatedTextView>()
		{
			@Override
			public int compare(DatedTextView lhs, DatedTextView rhs)
			{
				return (int) (lhs.getDateStamp() - rhs.getDateStamp());
			}
		});

		view = inflater.inflate(R.layout.fragment_add_game_play_players, container, false);

		tempDataManager = TempDataManager.getInstance(getActivity().getApplication());

		DataManager dataManager = DataManager.getInstance(getActivity().getApplication());
		allPlayers = new ArrayList<>(dataManager.getAllPlayers());
		playersInAdpater = new ArrayList<>(allPlayers);
		if (playersInAdpater.contains("master_user")) playersInAdpater.remove("master_user");
		arrayAdapter = new FilteredPlayerArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, playersInAdpater, true);

		addPlayerView = new AddPlayerView(getContext());
		linearLayout = (LinearLayout) view.findViewById(R.id.linearlayout_add_players);
		linearLayout.addView(addPlayerView);

		final TextView button = (TextView) view.findViewById(R.id.button_add_player);
		button.setOnClickListener(new AddPlayerViewClickListener());

		playerViews.put((DatedTextView) addPlayerView.findViewById(R.id.button_remove_player), addPlayerView);
		addPlayerView.findViewById(R.id.button_remove_player)
		             .setVisibility(View.INVISIBLE);
		((AutoCompleteTextView) addPlayerView.findViewById(R.id.edittext_other_player)).setText(Preferences.getUsername(getContext()));
		addPlayerView.findViewById(R.id.edittext_other_player)
		             .setEnabled(false);
		addPlayerView.findViewById(R.id.textview_name)
		             .setVisibility(View.VISIBLE);

		if (gamePlayerDatas == null) gamePlayerDatas = new ArrayList<>();
		if (!gamePlayerDatas.isEmpty())
		{
			for (GamePlayerData player : gamePlayerDatas)
			{
				addPlayer(player);
			}
		}

		setColors();
		colorComponents();

		return view;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		addTempPlayers();
	}

	public void addTempPlayers()
	{
		try
		{
			if (!initializing)
			{
				tempDataManager.clearTempPlayers();
				tempDataManager.getTempPlayers()
				               .clear();
				for (GamePlayerData gamePlayerData : getPlayerData()) tempDataManager.addTempPlayer(gamePlayerData);
				tempDataManager.saveTempPlayers();
			}
		}
		catch (Exception e)
		{

		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		new AsyncTask<String, Void, List<GamePlayerData>>()
		{
			@Override
			protected List<GamePlayerData> doInBackground(String... params)
			{
				initializing = true;
				tempDataManager.loadTempPlayers();
				return tempDataManager.getTempPlayers();
			}

			@Override
			protected void onPostExecute(List<GamePlayerData> gamePlayerDataList)
			{
				if (gamePlayerDataList.size() > 0)
				{
					playerViews.clear();
					playerViews.put((DatedTextView) addPlayerView.findViewById(R.id.button_remove_player), addPlayerView);
					linearLayout.removeAllViews();
					linearLayout.addView(addPlayerView);
					for (int i = 0; i < gamePlayerDataList.size(); i++)
						addPlayer(gamePlayerDataList.get(i));
				}

				colorComponents();
				initializing = false;
			}
		}.execute("");
	}

	private void setColors()
	{
		backgroundColor = Preferences.getBackgroundColor(getContext());
		foregroundColor = Preferences.getForegroundColor(getContext());
		hintTextColor = Preferences.getHintTextColor(getContext());
	}

	private void colorComponents()
	{
		view.setBackgroundColor(backgroundColor);
		view.findViewById(R.id.button_add_player)
		    .setBackgroundColor(backgroundColor);
		((TextView) view.findViewById(R.id.button_add_player)).setTextColor(foregroundColor);
		addPlayerView.colorComponents(backgroundColor, foregroundColor, hintTextColor);
		for (AddPlayerView playerView : playerViews.values())
			playerView.colorComponents(backgroundColor, foregroundColor, hintTextColor);
	}

	private void addPlayer(GamePlayerData player)
	{
		if (player.getPlayerName()
		          .equals("master_user"))
		{
			((EditText) addPlayerView.findViewById(R.id.edittext_other_player)).setText(Preferences.getUsername(getActivity()));
			double score = player.getScore();
			if (score > -10000)
			{
				if (score == (int) score)
					((EditText) addPlayerView.findViewById(R.id.edittext_other_score)).setText((int) score + "");
				else
					((EditText) addPlayerView.findViewById(R.id.edittext_other_score)).setText(score + "");
			}
			((CheckBox) addPlayerView.findViewById(R.id.checkbox_other_win)).setChecked(player.isWin());
		}
		else
		{
			AddPlayerView newAddPlayerView = new AddPlayerView(view.getContext());
			((AutoCompleteTextView) newAddPlayerView.findViewById(R.id.edittext_other_player)).setAdapter(arrayAdapter);
			((AutoCompleteTextView) newAddPlayerView.findViewById(R.id.edittext_other_player)).setThreshold(2);
			((AutoCompleteTextView) newAddPlayerView.findViewById(R.id.edittext_other_player)).setText(player.getPlayerName());
			double score = player.getScore();
			if (score > -10000)
			{
				if (score == (int) score)
					((EditText) newAddPlayerView.findViewById(R.id.edittext_other_score)).setText((int) score + "");
				else
					((EditText) newAddPlayerView.findViewById(R.id.edittext_other_score)).setText(score + "");
			}
			((CheckBox) newAddPlayerView.findViewById(R.id.checkbox_other_win)).setChecked(player.isWin());
			newAddPlayerView.findViewById(R.id.button_remove_player)
			                .setOnClickListener(new View.OnClickListener()
			                {
				                @Override
				                public void onClick(View v)
				                {
					                linearLayout.removeView(playerViews.remove(v));
				                }
			                });
			linearLayout.addView(newAddPlayerView);
			playerViews.put((DatedTextView) newAddPlayerView.findViewById(R.id.button_remove_player), newAddPlayerView);
		}
	}

	public List<GamePlayerData> getPlayerData()
	{
		List<GamePlayerData> players = new ArrayList<>();

		for (AddPlayerView view : playerViews.values())
		{
			String name = ((EditText) view.findViewById(R.id.edittext_other_player)).getText()
			                                                                        .toString();
			if (name.equals("")) name = "OTHER";
			else if (name.equals(Preferences.getUsername(getContext()))) name = "master_user";

			String scoreString = ((EditText) view.findViewById(R.id.edittext_other_score)).getText()
			                                                                              .toString();
			double score = -10000;
			if (NumberUtils.isParsable(scoreString)) score = Double.parseDouble(scoreString);
			players.add(new GamePlayerData(name, score, ((CheckBox) view.findViewById(R.id.checkbox_other_win)).isChecked()));
		}

		return players;
	}

	public void setPlayers(Collection<GamePlayerData> players)
	{
		gamePlayerDatas = new ArrayList<>(players);
	}

	private class AddPlayerViewClickListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			final AddPlayerView newAddPlayerView = new AddPlayerView(view.getContext());
			newAddPlayerView.colorComponents(backgroundColor, foregroundColor, hintTextColor);
			((AutoCompleteTextView) newAddPlayerView.findViewById(R.id.edittext_other_player)).setAdapter(arrayAdapter);
			((AutoCompleteTextView) newAddPlayerView.findViewById(R.id.edittext_other_player)).setThreshold(2);

			newAddPlayerView.findViewById(R.id.edittext_other_player).setOnFocusChangeListener(new View.OnFocusChangeListener()
			{
				@Override
				public void onFocusChange(View view, boolean b)
				{
					Log.d("HI", "Removing");
					String player = ((TextView)newAddPlayerView.findViewById(R.id.edittext_other_player)).getText().toString();
					if (allPlayers.contains(player))
					{
						playersInAdpater.remove(player);
						arrayAdapter.remove(player);
					}
					arrayAdapter.notifyDataSetChanged();				}
			});

			newAddPlayerView.findViewById(R.id.button_remove_player)
			                .setOnClickListener(new View.OnClickListener()
			                {
				                @Override
				                public void onClick(View v)
				                {
					                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					                {
						                TransitionManager.beginDelayedTransition((ViewGroup)view);
					                }
									String player = ((TextView)newAddPlayerView.findViewById(R.id.edittext_other_player)).getText().toString();
					                if (allPlayers.contains(player))
					                {
						                playersInAdpater.add(player);
						                arrayAdapter.add(player);
					                }
					                arrayAdapter.notifyDataSetChanged();

					                linearLayout.removeView(playerViews.remove(v));
				                }
			                });

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
				TransitionManager.beginDelayedTransition((ViewGroup)view, new Fade(Fade.IN));
			}

			linearLayout.addView(newAddPlayerView);

			boolean jumpToNewView = true;
			for (AddPlayerView playerView : playerViews.values())
			{
				String text = ((TextView) playerView.findViewById(R.id.edittext_other_player)).getText()
				                                                                              .toString();
				if (text == null || text.equals(""))
				{
					jumpToNewView = false;
					break;
				}
			}
			if (jumpToNewView) newAddPlayerView.findViewById(R.id.edittext_other_player)
			                                   .requestFocus();
			playerViews.put((DatedTextView) newAddPlayerView.findViewById(R.id.button_remove_player), newAddPlayerView);
		}
	}

}
