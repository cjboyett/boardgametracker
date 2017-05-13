package com.cjboyett.boardgamestats.conductor.extras;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.conductor.ConductorActivity;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.conductor.changehandlers.DirectionalChangeHandler;
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

import timber.log.Timber;

public class ExtrasController extends BaseController {
	private Activity activity;
	private View view;

	private CallbackManager callbackManager;
	private AccessTokenTracker accessTokenTracker;
	private FirebaseUtility firebaseUtility;

	private GestureDetectorCompat gestureDetector;

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.content_extras, container, false);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		activity = getActivity();
		firebaseUtility = new FirebaseUtility(activity);
		gestureDetector = new GestureDetectorCompat(activity, new ScrollGestureListener());
		((ConductorActivity) getActivity()).setGestureDetector(gestureDetector);

		generateLayout();
		setColors();
		colorComponents();

		if (Preferences.isFirstExtrasVisit(activity)) {
			final AlertDialog recommendDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Extras")
					.setMessage(
							"You can also get board game recommendations based off of the games you have played.  By default the recommendation will be based off of all of the games you have played.  You can also narrow it down by using only specific games for the recommendation.  Weights can be added to these games as well, both positive and negative.")
					.setPositiveButton("Okay", new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							Preferences.setFirstExtrasVisit(activity, false);
						}
					})
					.create();

			AlertDialog backupDialog = new ViewUtilities.DialogBuilder(activity)
					.setTitle("Extras")
					.setMessage(
							"Here you can back up your game play records to an online database or restore your records from the online database.")
					.setPositiveButton("Okay", new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							recommendDialog.show();
						}
					})
					.create();

			backupDialog.show();
		}
	}

	@Override
	protected void onDetach(@NonNull View view) {
		((ConductorActivity) getActivity()).removeGestureDetector();
		firebaseUtility.close();
		try {
			accessTokenTracker.stopTracking();
		} catch (Exception e) {
		}
		super.onDetach(view);
	}

	protected void generateLayout() {
		if (!firebaseUtility.signedInFrom()
							.equals("Email")) {
			view.findViewById(R.id.textview_email_logout)
				.setVisibility(View.GONE);
			// TODO Check authentication/reauthenticate
//			Timber.d(firebaseUtility.signedInFrom());

			callbackManager = CallbackManager.Factory.create();
			LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
			loginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));
			loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(LoginResult loginResult) {
					firebaseUtility.facebookSignIn(loginResult);
					Timber.d(loginResult.toString());
					if (loginResult.getAccessToken() == null) Timber.d("Token null");
				}

				@Override
				public void onCancel() {
					Timber.d("Canceled");
				}

				@Override
				public void onError(FacebookException error) {
					Timber.d(error.toString());
				}
			});

			accessTokenTracker = new AccessTokenTracker() {
				@Override
				protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
					if (currentAccessToken == null) {
						firebaseUtility.signOut();
						handleBack();
					}
				}
			};
			accessTokenTracker.startTracking();
		} else {
			view.findViewById(R.id.login_button)
				.setVisibility(View.INVISIBLE);
			view.findViewById(R.id.textview_email_logout)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						AlertDialog dialog = new ViewUtilities.DialogBuilder(activity)
								.setTitle("Logout")
								.setMessage("Would you like to log out?")
								.withYancey(true)
								.setPositiveButton("Log Out", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										firebaseUtility.signOut();
										handleBack();
									}
								})
								.setNegativeButton("Cancel", null)
								.create();

						dialog.show();
					}
				});
		}

		view.findViewById(R.id.textview_backup_database)
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
							.setTitle("Backup Database")
							.setMessage("Would you like me to back up your database online?")
							.withYancey(true)
							.setPositiveButton("Yes", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									firebaseUtility.backupDatabase();
								}
							})
							.setNegativeButton("No", null)
							.create();
					alertDialog.show();
				}
			});

		view.findViewById(R.id.textview_restore_database)
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog alertDialog = new ViewUtilities.DialogBuilder(activity)
							.setTitle("Restore Database")
							.setMessage("Would you like me to restore your database from the online backup?")
							.withYancey(true)
							.setPositiveButton("Yes", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									firebaseUtility.readFromDatabase();
								}
							})
							.setNegativeButton("No", null)
							.create();
					alertDialog.show();
				}
			});

		view.findViewById(R.id.textview_board_game_recommendation)
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getRouter().pushController(RouterTransaction.with(new RecommendationController())
																.pushChangeHandler(DirectionalChangeHandler.from(
																		DirectionalChangeHandler.LEFT))
																.popChangeHandler(DirectionalChangeHandler.from(
																		DirectionalChangeHandler.LEFT)));
				}
			});
	}

	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);

/*
		if (Preferences.lightUI(this)) {
			view.findViewById(R.id.textview_backup_database)
				.setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.textview_restore_database)
				.setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.textview_board_game_recommendation)
				.setBackgroundResource(R.drawable.main_button_background_dark);
			view.findViewById(R.id.textview_email_logout)
				.setBackgroundResource(R.drawable.main_button_background_dark);
		} else {
			view.findViewById(R.id.textview_backup_database)
				.setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.textview_restore_database)
				.setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.textview_board_game_recommendation)
				.setBackgroundResource(R.drawable.main_button_background_light);
			view.findViewById(R.id.textview_email_logout)
				.setBackgroundResource(R.drawable.main_button_background_light);
		}
*/
		ViewUtilities.tintButtonBackground((AppCompatButton) view.findViewById(R.id.textview_backup_database),
										   buttonColor);
		ViewUtilities.tintButtonBackground((AppCompatButton) view.findViewById(R.id.textview_restore_database),
										   buttonColor);
		ViewUtilities.tintButtonBackground((AppCompatButton) view.findViewById(R.id.textview_board_game_recommendation),
										   buttonColor);
		ViewUtilities.tintButtonBackground((AppCompatButton) view.findViewById(R.id.textview_email_logout),
										   buttonColor);


		((TextView) view.findViewById(R.id.textview_backup_database)).setTextColor(foregroundColor);
		((TextView) view.findViewById(R.id.textview_restore_database)).setTextColor(foregroundColor);
		((TextView) view.findViewById(R.id.textview_board_game_recommendation)).setTextColor(foregroundColor);
		((TextView) view.findViewById(R.id.textview_email_logout)).setTextColor(foregroundColor);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			callbackManager.onActivityResult(requestCode, resultCode, data);
		} catch (Exception e) {
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
					} else if (velocityX > 2000) {
						getRouter().pushController(RouterTransaction.with(new RecommendationController())
																	.pushChangeHandler(DirectionalChangeHandler.from(
																			DirectionalChangeHandler.LEFT))
																	.popChangeHandler(DirectionalChangeHandler.from(
																			DirectionalChangeHandler.LEFT)));
						return true;
					}
				}
			}
			return false;
		}
	}
}
