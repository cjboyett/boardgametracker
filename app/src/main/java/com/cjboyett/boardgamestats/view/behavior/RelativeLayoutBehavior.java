package com.cjboyett.boardgamestats.view.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Casey on 4/18/2016.
 */
public class RelativeLayoutBehavior extends CoordinatorLayout.Behavior<RelativeLayout>
{
	public RelativeLayoutBehavior(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, RelativeLayout child, View dependency)
	{
		return super.layoutDependsOn(parent, child, dependency) || dependency instanceof Snackbar.SnackbarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, RelativeLayout child, View dependency)
	{
		float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
		child.setTranslationY(translationY);
		return true;
	}

}
