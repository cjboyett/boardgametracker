package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.model.games.rpg.RPGPlayData;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ImageController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Casey on 4/3/2016.
 */
public class GamePlayAdapter extends BaseAdapter {
	private Activity activity;
	private List<GamePlayData> gamePlayDataList;
	private ImageController imageController;
	private float ratio;
	private String userName;

	private boolean oneGame;

	private int backgroundColor, foregroundColor;

	public GamePlayAdapter(final Activity activity, List<GamePlayData> gamePlayDataList) {
		this.activity = activity;
		this.gamePlayDataList = gamePlayDataList;
		imageController = new ImageController(activity).setDirectoryName("thumbnails");

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		ratio = metrics.density;

		backgroundColor = Preferences.getBackgroundColor(activity);
		foregroundColor = Preferences.getForegroundColor(activity);

		if (Preferences.generatePalette(activity)) {
			oneGame = true;

			if (gamePlayDataList.size() > 1) {
				for (int i = 0; i < gamePlayDataList.size() - 1; i++) {
					for (int j = i + 1; j < gamePlayDataList.size(); j++) {
						if (!gamePlayDataList.get(i).getGame().getName().equals(
								gamePlayDataList.get(j).getGame().getName())) {
							oneGame = false;
							i = gamePlayDataList.size();
							break;
						}
					}
				}
			}

			if (oneGame && gamePlayDataList.size() > 0) {
				String thumbnailUrl = gamePlayDataList.get(0).getGame().getThumbnailUrl();
				if (thumbnailUrl != null) {
					Bitmap thumbnail =
							imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
										   .load();
					Palette palette = Palette.generate(thumbnail);
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

					backgroundColor = swatch.getRgb();
					foregroundColor = swatch.getBodyTextColor();
				}
			}
		}
		userName = Preferences.getUsername(activity);
	}

	@Override
	public int getCount() {
		return gamePlayDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return gamePlayDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = activity.getLayoutInflater().inflate(R.layout.list_item_gameplay, null);
		GamePlayData gamePlayData = (GamePlayData) getItem(position);
		((TextView) view.findViewById(R.id.textview_date)).setText(gamePlayData.getDate().toString());
		List<GamePlayerData> gamePlayerDataList = new ArrayList<>(gamePlayData.getOtherPlayers().values());
		Collections.sort(gamePlayerDataList, new Comparator<GamePlayerData>() {
			@Override
			public int compare(GamePlayerData lhs, GamePlayerData rhs) {
				if (lhs.getPlayerName().equals("master_user"))
					return userName.compareTo(rhs.getPlayerName());
				else if (rhs.getPlayerName().equals("master_user"))
					return lhs.getPlayerName().compareTo(userName);
				else
					return lhs.getPlayerName().compareTo(rhs.getPlayerName());
			}
		});

		String players = "";
		if (!Preferences.sortWinnersFirst(activity)) {
			for (GamePlayerData gamePlayerData : gamePlayerDataList) {
				String playerName = gamePlayerData.getPlayerName();
				if (playerName.equals("master_user")) playerName = userName;
				if (gamePlayerData.isWin()) players += "<b>" + playerName + "</b>, ";
				else players += playerName + ", ";
			}
		} else {
			for (GamePlayerData gamePlayerData : gamePlayerDataList) {
				String playerName = gamePlayerData.getPlayerName();
				if (playerName.equals("master_user")) playerName = userName;
				if (gamePlayerData.isWin()) players += "<b>" + playerName + "</b>, ";
			}
			for (GamePlayerData gamePlayerData : gamePlayerDataList) {
				String playerName = gamePlayerData.getPlayerName();
				if (playerName.equals("master_user")) playerName = userName;
				if (!gamePlayerData.isWin()) players += playerName + ", ";
			}
		}

		if (gamePlayData instanceof RPGPlayData) players = players.replaceAll("<b>", "").replaceAll("</b>", "");

		if (players.length() >= 2) players = players.substring(0, players.length() - 2);
		((TextView) view.findViewById(R.id.textview_players)).setText(Html.fromHtml(players));
		String thumbnailUrl = gamePlayData.getGame().getThumbnailUrl();
		Bitmap thumbnail;
		if (thumbnailUrl != null) {
			thumbnail = imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1)).load();
			ImageView imageView = (ImageView) view.findViewById(R.id.imageview_avatar);
			imageView.setMinimumWidth((int) (128 * ratio));
			imageView.setMinimumHeight((int) (128 * ratio));
			imageView.setPadding(8, 8, 8, 8);
			if (!(gamePlayData instanceof RPGPlayData))
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			else
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageBitmap(thumbnail);
		} else view.findViewById(R.id.imageview_avatar).setVisibility(View.GONE);

		if (!gamePlayData.isCountForStats()) view.findViewById(R.id.imageview_ignore).setVisibility(View.VISIBLE);

		colorView(view);

		return view;
	}

	private void colorView(View view) {
		view.setBackgroundColor(backgroundColor);
		((TextView) view.findViewById(R.id.textview_date)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_date).setBackgroundColor(backgroundColor);
		((TextView) view.findViewById(R.id.textview_players)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_players).setBackgroundColor(backgroundColor);
	}
}
