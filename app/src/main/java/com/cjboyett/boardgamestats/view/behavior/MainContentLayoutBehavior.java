package com.cjboyett.boardgamestats.view.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.bluelinelabs.conductor.ChangeHandlerFrameLayout;

public class MainContentLayoutBehavior extends CoordinatorLayout.Behavior<ChangeHandlerFrameLayout> {

	public MainContentLayoutBehavior() {
	}

	public MainContentLayoutBehavior(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, ChangeHandlerFrameLayout child, View dependency) {
		return dependency instanceof ChangeHandlerFrameLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, ChangeHandlerFrameLayout child, View dependency) {
		ViewCompat.offsetTopAndBottom(child, (dependency.getBottom() - child.getTop()));
		return super.onDependentViewChanged(parent, child, dependency);
	}
}
