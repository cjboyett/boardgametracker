package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import timber.log.Timber;

/**
 * Created by Casey on 4/23/2016.
 */
public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder> {
	private Activity activity;
	private List<String> thumbnailUrls;
	private List<Bitmap> thumbnails;
	private List<String> games;

	private GamesGridView gamesGridView;

	//	private ImageController imageController;
	private float ratio = 1f;
	private int foregroundColor;

	private float SCALE_FACTOR;

	public ImageRecyclerAdapter(GamesGridView gamesGridView, List<String> games, int foregroundColor) {
		this.gamesGridView = gamesGridView;
		this.activity = gamesGridView.getActivity();
		this.games = games;
		this.foregroundColor = foregroundColor;

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
		String gameType = null;
		TextView textView = null;
		try {
			thumbnail = thumbnails.get(position);
		} catch (Exception e) {
			Timber.d("Making thumbnail for " + position);
			String thumbnailUrl = thumbnailUrls.get(position);
			if (thumbnailUrl.length() > 1) {
				ImageController imageController = new ImageController(activity).setDirectoryName("thumbnails");
				thumbnail =
						imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1)).load();
				imageController.close();
			}
			thumbnails.add(thumbnail);
		}
		boolean noThumbnail = (thumbnail == null);
		String game = games.get(position);

		if (noThumbnail && !game.startsWith("---")) {
			thumbnail = new StringToBitmapBuilder(activity)
					.setTextSize(16)
					.setImageWidth((int) (128 * ratio * SCALE_FACTOR))
					.buildBitmap(game.substring(0, game.length() - 2));
		}

		ImageView imageView = new ImageView(activity);
		imageView.setMinimumWidth((int) (128 * ratio * SCALE_FACTOR));
		imageView.setMinimumHeight((int) (128 * ratio * SCALE_FACTOR));
		imageView.setPadding(8, 8, 8, 8);

		if (game.startsWith("---")) {
			textView = new TextView(activity);
			textView.setText(thumbnailUrls.get(position).charAt(0) + "");
			textView.setTextSize(48);
			textView.setTextColor(foregroundColor);
			textView.setWidth((int) (128 * ratio * SCALE_FACTOR));
			textView.setHeight((int) (128 * ratio * SCALE_FACTOR));
			textView.setGravity(Gravity.CENTER);
			((LinearLayout) holder.view).addView(textView);
		} else {
			if (game.substring(game.length() - 1).equals("r") || noThumbnail) {
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			} else {
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			}

			imageView.setImageBitmap(thumbnail);
			((LinearLayout) holder.view).addView(imageView);
		}

		if (!game.startsWith("---")) {
			gameType = game.substring(game.length() - 1);
			game = game.substring(0, game.length() - 2);
		}

		addOnTouchListener(holder.view, game, thumbnailUrls.get(position), thumbnail, gameType);
	}

	@Override
	public int getItemCount() {
		return games.size();
	}

	@Override
	public void onViewRecycled(ViewHolder holder) {
		((LinearLayout) holder.view).removeAllViews();
	}

	private void addOnTouchListener(View view, final String game, final String thumbnailUrl, final Bitmap thumbnail,
									final String gameType) {
		view.setOnClickListener((View.OnClickListener) (v) -> {
			if (!game.startsWith("---")) {
				String bggId = "102030";
				switch (gameType) {
					case "b":
						//bggId = BoardGameDbUtility.getBggId(dbHelper, game);
						break;
					case "r":
						break;
					case "v":
						break;
					default:
						break;
				}

				final Bitmap thumbnail1 = new ImageController(activity)
						.setDirectoryName("thumbnails")
						.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
						.load();
				if (Preferences.generatePalette(activity) && thumbnail1 != null) {
					Palette.from(thumbnail1).generate(new Palette.PaletteAsyncListener() {
						@Override
						public void onGenerated(Palette palette) {
							List<Palette.Swatch> swatchList = new ArrayList<>();
							if (palette.getDarkVibrantSwatch() != null)
								swatchList.add(palette.getDarkVibrantSwatch());
							if (palette.getDarkMutedSwatch() != null)
								swatchList.add(palette.getDarkMutedSwatch());
							if (palette.getMutedSwatch() != null)
								swatchList.add(palette.getMutedSwatch());
							if (palette.getVibrantSwatch() != null)
								swatchList.add(palette.getVibrantSwatch());
							if (palette.getLightMutedSwatch() != null)
								swatchList.add(palette.getLightMutedSwatch());
							if (palette.getLightVibrantSwatch() != null)
								swatchList.add(palette.getLightVibrantSwatch());

							Palette.Swatch swatch;
							if (Preferences.lightUI(activity))
								swatch = swatchList.get(swatchList.size() - 1);
							else swatch = swatchList.get(0);

							Preferences.setGeneratedPaletteColors(activity,
																  swatch.getRgb(),
																  swatch.getBodyTextColor());

							gamesGridView.navigateToGameDetails(game, gameType,
																thumbnailUrl,
																bggId);
						}
					});
				} else gamesGridView.navigateToGameDetails(game, gameType, thumbnailUrl, bggId);
			}
		});
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public View view;

		public ViewHolder(View view) {
			super(view);
			this.view = view;
		}
	}

	public interface GamesGridView {
		void navigateToGameDetails(String game, String gameType, String thumbnailUrl, String bggId);

		Activity getActivity();
	}
}
