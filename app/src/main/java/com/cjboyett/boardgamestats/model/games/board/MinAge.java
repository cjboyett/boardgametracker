package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "minage")
public class MinAge {
	@Attribute(name = "value")
	String value;
}
