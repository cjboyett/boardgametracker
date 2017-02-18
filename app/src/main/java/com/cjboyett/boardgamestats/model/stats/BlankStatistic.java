package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.cjboyett.boardgamestats.view.BlankView;

/**
 * Created by Casey on 4/22/2016.
 */
public class BlankStatistic extends Statistic {
	public BlankStatistic(Activity activity) {
		super(activity);
	}

	@Override
	public String getTitle() {
		return "";
	}

	@NonNull
	@Override
	public View getView() {
		View view = new BlankView(activity);
		return view;
	}

	@Nullable
	@Override
	public void getMoreStats() {

	}

	@Nullable
	@Override
	public void getFewerStats() {

	}
}
