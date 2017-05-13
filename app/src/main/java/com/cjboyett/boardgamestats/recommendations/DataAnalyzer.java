package com.cjboyett.boardgamestats.recommendations;

import android.content.Context;

import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.utility.data.FileController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import timber.log.Timber;

//import java.nio.file.Files;

/**
 * Created by Casey on 9/5/2016.
 */
public class DataAnalyzer {
	private Map<Integer, RecBoardGame> gamesList;
	private Map<String, List<Integer>> mechanicsMap, categoriesMap, typesMap, pairsMap, triplesMap;
	private static final double PAIR_MULTIPLIER = 1, TRIPLE_MULTIPLIER = 1;

	private FileController fileController;

	public DataAnalyzer(Context context) {
		fileController = new FileController(context);
		gamesList = generateGamesMap();
		mechanicsMap = generateMap("mechanics.txt");
		categoriesMap = generateMap("categories.txt");
		typesMap = generateMap("types.txt");
		pairsMap = generateMap("pairs.txt");
		triplesMap = generateMap("triples.txt");
	}

	public Map<Integer, RecBoardGame> getGamesList() {
		return gamesList;
	}

	private Map<String, List<Integer>> generateMap(String filePath) {
		fileController.setFileName(filePath);
		Map<String, List<Integer>> map = new TreeMap<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileController.load()));
			String line;
			while ((line = reader.readLine()) != null) {
				String key = line.substring(0, line.lastIndexOf(":"));
				String[] ids = line.substring(line.lastIndexOf(":") + 1).split("\\|");
				List<Integer> idsList = new ArrayList<>();
				for (String id : ids) idsList.add(Integer.parseInt(id));
				map.put(key, idsList);
			}

/*
			File file = new File(filePath);
			Files.lines(file.toPath()).forEach(s ->
			                                   {
				                                   String key = s.substring(0, s.lastIndexOf(":"));
				                                   String[] ids = s.substring(s.lastIndexOf(":") + 1).split("\\|");
				                                   List<Integer> idsList = new ArrayList<>();
				                                   for (String id : ids) idsList.add(Integer.parseInt(id));
				                                   map.put(key, idsList);
			                                   });
*/
		} catch (Exception e) {
			Timber.e(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				Timber.e(e);
			}
		}
		return map;
	}

	private Map<Integer, RecBoardGame> generateGamesMap() {
		Map<Integer, RecBoardGame> gamesMap = new TreeMap<>();
		BufferedReader reader = null;
		try {
			fileController.setFileName("games.txt");
			reader = new BufferedReader(new FileReader(fileController.load()));
			String line;
			while ((line = reader.readLine()) != null) {
				RecBoardGame game = RecBoardGame.loadGameFromString(line);
				gamesMap.put(game.getId(), game);
			}

/*
			File gameFile = new File("game.txt");
			Files.lines(gameFile.toPath()).forEachOrdered(s ->
			                                              {
				                                              BoardGame game = BoardGame.loadGameFromString(s);
				                                              gamesMap.put(game.getId(), game);
			                                              });
*/
		} catch (Exception e) {
			Timber.e(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				Timber.e(e);
			}
		}
		return gamesMap;
	}

	public List<RecBoardGame> recommendationsFromPlayedGames(Map<String, Double> playerScores) {
		List<RecBoardGame> recommendations = new ArrayList<>();
		Map<Integer, Double> potentialMatches = new TreeMap<>();

		for (String key : playerScores.keySet()) {
			if (mechanicsMap.containsKey(key)) {
				for (Integer id : mechanicsMap.get(key)) {
					addToMap(potentialMatches, id, playerScores.get(key) * gamesList.get(id).getRating());
				}
			} else if (categoriesMap.containsKey(key)) {
				for (Integer id : categoriesMap.get(key)) {
					addToMap(potentialMatches, id, playerScores.get(key) * gamesList.get(id).getRating());
				}
			} else if (typesMap.containsKey(key)) {
				for (Integer id : typesMap.get(key)) {
					addToMap(potentialMatches, id, playerScores.get(key) * gamesList.get(id).getRating());
				}
			} else if (pairsMap.containsKey(key)) {
				for (Integer id : pairsMap.get(key)) {
					addToMap(potentialMatches,
							 id,
							 playerScores.get(key) * gamesList.get(id).getRating() * PAIR_MULTIPLIER);
				}
			} else if (triplesMap.containsKey(key)) {
				for (Integer id : triplesMap.get(key)) {
					addToMap(potentialMatches,
							 id,
							 playerScores.get(key) * gamesList.get(id).getRating() * TRIPLE_MULTIPLIER);
				}
			}
		}

		for (Integer id : potentialMatches.keySet()) {
			RecBoardGame game = gamesList.get(id);
			game.setRecommendationLevel(potentialMatches.get(id));
			recommendations.add(game);
		}

		Collections.sort(recommendations, new Comparator<RecBoardGame>() {
			@Override
			public int compare(RecBoardGame game1, RecBoardGame game2) {
				if (game1.getRecommendationLevel() == game2.getRecommendationLevel()) return 0;
				else return (game1.getRecommendationLevel() <= game2.getRecommendationLevel() ? 1 : -1);

			}
		});

		return recommendations;
	}

	public List<RecBoardGame> recommendationsFromGames(Map<RecBoardGame, Double> seeds) {
		Map<String, Double> scores = new TreeMap<>();
		for (RecBoardGame game : seeds.keySet()) addScores(scores, game, seeds.get(game));
		return recommendationsFromPlayedGames(scores);
	}

	public Map<RecBoardGame, Double> convertToRecBoardGame(Map<BoardGame, Double> seeds) {
		Map<RecBoardGame, Double> convertedGames = new HashMap<>();
		for (BoardGame game : seeds.keySet()) {
			if (getGamesList().containsKey(game.getBggId()))
				convertedGames.put(getGamesList().get(game.getBggId()), seeds.get(game));
			else convertedGames.put(RecBoardGame.convertBoardGame(game), seeds.get(game));
		}
		return convertedGames;
	}

	private void addScores(Map<String, Double> scores, RecBoardGame game, double score) {
		List<String> categoriesList = game.getCategories();
		List<String> mechanicsList = game.getMechanics();
		List<String> typesList = game.getTypes();

		for (int i = 0; i < categoriesList.size(); i++) {
			String category = categoriesList.get(i);

			for (int j = i + 1; j < categoriesList.size(); j++) {
				String key = category + "-" + categoriesList.get(j);
				addToMap(scores, key, score);
			}

			for (int j = 0; j < mechanicsList.size(); j++) {
				String key = category + "-" + mechanicsList.get(j);
				addToMap(scores, key, score);
				for (int k = j + 1; k < mechanicsList.size(); k++) {
					String secondKey = key + "-" + mechanicsList.get(k);
					addToMap(scores, secondKey, score);
				}
			}

			for (int j = 0; j < typesList.size(); j++) {
				String key = category + "-" + typesList.get(j);
				addToMap(scores, key, score);
			}
		}

		for (int i = 0; i < mechanicsList.size(); i++) {
			String mechanic = mechanicsList.get(i);
			for (int j = i + 1; j < mechanicsList.size(); j++) {
				String key = mechanic + "-" + mechanicsList.get(j);
				addToMap(scores, key, score);
			}

			for (int j = 0; j < typesList.size(); j++) {
				String key = mechanic + "-" + typesList.get(j);
				addToMap(scores, key, score);
			}
		}
	}

	private void addToMap(Map<String, Double> scores, String key, double score) {
		if (!scores.containsKey(key)) scores.put(key, 0d);
		scores.put(key, scores.get(key) + score);
	}

	private void addToMap(Map<Integer, Double> scores, Integer key, double score) {
		if (!scores.containsKey(key)) scores.put(key, 0d);
		scores.put(key, scores.get(key) + score);
	}

}
