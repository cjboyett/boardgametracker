package com.cjboyett.boardgamestats.view.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Casey on 4/27/2016.
 * Borrowed from 2cupsoftech.wordpress.com
 */
public class FlipAnimation extends Animation {
	private Camera camera;

	private View fromView, toView;

	private float centerX, centerY;

	private boolean forward = true;

	private long DURATION = 700;

	public FlipAnimation(View fromView, View toView) {
		this.fromView = fromView;
		this.toView = toView;

		setDuration(DURATION);
		setFillAfter(false);
		setInterpolator(new AccelerateDecelerateInterpolator());
	}

	public void reverse() {
		forward = false;
		View switchView = toView;
		toView = fromView;
		fromView = switchView;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		centerX = width / 2;
		centerY = height / 2;
		camera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		Log.d("FLIP", "Animating");
		final double radians = Math.PI * interpolatedTime;
		float degrees = (float) (180.0 * radians / Math.PI);

		if (interpolatedTime >= 0.5f) {
			degrees -= 180f;
			fromView.setVisibility(View.GONE);
			toView.setVisibility(View.VISIBLE);
		}

		if (forward)
			degrees = -degrees;

		final Matrix matrix = t.getMatrix();
		camera.save();
		camera.rotateY(degrees);
		camera.getMatrix(matrix);
		camera.restore();
		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
	}
}
