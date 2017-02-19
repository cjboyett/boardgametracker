package com.cjboyett.boardgamestats.activity.addgame;

import android.content.Intent;

import com.lyft.scoop.Controller;
import com.lyft.scoop.Screen;

@Controller(AddGameViewImpl.class)
public class AddGameScreen extends Screen {
	private Intent intent;

	public AddGameScreen(Intent intent) {
		this.intent = intent;
	}

	public Intent getIntent() {
		return intent;
	}
}
