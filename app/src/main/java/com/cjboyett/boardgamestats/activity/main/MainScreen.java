package com.cjboyett.boardgamestats.activity.main;

import com.lyft.scoop.Controller;
import com.lyft.scoop.Screen;

import timber.log.Timber;

@Controller(MainViewImpl.class)
public class MainScreen extends Screen {
	public MainScreen() {
		Timber.d("MainScreen");
	}
}
