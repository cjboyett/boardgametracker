package com.cjboyett.boardgamestats.model.games.rpg;

import com.cjboyett.boardgamestats.model.games.GameExtra;

/**
 * Created by Casey on 4/10/2016.
 */
public class RPGMechanic extends GameExtra {
	public RPGMechanic(String name, int bggId) {
		super(name, bggId);
	}

	@Override
	public String getType() {
		return "Mechanic";
	}
}
