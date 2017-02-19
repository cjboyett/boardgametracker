package com.cjboyett.boardgamestats.activity.base;

import com.cjboyett.boardgamestats.scoop.AppRouter;
import com.cjboyett.boardgamestats.scoop.DialogRouter;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.lyft.scoop.ViewController;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseViewController extends ViewController {
	protected int backgroundColor, foregroundColor, hintTextColor;
	protected AppRouter appRouter;
	protected DialogRouter dialogRouter;

	private Unbinder unbinder;

	protected abstract void colorComponents();

	@Override
	public void onAttach() {
		unbinder = ButterKnife.bind(this, getView());

		appRouter = getScoop().findService(AppRouter.SERVICE_NAME);
		dialogRouter = getScoop().findService(DialogRouter.SERVICE_NAME);
	}

	@Override
	public void onDetach() {
		if (unbinder != null) {
			unbinder.unbind();
		}
		unbinder = null;

		getActivity().destoryGestureDetector();

		appRouter = null;
		dialogRouter = null;
	}

	ScoopActivity getActivity() {
		return getScoop().findService(ScoopActivity.ACTIVITY_SERVICE);
	}

	protected void setColors() {
		backgroundColor = Preferences.getBackgroundColor(getActivity());
		foregroundColor = Preferences.getForegroundColor(getActivity());
		hintTextColor = Preferences.getHintTextColor(getActivity());
	}
}
