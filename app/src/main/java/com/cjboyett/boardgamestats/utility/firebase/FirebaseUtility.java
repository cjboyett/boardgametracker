package com.cjboyett.boardgamestats.utility.firebase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.extras.LoginActivity;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameContract;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.board.BoardGameXmlParser;
import com.cjboyett.boardgamestats.data.games.rpg.RPGContract;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGXmlParser;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameXmlParser;
import com.cjboyett.boardgamestats.model.firebase.FirebaseGamePlayData;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.board.BoardGame;
import com.cjboyett.boardgamestats.model.games.board.BoardGamePlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.model.games.rpg.RolePlayingGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGame;
import com.cjboyett.boardgamestats.model.games.video.VideoGamePlayData;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.GameDownloadUtilities;
import com.cjboyett.boardgamestats.utility.data.UrlUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.facebook.AccessToken;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Casey on 8/29/2016.
 */
public class FirebaseUtility {
	private FirebaseAuth auth;
	private FirebaseAuth.AuthStateListener authStateListener;

	private Activity activity;

	public FirebaseUtility(Activity activity) {
		this.activity = activity;
		auth = FirebaseAuth.getInstance();
		authStateListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = auth.getCurrentUser();
				if (user != null) Log.d("USER", user.getEmail() + " " + user.getDisplayName());
				else Log.d("USER", "Does not exist");
			}
		};
		auth.addAuthStateListener(authStateListener);
	}

	public void showEmailSignup() {
		View signUpView = activity.getLayoutInflater().inflate(R.layout.dialog_sign_up_form, null);
		final EditText nameEditText, emailEditText;
		nameEditText = (EditText) signUpView.findViewById(R.id.edittext_name);
		emailEditText = (EditText) signUpView.findViewById(R.id.edittext_email);

		int foregroundColor = Preferences.getForegroundColor(activity);
		int hintTextColor = Preferences.getHintTextColor(activity);

		((TextView) signUpView.findViewById(R.id.textview_name)).setTextColor(foregroundColor);
		nameEditText.setTextColor(foregroundColor);
		nameEditText.setHintTextColor(hintTextColor);

		((TextView) signUpView.findViewById(R.id.textview_email)).setTextColor(foregroundColor);
		emailEditText.setTextColor(foregroundColor);
		emailEditText.setHintTextColor(hintTextColor);
		AlertDialog signUpDialog = new ViewUtilities.DialogBuilder(activity)
				.setTitle("Sign up")
				.setView(signUpView)
				.setPositiveButton("Okay", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String name = nameEditText.getText().toString();
						String email = emailEditText.getText().toString();
						if (validateEmail(email))
							signUpUser(name, email);
						else {
							AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
									.setTitle("Error")
									.setMessage(
											email + " does not appear to be a valid email address.  Please try again.")
									.withYancey(true)
									.setPositiveButton("Okay", null)
									.create();
							alertDialog.show();
						}
					}
				})
				.setNegativeButton("Cancel", null)
				.create();
		signUpDialog.show();
	}

	private boolean validateEmail(String email) {
		return email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-za-z]{2,})$");
	}

	private void signUpUser(final String name, final String email) {
		auth.createUserWithEmailAndPassword(email, createTempPassword())
			.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					if (task.isSuccessful()) {
						FirebaseUser user = auth.getCurrentUser();
						user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build());
						auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
							@Override
							public void onComplete(@NonNull Task<Void> task) {
								if (task.isSuccessful()) {
									auth.signOut();
									AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
											.setTitle("Reset password")
											.setMessage("I have sent an email to " + email +
																" to reset your password.  You will need to check your email and follow the link provided to reset your password before you can sign in.")
											.withYancey(true)
											.setPositiveButton("Okay", null)
											.create();
									alertDialog.show();

								} else {
									auth.getCurrentUser().delete();
									AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
											.setTitle("Error")
											.setMessage("I could not send an email to " + email +
																".  I need to send a link to reset your password before you can sign in.  You will have to try again.")
											.withYancey(true)
											.setPositiveButton("Okay", null)
											.create();
									alertDialog.show();
								}
							}
						});
					} else {
						AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
								.setTitle("Error")
								.setMessage("I could not create an account for " + email +
													".  Perhaps there is already any an account for that email.")
								.withYancey(true)
								.setPositiveButton("Okay", null)
								.create();
						alertDialog.show();
					}
				}
			});
	}

	private String createTempPassword() {
		SecureRandom r = new SecureRandom();
		return new BigInteger(130, r).toString(32);
	}

	public void showEmailSignIn(final LoginActivity loginActivity) {
		if (!signedIn()) {
			View signInView = activity.getLayoutInflater()
									  .inflate(R.layout.dialog_sign_in_form, null);
			final EditText emailEditText, passwordEditText;

			emailEditText = (EditText) signInView.findViewById(R.id.edittext_email);
			passwordEditText = (EditText) signInView.findViewById(R.id.edittext_password);

			int foregroundColor = Preferences.getForegroundColor(activity);
			int hintTextColor = Preferences.getHintTextColor(activity);

			((TextView) signInView.findViewById(R.id.textview_password)).setTextColor(foregroundColor);
			passwordEditText.setTextColor(foregroundColor);
			passwordEditText.setHintTextColor(hintTextColor);

			((TextView) signInView.findViewById(R.id.textview_email)).setTextColor(foregroundColor);
			emailEditText.setTextColor(foregroundColor);
			emailEditText.setHintTextColor(hintTextColor);
			AlertDialog signUpDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Sign in")
					.setView(signInView)
					.setPositiveButton("Okay", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							final String email = emailEditText.getText()
															  .toString();
							final String password = passwordEditText.getText()
																	.toString();
							if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
								auth.signInWithEmailAndPassword(email, password)
									.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
										@Override
										public void onComplete(@NonNull Task<AuthResult> task) {
											if (task.isSuccessful()) {
												if (TextUtils.isEmpty(Preferences.getAuthId(activity)))
													Preferences.setAuthId(activity, auth.getCurrentUser().getUid());
												else
													auth.getCurrentUser()
														.linkWithCredential(EmailAuthProvider.getCredential(email,
																											password));

												Preferences.setCurrentAuthProvider(activity, "Email");

												Toast.makeText(activity, "Log-in successful", Toast.LENGTH_SHORT)
													 .show();
												loginActivity.loginSuccessful();
											} else
												Toast.makeText(activity, "Log-in failed", Toast.LENGTH_SHORT)
													 .show();
										}
									});
						}
					})
					.setNegativeButton("Cancel", null)
					.create();
			signUpDialog.show();
		} else {
			AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Already signed in")
					.setMessage("You are already signed in.  Would you like to sign out and sign back in?")
					.setPositiveButton("Sign out", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							signOut();
							showEmailSignIn(loginActivity);
						}
					})
					.setNegativeButton("Cancel", null)
					.create();
			alertDialog.show();
		}
	}

	public void facebookSignIn(LoginResult loginResult) {
		handleFacebookAccessToken(loginResult.getAccessToken());
	}

	private void handleFacebookAccessToken(AccessToken token) {
		final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

		auth.signInWithCredential(credential)
			.addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					Log.d(FirebaseUtility.class.getSimpleName(),
						  "signInWithCredential:onComplete:" + task.isSuccessful());

					if (task.isSuccessful()) {
						Preferences.setCurrentAuthProvider(activity, "Facebook");

						Toast.makeText(activity, "Authentication successful",
									   Toast.LENGTH_SHORT)
							 .show();
						if (TextUtils.isEmpty(Preferences.getAuthId(activity)))
							Preferences.setAuthId(activity, auth.getCurrentUser().getUid());
						else
							auth.getCurrentUser().linkWithCredential(credential);
					}

					if (!task.isSuccessful()) {
						Log.w(FirebaseUtility.class.getSimpleName(), "signInWithCredential", task.getException());
						Toast.makeText(activity, "Authentication failed",
									   Toast.LENGTH_SHORT)
							 .show();
					}
				}
			});
	}

	public void backupDatabase() {
		FirebaseUser user = auth.getCurrentUser();
		if (user != null) {
			final GamesDbHelper dbHelper = new GamesDbHelper(activity);

			FirebaseDatabase database = FirebaseDatabase.getInstance();
			String uid = Preferences.getAuthId(activity);
			if (TextUtils.isEmpty(uid)) {
				uid = user.getUid();
				Preferences.setAuthId(activity, uid);
			}
			final DatabaseReference reference = database.getReference(uid);

			new AsyncTask<String, Void, Void>() {
				ProgressDialog progressDialog;

				@Override
				protected void onPreExecute() {
					progressDialog = new ProgressDialog(activity);
					progressDialog.setMessage("Backing up your data");
					progressDialog.show();
				}

				@Override
				protected Void doInBackground(String... params) {
					Cursor boardCursor = dbHelper.getReadableDatabase()
												 .query(BoardGameContract.GamePlayEntry.TABLE_NAME,
														new String[]{BoardGameContract.GamePlayEntry._ID},
														null,
														null,
														null,
														null,
														null);
					while (boardCursor.moveToNext()) {
						long id = boardCursor.getLong(0);
//						Log.d("ID", id + "");
						reference.child("board")
								 .child(id + "")
								 .setValue(FirebaseGamePlayData.makeFirebaseData(BoardGameDbUtility.getGamePlay(dbHelper,
																												id)));
					}
					boardCursor.close();

					Cursor rpgCursor = dbHelper.getReadableDatabase()
											   .query(RPGContract.GamePlayEntry.TABLE_NAME,
													  new String[]{RPGContract.GamePlayEntry._ID},
													  null,
													  null,
													  null,
													  null,
													  null);
					while (rpgCursor.moveToNext()) {
						long id = rpgCursor.getLong(0);
//						Log.d("ID", id + "");
						reference.child("rpg")
								 .child(id + "")
								 .setValue(FirebaseGamePlayData.makeFirebaseData(RPGDbUtility.getGamePlay(dbHelper,
																										  id)));
					}
					rpgCursor.close();

					Cursor videoCursor = dbHelper.getReadableDatabase()
												 .query(VideoGameContract.GamePlayEntry.TABLE_NAME,
														new String[]{VideoGameContract.GamePlayEntry._ID},
														null,
														null,
														null,
														null,
														null);
					while (videoCursor.moveToNext()) {
						long id = videoCursor.getLong(0);
//						Log.d("ID", id + "");
						reference.child("video")
								 .child(id + "")
								 .setValue(FirebaseGamePlayData.makeFirebaseData(VideoGameDbUtility.getGamePlay(dbHelper,
																												id)));
					}
					videoCursor.close();

					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					progressDialog.cancel();
					dbHelper.close();
				}
			}.execute("");
		}
	}

	public void readFromDatabase() {
		FirebaseUser user = auth.getCurrentUser();
		if (user != null) {
			final GamesDbHelper dbHelper = new GamesDbHelper(activity);

			FirebaseDatabase database = FirebaseDatabase.getInstance();
			String uid = Preferences.getAuthId(activity);
			if (TextUtils.isEmpty(uid)) {
				uid = user.getUid();
				Preferences.setAuthId(activity, uid);
			}
			final DatabaseReference reference = database.getReference(uid);

			new AsyncTask<String, Void, Void>() {
				ProgressDialog progressDialog;

				@Override
				protected void onPreExecute() {
					progressDialog = new ProgressDialog(activity);
					progressDialog.setMessage("Reading your data");
					progressDialog.show();
				}

				@Override
				protected Void doInBackground(String... params) {
					reference.child("board").addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot dataSnapshot) {
							Set<Integer> boardGameIds = new HashSet<>();
							Set<String> customGames = new HashSet<>();

							for (DataSnapshot gamePlaySnapshot : dataSnapshot.getChildren()) {
								FirebaseGamePlayData gamePlayData =
										gamePlaySnapshot.getValue(FirebaseGamePlayData.class);
								BoardGamePlayData boardGamePlayData =
										FirebaseGamePlayDataUtility.makeBoardGamePlayData(gamePlayData);
								if (BoardGameDbUtility.addGamePlayIfNotPresent(dbHelper, boardGamePlayData)) {
									if (gamePlayData.gameId > 0) boardGameIds.add(gamePlayData.gameId);
									else customGames.add(gamePlayData.game);
								}
							}

							for (Integer id : boardGameIds) {
								try {
									progressDialog.setMessage("Adding game id: " + id);
									new DownloadXmlGameTask(Game.GameType.BOARD).execute(
											"https://www.boardgamegeek.com/xmlapi2/thing?id=" + id);
									Thread.sleep(1000);
								} catch (Exception e) {
								}
							}

							for (String game : customGames) {
								try {
									progressDialog.setMessage("Adding custom game: " + game);
									BoardGameDbUtility.addBoardGame(dbHelper, new BoardGame(game, "", -10000));
								} catch (Exception e) {
								}
							}
						}

						@Override
						public void onCancelled(DatabaseError databaseError) {

						}
					});

					reference.child("rpg").addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot dataSnapshot) {
							Set<Integer> rpgIds = new HashSet<>();
							Set<String> customGames = new HashSet<>();

							for (DataSnapshot gamePlaySnapshot : dataSnapshot.getChildren()) {
								FirebaseGamePlayData gamePlayData =
										gamePlaySnapshot.getValue(FirebaseGamePlayData.class);
								RPGPlayData rpgPlayData = FirebaseGamePlayDataUtility.makeRPGPlayData(gamePlayData);
								if (RPGDbUtility.addGamePlayIfNotPresent(dbHelper, rpgPlayData)) {
									if (gamePlayData.gameId > 0) rpgIds.add(gamePlayData.gameId);
									else customGames.add(gamePlayData.game);
								}
							}

							for (Integer id : rpgIds) {
								try {
									progressDialog.setMessage("Adding game id: " + id);
									new DownloadXmlGameTask(Game.GameType.RPG).execute(
											"https://www.boardgamegeek.com/xmlapi2/family?id=" + id);
									Thread.sleep(1000);
								} catch (Exception e) {
								}
							}

							for (String game : customGames) {
								try {
									progressDialog.setMessage("Adding custom game: " + game);
									RPGDbUtility.addRPG(dbHelper, new RolePlayingGame(game, "", -10000));
								} catch (Exception e) {
								}
							}
						}

						@Override
						public void onCancelled(DatabaseError databaseError) {

						}
					});

					reference.child("video").addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot dataSnapshot) {
							Set<Integer> videoGameIds = new HashSet<>();
							Set<String> customGames = new HashSet<>();

							for (DataSnapshot gamePlaySnapshot : dataSnapshot.getChildren()) {
								FirebaseGamePlayData gamePlayData =
										gamePlaySnapshot.getValue(FirebaseGamePlayData.class);
								VideoGamePlayData videoGamePlayData =
										FirebaseGamePlayDataUtility.makeVideoGamePlayData(gamePlayData);
								if (VideoGameDbUtility.addGamePlayIfNotPresent(dbHelper, videoGamePlayData)) {
									if (gamePlayData.gameId > 0) videoGameIds.add(gamePlayData.gameId);
									else customGames.add(gamePlayData.game);
								}
							}

							for (Integer id : videoGameIds) {
								try {
									progressDialog.setMessage("Adding game id: " + id);
									new DownloadXmlGameTask(Game.GameType.VIDEO).execute(
											"https://www.boardgamegeek.com/xmlapi2/thing?id=" + id);
									Thread.sleep(1000);
								} catch (Exception e) {
								}
							}

							for (String game : customGames) {
								try {
									progressDialog.setMessage("Adding custom game: " + game);
									VideoGameDbUtility.addVideoGame(dbHelper, new VideoGame(game, "", -10000));
								} catch (Exception e) {
								}
							}
						}

						@Override
						public void onCancelled(DatabaseError databaseError) {

						}
					});

					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					progressDialog.cancel();
					dbHelper.close();
				}
			}.execute("");
		}
	}

	public String signedInFrom() {
		return Preferences.getCurrentAuthProvider(activity);
	}

	public boolean signedIn() {
		return auth.getCurrentUser() != null;
	}

	public void signOut() {
		auth.signOut();
		Preferences.setCurrentAuthProvider(activity, "");
	}

	public void close() {
		if (authStateListener != null) auth.removeAuthStateListener(authStateListener);
	}

	private class DownloadXmlGameTask extends AsyncTask<String, Void, Game> {
		private Game.GameType gameType;
		private GamesDbHelper dbHelper;

		public DownloadXmlGameTask(Game.GameType gameType) {
			this.gameType = gameType;
			dbHelper = new GamesDbHelper(activity);
		}

		@Override
		protected Game doInBackground(String... urls) {
			if (gameType == Game.GameType.BOARD) {
				try {
					List<BoardGameXmlParser.Item> items =
							UrlUtilities.loadBoardGameXmlFromNetwork(urls[0]);
					return BoardGame.createGame(items.get(0));
				} catch (Exception e) {
					Log.e("PARSER", e.getMessage());
				}
			} else if (gameType == Game.GameType.RPG) {
				InputStream inputStream = null;
				try {
					inputStream = UrlUtilities.downloadUrl(urls[0]);
					List<RPGXmlParser.Item> items = new RPGXmlParser().parse(inputStream);
					return RolePlayingGame.createGame(items.get(0));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						inputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (gameType == Game.GameType.VIDEO) {
				try {
					List<VideoGameXmlParser.Item> items =
							UrlUtilities.loadVideoGameXmlFromNetwork(urls[0]);
					return VideoGame.createGame(items.get(0));
				} catch (Exception e) {
					Log.e("PARSER", e.getMessage());
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Game game) {
			if (game != null) {
				if (gameType == Game.GameType.BOARD) {
					BoardGameDbUtility.addBoardGame(dbHelper, (BoardGame) game);
				} else if (gameType == Game.GameType.RPG) {
					RPGDbUtility.addRPG(dbHelper, (RolePlayingGame) game);
				} else if (gameType == Game.GameType.VIDEO) {
					VideoGameDbUtility.addVideoGame(dbHelper, (VideoGame) game);
				}
				String thumbnailUrl = game.getThumbnailUrl();
				if (game.getThumbnailUrl() != null)
					GameDownloadUtilities.downloadThumbnail("http://" + thumbnailUrl, activity);
				ActivityUtilities.setDatabaseChanged(activity, true);
				dbHelper.close();
			}
		}
	}

	private class DownloadThumbnailTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... url) {
			Bitmap bitmap = null;
			InputStream in = null;
			try {
				URL thumbnailUrl = new URL(url[0]);
				HttpURLConnection connection = (HttpURLConnection) thumbnailUrl.openConnection();
				connection.setReadTimeout(10000);
				connection.setConnectTimeout(15000);
//				connection.setRequestMethod("GET");
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
				}
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			//thumbnail.setImageBitmap(bitmap);
		}
	}

}
