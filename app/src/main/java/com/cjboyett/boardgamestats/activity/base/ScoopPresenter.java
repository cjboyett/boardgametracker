package com.cjboyett.boardgamestats.activity.base;

public interface ScoopPresenter<V extends MvpScoopView> {
	void attachView(V view);

	void detachView();
}
