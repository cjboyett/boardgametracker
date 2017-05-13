package com.cjboyett.boardgamestats.conductor.changehandlers;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler;

public class LeftRightChangeHandler extends AnimatorChangeHandler {
	private boolean fromRight;
	public LeftRightChangeHandler() {
	}

	public LeftRightChangeHandler(boolean removesFromViewOnPush) {
		super(removesFromViewOnPush);
	}

	public LeftRightChangeHandler(long duration) {
		super(duration);
	}

	public LeftRightChangeHandler(long duration, boolean removesFromViewOnPush) {
		super(duration, removesFromViewOnPush);
	}

	public LeftRightChangeHandler(boolean fromRight, long duration) {
		super(duration);
		this.fromRight = fromRight;
	}

	public LeftRightChangeHandler(boolean fromRight, long duration, boolean removesFromViewOnPush) {
		super(duration, removesFromViewOnPush);
		this.fromRight = fromRight;
	}

	@Override
	@NonNull
	protected Animator getAnimator(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush,
								   boolean toAddedToContainer) {
		AnimatorSet animatorSet = new AnimatorSet();
		int multiplier = fromRight ? 1 : -1;

		if (isPush) {
			if (from != null) {
				animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, -from.getWidth() * multiplier));
			}
			if (to != null) {
				animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, to.getWidth() * multiplier, 0));
			}
		} else {
			if (from != null) {
				animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, from.getWidth() * multiplier));
			}
			if (to != null) {
				// Allow this to have a nice transition when coming off an aborted push animation
				float fromLeft = from != null ? from.getX() : 0;
				animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, (fromLeft - to.getWidth()) * multiplier, 0));
			}
		}

		return animatorSet;
	}

	@Override
	protected void resetFromView(@NonNull View from) {
		from.setTranslationX(0);
	}

	@Override
	@NonNull
	public ControllerChangeHandler copy() {
		return new LeftRightChangeHandler(fromRight, getAnimationDuration(), removesFromViewOnPush());
	}
}