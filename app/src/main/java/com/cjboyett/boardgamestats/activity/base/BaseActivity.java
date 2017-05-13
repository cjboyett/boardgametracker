package com.cjboyett.boardgamestats.activity.base;

import android.support.v7.app.AppCompatActivity;

import com.cjboyett.boardgamestats.utility.Preferences;

public abstract class BaseActivity extends AppCompatActivity {
	protected int backgroundColor, foregroundColor, buttonColor, hintTextColor;

	protected abstract void generateLayout();

	protected abstract void colorComponents();

	protected void setColors() {
		backgroundColor = Preferences.getBackgroundColor(this);
		foregroundColor = Preferences.getForegroundColor(this);
		buttonColor = Preferences.getButtonColor(this);
		hintTextColor = Preferences.getHintTextColor(this);
	}
}
