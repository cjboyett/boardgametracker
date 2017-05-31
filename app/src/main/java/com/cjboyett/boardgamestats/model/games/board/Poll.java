package com.cjboyett.boardgamestats.model.games.board;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "poll", strict = false)
public class Poll {
	@Attribute(name = "name", required = false)
	private String name;

	@Attribute(name = "title", required = false)
	private String title;

	@Attribute(name = "totalvotes", required = false)
	private String totalVotes;

	@ElementList(name = "results", type = Results.class, inline = true, required = false)
	private List<Results> results;

	public List<Results> getResults() {
		return results;
	}

	public String getName() {
		return name != null ? name : "";
	}

	public String getTitle() {
		return title != null ? title : "";
	}

	public String getTotalVotes() {
		return totalVotes != null ? totalVotes : "";
	}
}
