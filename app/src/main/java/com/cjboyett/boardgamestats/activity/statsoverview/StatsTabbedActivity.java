package com.cjboyett.boardgamestats.activity.statsoverview;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ColorUtilities;
import com.cjboyett.boardgamestats.view.animation.ZoomOutPageTransformer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class StatsTabbedActivity extends AppCompatActivity {
	private Activity activity = this;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	private StatsOverviewFragment statsOverviewFragment;
	private GameStatsListFragment gameStatsListFragment;
	private GamePlayCalendarFragment gamePlayCalendarFragment;
	private PlayerStatsListFragment playerStatsListFragment;

	private AdView googleAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = getLayoutInflater().inflate(R.layout.activity_stats_tabbed, null);
		setContentView(view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		view.setBackgroundColor(ColorUtilities.darken(Preferences.getBackgroundColor(this)));

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		statsOverviewFragment = new StatsOverviewFragment();
		gameStatsListFragment = new GameStatsListFragment();
		gamePlayCalendarFragment = new GamePlayCalendarFragment();
		playerStatsListFragment = new PlayerStatsListFragment();

		mViewPager.setCurrentItem(ActivityUtilities.getLastStatsPage(this));
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				ActivityUtilities.setLastStatsPage(activity, position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		if (Preferences.showAds(this)) {
			final RelativeLayout adViewContainer = (RelativeLayout) view.findViewById(R.id.ad_container);

			googleAdView = new AdView(this);
			googleAdView.setAdSize(AdSize.SMART_BANNER);
			googleAdView.setAdUnitId("ca-app-pub-1437859753538305/7571887074");
			googleAdView.setVisibility(View.GONE);
			adViewContainer.addView(googleAdView);
			RelativeLayout.LayoutParams layoutParams =
					new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
													ViewGroup.LayoutParams.WRAP_CONTENT);
			googleAdView.setLayoutParams(layoutParams);
			adViewContainer.setGravity(RelativeLayout.CENTER_IN_PARENT);

			AdRequest adRequest = new AdRequest.Builder()
//			        .addTestDevice("A7AC3AA36B47EF166BA856BD3C6009BF")
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
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (googleAdView != null) googleAdView.resume();
		if (ActivityUtilities.databaseChanged(this)) {
			statsOverviewFragment.setRegenerateLayout(true);
			gameStatsListFragment.setRegenerateLayout(true);
			gamePlayCalendarFragment.setRegenerateLayout(true);
			playerStatsListFragment.setRegenerateLayout(true);
		}
		ActivityUtilities.setDatabaseChanged(this, false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (googleAdView != null) googleAdView.pause();
	}

	@Override
	protected void onDestroy() {
		if (googleAdView != null) googleAdView.destroy();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ActivityUtilities.exitDown(this);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return statsOverviewFragment;
				case 1:
					return gameStatsListFragment;
				case 2:
					return gamePlayCalendarFragment;
				case 3:
					return playerStatsListFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "OVERVIEW";
				case 1:
					return "GAMES";
				case 2:
					return "DATE";
				case 3:
					return "PLAYERS";
			}
			return null;
		}
	}
}
