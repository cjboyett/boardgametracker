package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GameExtra;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.utility.Preferences;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Casey on 8/31/2016.
 */
public class GameExtrasAdapter extends BaseAdapter {
	private Activity activity;
	private Game game;
	private List<List<GameExtra>> extrasList;

	private int foregroundColor;

	public GameExtrasAdapter(Activity activity, Game game) {
		this.activity = activity;
		this.game = game;
		extrasList = new ArrayList<>();

		if (game instanceof BoardGame) {
			if (!((BoardGame) game).getExpansions().isEmpty()) {
				List<GameExtra> expansions = new ArrayList<>();
				for (BoardGame expansion : ((BoardGame) game).getExpansions())
					expansions.add(new GameExtra(expansion.getName(), expansion.getBggId()) {
						@Override
						public String getType() {
							return "Expansion";
						}
					});
				extrasList.add(expansions);
			}
			extrasList.add(((BoardGame) game).getCategories());
			extrasList.add(((BoardGame) game).getMechanics());
			extrasList.add(((BoardGame) game).getFamilies());
			extrasList.add(((BoardGame) game).getPublishers());
		} else if (game instanceof RolePlayingGame) {
			extrasList.add(((RolePlayingGame) game).getMechanics());
			extrasList.add(((RolePlayingGame) game).getFamilies());
		} else if (game instanceof VideoGame) {
			extrasList.add(((VideoGame) game).getCompilations());
			extrasList.add(((VideoGame) game).getDevelopers());
			extrasList.add(((VideoGame) game).getFranchises());
			extrasList.add(((VideoGame) game).getGenres());
			extrasList.add(((VideoGame) game).getModes());
			extrasList.add(((VideoGame) game).getPlatforms());
			extrasList.add(((VideoGame) game).getPublishers());
			extrasList.add(((VideoGame) game).getSeries());
			extrasList.add(((VideoGame) game).getThemes());
		}

		Iterator<List<GameExtra>> iterator = extrasList.iterator();
		while (iterator.hasNext())
			if (iterator.next().isEmpty()) iterator.remove();

		foregroundColor = Preferences.getGeneratedForegroundColor(activity);
	}

	@Override
	public int getCount() {
		return extrasList.size();
	}

	@Override
	public List<GameExtra> getItem(int position) {
		return extrasList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = activity.getLayoutInflater().inflate(R.layout.list_item_game_extras, null);
		List<GameExtra> gameExtras = getItem(position);
		if (!gameExtras.isEmpty()) {
			((TextView) view.findViewById(R.id.textview_extra_type)).setTextColor(foregroundColor);
			((TextView) view.findViewById(R.id.textview_extra_list)).setTextColor(foregroundColor);

			String extraString = "";

			((TextView) view.findViewById(R.id.textview_extra_type)).setText(gameExtras.get(0).getType());
			for (GameExtra gameExtra : gameExtras) extraString += gameExtra.getName() + "\n";
			((TextView) view.findViewById(R.id.textview_extra_list)).setText(StringUtils.chomp(extraString));
		}
		return view;
	}
}
