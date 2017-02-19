package com.cjboyett.boardgamestats.activity.base;

import java.lang.ref.WeakReference;

public class BaseScoopPresenter<T extends MvpScoopView> implements ScoopPresenter<T> {
	private WeakReference<T> view;

	@Override
	public void attachView(T view) {
		this.view = new WeakReference<T>(view);
	}

	@Override
	public void detachView() {
		view = null;
	}

	public boolean isViewAttached() {
		return view.get() != null;
	}

	public T getView() {
		return view.get();
	}

	public void checkViewAttached() {
		if (!isViewAttached()) {
			throw new MvpViewAttachedException();
		}
	}

	public static class MvpViewAttachedException extends RuntimeException {
		public MvpViewAttachedException() {
			super("Please call Presenter.attachView(MvpView) before requesting data to the Presenter");
		}
	}
}
