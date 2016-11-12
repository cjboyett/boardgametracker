package com.cjboyett.boardgamestats.model.games.rpg;

import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;

/**
 * Created by Casey on 4/10/2016.
 */
public class RPGPlayData extends GamePlayData
{
	public RPGPlayData(RolePlayingGame game, int timePlayed, Date date, String notes, long id)
	{
		setGame(game);
		setTimePlayed(timePlayed);
		setDate(date);
		setNotes(notes);
		setId(id);
	}

	@Override
	public String toString()
	{
		return getGame().getName() + " " + getTimePlayed() + " " + getDate() + " " + getNotes() + " " + getOtherPlayers().values().toString();
	}

}
