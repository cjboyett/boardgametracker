package com.cjboyett.boardgamestats.activity.main;

import com.cjboyett.boardgamestats.activity.base.BaseScoopPresenter;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Casey on 1/29/2017.
 */

public class MainPresenter extends BaseScoopPresenter<MainView> {
	public void initializeView() {
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

	public void processWelcomeBackText() {
		String welcomeBack = "Welcome Back";
		String username = Preferences.getUsername(getView().getActivity());
		if (username != null && !username.equals("") && !username.equalsIgnoreCase("User"))
			welcomeBack += ", " + username + "!";
		else welcomeBack += "!";

		getView().setWelcomeMessage(welcomeBack);
	}

	public void processAddGamePlay() {
		if (!Preferences.isTimerRunning(getView().getActivity())) {
			TempDataManager tempDataManager = TempDataManager.getInstance(getView().getActivity().getApplication());
			tempDataManager.initialize();
		}
		getView().openAddGamePlay();
	}

	public void processCollections() {
		getView().openCollections();
	}

	public void processStatsOverview() {
		getView().openStatsOverview();
	}

	public void processExtras() {
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			getView().openExtras();
		} else {
			getView().openLogin();
		}
	}

	public void processSettings() {
		getView().openSettings();
	}

	public void processAchievements() {
		getView().openAchievements();
	}

	public void processHelp() {
		getView().openHelp();
	}

	@Override
	public void detachView() {
		getView().stopTicker();
		super.detachView();
	}
}
