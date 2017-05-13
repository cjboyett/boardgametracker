package com.cjboyett.boardgamestats.conductor.changehandlers;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;

public class UpDownChangeHandler extends AnimatorChangeHandler {
	private boolean fromBottom;

	public UpDownChangeHandler() { }

	public UpDownChangeHandler(boolean removesFromViewOnPush) {
		super(removesFromViewOnPush);
	}

	public UpDownChangeHandler(long duration) {
		super(duration);
	}

	public UpDownChangeHandler(long duration, boolean removesFromViewOnPush) {
		super(duration, removesFromViewOnPush);
	}

	public UpDownChangeHandler(boolean fromBottom, long duration) {
		super(duration);
		this.fromBottom = fromBottom;
	}

	public UpDownChangeHandler(boolean fromBottom, long duration, boolean removesFromViewOnPush) {
		super(duration, removesFromViewOnPush);
		this.fromBottom = fromBottom;
	}

	@NonNull
	@Override
	protected Animator getAnimator(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, boolean toAddedToContainer) {
		AnimatorSet animatorSet = new AnimatorSet();
		int multiplier = fromBottom ? 1 : -1;

		if (isPush) {
			if (from != null) {
				animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, -from.getHeight() * multiplier));
			}
			if (to != null) {
				animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, to.getHeight() * multiplier, 0));
			}
		} else {
			if (from != null) {
				animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, from.getHeight() * multiplier));
			}
			if (to != null) {
				// Allow this to have a nice transition when coming off an aborted push animation
				float fromTop = from != null ? from.getY() : 0;
				animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, (fromTop - to.getHeight()) * multiplier, 0));
			}
		}

		return animatorSet;
	}

	@Override
	protected void resetFromView(@NonNull View from) {
		from.setTranslationY(0);
	}

	@NonNull
	@Override
	public ControllerChangeHandler copy() {
		return new UpDownChangeHandler(fromBottom, getAnimationDuration(), removesFromViewOnPush());
	}

}
