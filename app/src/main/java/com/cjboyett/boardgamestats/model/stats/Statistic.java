package com.cjboyett.boardgamestats.model.stats;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by Casey on 4/21/2016.
 */
public abstract class Statistic {
	protected Activity activity;
	private boolean hasMoreStats, hasFewerStats;

	public Statistic(Activity activity) {
		this.activity = activity;
	}

	public abstract String getTitle();

	@NonNull
	public abstract View getView();

	@Nullable
	public abstract void getMoreStats();

	@Nullable
	public abstract void getFewerStats();

	public boolean hasMoreStats() {
		return hasMoreStats;
	}

	public void setHasMoreStats(boolean hasMoreStats) {
		this.hasMoreStats = hasMoreStats;
	}

	public boolean hasFewerStats() {
		return hasFewerStats;
	}

	public void setHasFewerStats(boolean hasFewerStats) {
		this.hasFewerStats = hasFewerStats;
	}
}
