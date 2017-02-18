package com.cjboyett.boardgamestats.model.achievements;

import android.graphics.Bitmap;

/**
 * Created by Casey on 9/17/2016.
 */
public class Achievement {
	private String title, description;
	private int id, experience;
	private boolean completed;

	private Bitmap icon;

	public Achievement(int id, String title, String description, int experience, Bitmap icon) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.experience = experience;
		this.icon = icon;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
}
