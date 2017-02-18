package com.cjboyett.boardgamestats.model.games.video;

import com.cjboyett.boardgamestats.model.games.GameExtra;

/**
 * Created by Casey on 4/10/2016.
 */
public class VideoGameFranchise extends GameExtra {
	public VideoGameFranchise(String name, int bggId) {
		super(name, bggId);
	}

	@Override
	public String getType() {
		return "Franchise";
	}
}
