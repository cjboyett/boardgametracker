package com.cjboyett.boardgamestats.utility.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;

import com.cjboyett.boardgamestats.utility.Preferences;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Casey on 4/2/2016.
 */
public class StringToBitmapBuilder {
	private Activity activity;
	private int textWidth = 20;
	private float textSize = 14;
	private Paint.Align align = Paint.Align.LEFT;
	private boolean antiAlias = true;

	private Paint paint;
	private int paintWidth;

	public StringToBitmapBuilder(Activity activity) {
		this.activity = activity;
		paint = new Paint();
	}

	public Bitmap buildBitmap(String string) {
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float ratio = metrics.density;

		paint.setTextSize(textSize * ratio);
		paint.setColor(Preferences.getForegroundColor(activity));
		paint.setTextAlign(align);
		paint.setAntiAlias(antiAlias);

		String toPaint = breakString(string);

		float baseline = -paint.ascent();
//		int width = (int)(paint.measureText(toPaint));
		int height = (int) (baseline + paint.descent() + 0.5f);
		Bitmap image = Bitmap.createBitmap(paintWidth, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(image);
//		if (align == Paint.Align.LEFT)
		canvas.drawText(toPaint, 0, baseline, paint);
//		else if (align == Paint.Align.CENTER)
//			canvas.drawText(toPaint, -paintWidth, baseline, paint);
		return image;
	}

	public StringToBitmapBuilder setTextSize(float textSize) {
		this.textSize = textSize;
		return this;
	}

	public StringToBitmapBuilder setAntiAlias(boolean antiAlias) {
		this.antiAlias = antiAlias;
		return this;
	}

	public StringToBitmapBuilder setAlign(Paint.Align align) {
		this.align = align;
		return this;
	}

	public StringToBitmapBuilder setTextWidth(int textWidth) {
		this.textWidth = textWidth;
		return this;
	}

	private String breakString(String string) {
		if (string.length() < textWidth) {
			paintWidth = (int) (paint.measureText(string));
			return string;
		} else {
			Timber.d(string);
			// TODO Lazy breaking.  Fix later?
			String[] bits = string.split(" ");
			String tempLine = "";
			List<String> lines = new ArrayList<>();
			int lineLength = 0;

			for (int i = 0; i < bits.length; i++) {
				if (lineLength + bits[i].length() > textWidth) {
					lineLength = bits[i].length();
					lines.add(tempLine);
					tempLine = bits[i];
				} else {
					lineLength += bits[i].length();
					tempLine += " " + bits[i];
				}
			}

			if (lines.isEmpty()) lines.add(string);

			String toReturn = "";
			paintWidth = 0;

			for (String line : lines) {
				Timber.d(paintWidth + "");
				paintWidth = Math.max(paintWidth, (int) paint.measureText(line));
				toReturn += line + "\n";
			}
			Timber.d(paintWidth + "");
			Timber.d(toReturn);

			toReturn = toReturn.substring(0, toReturn.length() - 1);

			return toReturn;
		}
	}
}
