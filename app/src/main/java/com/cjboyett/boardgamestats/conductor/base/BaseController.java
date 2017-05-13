package com.cjboyett.boardgamestats.conductor.base;

import com.bluelinelabs.conductor.Controller;
import com.cjboyett.boardgamestats.utility.Preferences;

public abstract class BaseController extends Controller {
	protected int foregroundColor;
	protected int backgroundColor;
	protected int buttonColor;
	protected int hintTextColor;

	protected void setColors() {
		foregroundColor = Preferences.getForegroundColor(getActivity());
		backgroundColor = Preferences.getBackgroundColor(getActivity());
		buttonColor = Preferences.getButtonColor(getActivity());
		hintTextColor = Preferences.getHintTextColor(getActivity());
	}
}
