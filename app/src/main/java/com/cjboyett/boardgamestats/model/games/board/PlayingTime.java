package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "playingtime")
public class PlayingTime {
	@Attribute(name = "value")
	String value;
}
