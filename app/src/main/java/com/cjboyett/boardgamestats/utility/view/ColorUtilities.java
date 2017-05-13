package com.cjboyett.boardgamestats.utility.view;

import android.graphics.Color;

public class ColorUtilities {
	private static final float MAX_TIME = 180f;

	public static int lighten(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = 0.25f + 0.75f * hsv[2];
		if (hsv[2] > 1f) hsv[2] = 1f;
		return Color.HSVToColor(hsv);
	}

	public static int darken(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = 0.75f * hsv[2];
		if (hsv[2] < 0f) hsv[2] = 0f;
		return Color.HSVToColor(hsv);
	}

	public static int complement(int color) {
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		return Color.rgb(255 - red, 255 - green, 255 - blue);
	}

	public static int hintTextColor(int backgroundColor, int foregroundColor) {
		float[] hsv = new float[3];
		Color.colorToHSV(backgroundColor, hsv);
//		return hsv[2] > 0.5f;

		if (hsv[2] > 0.5f) return ColorUtilities.lighten(foregroundColor);
		else return ColorUtilities.darken(foregroundColor);
	}

	//TODO Maybe fix this
	public static int adjustBasedOnHSV(int color) {
/*
		float[] hsv = new float[3];
		Color.colorToHSV(color,hsv);
		if (hsv[2] > 0.5f) return darken(color);
		else return lighten(color);
*/
		return color;
	}

	public static int mixWithBaseColor(int color, int weight1, int baseColor, int weight2) {
		int red = (weight1 * Color.red(color) + weight2 * Color.red(baseColor)) / (weight1 + weight2);
		int green = (weight1 * Color.green(color) + weight2 * Color.green(baseColor)) / (weight1 + weight2);
		int blue = (weight1 * Color.blue(color) + weight2 * Color.blue(baseColor)) / (weight1 + weight2);
		return Color.rgb(red, green, blue);
	}

	public static int pieSliceColor(int gameType, int timePlayed) {
		switch (gameType) {
			case 0:
				return Color.HSVToColor(new float[]{120, Math.min(MAX_TIME, timePlayed) / MAX_TIME, 1});
			case 1:
				return Color.HSVToColor(new float[]{0, Math.min(MAX_TIME, timePlayed) / MAX_TIME, 1});
			case 2:
				return Color.HSVToColor(new float[]{240, Math.min(MAX_TIME, timePlayed) / MAX_TIME, 1});
			default:
				return Color.BLACK;
		}
	}
}
