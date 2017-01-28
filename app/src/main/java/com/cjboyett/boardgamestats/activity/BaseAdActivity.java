package com.cjboyett.boardgamestats.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by Casey on 10/15/2016.
 */
abstract class BaseAdActivity extends BaseActivity {
	private String googleAdUnitId;
	private AdView googleAdView;

	public BaseAdActivity(String googleAdUnitId) {
		super();
		this.googleAdUnitId = googleAdUnitId;
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (Preferences.showAds(this)) {
			try {
				final RelativeLayout adViewContainer = (RelativeLayout) findViewById(R.id.ad_container);

				googleAdView = new AdView(this);
				googleAdView.setAdSize(AdSize.SMART_BANNER);
				googleAdView.setAdUnitId(googleAdUnitId);
				googleAdView.setVisibility(View.GONE);
				adViewContainer.addView(googleAdView);
				RelativeLayout.LayoutParams layoutParams =
						new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
														ViewGroup.LayoutParams.WRAP_CONTENT);
				googleAdView.setLayoutParams(layoutParams);
				adViewContainer.setGravity(RelativeLayout.CENTER_IN_PARENT);

				AdRequest adRequest = new AdRequest.Builder()
//					.addTestDevice("EFC2D62A72499E15BA2294EEB7737A12")
.build();
				googleAdView.setAdListener(new AdListener() {
					@Override
					public void onAdLoaded() {
						super.onAdLoaded();
						googleAdView.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAdFailedToLoad(int errorCode) {
						super.onAdFailedToLoad(errorCode);
						googleAdView.setVisibility(View.GONE);
					}
				});
				googleAdView.loadAd(adRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (googleAdView != null) googleAdView.resume();
	}

	@Override
	protected void onPause() {
		if (googleAdView != null) googleAdView.pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (googleAdView != null) googleAdView.destroy();
		super.onDestroy();
	}
}
