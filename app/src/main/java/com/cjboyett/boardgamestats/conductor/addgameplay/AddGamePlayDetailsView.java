package com.cjboyett.boardgamestats.conductor.addgameplay;

import com.cjboyett.boardgamestats.activity.base.MvpView;
import com.cjboyett.boardgamestats.model.Date;
import com.cjboyett.boardgamestats.model.Timer;

public interface AddGamePlayDetailsView extends MvpView {
	void setGame(String gameName, String gameType);

	void setData(int timePlayed, String date, String location, String notes);

	void setGamePlayDetails(GamePlayDetails gamePlayDetails);

	void saveGamePlayDetails(String gameName, Timer timer, Date date, String location, String notes);

	void loadGamePlayDetails();
}
