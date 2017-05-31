package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "items", strict = false)
public class SimpleGameList {
	@ElementList(name = "item", type = SimpleGame.class, inline = true)
	private List<SimpleGame> simpleGameList;

	public List<SimpleGame> getSimpleGameList() {
		return simpleGameList;
	}
}
