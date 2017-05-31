package com.cjboyett.boardgamestats.model.games.board;

import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "item", strict = false)
public class SimpleGame {
	@Element(name = "thumbnail", required = false)
	private String thumbnail;

	@Element(name = "image", required = false)
	private String image;

	@ElementList(name = "names", inline = true, type = Name.class)
	private List<Name> names;

	@Element(name = "description")
	private String description;

	@Element(name = "yearpublished", type = YearPublished.class, required = false)
	private YearPublished yearPublished;

	@Element(name = "minplayers", type = MinPlayers.class, required = false)
	private MinPlayers minPlayers;

	@Element(name = "maxplayers", type = MaxPlayers.class, required = false)
	private MaxPlayers maxPlayers;

	@Element(name = "playingtime", type = PlayingTime.class, required = false)
	private PlayingTime playingTime;

	@Element(name = "minplaytime", type = MinPlayTime.class, required = false)
	private MinPlayTime minPlayTime;

	@Element(name = "maxplaytime", type = MaxPlayTime.class, required = false)
	private MaxPlayTime maxPlayTime;

	@Element(name = "minage", type = MinAge.class, required = false)
	private MinAge minAge;

	@ElementList(name = "poll", inline = true, type = Poll.class, required = false)
	private List<Poll> polls;

	@ElementList(name = "link", inline = true, type = Link.class, required = false)
	private List<Link> links;

	public List<Name> getNames() {
		return names;
	}

	public String getPrimaryName() {
		for (Name name : names) {
			if (name.type.equals("primary")) {
				return name.value;
			}
		}
		return "";
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public String getImage() {
		return image;
	}

	public String getDescription() {
		return StringEscapeUtils.unescapeHtml4(description);
	}

	public String getYearPublished() {
		return yearPublished != null ? yearPublished.value : "";
	}

	public String getMinPlayers() {
		return minPlayers != null ? minPlayers.value : "";
	}

	public String getMaxPlayers() {
		return maxPlayers != null ? maxPlayers.value : "";
	}

	public String getPlayingTime() {
		return playingTime != null ? playingTime.value : "";
	}

	public String getMinPlayTime() {
		return minPlayTime != null ? minPlayTime.value : "";
	}

	public String getMaxPlayTime() {
		return maxPlayTime != null ? maxPlayTime.value : "";
	}

	public String getMinAge() {
		return minAge != null ? minAge.value : "";
	}

	@Nullable
	public List<Poll> getPolls() {
		return polls;
	}

	@Nullable
	public List<Link> getLinks() {
		return links;
	}
}
