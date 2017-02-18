package com.cjboyett.boardgamestats.activity.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.cjboyett.boardgamestats.activity.main.MainActivity;

public class SplashActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SystemClock.sleep(1000);
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
//		overridePendingTransition(R.anim.main_enter, R.anim.splash_exit);
	}
}
