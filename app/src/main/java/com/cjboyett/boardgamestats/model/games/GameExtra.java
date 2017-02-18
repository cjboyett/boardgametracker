package com.cjboyett.boardgamestats.model.games;

/**
 * Created by Casey on 3/7/2016.
 */
public abstract class GameExtra {
	private String name;
	private int bggId;

	public GameExtra(String name, int bggId) {
		this.name = name;
		this.bggId = bggId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBggId() {
		return bggId;
	}

	public void setBggId(int bggId) {
		this.bggId = bggId;
	}

	public abstract String getType();

	@Override
	public boolean equals(Object o) {
		if (o instanceof GameExtra)
			return (((GameExtra) o).getName()).equals(getName()) && (((GameExtra) o).getType()).equals(getType());
		return false;
	}

	@Override
	public String toString() {
		return name + " " + bggId;
	}
}
