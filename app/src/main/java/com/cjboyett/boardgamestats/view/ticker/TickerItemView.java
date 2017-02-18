package com.cjboyett.boardgamestats.view.ticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;

/**
 * Container view used in ticker.
 * Created by Casey on 5/8/2016.
 */
public class TickerItemView extends LinearLayout {
	private TextView blurb;
	private ImageView image;

	public TickerItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.ticker_item, this);
		blurb = (TextView) findViewById(R.id.ticker_blurb);
		image = (ImageView) findViewById(R.id.ticker_image);
	}

	public void setBlurb(String blurbText) {
		blurb.setText(Html.fromHtml(blurbText));
	}

	public void setImage(Bitmap tickerImage) {
		image.setImageBitmap(tickerImage);
	}

	public void setImageScaleType(ImageView.ScaleType scaleType) {
		image.setScaleType(scaleType);
	}

	public void colorComponents(int foregroundColor) {
		blurb.setTextColor(foregroundColor);
	}

	public void setOnImageClickListener(OnClickListener l) {
		image.setOnClickListener(l);
	}
}
