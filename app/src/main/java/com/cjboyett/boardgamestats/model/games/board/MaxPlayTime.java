package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "maxplaytime")
public class MaxPlayTime {
	@Attribute(name = "value")
	String value;
}
