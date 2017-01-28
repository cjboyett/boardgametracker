package com.cjboyett.boardgamestats.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
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

public class LoginActivity extends BaseAdActivity {
	private Activity activity = this;
	private View view;

	private CallbackManager callbackManager;
	private AccessTokenTracker accessTokenTracker;
	private FirebaseUtility firebaseUtility;

	private GestureDetectorCompat gestureDetector;

	public LoginActivity() {
		super("");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_login, null);
		setContentView(view);

		firebaseUtility = new FirebaseUtility(this);
		gestureDetector = new GestureDetectorCompat(this, new ScrollGestureListener());

		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	void generateLayout() {
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

		accessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
				if (currentAccessToken != null) {
//					firebaseUtility.signOut();
					Log.d("ACCESS TOKEN", currentAccessToken.getExpires().toString());
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
				firebaseUtility.showEmailSignIn((LoginActivity) activity);
			}
		});
	}

	void colorComponents() {
		view.setBackgroundColor(backgroundColor);

		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.textview_sign_in_email), foregroundColor);
		((TextView) view.findViewById(R.id.textview_sign_in_email)).setTextColor(foregroundColor);

		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.textview_sign_up_email), foregroundColor);
		((TextView) view.findViewById(R.id.textview_sign_up_email)).setTextColor(foregroundColor);
	}

	public void loginSuccessful() {
		finish();
		startActivity(new Intent(this, ExtrasActivity.class));
		ActivityUtilities.exitRight(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		firebaseUtility.close();
		accessTokenTracker.stopTracking();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Preferences.useSwipes(this)) gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ActivityUtilities.exitLeft(activity);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		callbackManager.onActivityResult(requestCode, resultCode, data);
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
						onBackPressed();
						return true;
					}
				}
			}
			return false;
		}
	}

}
