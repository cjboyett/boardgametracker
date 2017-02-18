package com.cjboyett.boardgamestats.model.games;

import com.cjboyett.boardgamestats.model.Date;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Casey on 4/10/2016.
 */
public abstract class GamePlayData {
	private Game game;
	private int timePlayed;
	private String notes;
	private Date date;
	private String location;
	private boolean countForStats;
	private List<String> images;
	private Map<String, GamePlayerData> otherPlayers;
	private long id;
	private String bggPlayId;

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Map<String, GamePlayerData> getOtherPlayers() {
		if (otherPlayers == null) otherPlayers = new TreeMap<>();
		return otherPlayers;
	}

	public GamePlayerData addOtherPlayer(String player, GamePlayerData data) {
		return getOtherPlayers().put(player, data);
	}

	public int getTimePlayed() {
		return timePlayed;
	}

	public void setTimePlayed(int timePlayed) {
		this.timePlayed = timePlayed;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isCountForStats() {
		return countForStats;
	}

	public void setCountForStats(boolean countForStats) {
		this.countForStats = countForStats;
	}

	public List<String> getImages() {
		if (images == null) images = new ArrayList<>();
		return images;
	}

	public void addImage(String... images) {
		for (String image : images) getImages().add(image);
	}

	public String getBggPlayId() {
		return bggPlayId;
	}

	public void setBggPlayId(String bggPlayId) {
		this.bggPlayId = bggPlayId;
	}
}
