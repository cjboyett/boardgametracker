package com.cjboyett.boardgamestats.conductor.collection;

import android.graphics.Bitmap;

import com.cjboyett.boardgamestats.activity.base.MvpView;
import com.cjboyett.boardgamestats.model.games.board.SimpleGame;

interface GameDataView extends MvpView {
	void populateView(SimpleGame simpleGame);

	void populateThumbnail(Bitmap thumbnail);

	void populateToolbarImage(Bitmap image);
}
