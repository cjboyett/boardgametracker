package com.cjboyett.boardgamestats.scoop.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.cjboyett.boardgamestats.R;
import com.lyft.scoop.TransitionListener;
import com.lyft.scoop.transitions.ObjectAnimatorTransition;

public class SlideRightTransition extends ObjectAnimatorTransition {
	private final Interpolator interpolator;

	public SlideRightTransition() {
		interpolator = new LinearInterpolator();
	}

	public SlideRightTransition(Interpolator interpolator) {
		this.interpolator = interpolator;
	}

	@Override
	protected void performTranslate(ViewGroup root, View from, View to, TransitionListener transitionListener) {
		Animator animator = createAnimator(from, to);
	}

	private Animator createAnimator(View from, View to) {
		AnimatorSet set = new AnimatorSet();
		if (from != null) {
			Animation animation = AnimationUtils.loadAnimation(from.getContext(), R.anim.shrink_right_exit);
			animation.setInterpolator(interpolator);
			from.startAnimation(animation);
		}
		if (to != null) {
			Animation animation = AnimationUtils.loadAnimation(to.getContext(), R.anim.shrink_right_enter);
			animation.setInterpolator(interpolator);
			to.startAnimation(animation);
		}
		return set;
	}
}
