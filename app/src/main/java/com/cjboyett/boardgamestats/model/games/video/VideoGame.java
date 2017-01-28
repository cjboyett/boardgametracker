package com.cjboyett.boardgamestats.model.games.video;

import com.cjboyett.boardgamestats.data.games.video.VideoGameXmlParser;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.GameExtra;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 3/24/2016.
 */
public class VideoGame extends Game {
	private String releaseDate;
	private List<GameExtra> compilations;
	private List<GameExtra> developers;
	private List<GameExtra> franchises;
	private List<GameExtra> genres;
	private List<GameExtra> modes;
	private List<GameExtra> platforms;
	private List<GameExtra> publishers;
	private List<GameExtra> series;
	private List<GameExtra> themes;

	public static final int COMPILATION = 0, DEVELOPER = 1, FRANCHISE = 2, GENRE = 3, MODE = 4, PLATFORM = 5,
			PUBLISHER = 6, SERIES = 7, THEME = 8;

	public VideoGame(String name) {
		super(name);
	}

	public VideoGame(String name, String description, int bggId) {
		super(name, description, bggId);
	}

	public VideoGame(String name, String description, String thumbnailUrl, int bggId) {
		super(name, description, thumbnailUrl, bggId);
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public List<GameExtra> getCompilations() {
		if (compilations == null) compilations = new ArrayList<>();
		return compilations;
	}

	public void addCompilation(VideoGameCompilation compilation) {
		getCompilations().add(compilation);
	}

	public List<GameExtra> getDevelopers() {
		if (developers == null) developers = new ArrayList<>();
		return developers;
	}

	public void addDeveloper(VideoGameDeveloper developer) {
		getDevelopers().add(developer);
	}

	public List<GameExtra> getFranchises() {
		if (franchises == null) franchises = new ArrayList<>();
		return franchises;
	}

	public void addFranchise(VideoGameFranchise franchise) {
		getFranchises().add(franchise);
	}

	public List<GameExtra> getGenres() {
		if (genres == null) genres = new ArrayList<>();
		return genres;
	}

	public void addGenre(VideoGameGenre genre) {
		getGenres().add(genre);
	}

	public List<GameExtra> getModes() {
		if (modes == null) modes = new ArrayList<>();
		return modes;
	}

	public void addMode(VideoGameMode mode) {
		getModes().add(mode);
	}

	public List<GameExtra> getPlatforms() {
		if (platforms == null) platforms = new ArrayList<>();
		return platforms;
	}

	public void addPlatform(VideoGamePlatform platform) {
		getPlatforms().add(platform);
	}

	public List<GameExtra> getPublishers() {
		if (publishers == null) publishers = new ArrayList<>();
		return publishers;
	}

	public void addPublisher(VideoGamePublisher publisher) {
		getPublishers().add(publisher);
	}

	public List<GameExtra> getSeries() {
		if (series == null) series = new ArrayList<>();
		return series;
	}

	public void addSeries(VideoGameSeries series) {
		getSeries().add(series);
	}

	public List<GameExtra> getThemes() {
		if (themes == null) themes = new ArrayList<>();
		return themes;
	}

	public void addTheme(VideoGameTheme theme) {
		getThemes().add(theme);
	}

	public static VideoGame createGame(VideoGameXmlParser.Item item) {
		VideoGame game = new VideoGame(item.name, item.description, item.thumbnailUrl, item.id);

		game.setYearPublished(Integer.parseInt(item.releaseDate.substring(0, 4)));
		game.setReleaseDate(item.releaseDate);

		for (String[] extra : item.links) {
			if (extra[0].equals("videogamecompilation"))
				game.addCompilation(new VideoGameCompilation(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("videogamedeveloper"))
				game.addDeveloper(new VideoGameDeveloper(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("videogamefranchise"))
				game.addFranchise(new VideoGameFranchise(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("videogamegenre"))
				game.addGenre(new VideoGameGenre(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("videogamemode"))
				game.addMode(new VideoGameMode(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("videogameplatform"))
				game.addPlatform(new VideoGamePlatform(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("videogamepublisher"))
				game.addPublisher(new VideoGamePublisher(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("videogameseries"))
				game.addSeries(new VideoGameSeries(extra[2], Integer.parseInt(extra[1])));
			else if (extra[0].equals("videogametheme"))
				game.addTheme(new VideoGameTheme(extra[2], Integer.parseInt(extra[1])));
		}

		return game;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof VideoGame &&
				((VideoGame) obj).getBggId() == getBggId() &&
				((VideoGame) obj).getName().equalsIgnoreCase(getName());
	}

}
