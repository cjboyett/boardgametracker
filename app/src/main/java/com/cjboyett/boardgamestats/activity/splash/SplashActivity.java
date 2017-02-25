package com.cjboyett.boardgamestats.activity.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.cjboyett.boardgamestats.conductor.ConductorActivity;

public class SplashActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SystemClock.sleep(1000);
		Intent intent = new Intent(this, ConductorActivity.class);
		startActivity(intent);
		finish();
	}
}
