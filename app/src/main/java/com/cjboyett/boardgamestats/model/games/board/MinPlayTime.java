package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "minplaytime")
public class MinPlayTime {
	@Attribute(name = "value")
	String value;
}
