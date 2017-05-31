package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "results", strict = false)
public class Results {
	@Attribute(name = "numplayers", required = false)
	private String numPlayers;

	@ElementList(name = "results", type = Result.class, inline = true)
	private List<Result> resultList;

	public String getNumPlayers() {
		return numPlayers != null ? numPlayers : "";
	}

	public List<Result> getResultList() {
		return resultList;
	}
}
