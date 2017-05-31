package com.cjboyett.boardgamestats.conductor.extras;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.conductor.changehandlers.DirectionalChangeHandler;
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

public class LoginController extends BaseController {
	private Activity activity;
	private View view;

	private CallbackManager callbackManager;
	private AccessTokenTracker accessTokenTracker;
	private FirebaseUtility firebaseUtility;

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.content_login, container, false);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		activity = getActivity();
		firebaseUtility = new FirebaseUtility(activity);

		getToolbar().setTitle("Login");
		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	protected void onDetach(@NonNull View view) {
		firebaseUtility.close();
		accessTokenTracker.stopTracking();
		super.onDetach(view);
	}

	protected void generateLayout() {
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
				if (currentAccessToken != null) {
					Timber.d(currentAccessToken.getExpires().toString());
					loginSuccessful();
				}
			}
		};
		accessTokenTracker.startTracking();

		view.findViewById(R.id.textview_sign_up_email).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				firebaseUtility.showEmailSignup();
			}
		});

		view.findViewById(R.id.textview_sign_in_email).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				firebaseUtility.showEmailSignIn(LoginController.this);
			}
		});
	}

	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);

		ViewUtilities.tintButtonBackground((AppCompatButton) view.findViewById(R.id.textview_sign_in_email),
										   buttonColor);
		ViewUtilities.tintButtonBackground((AppCompatButton) view.findViewById(R.id.textview_sign_up_email),
										   buttonColor);

//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.textview_sign_in_email), foregroundColor);
		((TextView) view.findViewById(R.id.textview_sign_in_email)).setTextColor(foregroundColor);

//		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.textview_sign_up_email), foregroundColor);
		((TextView) view.findViewById(R.id.textview_sign_up_email)).setTextColor(foregroundColor);
	}

	public void loginSuccessful() {
		getRouter().replaceTopController(RouterTransaction.with(new ExtrasController())
														  .pushChangeHandler(DirectionalChangeHandler.from(
																  DirectionalChangeHandler.LEFT))
														  .popChangeHandler(DirectionalChangeHandler.from(
																  DirectionalChangeHandler.LEFT)));
	}

/*
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Preferences.useSwipes(this)) gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
*/

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}
}
