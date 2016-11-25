package com.cjboyett.boardgamestats.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.firebase.FirebaseUtility;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

public class ExtrasActivity extends BaseAdActivity
{
	private Activity activity = this;
	private View view;

	private CallbackManager callbackManager;
	private AccessTokenTracker accessTokenTracker;
	private FirebaseUtility firebaseUtility;

	public ExtrasActivity()
	{
		super("ca-app-pub-1437859753538305/8564017075");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_extras, null);
		setContentView(view);

		firebaseUtility = new FirebaseUtility(this);

		generateLayout();
		setColors();
		colorComponents();

		if (Preferences.isFirstExtrasVisit(this))
		{
			final AlertDialog recommendDialog = new ViewUtilities.DialogBuilder(this)
					.setTitle("Extras")
					.setMessage("You can also get board game recommendations based off of the games you have played.  By default the recommendation will be based off of all of the games you have played.  You can also narrow it down by using only specific games for the recommendation.  Weights can be added to these games as well, both positive and negative.")
					.setPositiveButton("Okay", new View.OnClickListener()
					{
						@Override
						public void onClick(View view)
						{
							Preferences.setFirstExtrasVisit(activity, false);
						}
					})
					.create();

			AlertDialog backupDialog = new ViewUtilities.DialogBuilder(this)
					.setTitle("Extras")
					.setMessage("Here you can back up your game play records to an online database.  In a future update the ability to restore your database from the online storage will be added.")
					.setPositiveButton("Okay", new View.OnClickListener()
					{
						@Override
						public void onClick(View view)
						{
							recommendDialog.show();
						}
					})
					.create();

			backupDialog.show();
		}
	}

	@Override
	void generateLayout()
	{
		if (!firebaseUtility.signedInFrom().equals("Email"))
		{
			view.findViewById(R.id.textview_email_logout).setVisibility(View.GONE);
			// TODO Check authentication/reauthenticate
//			Log.d("LOGGED IN FROM", firebaseUtility.signedInFrom());

			callbackManager = CallbackManager.Factory.create();
			LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
			loginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));
			loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
			{
				@Override
				public void onSuccess(LoginResult loginResult)
				{
					firebaseUtility.facebookSignIn(loginResult);
					Log.d("RESULT", loginResult.toString());
					if (loginResult.getAccessToken() == null) Log.d("RESULT", "Token null");
				}

				@Override
				public void onCancel()
				{
					Log.d("CANCEL", "Canceled");
				}

				@Override
				public void onError(FacebookException error)
				{
					Log.d("ERROR", error.toString());
				}
			});

			accessTokenTracker = new AccessTokenTracker()
			{
				@Override
				protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
				{
					if (currentAccessToken == null)
					{
						firebaseUtility.signOut();
						onBackPressed();
					}
				}
			};
			accessTokenTracker.startTracking();
		}

		else
		{
			view.findViewById(R.id.login_button).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.textview_email_logout).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					AlertDialog dialog = new ViewUtilities.DialogBuilder(activity)
							.setTitle("Logout")
							.setMessage("Would you like to log out?")
							.withYancey(true)
							.setPositiveButton("Log Out", new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									firebaseUtility.signOut();
									onBackPressed();
								}
							})
							.setNegativeButton("Cancel", null)
							.create();

					dialog.show();
				}
			});
		}

		view.findViewById(R.id.textview_backup_database).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
//				startActivity(new Intent(activity, ImageGalleryActivity.class));
				AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
						.setTitle("Backup Database")
						.setMessage("Would you like me to back up your database online?")
						.withYancey(true)
						.setPositiveButton("Yes", new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								firebaseUtility.backupDatabase();
							}
						})
						.setNegativeButton("No", null)
						.create();
				alertDialog.show();
			}
		});

		view.findViewById(R.id.textview_board_game_recommendation).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(activity, RecommendationActivity.class));
				ActivityUtilities.exitRight(activity);
			}
		});
	}

	void colorComponents()
	{
		view.setBackgroundColor(backgroundColor);

		if (Preferences.lightUI(this))
		{
			view.findViewById(R.id.textview_backup_database).setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.textview_board_game_recommendation).setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.textview_email_logout).setBackgroundResource(R.drawable.main_button_background_dark);
		}
		else
		{
			view.findViewById(R.id.textview_backup_database).setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.textview_board_game_recommendation).setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.textview_email_logout).setBackgroundResource(R.drawable.main_button_background_light);
		}

		((TextView)view.findViewById(R.id.textview_backup_database)).setTextColor(foregroundColor);
		((TextView)view.findViewById(R.id.textview_board_game_recommendation)).setTextColor(foregroundColor);
		((TextView)view.findViewById(R.id.textview_email_logout)).setTextColor(foregroundColor);

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		firebaseUtility.close();
		try
		{
			accessTokenTracker.stopTracking();
		}
		catch (Exception e) {}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		ActivityUtilities.exitLeft(activity);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		try
		{
			callbackManager.onActivityResult(requestCode, resultCode, data);
		}
		catch (Exception e){}
	}

}
