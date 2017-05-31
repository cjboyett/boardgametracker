package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "maxplayers")
public class MaxPlayers {
	@Attribute(name = "value")
	String value;
}
