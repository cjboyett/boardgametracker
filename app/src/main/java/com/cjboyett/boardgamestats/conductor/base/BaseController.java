package com.cjboyett.boardgamestats.conductor.base;

import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bluelinelabs.conductor.Controller;
import com.cjboyett.boardgamestats.conductor.ConductorActivity;
import com.cjboyett.boardgamestats.conductor.MyNavigationDrawer;
import com.cjboyett.boardgamestats.utility.Preferences;

public abstract class BaseController extends Controller {
	protected int foregroundColor;
	protected int backgroundColor;
	protected int buttonColor;
	protected int hintTextColor;

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		setColors();
		getToolbar().setTitleTextColor(foregroundColor);
		getToolbar().setBackgroundColor(buttonColor);
		getToolbarImage().setImageBitmap(null);
	}

	protected MyNavigationDrawer getNavigationDrawer() {
		return ((ConductorActivity) getActivity()).getMyNavigationDrawer();
	}

	protected AppBarLayout getAppBarLayout() {
		return ((ConductorActivity) getActivity()).getAppBarLayout();
	}

	protected CollapsingToolbarLayout getCollapsingToolbarLayout() {
		return ((ConductorActivity) getActivity()).getCollapsingToolbarLayout();
	}

	protected Toolbar getToolbar() {
//		return ((ConductorActivity) getActivity()).getSupportActionBar();
		return ((ConductorActivity) getActivity()).getToolbar();
	}

	protected ImageView getToolbarImage() {
		return ((ConductorActivity) getActivity()).getToolbarImage();
	}

	protected void setColors() {
		foregroundColor = Preferences.getForegroundColor(getActivity());
		backgroundColor = Preferences.getBackgroundColor(getActivity());
		buttonColor = Preferences.getButtonColor(getActivity());
		hintTextColor = Preferences.getHintTextColor(getActivity());
	}
}
