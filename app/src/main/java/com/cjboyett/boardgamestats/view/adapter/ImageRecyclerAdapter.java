package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.StringToBitmapBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 4/23/2016.
 */
public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder> {
	private Activity activity;
	private List<String> thumbnailUrls;
	private List<Bitmap> thumbnails;
	private List<String> games;
	//	private ImageController imageController;
	private float ratio = 1f;

	private float SCALE_FACTOR;

	public ImageRecyclerAdapter(Activity activity, List<String> games) {
		this.activity = activity;
		this.games = games;

		SCALE_FACTOR = Preferences.scaleFactor(activity);

		thumbnails = new ArrayList<>();
//		imageController = new ImageController(activity).setDirectoryName("thumbnails");
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		ratio = metrics.density;

		GamesDbHelper dbHelper = new GamesDbHelper(activity);
		thumbnailUrls = new ArrayList<>();
		for (String game : games) {
			if (game.startsWith("---")) thumbnailUrls.add(game.substring(3));
			else {
				int l = game.length();
				String gameType = game.substring(l - 1);
				game = game.substring(0, l - 2);
				switch (gameType) {
					case "b":
						thumbnailUrls.add("http://" + BoardGameDbUtility.getThumbnailUrl(dbHelper, game));
						break;
					case "r":
						thumbnailUrls.add("http://" + RPGDbUtility.getThumbnailUrl(dbHelper, game));
						break;
					case "v":
						thumbnailUrls.add("http://" + VideoGameDbUtility.getThumbnailUrl(dbHelper, game));
						break;
				}
			}
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View linearLayout = LayoutInflater.from(parent.getContext())
										  .inflate(R.layout.simple_linear_layout, parent, false);
		ViewHolder viewHolder = new ViewHolder(linearLayout);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Bitmap thumbnail = null;
		try {
			thumbnail = thumbnails.get(position);
		} catch (Exception e) {
			Log.d("THUMBNAIL", "Making thumbnail for " + position);
			String thumbnailUrl = thumbnailUrls.get(position);
			if (thumbnailUrl.length() > 1) {
				ImageController imageController = new ImageController(activity).setDirectoryName("thumbnails");
				thumbnail =
						imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1)).load();
				imageController.close();
			} else {
				thumbnail = new StringToBitmapBuilder(activity)
						.setTextSize(90 * SCALE_FACTOR)
						.buildBitmap(thumbnailUrl.charAt(0) + "");
			}
			thumbnails.add(thumbnail);
		}
		boolean noThumbnail = (thumbnail == null);
		String game = games.get(position);

		if (noThumbnail)
			thumbnail = new StringToBitmapBuilder(activity)
					.setTextSize(16)
//					.setTextWidth(10)
//					.setAlign(Paint.Align.CENTER)
					.buildBitmap(game.substring(0, game.length() - 2));

		ImageView imageView = new ImageView(activity);
		imageView.setMinimumWidth((int) (128 * ratio * SCALE_FACTOR));
		imageView.setMinimumHeight((int) (128 * ratio * SCALE_FACTOR));
		imageView.setPadding(8, 8, 8, 8);

		if (game.startsWith("---"))
			imageView.setScaleType(ImageView.ScaleType.CENTER);
		else if (game.substring(game.length() - 1).equals("r") || noThumbnail)
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		else
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

		imageView.setImageBitmap(thumbnail);
		((LinearLayout) holder.view).addView(imageView);
	}

	@Override
	public int getItemCount() {
		return games.size();
	}

	@Override
	public void onViewRecycled(ViewHolder holder) {
		((LinearLayout) holder.view).removeAllViews();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View view;

		public ViewHolder(View view) {
			super(view);
			this.view = view;
		}
	}
}
