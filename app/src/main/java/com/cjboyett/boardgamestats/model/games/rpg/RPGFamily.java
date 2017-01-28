package com.cjboyett.boardgamestats.model.games.rpg;

import com.cjboyett.boardgamestats.model.games.GameExtra;

/**
 * Created by Casey on 4/10/2016.
 */
public class RPGFamily extends GameExtra {
	public RPGFamily(String name, int bggId) {
		super(name, bggId);
	}

	@Override
	public String getType() {
		return "Family";
	}
}
