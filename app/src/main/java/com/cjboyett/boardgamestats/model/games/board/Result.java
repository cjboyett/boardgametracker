package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "result", strict = false)
public class Result {
	@Attribute(name = "level", required = false)
	private String level;

	@Attribute(name = "value")
	private String value;

	@Attribute(name = "numvotes")
	private String numVotes;

	public String getLevel() {
		return level != null ? level : "";
	}

	public String getValue() {
		return value;
	}

	public String getNumVotes() {
		return numVotes;
	}
}
