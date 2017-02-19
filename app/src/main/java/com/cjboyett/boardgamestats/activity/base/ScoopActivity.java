package com.cjboyett.boardgamestats.activity.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.main.MainScreen;
import com.cjboyett.boardgamestats.scoop.AppRouter;
import com.cjboyett.boardgamestats.scoop.DialogRouter;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.view.scoop.DialogUiContainer;
import com.cjboyett.boardgamestats.view.scoop.MainUiContainer;
import com.lyft.scoop.Scoop;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ScoopActivity extends AppCompatActivity {
	private static final String ACTIVITY_SCOOP = "activity_scoop";
	public static final String ACTIVITY_SERVICE = "activity_service";

	@BindView(R.id.screen_container)
	protected MainUiContainer mainUiContainer;

	@BindView(R.id.dialog_container)
	protected DialogUiContainer dialogUiContainer;

	private AppRouter appRouter;
	private DialogRouter dialogRouter;
	private Scoop activityScoop;

	private GestureDetectorCompat gestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoop);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);


		appRouter = new AppRouter();
		dialogRouter = new DialogRouter();

		getScoop().inflate(R.layout.root, (ViewGroup) findViewById(R.id.root), true);
		ButterKnife.bind(this);

		appRouter.setUiContainer(mainUiContainer);
		dialogRouter.setUiContainer(dialogUiContainer);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Timber.d("onResume");
		if (!appRouter.hasActiveScreen()) {
			appRouter.goTo(new MainScreen());
		}
	}

	@Override
	protected void onDestroy() {
		getScoop().destroy();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (dialogUiContainer.onBack() || mainUiContainer.onBack() || dialogRouter.dismiss() || appRouter.goBack())
			return;
		super.onBackPressed();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector != null && Preferences.useSwipes(this)) gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	public void setGestureDetector(GestureDetectorCompat gestureDetector) {
		this.gestureDetector = gestureDetector;
	}

	public void destoryGestureDetector() {
		gestureDetector = null;
	}

	private Scoop getScoop() {
		if (activityScoop == null) {
			activityScoop = new Scoop.Builder(ACTIVITY_SCOOP).service(ACTIVITY_SERVICE, this)
															 .service(AppRouter.SERVICE_NAME, appRouter)
															 .service(DialogRouter.SERVICE_NAME, dialogRouter)
															 .build();
		}
		return activityScoop;
	}
}
