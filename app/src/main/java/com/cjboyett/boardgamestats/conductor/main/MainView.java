package com.cjboyett.boardgamestats.conductor.main;

import com.cjboyett.boardgamestats.activity.base.MvpView;

interface MainView extends MvpView {
	void setWelcomeMessage(String welcomeMessage);

	void processFirstVisit();

	void processNeedAllPlayerTableUpgrade();

	void startTicker();

	void pauseTicker();

	void stopTicker();

	void openCollections();

	void openAddGamePlay();

	void openLogin();

	void openExtras();

	void openStatsOverview();

	void openSettings();

	void openAchievements();

	void openHelp();
}
