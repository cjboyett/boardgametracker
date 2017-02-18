package com.cjboyett.boardgamestats.activity;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.BaseInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.addgame.AddGameActivity;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameStatsDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameXmlParser;
import com.cjboyett.boardgamestats.model.games.GameExtra;
import com.cjboyett.boardgamestats.model.games.GameExtraPair;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.recommendations.DataAnalyzer;
import com.cjboyett.boardgamestats.recommendations.RecBoardGame;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.FileController;
import com.cjboyett.boardgamestats.utility.data.UrlUtilities;
import com.cjboyett.boardgamestats.utility.firebase.FirebaseUtility;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.RecommendedGamesAdapter;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class TestActivity extends AppCompatActivity {
	private View overlay;
	private ImageView[] imageViews;
	private List<Bitmap> images;
	private int currentImage = 5;
	private boolean[] finished;
	private int IMAGE_TRANSLATION_X, IMAGE_TRANSITION_DURATION = 500, IMAGE_ROTATION = 60;

	private static final int REQUEST_IMAGE = 200, REQUEST_PERMISSIONS = 250;

	private Activity activity = this;
	private CallbackManager callbackManager;

	private FirebaseUtility firebaseUtility;

	private DataAnalyzer dataAnalyzer;

	private List<String> paths;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = getLayoutInflater().inflate(R.layout.activity_test, null);
		setContentView(view);

		dataAnalyzer = new DataAnalyzer(this);

		overlay = view.findViewById(R.id.overlay);
		imageViews = new ImageView[6];
		imageViews[0] = (ImageView) view.findViewById(R.id.image1);
		imageViews[1] = (ImageView) view.findViewById(R.id.image2);
		imageViews[2] = (ImageView) view.findViewById(R.id.image3);
		imageViews[3] = (ImageView) view.findViewById(R.id.image4);
		imageViews[4] = (ImageView) view.findViewById(R.id.image5);
		imageViews[5] = (ImageView) view.findViewById(R.id.image6);

		images = new ArrayList<>();

		finished = new boolean[6];

		IMAGE_TRANSLATION_X = ViewUtilities.dpToPx(this, 280);

		((Button) view.findViewById(R.id.button_show_overlay)).setText("Analyze Games");
		view.findViewById(R.id.button_show_overlay).setVisibility(View.VISIBLE);
		view.findViewById(R.id.button_show_overlay).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				overlay.setVisibility(View.VISIBLE);
/*
				new AsyncTask<String, Void, Void>()
				{
					@Override
					protected Void doInBackground(String... params)
					{
//						downloadTextFiles();
//						downloadTopRankedGames();
						return null;
					}
				}.execute("");
				Map<BoardGame, Double> seeds = new HashMap<>();
				final GamesDbHelper dbHelper = new GamesDbHelper(activity);
				final List<String> games = BoardGameDbUtility.getAllPlayedGames(dbHelper);
				for (String game : games)
				{
					if (game.equals("Istanbul") || game.equals("Concordia") || game.equals("Terra Mystica"))
					{
						BoardGame boardGame = BoardGameDbUtility.getBoardGame(dbHelper, game);
//						int boardGameScore = BoardGameStatsDbUtility.getGameScore(dbHelper, game);
//						seeds.put(boardGame, (double) boardGameScore);
						switch (game)
						{
							case "Istanbul":
								seeds.put(boardGame, 0.75);
								break;
							case "Concordia":
								seeds.put(boardGame, 1d);
								break;
							case "Terra Mystica":
								seeds.put(boardGame, -0.5d);
								break;
						}
					}
				}
				dbHelper.close();
*/

//				analyzeGamesWithDataAnalyzer(seeds);
//				analyzeGames();
			}
		});
		view.findViewById(R.id.button_hide_overlay).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				overlay.setVisibility(View.GONE);
			}
		});

		view.findViewById(R.id.button_pick_images).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectImages();
			}
		});

		view.findViewById(R.id.button_next).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				shareImages();
				//nextImage();
			}
		});

		view.findViewById(R.id.button_email_sign_up).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				firebaseUtility.showEmailSignup();
			}
		});

		view.findViewById(R.id.button_email_sign_in).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				firebaseUtility.showEmailSignIn();
			}
		});

		view.findViewById(R.id.button_backup_database).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				firebaseUtility.backupDatabase();
			}
		});

		callbackManager = CallbackManager.Factory.create();
		LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
		loginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));
		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				firebaseUtility.facebookSignIn(loginResult);
			}

			@Override
			public void onCancel() {
				Log.d("CANCEL", "Canceled");
			}

			@Override
			public void onError(FacebookException error) {
				Log.d("ERROR", error.toString());
			}
		});

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
			if (true)//!Preferences.hasAskedPermission(this))
			{
				String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};
				requestPermissions(perms, REQUEST_PERMISSIONS);
				Preferences.setHasAskedPermission(this, true);
			}
		}
	}

	private void selectImages() {
		MultiImageSelector.create()
						  .count(6)
						  .start(this, REQUEST_IMAGE);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void nextImage() {
		imageViews[0].animate()
					 .translationX(-IMAGE_TRANSLATION_X)
					 .translationZ(-1f)
					 .rotationY(-IMAGE_ROTATION)
					 .scaleX(11f / 16f)
					 .scaleY(13f / 16f)
					 .setDuration(IMAGE_TRANSITION_DURATION)
					 .setInterpolator(new CustomInterpolator(IMAGE_ROTATION, false))
					 .setListener(new Animator.AnimatorListener() {
						 @Override
						 public void onAnimationStart(Animator animation) {
						 }

						 @Override
						 public void onAnimationEnd(Animator animation) {
							 finished[0] = true;
							 shiftImages();
						 }

						 @Override
						 public void onAnimationCancel(Animator animation) {
						 }

						 @Override
						 public void onAnimationRepeat(Animator animation) {
						 }
					 })
					 .start();

		imageViews[1].animate()
					 .translationX(-1.1547f * IMAGE_TRANSLATION_X)
					 .translationZ(-2)
					 .rotationY(-90)
					 .scaleX(5f / 8f)
					 .scaleY(5f / 8f)
					 .setDuration(IMAGE_TRANSITION_DURATION / 2)
					 .setInterpolator(new CustomInterpolator(IMAGE_ROTATION, false))
					 .setListener(new Animator.AnimatorListener() {
						 @Override
						 public void onAnimationStart(Animator animation) {
						 }

						 @Override
						 public void onAnimationEnd(Animator animation) {
							 imageViews[1].animate()
										  .translationX(-IMAGE_TRANSLATION_X)
										  .rotationY(-120)
										  .scaleX(7f / 16f)
										  .scaleY(7f / 16f)
										  .setDuration(IMAGE_TRANSITION_DURATION / 2)
										  .setInterpolator(new CustomInterpolator(IMAGE_ROTATION, false))
										  .setListener(new Animator.AnimatorListener() {
											  @Override
											  public void onAnimationStart(Animator animation) {
											  }

											  @Override
											  public void onAnimationEnd(Animator animation) {
												  finished[1] = true;
												  shiftImages();
											  }

											  @Override
											  public void onAnimationCancel(Animator animation) {
											  }

											  @Override
											  public void onAnimationRepeat(Animator animation) {
											  }
										  })
										  .start();
						 }

						 @Override
						 public void onAnimationCancel(Animator animation) {
						 }

						 @Override
						 public void onAnimationRepeat(Animator animation) {
						 }
					 })
					 .start();

		imageViews[2].animate()
					 .translationX(0)
					 .rotationY(-180)
					 .scaleX(1f / 4f)
					 .scaleY(1f / 4f)
					 .setDuration(IMAGE_TRANSITION_DURATION)
					 .setInterpolator(new CustomInterpolator(IMAGE_ROTATION, false))
					 .setListener(new Animator.AnimatorListener() {
						 @Override
						 public void onAnimationStart(Animator animation) {
						 }

						 @Override
						 public void onAnimationEnd(Animator animation) {
							 finished[2] = true;
							 shiftImages();
						 }

						 @Override
						 public void onAnimationCancel(Animator animation) {
						 }

						 @Override
						 public void onAnimationRepeat(Animator animation) {
						 }
					 })
					 .start();

		imageViews[3].animate()
					 .translationX(IMAGE_TRANSLATION_X)
					 .rotationY(-240)
					 .scaleX(7f / 16f)
					 .scaleY(7f / 16f)
					 .setDuration(IMAGE_TRANSITION_DURATION)
					 .setInterpolator(new CustomInterpolator(IMAGE_ROTATION, false))
					 .setListener(new Animator.AnimatorListener() {
						 @Override
						 public void onAnimationStart(Animator animation) {
						 }

						 @Override
						 public void onAnimationEnd(Animator animation) {
							 finished[3] = true;
							 shiftImages();
						 }

						 @Override
						 public void onAnimationCancel(Animator animation) {
						 }

						 @Override
						 public void onAnimationRepeat(Animator animation) {
						 }
					 })
					 .start();

		imageViews[4].animate()
					 .translationX(1.1547f * IMAGE_TRANSLATION_X)
					 .rotationY(-270)
					 .scaleX(5f / 8f)
					 .scaleY(5f / 8f)
					 .setDuration(IMAGE_TRANSITION_DURATION / 2)
					 .setInterpolator(new CustomInterpolator(IMAGE_ROTATION, false))
					 .setListener(new Animator.AnimatorListener() {
						 @Override
						 public void onAnimationStart(Animator animation) {
						 }

						 @Override
						 public void onAnimationEnd(Animator animation) {
							 currentImage++;
							 currentImage %= images.size();
							 imageViews[4].setImageBitmap(images.get(currentImage));
							 imageViews[4].animate()
										  .translationX(IMAGE_TRANSLATION_X)
										  .rotationY(-300)
										  .scaleX(11f / 16f)
										  .scaleY(13f / 16f)
										  .setDuration(IMAGE_TRANSITION_DURATION / 2)
										  .setInterpolator(new CustomInterpolator(IMAGE_ROTATION, false))
										  .setListener(new Animator.AnimatorListener() {
											  @Override
											  public void onAnimationStart(Animator animation) {

											  }

											  @Override
											  public void onAnimationEnd(Animator animation) {
												  finished[4] = true;
												  shiftImages();
											  }

											  @Override
											  public void onAnimationCancel(Animator animation) {
											  }

											  @Override
											  public void onAnimationRepeat(Animator animation) {
											  }
										  })
										  .start();
						 }

						 @Override
						 public void onAnimationCancel(Animator animation) {
						 }

						 @Override
						 public void onAnimationRepeat(Animator animation) {
						 }
					 })
					 .start();

		imageViews[5].animate()
					 .translationX(0)
					 .rotationY(-360)
					 .scaleX(1)
					 .scaleY(1)
					 .setDuration(IMAGE_TRANSITION_DURATION)
					 .setInterpolator(new CustomInterpolator(IMAGE_ROTATION, false))
					 .setListener(new Animator.AnimatorListener() {
						 @Override
						 public void onAnimationStart(Animator animation) {
						 }

						 @Override
						 public void onAnimationEnd(Animator animation) {
							 finished[5] = true;
							 shiftImages();
						 }

						 @Override
						 public void onAnimationCancel(Animator animation) {
						 }

						 @Override
						 public void onAnimationRepeat(Animator animation) {
						 }
					 })
					 .start();
	}

	private void shiftImages() {
		if (finished[0] && finished[1] && finished[2] && finished[3] && finished[4] && finished[5]) {
			imageViews[5].setRotationY(0);
			ImageView temp = imageViews[5];
			for (int i = 5; i > 0; i--) imageViews[i] = imageViews[i - 1];
			imageViews[0] = temp;
			imageViews[5].bringToFront();
			overlay.invalidate();

/*
			offset--;
			if (offset == -1) offset = 5;
			Log.d("OFFSET", offset + "");
*/
			for (int i = 0; i < 6; i++) finished[i] = false;
		}
	}

	private void shareImages() {
		long ONE_MEGABYTE = 1024 * 1024;
		FirebaseStorage storage = FirebaseStorage.getInstance();
		StorageReference storageReference = storage.getReferenceFromUrl("gs://games-tracker-53f3f.appspot.com")
												   .child(Preferences.getAuthId(this));
		for (int i = 0; i < images.size(); i++) {
			final Bitmap bitmap = images.get(i);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

			Log.d("SIZE", bitmap.getByteCount() + "");
/*
			if (bitmap.getByteCount() > ONE_MEGABYTE)
			{
				double scale = Math.sqrt(1024. * 1024. / bitmap.getByteCount());
				Log.d("SCALE", scale + " " + (int)(bitmap.getWidth() * scale) + " " + (int)(bitmap.getHeight() * scale));
				bitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() * scale), (int)(bitmap.getHeight() * scale), false);
			}
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
*/
/*
			byte[] bytes = outputStream.toByteArray();

			UploadTask uploadTask = storageReference.child(i + ".jpg").putBytes(bytes);
			uploadTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>()
			{
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
				{
					Log.d("SUCCESS", "It was successful");
				}
			}).addOnFailureListener(this, new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					Log.d("FAILURE", "It was not successful");
				}
			});
		}
*/
		}
/*
		ShareContent shareContent = ViewUtilities.createShareMediaContent(images);
		ShareDialog shareDialog = new ShareDialog(this);
		shareDialog.show(shareContent, ShareDialog.Mode.AUTOMATIC);
*/
	}

	@Override
	protected void onResume() {
		super.onResume();
		firebaseUtility = new FirebaseUtility(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		firebaseUtility.close();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		Log.d("PERMISSIONS", requestCode + " " + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));

		if (requestCode == REQUEST_PERMISSIONS) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Preferences.setCanAccessStoragePreference(this, true);
			} else Preferences.setCanAccessStoragePreference(this, false);
			if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				Preferences.setCanUseCameraPreference(this, true);
			} else Preferences.setCanUseCameraPreference(this, false);

//			canWrite = Preferences.canAccessStorage(this);
//			canUseCamera = Preferences.canUseCamera(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE) {
			final List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
			this.paths = paths;
			images.clear();

			for (int i = 0; i < paths.size(); i++) {
				final int finalI = i;
				new AsyncTask<String, Void, Bitmap>() {
					@Override
					protected Bitmap doInBackground(String... params) {
						BitmapFactory.Options options = new BitmapFactory.Options();
						Bitmap bitmap = BitmapFactory.decodeFile(params[0], options);

						ExifInterface exifInterface = null;
						try {
							exifInterface = new ExifInterface(params[0]);
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (exifInterface != null) {
							bitmap = rotateBitmap(bitmap,
												  exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
																				ExifInterface.ORIENTATION_UNDEFINED));
						}

						int maxDimension = 1200;

/*
						if (Math.max(bitmap.getWidth(), bitmap.getHeight()) > maxDimension)
						{
							bitmap = scaleBitmap(bitmap, maxDimension);
							int width = bitmap.getWidth();
							int height = bitmap.getHeight();
							float scale = 1200f / Math.max(width, height);
//							Log.d("STUFF", width + " " + height + " " + scale);
//							float scaleWidth = scale * width;
//							float scaleHeight = scale * height;

							Matrix matrix = new Matrix();
							matrix.postScale(scale, scale);

							Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
							bitmap = scaledBitmap;
						}
*/

						Log.d("DIMENSIONS", bitmap.getWidth() + " " + bitmap.getHeight());

						return bitmap;
					}

					@Override
					protected void onPostExecute(Bitmap bitmap) {
						images.add(bitmap);
						int i = 0;
						while (finalI + i * paths.size() < 6) {
							imageViews[finalI + i * paths.size()].setImageBitmap(bitmap);
/*							if (bitmap.getHeight() < 500 && bitmap.getWidth() < 300)
							{
								imageViews[finalI + i * paths.size()].setMaxWidth(bitmap.getWidth());
								imageViews[finalI + i * paths.size()].setMaxHeight(bitmap.getHeight());
							}
							else
							{
								imageViews[finalI + i * paths.size()].setMaxWidth(300);
								imageViews[finalI + i * paths.size()].setMaxHeight(500);
							}*/
							i++;
						}
					}
				}.execute(paths.get(i));
			}
		}
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

		Matrix matrix = new Matrix();
		switch (orientation) {
			case ExifInterface.ORIENTATION_NORMAL:
				return bitmap;
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				matrix.setScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				matrix.setRotate(180);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.setRotate(90);
				break;
			case ExifInterface.ORIENTATION_TRANSVERSE:
				matrix.setRotate(-90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				matrix.setRotate(-90);
				break;
			default:
				return bitmap;
		}
		try {
			Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle();
			return bmRotated;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

	private Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float n = Math.max(width, height) / maxDimension;
		int newWidth = (int) (width / n);
		int newHeight = (int) (height / n);
		Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.getConfig());
		scaledBitmap.setHasAlpha(false);

		int[] pixels = new int[newWidth * newHeight];

		for (int i = 0; i < newWidth; i++)
			for (int j = 0; j < newHeight; j++)
				pixels[j * newWidth + newHeight] = scaleBlock(bitmap, i, j, n);
//				scaledBitmap.setPixel(i, j, scaleBlock(bitmap, i, j, n));

		scaledBitmap.setPixels(pixels, 0, newWidth, 0, 0, newWidth, newHeight);

		return scaledBitmap;
	}

	private int scaleBlock(Bitmap bitmap, int i, int j, float n) {
		int red = 0, green = 0, blue = 0;

		int xStart = (int) (Math.floor(n * i));
		int yStart = (int) (Math.floor(n * j));
		int xStep = (int) (Math.floor(n * (i + 1)) - xStart);
		int yStep = (int) (Math.floor(n * (j + 1)) - yStart);

		int total = 0;

		for (int x = xStart; x < xStart + xStep; x++) {
			for (int y = yStart; y < yStart + yStep; y++) {
				try {
					int color = bitmap.getPixel(x, y);
					red += Color.red(color);
					green += Color.green(color);
					blue += Color.blue(color);
					total++;
				} catch (Exception e) {
				}
			}
		}

		red /= total;
		blue /= total;
		green /= total;

		return Color.rgb(red, green, blue);
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
//		for (RecBoardGame game : recommendations) Log.d("GAME", game.getRank() + " " + game.getName() + " " + game.getRecommendationLevel());
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
									e.printStackTrace();
								} finally {
									try {
										in.close();
									} catch (Exception e) {
										e.printStackTrace();
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
//											.setMessage(toShow[0].getName() + "\n" + toShow[1].getName() + "\n" + toShow[2].getName())
											.setPositiveButton("Okay", null)
											.withYancey(false)
											.create();
									alertDialog.show();
								}
							}
						}.execute(item.thumbnailUrl);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.execute(toShow[0].getId(), toShow[1].getId(), toShow[2].getId());
	}

	private void analyzeGames() {
		Map<String, Integer> mechanicIntegerMap = new TreeMap<>();
		Map<String, Integer> categoryIntegerMap = new TreeMap<>();
		Map<String, Integer> pairIntegerMap = new TreeMap<>();

		final GamesDbHelper dbHelper = new GamesDbHelper(this);
		List<String> games = BoardGameDbUtility.getAllPlayedGames(dbHelper);
		for (String game : games) {
			BoardGame boardGame = BoardGameDbUtility.getBoardGame(dbHelper, game);
			int boardGameScore = BoardGameStatsDbUtility.getGameScore(dbHelper, game);
			for (GameExtra mechanic : boardGame.getMechanics()) {
				if (!mechanicIntegerMap.containsKey(mechanic.getName())) mechanicIntegerMap.put(mechanic.getName(), 0);
				mechanicIntegerMap.put(mechanic.getName(), mechanicIntegerMap.get(mechanic.getName()) + boardGameScore);
			}
			for (GameExtra category : boardGame.getCategories()) {
				if (!categoryIntegerMap.containsKey(category.getName())) categoryIntegerMap.put(category.getName(), 0);
				categoryIntegerMap.put(category.getName(), categoryIntegerMap.get(category.getName()) + boardGameScore);

				for (GameExtra mechanic : boardGame.getMechanics()) {
					GameExtraPair pair = new GameExtraPair(category, mechanic);
					if (!pairIntegerMap.containsKey(pair.toString())) pairIntegerMap.put(pair.toString(), 0);
					pairIntegerMap.put(pair.toString(), pairIntegerMap.get(pair.toString()) + boardGameScore);
				}
			}
		}
		dbHelper.close();

		String mechanicsWinners = "", categoryWinners = "", pairWinners = "";
		int max = -1, second = -1, third = -1;

		for (String mechanic : mechanicIntegerMap.keySet()) {
			int score = mechanicIntegerMap.get(mechanic);
			if (score > max) {
				third = second;
				second = max;
				max = score;
			} else if (score > second) {
				third = second;
				second = score;
			} else if (score > third) {
				third = score;
			}
		}
		for (String mechanic : mechanicIntegerMap.keySet())
			if (mechanicIntegerMap.get(mechanic) >= third) {
				Log.d("MECHANIC", mechanic + " " + mechanicIntegerMap.get(mechanic));
				mechanicsWinners += mechanic + " " + mechanicIntegerMap.get(mechanic) + "\n";
			}

		max = -1;
		second = -1;
		third = -1;

		for (String category : categoryIntegerMap.keySet()) {
			int score = categoryIntegerMap.get(category);
			if (score > max) {
				third = second;
				second = max;
				max = score;
			} else if (score > second) {
				third = second;
				second = score;
			} else if (score > third) {
				third = score;
			}
		}

		for (String category : categoryIntegerMap.keySet())
			if (categoryIntegerMap.get(category) >= third) {
				Log.d("CATEGORY", category + " " + categoryIntegerMap.get(category));
				categoryWinners += category + " " + categoryIntegerMap.get(category) + "\n";
			}

		max = -1;
		second = -1;
		third = -1;

		for (String pair : pairIntegerMap.keySet()) {
			int score = pairIntegerMap.get(pair);
			if (score > max) {
				third = second;
				second = max;
				max = score;
			} else if (score > second) {
				third = second;
				second = score;
			} else if (score > third) {
				third = score;
			}
		}
		for (String pair : pairIntegerMap.keySet())
			if (pairIntegerMap.get(pair) >= third) {
				Log.d("PAIR", pair + " " + pairIntegerMap.get(pair));
				pairWinners += pair + " " + pairIntegerMap.get(pair) + "\n";
			}

		final AlertDialog pairsDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Pairs")
				.setMessage(StringUtils.chomp(pairWinners))
				.setPositiveButton("Okay", null)
				.create();


		final AlertDialog categoriesDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Categories")
				.setMessage(StringUtils.chomp(categoryWinners))
				.setPositiveButton("Okay", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						pairsDialog.show();
					}
				})
				.create();

		AlertDialog mechanicsDialog = new ViewUtilities.DialogBuilder(this)
				.setTitle("Mechanics")
				.setMessage(StringUtils.chomp(mechanicsWinners))
				.setPositiveButton("Okay", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						categoriesDialog.show();
					}
				})
				.create();

		mechanicsDialog.show();

	}

	private void downloadTextFiles() {
		downloadTextFile("game.txt", "games.txt");
		downloadTextFile("mechanics.txt", "mechanics.txt");
		downloadTextFile("categories.txt", "categories.txt");
		downloadTextFile("types.txt", "types.txt");
		downloadTextFile("pairs.txt", "pairs.txt");
		downloadTextFile("triples.txt", "triples.txt");
	}

	private void downloadTextFile(String fileUrl, String filePath) {
		long ONE_MEGABYTE = 1024 * 1024;
		final FileController fileController = new FileController(this);
		fileController.setFileName(filePath);

		FirebaseStorage storage = FirebaseStorage.getInstance();
		StorageReference storageReference = storage.getReferenceFromUrl("gs://games-tracker-53f3f.appspot.com");
		storageReference.child(fileUrl)
						.getBytes(2 * ONE_MEGABYTE)
						.addOnSuccessListener(new OnSuccessListener<byte[]>() {
							@Override
							public void onSuccess(byte[] bytes) {
								fileController.save(bytes);
							}
						})
						.addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {

							}
						});

	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
	private class CustomInterpolator extends BaseInterpolator {
		// In degrees
		private float angle;
		private boolean reversed;

		public CustomInterpolator(float angle, boolean reversed) {
			this.angle = (float) (angle / 360f * 2 * Math.PI);
			this.reversed = reversed;
		}

		@Override
		public float getInterpolation(float input) {
			float toReturn;
			if (!reversed) toReturn = (float) Math.sin(input * angle);
			else toReturn = (float) Math.sin((1 - input) * angle);
//			Log.d("INTERPOLATING", input + " " + (float)(toReturn / Math.sin(angle)));
			return (float) (toReturn / Math.sin(angle));
		}
	}
}
