package com.cjboyett.boardgamestats.utility.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.co.deanwild.flowtextview.FlowTextView;

/**
 * Created by Casey on 4/8/2016.
 */
public class ViewUtilities
{
	public static <V extends View> Collection<V> findChildrenByClass(ViewGroup viewGroup, Class<V> vClass)
	{
		return gatherChildrenByClass(viewGroup, vClass, new ArrayList<V>());
	}

	private static <V extends View> Collection<V> gatherChildrenByClass(ViewGroup viewGroup, Class<V> vClass, Collection<V> childrenFound)
	{
		for (int i=0;i<viewGroup.getChildCount();i++)
		{
			final View child = viewGroup.getChildAt(i);
			if (vClass.isAssignableFrom(child.getClass()))
				childrenFound.add((V)child);
			if (child instanceof ViewGroup)
				gatherChildrenByClass((ViewGroup)child, vClass, childrenFound);
		}
		return childrenFound;
	}

	public static void setBackground(View view, Drawable background)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			view.setBackground(background);
		}
		else
		{
			view.setBackgroundDrawable(background);
		}
	}

	public static void tintCheckBox(AppCompatCheckBox checkBox, int color)
	{
		int[][] states = new int[][]{new int[]{android.R.attr.state_checked},
				new int[]{-android.R.attr.state_checked}};
		int[] colors = new int[]{color, color};
		ColorStateList colorStateList = new ColorStateList(states, colors);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			checkBox.setButtonTintList(colorStateList);
			checkBox.setButtonTintMode(PorterDuff.Mode.SRC_ATOP);
		}
		else
		{
			checkBox.setSupportButtonTintList(colorStateList);
			checkBox.setSupportButtonTintMode(PorterDuff.Mode.SRC_ATOP);
		}
	}

	public static void tintCheckBox(AppCompatCheckBox checkBox)
	{
		int foregroundColor = Preferences.getForegroundColor(checkBox.getContext());
		tintCheckBox(checkBox, foregroundColor);
	}

	public static void tintImageView(AppCompatImageView imageView)
	{
		int foregroundColor = Preferences.getForegroundColor(imageView.getContext());
		tintImageView(imageView, foregroundColor);
	}

	public static void tintImageView(AppCompatImageView imageView, int color)
	{
		int[][] states = new int[][]{new int[]{android.R.attr.state_enabled},
				new int[]{-android.R.attr.state_enabled}};
		int[] colors = new int[]{color, color};
		ColorStateList colorStateList = new ColorStateList(states, colors);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			imageView.setImageTintList(colorStateList);
			imageView.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
		}
		else
		{
			imageView.setSupportBackgroundTintList(colorStateList);
			imageView.setSupportBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
		}
	}

	public static void tintButtonBackground(AppCompatButton button)
	{
		int backgroundColor = Preferences.getBackgroundColor(button.getContext());
		tintButtonBackground(button, backgroundColor);
	}

	public static void tintButtonBackground(AppCompatButton button, int color)
	{
		int[][] states = new int[][]{new int[]{android.R.attr.state_enabled},
				new int[]{-android.R.attr.state_enabled}};
		int[] colors = new int[]{color, color};
		ColorStateList colorStateList = new ColorStateList(states, colors);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			button.setBackgroundTintList(colorStateList);
			button.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
		}
		else
		{
			button.setSupportBackgroundTintList(colorStateList);
			button.setSupportBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
		}
	}

	public static void tintLayoutBackground(View layout)
	{
		int color = Preferences.getForegroundColor(layout.getContext());
		tintLayoutBackground(layout, color);
	}

	public static void tintLayoutBackground(View layout, int color)
	{
		int[][] states = new int[][]{new int[]{android.R.attr.state_enabled},
		                             new int[]{-android.R.attr.state_enabled}};
		int[] colors = new int[]{color, color};
		ColorStateList colorStateList = new ColorStateList(states, colors);

		try
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			{
				layout.setBackgroundTintList(colorStateList);
				layout.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
			}
			else
			{
				Drawable background = DrawableCompat.wrap(layout.getBackground());
				DrawableCompat.setTintList(background, colorStateList);
				DrawableCompat.setTintMode(background, PorterDuff.Mode.SRC_ATOP);
				setBackground(layout, background);
			}
		}
		catch (Exception e)
		{}
	}

	public static void tintProgressBar(ProgressBar progressBar, int color)
	{
		int[][] states = new int[][]{new int[]{android.R.attr.state_enabled},
		                             new int[]{-android.R.attr.state_enabled}};
		int[] colors = new int[]{color, color};
		ColorStateList colorStateList = new ColorStateList(states, colors);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			progressBar.setIndeterminateTintList(colorStateList);
			progressBar.setIndeterminateTintMode(PorterDuff.Mode.SRC_ATOP);
		}
		else
		{
			Drawable background = DrawableCompat.wrap(progressBar.getIndeterminateDrawable());
			DrawableCompat.setTintList(background, colorStateList);
			DrawableCompat.setTintMode(background, PorterDuff.Mode.SRC_ATOP);
			progressBar.setIndeterminateDrawable(background);
		}
	}

	public static void tintRatingBar(RatingBar ratingBar, int progressColor, int progressBackgroundColor)
	{
		int[][] progressStates = new int[][]{new int[]{android.R.attr.state_enabled},
		                                     new int[]{-android.R.attr.state_enabled}};
		int[] progressColors = new int[]{progressColor, progressColor};
		ColorStateList progressColorStateList = new ColorStateList(progressStates, progressColors);

/*
		int[][] progressBackgroundStates = new int[][]{new int[]{android.R.attr.state_enabled},
		                                     new int[]{-android.R.attr.state_enabled}};
		int[] progressBackgroundColors = new int[]{progressBackgroundColor, progressBackgroundColor};
		ColorStateList progressBackgroundColorStateList = new ColorStateList(progressBackgroundStates, progressBackgroundColors);
*/

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			ratingBar.setProgressTintList(progressColorStateList);
			ratingBar.setProgressTintMode(PorterDuff.Mode.SRC_ATOP);

//			ratingBar.setProgressBackgroundTintList(progressBackgroundColorStateList);
//			ratingBar.setProgressBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
		}
		else
		{
			Drawable progress = DrawableCompat.wrap(ratingBar.getProgressDrawable());
			DrawableCompat.setTintList(progress, progressColorStateList);
			DrawableCompat.setTintMode(progress, PorterDuff.Mode.SRC_ATOP);
			ratingBar.setProgressDrawable(progress);

/*
			Drawable progressBackground = DrawableCompat.wrap(ratingBar.getIndeterminateDrawable());
			DrawableCompat.setTintList(progressBackground, progressBackgroundColorStateList);
			DrawableCompat.setTintMode(progressBackground, PorterDuff.Mode.SRC_ATOP);
			ratingBar.setIndeterminateDrawable(progressBackground);
*/
		}
	}

	public static Bitmap createAvatar(Context context, String name, boolean small)
	{
		Bitmap avatar = null;

		GamesDbHelper dbHelper = new GamesDbHelper(context);
		String avatarPath = PlayersDbUtility.getPlayerImageFilePath(dbHelper, name);
		dbHelper.close();

		if (!StringUtils.isEmpty(avatarPath))
		{
			Log.d("PATH", avatarPath);
			BitmapFactory.Options options = new BitmapFactory.Options();
			try
			{
				ImageController imageController = new ImageController(context);
				imageController.setDirectoryName("avatars")
						.setFileName(avatarPath + ".jpg");
				Bitmap original = imageController.load(); //BitmapFactory.decodeFile(avatarPath);

//				if (original.getByteCount() > 256 * 1024)
//				{
//					options.inSampleSize = (int)Math.ceil(Math.sqrt((original.getByteCount() / 1024) / 256));
//					original = BitmapFactory.decodeFile(avatarPath, options);
//				}

				Log.d("SIZE", original.getByteCount() / 1024 + "");

				avatar = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
				Canvas canvas = new Canvas(avatar);

				canvas.drawBitmap(original, 0, 0, null);

				Paint maskPaint = new Paint();
				maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					Bitmap roundedRect = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
					Canvas roundedRectCanvas = new Canvas(roundedRect);
					Paint roundedRectPaint = new Paint();
					roundedRectPaint.setColor(Color.BLUE);
					int cornerRadius = (int)Math.max(avatar.getWidth() / 16., avatar.getHeight() / 16.);
					roundedRectCanvas.drawRoundRect(0, 0, avatar.getWidth(), avatar.getHeight(), cornerRadius, cornerRadius, roundedRectPaint);
					canvas.drawBitmap(roundedRect, 0, 0, maskPaint);
				}

//				ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
//				if (avatar.compress(Bitmap.CompressFormat.JPEG, 40, byteOutputStream))
//				{
//					byte[] byteArray = byteOutputStream.toByteArray();
//					avatar = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//					Log.d("SIZE", avatar.getByteCount() / 1024 + "");
//				}
			}
			catch (Exception e)
			{

			}
		}
		else if (avatar == null)
		{
			int[] avatarHeads = {R.drawable.avatar_head_large_128,
			                     R.drawable.avatar_head_medium_128,
			                     R.drawable.avatar_head_small_128,
			                     R.drawable.avatar_head_large_512,
			                     R.drawable.avatar_head_medium_512,
			                     R.drawable.avatar_head_small_512};
			int[] avatarBodies = {R.drawable.avatar_body_large_128,
			                      R.drawable.avatar_body_medium_128,
			                      R.drawable.avatar_body_small_128,
			                      R.drawable.avatar_body_large_512,
			                      R.drawable.avatar_body_medium_512,
			                      R.drawable.avatar_body_small_512};
			Random r = new Random(name.hashCode() * Preferences.getUsername(context)
			                                                   .hashCode());

			Bitmap avatarHead, avatarBody;
			if (small)
			{
				avatarHead = BitmapFactory.decodeResource(context.getResources(), avatarHeads[r.nextInt(3)]);
				avatarBody = BitmapFactory.decodeResource(context.getResources(), avatarBodies[r.nextInt(3)]);
			}
			else
			{
				avatarHead = BitmapFactory.decodeResource(context.getResources(), avatarHeads[3 + r.nextInt(3)]);
				avatarBody = BitmapFactory.decodeResource(context.getResources(), avatarBodies[3 + r.nextInt(3)]);
			}

			avatar = Bitmap.createBitmap(avatarHead.getWidth(), avatarHead.getHeight(), avatarHead.getConfig());
			Canvas canvas = new Canvas(avatar);

			List<Integer> colors = new ArrayList<>();

			colors.add(r.nextInt(128));
			colors.add(64 + r.nextInt(128));
			colors.add(128 + r.nextInt(128));
/*
		colors.add(16 + 80 * r.nextInt(2));
		colors.add(16 + 64 + 80 * r.nextInt(2));
		colors.add(16 + 128 + 80 * r.nextInt(2));
*/

			int color = Color.rgb(colors.remove(r.nextInt(3)), colors.remove(r.nextInt(2)), colors.remove(0));
			color = ColorUtilities.mixWithBaseColor(color, 1, Preferences.getForegroundColor(context), 1);
			color = ColorUtilities.mixWithBaseColor(color, 4, Preferences.lightUI(context) ? Color.BLACK : Color.WHITE, 1);

			Paint paint = new Paint();
			paint.setColor(color);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			{
				if (small)
					canvas.drawRoundRect(0, 0, avatarHead.getWidth(), avatarHead.getHeight(), 16, 16, paint);
				else
					canvas.drawRoundRect(0, 0, avatarHead.getWidth(), avatarHead.getHeight(), 64, 64, paint);
			}
			else
				canvas.drawColor(color);

			Paint avatarPaint = new Paint();
			avatarPaint.setColor(ColorUtilities.complement(color));
			avatarPaint.setColorFilter(new PorterDuffColorFilter(ColorUtilities.complement(color), PorterDuff.Mode.SRC_ATOP));
			canvas.drawBitmap(avatarBody, 0, 0, avatarPaint);
			canvas.drawBitmap(avatarHead, 0, 0, avatarPaint);
//			Log.d("SIZE", avatar.getByteCount()/1024 + "");
		}

		return avatar;
	}

	public static void loadFacebookProfilePicture(final Map<String, Bitmap> bitmapMap, final String player, final String uid)
	{
		new AsyncTask<String, Void, Bitmap>()
		{
			@Override
			protected Bitmap doInBackground(String... param)
			{
				final Bitmap[] profilePicture = {null};
				Bundle params = new Bundle();
				params.putString("fields", "id,email,gender,cover,picture.type(large)");

				new GraphRequest(
						AccessToken.getCurrentAccessToken(),
						"/" + uid,
						params,
						HttpMethod.GET,
						new GraphRequest.Callback() {
							public void onCompleted(GraphResponse response)
							{
								Log.d("RESPONSE", response.toString());
								try
								{
									JSONObject data = response.getJSONObject();
									if (data.has("picture"))
									{
										String profilePictureURL = data.getJSONObject("picture").getJSONObject("data").getString("url");

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
												catch (IOException e)
												{
													e.printStackTrace();
												}
												return profilePicture[0];
											}

											@Override
											protected void onPostExecute(Bitmap bitmap)
											{
												if (bitmap != null) bitmapMap.put(player, bitmap);
											}

										}.execute(profilePictureURL);
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
				).executeAsync();

				return profilePicture[0];
			}
		}.execute(uid);
	}

	public static ShareLinkContent createShareLinkContent(Activity activity, String gameName, String gameType, String location, List<GamePlayerData> gamePlayerDataList, boolean now)
	{
		String thumbnailUrl = "";
		String bggid = null;
		GamesDbHelper dbHelper = new GamesDbHelper(activity);
		switch (gameType)
		{
			case "b":
			case "boardgame":
				thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, gameName);
				bggid = BoardGameDbUtility.getBggId(dbHelper, gameName);
				break;
			case "r":
			case "rpg":
				thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, gameName);
				bggid = RPGDbUtility.getBggId(dbHelper, gameName);
				break;
			case "v":
			case "videogame":
				thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, gameName);
				bggid = VideoGameDbUtility.getBggId(dbHelper, gameName);
				break;
		}

		String contentUrl = "";
		if (bggid != null && Integer.parseInt(bggid) > 0)
		{
			switch (gameType)
			{
				case "b":
				case "boardgame":
					contentUrl = "https://www.boardgamegeek.com/boardgame/" + bggid;
					break;
				case "r":
				case "rpg":
					contentUrl = "https://www.rpggeek.com/rpg/" + bggid;
					break;
				case "v":
				case "videogame":
					contentUrl = "https://www.videogamegeek.com/videogame/" + bggid;
					break;
			}
		}
		else contentUrl = "https://www.boardgamegeek.com";

		GamePlayerData mainPlayer = null;
		for (int i = 0; i < gamePlayerDataList.size(); i++)
		{
			if (gamePlayerDataList.get(i).getPlayerName().equalsIgnoreCase("master_user"))
			{
				mainPlayer = gamePlayerDataList.remove(i);
				break;
			}
		}

		String contentDescription = "";
		if (mainPlayer.isWin()) contentDescription += "I " + (now ? "just" : "recently") + " won " + gameName;
		else contentDescription += "I " + (now ? "just" : "recently") + " played " + gameName;

		if (!TextUtils.isEmpty(location))
		{
			if (!location.equalsIgnoreCase("online") && !location.equalsIgnoreCase("home")) contentDescription += " at " + location;
			else if (location.equalsIgnoreCase("online")) contentDescription += " " + location.toLowerCase();
			else contentDescription += " at " + location.toLowerCase();
		}

		String with = " with ";
		for (GamePlayerData gamePlayerData : gamePlayerDataList)
		{
			String player = gamePlayerData.getPlayerName();
			if (!player.equalsIgnoreCase("master_user") && !player.equalsIgnoreCase("OTHER") && !player.equalsIgnoreCase(Preferences.getUsername(activity))) with += player + ", ";
		}
		with = with.substring(0, with.length() - 2);
		if (gamePlayerDataList.size() >= 3) with = with.substring(0, with.lastIndexOf(",")) + ", and" + with.substring(with.lastIndexOf(",") + 1);
		else if (gamePlayerDataList.size() == 2) with = with.replace(",", " and");

		if (gamePlayerDataList.size() != 0) contentDescription += with + ".";
		else contentDescription += ".";

		ShareLinkContent feedContent = new ShareLinkContent.Builder()
				.setContentTitle(gameName)
				.setContentDescription(contentDescription)
				.setContentUrl(Uri.parse(contentUrl))
				.setImageUrl(Uri.parse("http://" + thumbnailUrl))
				.build();

		dbHelper.close();
		return feedContent;
	}

	public static ShareContent createShareMediaContent(List<Bitmap> pictures)
	{
		List<SharePhoto> sharePhotos = new ArrayList<>();
		for (Bitmap picture : pictures)
		{
			SharePhoto sharePhoto = new SharePhoto.Builder()
					.setBitmap(picture)
					.build();
			sharePhotos.add(sharePhoto);
		}

		ShareContent feedContent = new SharePhotoContent.Builder()
				.addPhotos(sharePhotos)
				.build();

		return feedContent;
	}

	public static AlertDialog errorDialog(Context context)
	{
		AlertDialog errorDialog = new DialogBuilder(context)
				.setTitle("Error")
				.setMessage("It looks like you are not connected to the Internet.  Please connect and try again later.")
				.setPositiveButton("Okay", null)
				.create();
		return errorDialog;
	}

	public static int dpToPx(Context context, int dp)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return Math.round(dp * scale);
	}

	// TODO Fix this to return based on os version?
	public static class DialogBuilder
	{
		private AlertDialog alertDialog;
		private AlertDialog.Builder builder;

		private View view;
		private TextView title, postiveButton, neutralButton, negativeButton;
		private FlowTextView message;
		private EditText input;
		private ImageView yancey;
		private int backgroundColor, foregroundColor, hintTextColor;

		public DialogBuilder(Context context)
		{
			builder = new AlertDialog.Builder(context);
			view = LayoutInflater.from(context).inflate(R.layout.dialog_simple_alert, null);
			builder.setView(view);

			title = (TextView)view.findViewById(R.id.textview_title);
			message = (FlowTextView)view.findViewById(R.id.textview_message);
			postiveButton = (TextView)view.findViewById(R.id.button_positive);
			neutralButton = (TextView)view.findViewById(R.id.button_neutral);
			negativeButton = (TextView)view.findViewById(R.id.button_negative);
			input = (EditText)view.findViewById(R.id.edittext_input);

			yancey = (ImageView)view.findViewById(R.id.imageview_yancey);
			yancey.setVisibility(View.VISIBLE);

			backgroundColor = Preferences.getBackgroundColor(context);
			foregroundColor = Preferences.getForegroundColor(context);
			hintTextColor = Preferences.getHintTextColor(context);

			view.setBackgroundColor(backgroundColor);

			title.setBackgroundColor(backgroundColor);
			title.setTextColor(foregroundColor);

			message.setBackgroundColor(backgroundColor);
			message.setTextColor(hintTextColor);
			message.setTextSize(dpToPx(context, 14));
			message.setVisibility(View.VISIBLE);

			postiveButton.setBackgroundColor(backgroundColor);
			postiveButton.setTextColor(foregroundColor);

			neutralButton.setBackgroundColor(backgroundColor);
			neutralButton.setTextColor(foregroundColor);

			negativeButton.setBackgroundColor(backgroundColor);
			negativeButton.setTextColor(foregroundColor);

//			input.setBackgroundColor(backgroundColor);
			input.setTextColor(foregroundColor);
			input.setHintTextColor(hintTextColor);
		}

		public DialogBuilder setMessage(CharSequence message)
		{
			this.message.setVisibility(View.VISIBLE);
			this.message.setText(message);
			return this;
		}

		public DialogBuilder setTitle(CharSequence title)
		{
			this.title.setVisibility(View.VISIBLE);
			this.title.setText(title);
			return this;
		}

		public DialogBuilder setPositiveButton(CharSequence text, final View.OnClickListener listener)
		{
			postiveButton.setVisibility(View.VISIBLE);
			postiveButton.setText(text);
			postiveButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (listener != null) listener.onClick(v);
					alertDialog.dismiss();
				}
			});
			return this;
		}

		public DialogBuilder setNeutralButton(CharSequence text, final View.OnClickListener listener)
		{
			neutralButton.setVisibility(View.VISIBLE);
			neutralButton.setText(text);
			neutralButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (listener != null) listener.onClick(v);
					alertDialog.dismiss();
				}
			});
			return this;
		}

		public DialogBuilder setNegativeButton(CharSequence text, final View.OnClickListener listener)
		{
			negativeButton.setVisibility(View.VISIBLE);
			negativeButton.setText(text);
			negativeButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (listener != null) listener.onClick(v);
					alertDialog.dismiss();
				}
			});
			return this;
		}

		public DialogBuilder setLinesOfInput(int lines)
		{
			input.setLines(lines);
			return this;
		}

		public EditText getInput()
		{
			return input;
		}

		public DialogBuilder setHintText(CharSequence hintText)
		{
			input.setVisibility(View.VISIBLE);
			input.setHint(hintText);
			return this;
		}

		public DialogBuilder withYancey(boolean yancey)
		{
			if (yancey) this.yancey.setVisibility((View.VISIBLE));
			else this.yancey.setVisibility(View.GONE);
			return this;
		}

		public DialogBuilder setView(View view)
		{
			((LinearLayout)this.view.findViewById(R.id.linearlayout_custom_view)).addView(view);
			withYancey(false);
			return this;
		}

		public AlertDialog create()
		{
//			withYancey(false);
			alertDialog = builder.create();
			return alertDialog;
		}
	}
}
