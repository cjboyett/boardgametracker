package com.cjboyett.boardgamestats.activity.base;

public interface Presenter<V extends MvpView> {
	void attachView(V view);

	void detachView();
}
