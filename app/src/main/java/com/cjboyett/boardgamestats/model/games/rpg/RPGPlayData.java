package com.cjboyett.boardgamestats.model.games.rpg;

import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;

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

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof RPGPlayData &&
		       ((RPGPlayData)obj).getGame().equals(getGame()) &&
		       ((RPGPlayData)obj).getTimePlayed() == getTimePlayed() &&
		       ((RPGPlayData)obj).getDate().equals(getDate()) &&
		       StringUtilities.blankIfNull(((RPGPlayData)obj).getLocation()).equalsIgnoreCase(StringUtilities.blankIfNull(getLocation())) &&
		       ((RPGPlayData)obj).isCountForStats() == isCountForStats() &&
		       GamePlayerData.equalPlayerMaps(((RPGPlayData)obj).getOtherPlayers(), getOtherPlayers());
	}

}
