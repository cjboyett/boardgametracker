package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "yearpublished")
public class YearPublished {
	@Attribute(name = "value")
	String value;
}
