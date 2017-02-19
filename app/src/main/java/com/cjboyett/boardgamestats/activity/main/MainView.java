package com.cjboyett.boardgamestats.activity.main;

import com.cjboyett.boardgamestats.activity.base.MvpScoopView;

public interface MainView extends MvpScoopView {
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
