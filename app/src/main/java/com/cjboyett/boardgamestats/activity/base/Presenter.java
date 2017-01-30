package com.cjboyett.boardgamestats.activity.base;

/**
 * Created by Casey on 1/29/2017.
 */

public interface Presenter<V extends MvpView> {
	void attachView(V view);

	void detachView();
}
