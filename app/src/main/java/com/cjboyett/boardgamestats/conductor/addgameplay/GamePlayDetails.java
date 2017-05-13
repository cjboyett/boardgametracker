package com.cjboyett.boardgamestats.conductor.addgameplay;

import com.cjboyett.boardgamestats.model.Timer;

public class GamePlayDetails {
	private String gameName;
	private String gameType;
	private Timer timer;
	private String timePlayed;
	private String date;
	private String location;
	private String notes;

	public GamePlayDetails() {
		this("", "", new Timer(), "", "01011970", "", "");
	}

	public GamePlayDetails(String gameName, String gameType, Timer timer, String timePlayed, String date,
						   String location,
						   String notes) {
		this.gameName = gameName;
		this.gameType = gameType;
		this.timer = timer;
		this.date = date;
		this.location = location;
		this.notes = notes;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public String getTimePlayed() {
		return timePlayed;
	}

	public void setTimePlayed(String timePlayed) {
		this.timePlayed = timePlayed;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
