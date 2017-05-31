package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "minplayers")
public class MinPlayers {
	@Attribute(name = "value")
	String value;
}
