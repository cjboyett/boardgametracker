package com.cjboyett.boardgamestats.activity.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Casey on 5/4/2016.
 */
public class ClearStackMainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(new Intent(this, MainActivity.class).setFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
}
