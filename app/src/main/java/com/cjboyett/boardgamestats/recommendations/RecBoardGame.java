package com.cjboyett.boardgamestats.recommendations;

import com.cjboyett.boardgamestats.model.games.GameExtra;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 9/5/2016.
 */
public class RecBoardGame {
	private String name;
	private int id, rank;
	private double rating, recommendationLevel;
	private List<String> mechanics, categories, types;

	public RecBoardGame(String name, int id, int rank, double rating) {
		this.name = name;
		this.id = id;
		this.rank = rank;
		this.rating = rating;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public double getRecommendationLevel() {
		return recommendationLevel;
	}

	public void setRecommendationLevel(double recommendationLevel) {
		this.recommendationLevel = recommendationLevel;
	}

	public List<String> getMechanics() {
		if (mechanics == null) mechanics = new ArrayList<>();
		return mechanics;
	}

	public void addMechanic(String mechanic) {
		getMechanics().add(mechanic);
	}

	public List<String> getCategories() {
		if (categories == null) categories = new ArrayList<>();
		return categories;
	}

	public void addCategory(String category) {
		getCategories().add(category);
	}

	public List<String> getTypes() {
		if (types == null) types = new ArrayList<>();
		return types;
	}

	public void addType(String type) {
		getTypes().add(type);
	}

	public static RecBoardGame loadGameFromString(String gameString) {
		RecBoardGame game = null;

		String[] bits = gameString.split("\\|\\|");
		game = new RecBoardGame(bits[0],
								Integer.parseInt(bits[1]),
								Integer.parseInt(bits[2]),
								Double.parseDouble(bits[3]));

		if (bits[4].length() > 2) {
			bits[4] = bits[4].substring(1, bits[4].length() - 1);
			for (String mechanic : bits[4].split("\\|")) game.addMechanic(mechanic);
		}

		if (bits[5].length() > 2) {
			bits[5] = bits[5].substring(1, bits[5].length() - 1);
			for (String category : bits[5].split("\\|")) game.addCategory(category);
		}

		if (bits[6].length() > 2) {
			bits[6] = bits[6].substring(1, bits[6].length() - 1);
			for (String type : bits[6].split("\\|")) game.addType(type);
		}

		return game;
	}

	public static RecBoardGame convertBoardGame(BoardGame boardGame) {
		RecBoardGame game = new RecBoardGame(boardGame.getName(), boardGame.getBggId(), -1, 5);
		for (GameExtra mechanic : boardGame.getMechanics()) game.addMechanic(mechanic.getName());
		for (GameExtra category : boardGame.getCategories()) game.addMechanic(category.getName());

		return game;
	}

	@Override
	public String toString() {
		String mainSeparator = "||", subSeperator = "|";
		String toReturn = "";
		toReturn += getName() + mainSeparator + getId() + mainSeparator + getRank() + mainSeparator + getRating();
		toReturn += mainSeparator + "[";
		if (!getMechanics().isEmpty()) {
			for (String mechanic : getMechanics()) toReturn += mechanic + subSeperator;
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		}
		toReturn += "]" + mainSeparator + "[";
		if (!getCategories().isEmpty()) {
			for (String category : getCategories()) toReturn += category + subSeperator;
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		}
		toReturn += "]" + mainSeparator + "[";
		if (!getTypes().isEmpty()) {
			for (String type : getTypes()) toReturn += type + subSeperator;
			toReturn = toReturn.substring(0, toReturn.length() - 1);
		}
		toReturn += "]";
		return toReturn;
	}
}
