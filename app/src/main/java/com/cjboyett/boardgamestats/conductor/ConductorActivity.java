package com.cjboyett.boardgamestats.conductor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.base.BaseActivity;
import com.cjboyett.boardgamestats.conductor.main.MainController;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ConductorActivity extends BaseActivity {
	@BindView(R.id.main_container)
	ViewGroup mainContainer;

	@BindView(R.id.dialog_container)
	ViewGroup dialogContainer;

	@BindView(R.id.ad_container)
	RelativeLayout adContainer;

	@BindView(R.id.app_bar_layout)
	AppBarLayout appBarLayout;

	@BindView(R.id.collapsing_toolbar)
	CollapsingToolbarLayout collapsingToolbarLayout;

	@BindView(R.id.toolbar)
	Toolbar toolbar;

	@BindView(R.id.toolbar_image)
	ImageView toolbarImage;

	@BindView(R.id.drawer_layout)
	DrawerLayout drawerLayout;

	@BindView(R.id.main_navigation)
	NavigationView navigationView;

	private Router router;
	private String googleAdUnitId;
	private AdView googleAdView;

	private MyNavigationDrawer myNavigationDrawer;

	private GestureDetectorCompat gestureDetector;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.root);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		appBarLayout.setExpanded(false, false);

		googleAdUnitId = "ca-app-pub-1437859753538305/9571180678";

		router = Conductor.attachRouter(this, mainContainer, savedInstanceState);
		if (!router.hasRootController()) {
			router.setRoot(RouterTransaction.with(new MainController()).tag(MainController.TAG));
		}

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		myNavigationDrawer = new MyNavigationDrawer(router, drawerLayout, navigationView);

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
						//.addTestDevice("EFC2D62A72499E15BA2294EEB7737A12")
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
				Timber.e(e);
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

	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerOpen(navigationView)) {
			drawerLayout.closeDrawers();
		} else if (!router.handleBack()) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			drawerLayout.openDrawer(navigationView);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void generateLayout() {
	}

	@Override
	protected void colorComponents() {
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Preferences.useSwipes(this) && gestureDetector != null) {
			return gestureDetector.onTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}

	public AppBarLayout getAppBarLayout() {
		return appBarLayout;
	}

	public CollapsingToolbarLayout getCollapsingToolbarLayout() {
		return collapsingToolbarLayout;
	}

	public Toolbar getToolbar() {
		return toolbar;
	}

	public ImageView getToolbarImage() {
		return toolbarImage;
	}

	public MyNavigationDrawer getMyNavigationDrawer() {
		return myNavigationDrawer;
	}
}
