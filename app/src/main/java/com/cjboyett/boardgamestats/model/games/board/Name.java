package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(name = "name", strict = false)
@Path("item")
public class Name {
	@Attribute(name = "type")
	String type;

	@Attribute(name = "sortindex")
	String sortIndex;

	@Attribute(name = "value")
	String value;
}
