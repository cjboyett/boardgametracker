package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "link", strict = false)
public class Link {
	@Attribute(name = "type")
	private String type;

	@Attribute(name = "id")
	private String id;

	@Attribute(name = "value")
	private String value;

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}
}
