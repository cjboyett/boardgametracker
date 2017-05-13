package com.cjboyett.boardgamestats.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.model.stats.StatisticsManager;
import com.cjboyett.boardgamestats.utility.BitmapCache;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Casey on 5/9/2016.
 */
public class PlayerListAdapter extends BaseAdapter {
	private Activity activity;
	private List<String> players;
	private Map<String, Integer> allPlayersWithTimesPlayed;
	private BitmapCache avatars;
//	private Map<String, Bitmap> avatars;

	private int backgroundColor, foregroundColor;

	public PlayerListAdapter(final Activity activity, final List<String> players) {
		this.activity = activity;
		this.players = new ArrayList<>(players);
		allPlayersWithTimesPlayed = StatisticsManager.getInstance(activity).getAllPlayersWithTimesPlayed();

		backgroundColor = Preferences.getBackgroundColor(activity);
		foregroundColor = Preferences.getForegroundColor(activity);

		avatars = new BitmapCache();
//		avatars = new HashMap<>();
//		int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);

		try {
			for (int i = 0; i <= Math.min(10, players.size()); i++) new BitmapWorkerTask(null).execute(players.get(i));
			for (int i = players.size() - 1; i > 10; i--) new BitmapWorkerTask(null).execute(players.get(i));
		} catch (Exception e) {
		}

/*
		for (int i=0;i<players.size();i++)
		{
			String player = players.get(i);
			new AsyncTask<String, Void, Bitmap>()
			{
				@Override
				protected Bitmap doInBackground(final String... player)
				{
					Bitmap avatar = null;
					avatar = ViewUtilities.createAvatar(activity, player[0], true);
					avatars.put(player[0], avatar);
					return avatar;
				}

				@Override
				protected void onPostExecute(Bitmap bitmap)
				{
					notifyDataSetChanged();
				}
			}.execute(player);
		}
*/

/*
		for (int i=0;i<players.size();i++)
		{
			final String player = players.get(i);
			if (false)//player.charAt(0) == 'A')
			{
				new AsyncTask<String, Void, Bitmap>()
				{
					@Override
					protected Bitmap doInBackground(String... param)
					{
						final Bitmap[] profilePicture = {null};
						final Bundle params = new Bundle();
						params.putString("fields", "friends, picture.type(square)");

						new GraphRequest(
							AccessToken.getCurrentAccessToken(),
							"/me",
							params,
							HttpMethod.GET,
							new GraphRequest.Callback() {
								public void onCompleted(GraphResponse response)
								{
									Timber.d(response.toString());
									JSONObject data = response.getJSONObject();

									if (data.has("friends"))
									{
										try
										{
											String uid = data.getJSONObject("friends").getJSONArray("data").getJSONObject(0).getString("id");
											new GraphRequest(
													AccessToken.getCurrentAccessToken(),
													"/" + uid,
													params,
													HttpMethod.GET,
													new GraphRequest.Callback()
													{
														@Override
														public void onCompleted(GraphResponse response)
														{
															JSONObject data = response.getJSONObject();
															if (data.has("picture"))
															{
																try
																{
																	String profilePictureURL = data.getJSONObject("picture")
																	                               .getJSONObject("data")
																	                               .getString("url");

																	new AsyncTask<String, Void, Bitmap>()
																	{
																		@Override
																		protected Bitmap doInBackground(String... params)
																		{
																			try
																			{
																				profilePicture[0] = BitmapFactory.decodeStream(new URL(params[0]).openConnection()
																				                                                                 .getInputStream());
																			}
																			catch (Exception e)
																			{
																				Timber.e(e);
																			}

																			return profilePicture[0];
																		}

																		@Override
																		protected void onPostExecute(Bitmap bitmap)
																		{
																			if (bitmap != null) avatars.put(player, bitmap);
																			else Timber.d("NULL");
																			notifyDataSetChanged();
																		}
																	}.execute(profilePictureURL);
																}
																catch (Exception e)
																{
																	Timber.e(e);
																}
															}
														}
													}
											).executeAsync();
										}
										catch (Exception e)
										{
											Timber.e(e);
										}
									}
								}
							}
						).executeAsync();

						return profilePicture[0];
					}
				}.execute(player);
			}
		}
*/
	}

	@Override
	public int getCount() {
		return players.size();
	}

	@Override
	public Object getItem(int position) {
		return players.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view;
		if (convertView != null) view = convertView;
		else {
			view = activity.getLayoutInflater().inflate(R.layout.player_list_item, null);

			view.setBackgroundColor(backgroundColor);
			view.findViewById(R.id.textview_player).setBackgroundColor(backgroundColor);
			((TextView) view.findViewById(R.id.textview_player)).setTextColor(foregroundColor);
			ViewUtilities.tintButtonBackground(((AppCompatButton) view.findViewById(R.id.button_player_plays)),
											   foregroundColor);
			((Button) view.findViewById(R.id.button_player_plays)).setTextColor(backgroundColor);
		}

		final String player = (String) getItem(position);

		Bitmap avatar = avatars.get(player);
		if (avatar != null)
			((ImageView) view.findViewById(R.id.imageview_avatar)).setImageBitmap(avatar);
		else
			new BitmapWorkerTask(((ImageView) view.findViewById(R.id.imageview_avatar))).execute(player);
		view.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
		((TextView) view.findViewById(R.id.textview_player)).setText(player);
		((Button) view.findViewById(R.id.button_player_plays)).setText(allPlayersWithTimesPlayed.get(player) + "");

		return view;
	}

	private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public BitmapWorkerTask(ImageView imageView) {
			imageViewReference = new WeakReference<>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... player) {
			Bitmap avatar = null;
			avatar = ViewUtilities.createAvatar(activity, player[0], true);
			avatars.addBitmapToCache(player[0], avatar);
			return avatar;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) imageView.setImageBitmap(bitmap);
				notifyDataSetChanged();
			}
		}
	}

}
