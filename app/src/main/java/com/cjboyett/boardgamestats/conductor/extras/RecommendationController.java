package com.cjboyett.boardgamestats.conductor.extras;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.MyApp;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.addgame.AddGameActivity;
import com.cjboyett.boardgamestats.conductor.ConductorActivity;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameXmlParser;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.recommendations.DataAnalyzer;
import com.cjboyett.boardgamestats.recommendations.RecBoardGame;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.FileController;
import com.cjboyett.boardgamestats.utility.data.UrlUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.AddGameForRecommendationView;
import com.cjboyett.boardgamestats.view.DatedTextView;
import com.cjboyett.boardgamestats.view.adapter.FilteredGameArrayAdapter;
import com.cjboyett.boardgamestats.view.adapter.RecommendedGamesAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import timber.log.Timber;

public class RecommendationController extends BaseController {
	private Activity activity;
	private View view;
	private LinearLayout linearLayout;
	private Map<DatedTextView, AddGameForRecommendationView> addGameForRecommendationViews;
	private FilteredGameArrayAdapter filteredArrayAdapter;

	private ProgressDialog progressDialog;
	private DataAnalyzer dataAnalyzer;

	private int progress, totalProgress;
	private boolean downloading;

	private GestureDetectorCompat gestureDetector;

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.content_recommendation, container, false);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		activity = getActivity();
		gestureDetector = new GestureDetectorCompat(activity, new ScrollGestureListener());
		((ConductorActivity) getActivity()).setGestureDetector(gestureDetector);

		addGameForRecommendationViews = new HashMap<>();

		dataAnalyzer = new DataAnalyzer(activity);

		FileController fileController = new FileController(activity);
		fileController.setFileName("games.txt");
		if (!fileController.exists()) {
			AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Missing Files")
					.setMessage(
							"In order to recommend games I need to first download a few small files (about 2 MB in total).  May I download them?")
					.withYancey(true)
					.setPositiveButton("Yes", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (((MyApp) activity.getApplication()).isConnectedToInternet())
								downloadTextFiles();
							else
								ViewUtilities.errorDialog(activity).show();
						}
					})
					.setNegativeButton("No", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							handleBack();
						}
					})
					.create();
			alertDialog.setCancelable(false);
			alertDialog.show();
		}

		setColors();
		generateLayout();
		colorComponents();
	}

	@Override
	protected void onDetach(@NonNull View view) {
		((ConductorActivity) getActivity()).removeGestureDetector();
		super.onDetach(view);
	}

	protected void generateLayout() {
		linearLayout = (LinearLayout) view.findViewById(R.id.linearlayout_add_games);
		progressDialog = new ProgressDialog(activity);
		progressDialog.setMessage("Analyzing your games...");

		DataManager dataManager = DataManager.getInstance(activity.getApplication());
		List<String> games = dataManager.getAllGamesCombined();
		Iterator iter = games.iterator();
		while (iter.hasNext()) {
			String s = (String) iter.next();
			if (s.endsWith(":v") || s.endsWith(":r")) iter.remove();
		}
		filteredArrayAdapter =
				new FilteredGameArrayAdapter(activity,
											 android.R.layout.simple_list_item_1,
											 new ArrayList<>(games),
											 true);

		view.findViewById(R.id.button_add_game).setOnClickListener(new AddGameForRecommendationViewClickListener());

		// TODO Get the stupid progress dialog to show properly

		view.findViewById(R.id.button_recommend_games).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((MyApp) activity.getApplication()).isConnectedToInternet()) {
					new AsyncTask<String, Void, Void>() {
						@Override
						protected void onPreExecute() {
							progressDialog.show();
						}

						@Override
						protected Void doInBackground(String... strings) {
							return null;
						}

						@Override
						protected void onPostExecute(Void aVoid) {
							boolean hasGames = false;
							Map<BoardGame, Double> seeds = new HashMap<>();
							GamesDbHelper dbHelper = new GamesDbHelper(activity);

							for (AddGameForRecommendationView addGameView : addGameForRecommendationViews.values()) {
								if (!TextUtils.isEmpty(addGameView.getGame())) {
									BoardGame game = BoardGameDbUtility.getBoardGame(dbHelper, addGameView.getGame());
									seeds.put(game, addGameView.getWeight());
									hasGames = true;
								}
							}

							dbHelper.close();

							if (hasGames) analyzeGamesWithDataAnalyzer(seeds);
							else analyzeAllGames();
						}
					}.execute("");
				} else
					ViewUtilities.errorDialog(activity).show();
			}
		});

		view.findViewById(R.id.scrollview_board_game_data).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (Preferences.useSwipes(v.getContext()))
					return gestureDetector.onTouchEvent(event);
				return false;
			}
		});
	}

	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);
		view.findViewById(R.id.button_add_game).setBackgroundColor(backgroundColor);
		((TextView) view.findViewById(R.id.button_add_game)).setTextColor(foregroundColor);
		view.findViewById(R.id.button_recommend_games).setBackgroundColor(backgroundColor);
		((TextView) view.findViewById(R.id.button_recommend_games)).setTextColor(foregroundColor);
//		addGameForRecommendationView.colorComponents(backgroundColor, foregroundColor, hintTextColor);
		for (AddGameForRecommendationView addGameView : addGameForRecommendationViews.values())
			addGameView.colorComponents(backgroundColor, foregroundColor, hintTextColor);
	}

	private void analyzeGamesWithDataAnalyzer(Map<BoardGame, Double> seeds) {
		List<RecBoardGame> recommendations =
				dataAnalyzer.recommendationsFromGames(dataAnalyzer.convertToRecBoardGame(seeds));

		final GamesDbHelper dbHelper = new GamesDbHelper(activity);
		final List<String> games = BoardGameDbUtility.getAllPlayedGames(dbHelper);
		dbHelper.close();

		Iterator<RecBoardGame> iter = recommendations.iterator();
		while (iter.hasNext())
			if (games.contains(iter.next().getName())) iter.remove();
		final RecBoardGame[] toShow = new RecBoardGame[3];
		Random r = new Random();
		int recSizeStep = Math.min(recommendations.size() / 10, 50);
		toShow[0] = recommendations.remove(r.nextInt(recSizeStep));
		toShow[1] = recommendations.remove(r.nextInt(2 * recSizeStep) + recSizeStep);
		toShow[2] = recommendations.remove(r.nextInt(3 * recSizeStep) + 3 * recSizeStep);
		new AsyncTask<Integer, Void, Void>() {
			List<BoardGameXmlParser.Item> gameItems = new ArrayList<>();
			List<Integer> gameOrder = new ArrayList<>();
			Map<Integer, String> gameNames = new TreeMap<>();
			Map<Integer, Bitmap> thumbnails = new TreeMap<>();
			Map<Integer, Boolean> finished = new TreeMap<>();

			@Override
			protected Void doInBackground(Integer... ids) {
				for (Integer id : ids) {
					gameOrder.add(id);
					finished.put(id, false);
				}
				for (Integer id : ids)
					gameItems.addAll(UrlUtilities.loadBoardGameXmlFromNetwork(
							"https://www.boardgamegeek.com/xmlapi2/thing?id=" + id));
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				progressDialog.dismiss();
				for (final BoardGameXmlParser.Item item : gameItems) {
					gameNames.put(item.id, item.name);
					try {
						new AsyncTask<String, Void, Bitmap>() {
							@Override
							protected Bitmap doInBackground(String... url) {
								Bitmap bitmap = null;
								InputStream in = null;
								try {
									if (!url[0].startsWith("http://")) url[0] = "http://" + url[0];
									URL thumbnailUrl = new URL(url[0]);
									HttpURLConnection connection = (HttpURLConnection) thumbnailUrl.openConnection();
									connection.setReadTimeout(10000);
									connection.setConnectTimeout(15000);
									connection.setDoInput(true);
									connection.connect();
									in = connection.getInputStream();
									bitmap = BitmapFactory.decodeStream(in);
								} catch (Exception e) {
									Timber.e(e);
								} finally {
									try {
										in.close();
									} catch (Exception e) {
										Timber.e(e);
									}
								}

								return bitmap;
							}

							@Override
							protected void onPostExecute(Bitmap bitmap) {
								thumbnails.put(item.id, bitmap);
								finished.put(item.id, true);
								boolean allFinished = true;
								for (Integer id : finished.keySet()) allFinished = allFinished && finished.get(id);
								if (allFinished) {
									ListView view = new ListView(activity);

									view.setAdapter(new RecommendedGamesAdapter(activity,
																				gameNames,
																				thumbnails,
																				gameOrder));
									view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
										@Override
										public void onItemClick(AdapterView<?> parent, View view, int position,
																long id) {
											startActivity(new Intent(activity, AddGameActivity.class).putExtra(
													"SEARCH",
													gameNames.get(gameOrder.get(position))));
											ActivityUtilities.exitUp(activity);
										}
									});
									AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
											.setTitle("Recommendation")
											.setView(view)
											.setPositiveButton("Okay", null)
											.withYancey(false)
											.create();
									alertDialog.show();
								}
							}
						}.execute(item.thumbnailUrl);
					} catch (Exception e) {
						Timber.e(e);
					}
				}
			}
		}.execute(toShow[0].getId(), toShow[1].getId(), toShow[2].getId());
	}

	private void analyzeAllGames() {
		Map<BoardGame, Double> seeds = new HashMap<>();
		final GamesDbHelper dbHelper = new GamesDbHelper(activity);
		final List<String> games = BoardGameDbUtility.getAllPlayedGames(dbHelper);
		for (String game : games) {
			BoardGame boardGame = BoardGameDbUtility.getBoardGame(dbHelper, game);
			int boardGameScore = BoardGameStatsDbUtility.getGameScore(dbHelper, game);
			seeds.put(boardGame, (double) boardGameScore);
		}
		dbHelper.close();
		analyzeGamesWithDataAnalyzer(seeds);
	}

	private void downloadTextFiles() {
		progressDialog.setMessage("Downloading files...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress = 0;
		totalProgress = 6;
		progressDialog.setProgress(progress * 100 / totalProgress);
		progressDialog.show();

		downloading = true;
		downloadTextFile("game.txt", "games.txt");
		downloadTextFile("mechanics.txt", "mechanics.txt");
		downloadTextFile("categories.txt", "categories.txt");
		downloadTextFile("types.txt", "types.txt");
		downloadTextFile("pairs.txt", "pairs.txt");
		downloadTextFile("triples.txt", "triples.txt");
	}

	private void downloadTextFile(String fileUrl, String filePath) {
		if (downloading) {
			long ONE_MEGABYTE = 1024 * 1024;
			final FileController fileController = new FileController(activity);
			fileController.setFileName(filePath);

			FirebaseStorage storage = FirebaseStorage.getInstance();
			StorageReference storageReference = storage.getReferenceFromUrl("gs://games-tracker-53f3f.appspot.com");
			storageReference.child(fileUrl)
							.getBytes(2 * ONE_MEGABYTE)
							.addOnSuccessListener(new OnSuccessListener<byte[]>() {
								@Override
								public void onSuccess(byte[] bytes) {
									fileController.save(bytes);
									dataAnalyzer = new DataAnalyzer(activity);

									progress++;
									if (progress < totalProgress)
										progressDialog.setProgress(progress * 100 / totalProgress);
									else
										progressDialog.dismiss();
								}
							})
							.addOnFailureListener(new OnFailureListener() {
								@Override
								public void onFailure(@NonNull Exception e) {
									progressDialog.dismiss();
									downloading = false;
									AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
											.setTitle("Error")
											.setMessage(
													"Something went wrong while downloading your files.  Please try again later.")
											.setPositiveButton("Okay", new View.OnClickListener() {
												@Override
												public void onClick(View v) {
													handleBack();
												}
											})
											.create();
									alertDialog.setCancelable(false);
									alertDialog.show();
								}
							});
		}
	}

/*
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Preferences.useSwipes(this)) gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
*/

	private class AddGameForRecommendationViewClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			AddGameForRecommendationView newAddGameForRecommendationView = new AddGameForRecommendationView(activity);
			newAddGameForRecommendationView.colorComponents(backgroundColor, foregroundColor, hintTextColor);
			newAddGameForRecommendationView.setAdapter(filteredArrayAdapter);
			newAddGameForRecommendationView.findViewById(R.id.button_remove_game)
										   .setOnClickListener(new View.OnClickListener() {
											   @Override
											   public void onClick(View v) {
												   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
													   TransitionManager.beginDelayedTransition((ViewGroup) view);
												   }

												   linearLayout.removeView(addGameForRecommendationViews.remove(v));
											   }
										   });

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				TransitionManager.beginDelayedTransition((ViewGroup) view, new Fade(Fade.IN));
			}

			linearLayout.addView(newAddGameForRecommendationView);

			boolean jumpToNewView = true;
			for (AddGameForRecommendationView addGameView : addGameForRecommendationViews.values()) {
				String text = ((TextView) addGameView.findViewById(R.id.edittext_add_game)).getText()
																						   .toString();
				if (text == null || text.equals("")) {
					jumpToNewView = false;
					break;
				}
			}
			if (jumpToNewView) newAddGameForRecommendationView.findViewById(R.id.edittext_add_game)
															  .requestFocus();
			addGameForRecommendationViews.put((DatedTextView) newAddGameForRecommendationView.findViewById(R.id.button_remove_game),
											  newAddGameForRecommendationView);
		}
	}

	private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (Math.abs(velocityX) > Math.abs(velocityY)) {
				if (Math.abs(e1.getX() - e2.getX()) >= 200) {
					if (velocityX < -2000) {
						getRouter().popCurrentController();
						return true;
					}
				}
			}
			return false;
		}
	}
}
