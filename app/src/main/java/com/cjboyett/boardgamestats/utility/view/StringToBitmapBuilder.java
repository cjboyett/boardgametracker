package com.cjboyett.boardgamestats.utility.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;

import com.cjboyett.boardgamestats.utility.Preferences;

/**
 * Created by Casey on 4/2/2016.
 */
public class StringToBitmapBuilder {
	private Activity activity;
	private int imageWidth = 100;
	private int imageHeight = 100;
	private float textSize = 14;
	private Paint.Align align = Paint.Align.LEFT;
	private Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
	private boolean antiAlias = true;

	private TextPaint paint;

	public StringToBitmapBuilder(Activity activity) {
		this.activity = activity;
		paint = new TextPaint();
	}

	public Bitmap buildBitmap(String string) {
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float ratio = metrics.density;

		paint.setTextSize(textSize * ratio);
		paint.setColor(Preferences.getForegroundColor(activity));
		paint.setTextAlign(align);
		paint.setAntiAlias(antiAlias);

		StaticLayout staticLayout = new StaticLayout(string, paint, imageWidth, alignment, 1, 0, false);
		Bitmap image = Bitmap.createBitmap(imageWidth, staticLayout.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(image);
		canvas.save();
		staticLayout.draw(canvas);
		canvas.restore();

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

	public StringToBitmapBuilder setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
		return this;
	}

	public StringToBitmapBuilder setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
		return this;
	}
}
