package com.cjboyett.boardgamestats.view.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.cjboyett.boardgamestats.view.AdViewContainer;

/**
 * Created by Casey on 4/18/2016.
 */
public class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
	public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
		return super.layoutDependsOn(parent, child, dependency) || dependency instanceof AdViewContainer;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
		float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
		child.setTranslationY(translationY);
		return true;
	}
}
