package com.cjboyett.boardgamestats.conductor.main;

import com.cjboyett.boardgamestats.activity.base.BasePresenter;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.google.firebase.auth.FirebaseAuth;

class MainPresenter extends BasePresenter<MainView> {
	void initializeView() {
		if (Preferences.isFirstVisit(getView().getActivity())) {
			getView().processFirstVisit();
		} else {
			getView().startTicker();
		}
		if (Preferences.needAllPlayerTableUpgrade(getView().getActivity())) {
			getView().processNeedAllPlayerTableUpgrade();
		}

		processWelcomeBackText();
	}

	private void processWelcomeBackText() {
		String welcomeBack = "Welcome Back";
		String username = Preferences.getUsername(getView().getActivity());
		if (username != null && !username.equals("") && !username.equalsIgnoreCase("User"))
			welcomeBack += ", " + username + "!";
		else welcomeBack += "!";

		getView().setWelcomeMessage(welcomeBack);
	}

	void processAddGamePlay() {
		if (!Preferences.isTimerRunning(getView().getActivity())) {
			TempDataManager tempDataManager = TempDataManager.getInstance(getView().getActivity().getApplication());
			tempDataManager.initialize();
		}
		getView().openAddGamePlay();
	}

	void processCollections() {
		getView().openCollections();
	}

	void processStatsOverview() {
		getView().openStatsOverview();
	}

	void processExtras() {
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			getView().openExtras();
		} else {
			getView().openLogin();
		}
	}

	void processSettings() {
		getView().openSettings();
	}

	void processAchievements() {
		getView().openAchievements();
	}

	void processHelp() {
		getView().openHelp();
	}

	@Override
	public void detachView() {
		getView().stopTicker();
		super.detachView();
	}
}
