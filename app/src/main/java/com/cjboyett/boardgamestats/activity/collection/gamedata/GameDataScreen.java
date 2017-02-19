package com.cjboyett.boardgamestats.activity.collection.gamedata;

import android.content.Intent;

import com.lyft.scoop.Controller;
import com.lyft.scoop.Screen;

@Controller(GameDataViewImpl.class)
public class GameDataScreen extends Screen {
	private Intent intent;

	public GameDataScreen(Intent intent) {
		this.intent = intent;
	}

	public Intent getIntent() {
		return intent;
	}
}
