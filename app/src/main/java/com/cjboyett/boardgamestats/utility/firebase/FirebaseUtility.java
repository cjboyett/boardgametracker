package com.cjboyett.boardgamestats.utility.firebase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cjboyett.boardgamestats.activity.LoginActivity;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameContract;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGContract;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameContract;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.firebase.FirebaseGamePlayData;
import com.cjboyett.boardgamestats.utility.Preferences;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Casey on 8/29/2016.
 */
public class FirebaseUtility
{
	private FirebaseAuth auth;
	private FirebaseAuth.AuthStateListener authStateListener;
	private ChildEventListener usernameChildEventListener;

	private Activity activity;

	public FirebaseUtility(Activity activity)
	{
		this.activity = activity;
		auth = FirebaseAuth.getInstance();
		authStateListener = new FirebaseAuth.AuthStateListener()
		{
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
			{
				FirebaseUser user = auth.getCurrentUser();
				if (user != null) Log.d("USER", user.getEmail() + " " + user.getDisplayName());
				else Log.d("USER", "Does not exist");
			}
		};
		auth.addAuthStateListener(authStateListener);

	}

	public void showEmailSignup()
	{
		View signUpView = activity.getLayoutInflater().inflate(R.layout.dialog_sign_up_form, null);
		final EditText nameEditText, emailEditText;
		nameEditText =(EditText)signUpView.findViewById(R.id.edittext_name);
		emailEditText =(EditText)signUpView.findViewById(R.id.edittext_email);

		int foregroundColor = Preferences.getForegroundColor(activity);
		int hintTextColor = Preferences.getHintTextColor(activity);

		((TextView)signUpView.findViewById(R.id.textview_name)).setTextColor(foregroundColor);
		nameEditText.setTextColor(foregroundColor);
		nameEditText.setHintTextColor(hintTextColor);

		((TextView)signUpView.findViewById(R.id.textview_email)).setTextColor(foregroundColor);
		emailEditText.setTextColor(foregroundColor);
		emailEditText.setHintTextColor(hintTextColor);
		AlertDialog signUpDialog = new ViewUtilities.DialogBuilder(activity)
				.setTitle("Sign up")
				.setView(signUpView)
				.setPositiveButton("Okay", new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						String name = nameEditText.getText().toString();
						String email = emailEditText.getText().toString();
						if (validateEmail(email))
							signUpUser(name, email);
						else
						{
							AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
									.setTitle("Error")
									.setMessage(email + " does not appear to be a valid email address.  Please try again.")
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

	private boolean validateEmail(String email)
	{
		return email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-za-z]{2,})$");
	}

	private void signUpUser(final String name, final String email)
	{
		auth.createUserWithEmailAndPassword(email, createTempPassword())
		    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>()
		    {
			    @Override
			    public void onComplete(@NonNull Task<AuthResult> task)
			    {
				    if (task.isSuccessful())
				    {
					    FirebaseUser user = auth.getCurrentUser();
					    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build());
					    auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>()
					    {
						    @Override
						    public void onComplete(@NonNull Task<Void> task)
						    {
							    if (task.isSuccessful())
							    {
								    auth.signOut();
								    AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
										    .setTitle("Reset password")
										    .setMessage("I have sent an email to " + email + " to reset your password.  You will need to check your email and follow the link provided to reset your password before you can sign in.")
										    .withYancey(true)
										    .setPositiveButton("Okay", null)
										    .create();
								    alertDialog.show();

							    }
								else
							    {
								    auth.getCurrentUser().delete();
								    AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
										    .setTitle("Error")
										    .setMessage("I could not send an email to " + email + ".  I need to send a link to reset your password before you can sign in.  You will have to try again.")
										    .withYancey(true)
										    .setPositiveButton("Okay", null)
										    .create();
								    alertDialog.show();
							    }
						    }
					    });
				    }
				    else
				    {
					    AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
							    .setTitle("Error")
							    .setMessage("I could not create an account for " + email + ".  Perhaps there is already any an account for that email.")
							    .withYancey(true)
							    .setPositiveButton("Okay", null)
							    .create();
					    alertDialog.show();
				    }
			    }
		    });
	}

	private String createTempPassword()
	{
		SecureRandom r = new SecureRandom();
		return new BigInteger(130, r).toString(32);
	}

	public void showEmailSignIn(final LoginActivity loginActivity)
	{
		if (!signedIn())
		{
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
					.setPositiveButton("Okay", new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							final String email = emailEditText.getText()
							                                  .toString();
							final String password = passwordEditText.getText()
							                                        .toString();
							if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
								auth.signInWithEmailAndPassword(email, password)
								    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>()
								    {
									    @Override
									    public void onComplete(@NonNull Task<AuthResult> task)
									    {
										    if (task.isSuccessful())
										    {
											    if (TextUtils.isEmpty(Preferences.getAuthId(activity)))
												    Preferences.setAuthId(activity, auth.getCurrentUser().getUid());
											    else
												    auth.getCurrentUser().linkWithCredential(EmailAuthProvider.getCredential(email, password));

											    Preferences.setCurrentAuthProvider(activity, "Email");

											    Toast.makeText(activity, "Log-in successful", Toast.LENGTH_SHORT)
											         .show();
											    loginActivity.loginSuccessful();
										    }
										    else
											    Toast.makeText(activity, "Log-in failed", Toast.LENGTH_SHORT)
											         .show();
									    }
								    });
						}
					})
					.setNegativeButton("Cancel", null)
					.create();
			signUpDialog.show();
		}
		else
		{
			AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Already signed in")
					.setMessage("You are already signed in.  Would you like to sign out and sign back in?")
					.setPositiveButton("Sign out", new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							signOut();
							showEmailSignIn(loginActivity);
						}
					})
					.setNegativeButton("Cancel", null)
					.create();
			alertDialog.show();
		}
	}

	public void facebookSignIn(LoginResult loginResult)
	{
		handleFacebookAccessToken(loginResult.getAccessToken());
	}

	private void handleFacebookAccessToken(AccessToken token)
	{
		final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

		auth.signInWithCredential(credential)
		    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>()
		    {
			    @Override
			    public void onComplete(@NonNull Task<AuthResult> task)
			    {
				    Log.d(FirebaseUtility.class.getSimpleName(), "signInWithCredential:onComplete:" + task.isSuccessful());

				    if (task.isSuccessful())
				    {
					    Preferences.setCurrentAuthProvider(activity, "Facebook");

					    Toast.makeText(activity, "Authentication successful",
					                   Toast.LENGTH_SHORT)
					         .show();
					    if (TextUtils.isEmpty(Preferences.getAuthId(activity)))
						    Preferences.setAuthId(activity, auth.getCurrentUser().getUid());
					    else
						    auth.getCurrentUser().linkWithCredential(credential);
				    }

				    if (!task.isSuccessful())
				    {
					    Log.w(FirebaseUtility.class.getSimpleName(), "signInWithCredential", task.getException());
					    Toast.makeText(activity, "Authentication failed",
					                   Toast.LENGTH_SHORT)
					         .show();
				    }
			    }
		    });
	}

	public void backupDatabase()
	{
		FirebaseUser user = auth.getCurrentUser();
		if (user != null)
		{
			final GamesDbHelper dbHelper = new GamesDbHelper(activity);

			FirebaseDatabase database = FirebaseDatabase.getInstance();
			String uid = Preferences.getAuthId(activity);
			if (TextUtils.isEmpty(uid))
			{
				uid = user.getUid();
				Preferences.setAuthId(activity, uid);
			}
			final DatabaseReference reference = database.getReference(uid);

			new AsyncTask<String, Void, Void>()
			{
				ProgressDialog progressDialog;

				@Override
				protected void onPreExecute()
				{
					progressDialog = new ProgressDialog(activity);
					progressDialog.setMessage("Backing up your data");
					progressDialog.show();
				}

				@Override
				protected Void doInBackground(String... params)
				{
					Cursor boardCursor = dbHelper.getReadableDatabase()
					                             .query(BoardGameContract.GamePlayEntry.TABLE_NAME,
					                                    new String[]{BoardGameContract.GamePlayEntry._ID},
					                                    null,
					                                    null,
					                                    null,
					                                    null,
					                                    null);
					while (boardCursor.moveToNext())
					{
						long id = boardCursor.getLong(0);
//						Log.d("ID", id + "");
						reference.child("board")
						         .child(id + "")
						         .setValue(FirebaseGamePlayData.makeFirebaseData(BoardGameDbUtility.getGamePlay(dbHelper, id)));
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
					while (rpgCursor.moveToNext())
					{
						long id = rpgCursor.getLong(0);
//						Log.d("ID", id + "");
						reference.child("rpg")
						         .child(id + "")
						         .setValue(FirebaseGamePlayData.makeFirebaseData(RPGDbUtility.getGamePlay(dbHelper, id)));
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
					while (videoCursor.moveToNext())
					{
						long id = videoCursor.getLong(0);
//						Log.d("ID", id + "");
						reference.child("video")
						         .child(id + "")
						         .setValue(FirebaseGamePlayData.makeFirebaseData(VideoGameDbUtility.getGamePlay(dbHelper, id)));
					}
					videoCursor.close();

					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid)
				{
					progressDialog.cancel();
					dbHelper.close();
				}
			}.execute("");
		}
	}

	public String signedInFrom()
	{
		return Preferences.getCurrentAuthProvider(activity);
	}

	public boolean signedIn()
	{
		return auth.getCurrentUser() != null;
	}

	public void signOut()
	{
		auth.signOut();
		Preferences.setCurrentAuthProvider(activity, "");
	}

	public void close()
	{
		if (authStateListener != null) auth.removeAuthStateListener(authStateListener);
		if (usernameChildEventListener != null) FirebaseDatabase.getInstance().getReference("users").removeEventListener(usernameChildEventListener);
	}
}
