package com.cjboyett.boardgamestats.conductor.changehandlers;

import android.support.annotation.IntRange;

import com.bluelinelabs.conductor.ControllerChangeHandler;

public class DirectionalChangeHandler {
	public static final int TOP = 0;
	public static final int BOTTOM = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	private static final int VERTICAL_DURATION = 500;
	private static final int HORIZONTAL_DURATION = 350;

	public static ControllerChangeHandler from(@IntRange(from = 0, to = 3) int fromDirection) {
		switch (fromDirection) {
			case TOP:
				return new UpDownChangeHandler(false, VERTICAL_DURATION);
			case BOTTOM:
				return new UpDownChangeHandler(true, VERTICAL_DURATION);
			case LEFT:
				return new LeftRightChangeHandler(false, HORIZONTAL_DURATION);
			case RIGHT:
			default:
				return new LeftRightChangeHandler(true, HORIZONTAL_DURATION);
		}
	}
}
