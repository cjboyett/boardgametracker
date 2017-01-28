package com.cjboyett.boardgamestats.activity;

import android.support.v7.app.AppCompatActivity;

import com.cjboyett.boardgamestats.utility.Preferences;

/**
 * Created by Casey on 10/15/2016.
 */
abstract class BaseActivity extends AppCompatActivity {
	protected int backgroundColor, foregroundColor, hintTextColor;

	abstract void generateLayout();

	abstract void colorComponents();

	protected void setColors() {
		backgroundColor = Preferences.getBackgroundColor(this);
		foregroundColor = Preferences.getForegroundColor(this);
		hintTextColor = Preferences.getHintTextColor(this);
	}
}
