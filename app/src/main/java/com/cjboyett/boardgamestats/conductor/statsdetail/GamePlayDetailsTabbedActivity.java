package com.cjboyett.boardgamestats.conductor.statsdetail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.activity.addgameplay.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.data.PlayersDbUtility;
import com.cjboyett.boardgamestats.data.TempDataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.model.games.GamePlayerData;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ColorUtilities;
import com.cjboyett.boardgamestats.utility.view.ImageController;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.PlayerDataAdapter;
import com.cjboyett.boardgamestats.view.animation.ZoomOutPageTransformer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class GamePlayDetailsTabbedActivity extends AppCompatActivity {
	private Activity activity = this;
	private SectionsPagerAdapter mSectionsPagerAdapter;

	private ViewPager mViewPager;

//	private ImageController imageController;

	private List<String> gameTypes, gameNames;
	private List<Long> gamePlayIds;
//	private GamesDbHelper dbHelper;

	private FloatingActionMenu fabMenu;
	private FloatingActionButton editFab, deleteFab;

	private AdView googleAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View view = getLayoutInflater().inflate(R.layout.activity_game_play_details_tabbed, null);
		setContentView(view);
		view.setBackgroundColor(ColorUtilities.darken(Preferences.getBackgroundColor(this)));

		final ImageController imageController = new ImageController(this).setDirectoryName("thumbnails");
		final GamesDbHelper dbHelper = new GamesDbHelper(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				if (fabMenu.isOpened()) fabMenu.close(true);
			}

			@Override
			public void onPageSelected(int position) {
				if (fabMenu.isOpened()) fabMenu.close(true);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (fabMenu.isOpened()) fabMenu.close(true);
			}
		});

		fabMenu = (FloatingActionMenu) findViewById(R.id.floating_menu);

		view.findViewById(R.id.fab_share).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String gameType = gameTypes.get(mViewPager.getCurrentItem());
				String gameName = gameNames.get(mViewPager.getCurrentItem());
				long id = gamePlayIds.get(mViewPager.getCurrentItem());
				GamePlayData gamePlayData = null;
				String location = "";
				List<GamePlayerData> gamePlayerDataList;
				switch (gameType) {
					case "b":
						gamePlayData = BoardGameDbUtility.getGamePlay(dbHelper, id);
						break;
					case "r":
						gamePlayData = RPGDbUtility.getGamePlay(dbHelper, id);
						break;
					case "v":
						gamePlayData = VideoGameDbUtility.getGamePlay(dbHelper, id);
						break;
				}

				if (gamePlayData != null) {
					location = gamePlayData.getLocation();
					gamePlayerDataList = new ArrayList<>(gamePlayData.getOtherPlayers()
																	 .values());

					ShareDialog shareDialog = new ShareDialog(activity);
					if (ShareDialog.canShow(ShareLinkContent.class)) {
						ShareLinkContent feedContent =
								ViewUtilities.createShareLinkContent(activity,
																	 gameName,
																	 gameType,
																	 location,
																	 gamePlayerDataList,
																	 false);
						shareDialog.show(feedContent, ShareDialog.Mode.AUTOMATIC);
					}
				}
			}
		});

		editFab = (FloatingActionButton) findViewById(R.id.fab_edit);
		editFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				fabMenu.close(true);
				TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
				tempDataManager.initialize();
				int position = mViewPager.getCurrentItem();
				startActivity(new Intent(view.getContext(), AddGamePlayTabbedActivity.class)
									  .putExtra("GAME", gameNames.get(position))
									  .putExtra("TYPE", gameTypes.get(position))
									  .putExtra("ID", gamePlayIds.get(position))
									  .putExtra("EXIT", "DOWN"));
				ActivityUtilities.exitUp(activity);
			}
		});

		deleteFab = (FloatingActionButton) findViewById(R.id.fab_delete);
		deleteFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final View finalView = view;
				fabMenu.close(true);
				AlertDialog dialog = new ViewUtilities.DialogBuilder(view.getContext())
						.setTitle("Delete Game Play")
						.setMessage("Are you sure you want to delete this game play?")
						.setPositiveButton("Delete", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								int position = mViewPager.getCurrentItem();
								long id = gamePlayIds.remove(position);
								String gameType = gameTypes.remove(position);
								gameNames.remove(position);

								GamePlayData playData = null;

								switch (gameType) {
									case "b":
										playData = BoardGameDbUtility.getGamePlay(dbHelper, id);
										BoardGameDbUtility.deleteGamePlay(dbHelper, id);
										break;
									case "r":
										playData = RPGDbUtility.getGamePlay(dbHelper, id);
										RPGDbUtility.deleteGamePlay(dbHelper, id);
										break;
									case "v":
										playData = VideoGameDbUtility.getGamePlay(dbHelper, id);
										VideoGameDbUtility.deleteGamePlay(dbHelper, id);
										break;
								}

								if (playData != null)
									for (String player : playData.getOtherPlayers().keySet())
										if (!player.equalsIgnoreCase("master_user"))
											PlayersDbUtility.generateNewPlayer(dbHelper, player);

								ActivityUtilities.setDatabaseChanged(activity, true);
								if (gamePlayIds.isEmpty()) onBackPressed();
								else {
									if (position > 0) mViewPager.setCurrentItem(position - 1, true);
									mSectionsPagerAdapter.setCount(gamePlayIds.size());
									mSectionsPagerAdapter.notifyDataSetChanged();
								}
							}
						})
						.setNegativeButton("Cancel", null)
						.create();
				dialog.show();
			}
		});

		gamePlayIds = new ArrayList<>();
		long[] ids = getIntent().getLongArrayExtra("IDS");
		for (long id : ids) gamePlayIds.add(id);
		gameNames = getIntent().getStringArrayListExtra("NAMES");
		gameTypes = getIntent().getStringArrayListExtra("TYPES");

		mViewPager.setCurrentItem(getIntent().getIntExtra("POSITION", 1), true);
		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
		mViewPager.setOffscreenPageLimit(2);

		int backgroundColor = Preferences.getBackgroundColor(activity);
		int foregroundColor = Preferences.getForegroundColor(activity);
		boolean oneGame;

		if (Preferences.generatePalette(activity)) {
			oneGame = true;

			if (gameNames.size() > 1) {
				for (int i = 0; i < gameNames.size() - 1; i++) {
					for (int j = i + 1; j < gameNames.size(); j++) {
						if (!gameNames.get(i).equals(gameNames.get(j))) {
							oneGame = false;
							i = gameNames.size();
							break;
						}
					}
				}
			}

			if (oneGame && gameNames.size() > 0) {
				String thumbnailUrl = null;
				String gameType = gameTypes.get(0);
				switch (gameType) {
					case "b":
						thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, gameNames.get(0));
						break;
					case "r":
						thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, gameNames.get(0));
						break;
					case "v":
						thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, gameNames.get(0));
						break;
				}
				if (thumbnailUrl != null) {
					Timber.d(thumbnailUrl);
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

		view.setBackgroundColor(ColorUtilities.darken(backgroundColor));

		if (Preferences.showAds(this)) {
			final RelativeLayout adViewContainer = (RelativeLayout) view.findViewById(R.id.ad_container);

			googleAdView = new AdView(this);
			googleAdView.setAdSize(AdSize.SMART_BANNER);
			googleAdView.setAdUnitId("ca-app-pub-1437859753538305/3524647072");
			googleAdView.setVisibility(View.GONE);
			adViewContainer.addView(googleAdView);
			RelativeLayout.LayoutParams layoutParams =
					new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
													ViewGroup.LayoutParams.WRAP_CONTENT);
			googleAdView.setLayoutParams(layoutParams);
			adViewContainer.setGravity(RelativeLayout.CENTER_IN_PARENT);

			AdRequest adRequest = new AdRequest.Builder()
//				    .addTestDevice("A7AC3AA36B47EF166BA856BD3C6009BF")
.build();
			googleAdView.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					super.onAdLoaded();
					googleAdView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAdFailedToLoad(int errorCode) {
					super.onAdFailedToLoad(errorCode);
					googleAdView.setVisibility(View.GONE);
				}
			});
			googleAdView.loadAd(adRequest);

			dbHelper.close();
			imageController.close();
		}
	}

	@Override
	protected void onDestroy() {
//		if (dbHelper != null) dbHelper.close();
		if (googleAdView != null) googleAdView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (googleAdView != null) googleAdView.pause();
//		dbHelper.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		dbHelper = new GamesDbHelper(this);
		if (googleAdView != null) googleAdView.pause();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ActivityUtilities.exitDown(this);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private int count = -1;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return GamePlayDataFragment.newInstance(gameTypes.get(position), gamePlayIds.get(position));
		}

		@Override
		public int getCount() {
			if (count == -1)
				count = getIntent().getIntExtra("COUNT", 1);
			return count;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "SECTION " + position;
		}

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public long getItemId(int position) {
			String gameType = gameTypes.get(position);
			switch (gameType) {
				case "b":
					return 3 * gamePlayIds.get(position);
				case "r":
					return 3 * gamePlayIds.get(position) + 1;
				case "v":
					return 3 * gamePlayIds.get(position) + 2;
			}
			return position;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	public static class GamePlayDataFragment extends Fragment {
		private static final String GAME_TYPE = "game_type";
		private static final String GAME_PLAY_ID = "game_play_id";

		private View view;

		private int backgroundColor;
		private int foregroundColor;

		private GamePlayData gamePlayData;
		private String gameName, gameType;

		GamesDbHelper dbHelper;

		public static GamePlayDataFragment newInstance(String gameType, long gamePlayId) {
			GamePlayDataFragment fragment = new GamePlayDataFragment();
			Bundle args = new Bundle();
			args.putString(GAME_TYPE, gameType);
			args.putLong(GAME_PLAY_ID, gamePlayId);
			fragment.setArguments(args);
			return fragment;
		}

		public GamePlayDataFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			view = inflater.inflate(R.layout.content_game_play_data, container, false);
			Bundle args = getArguments();
			generateLayout(args.getString(GAME_TYPE), args.getLong(GAME_PLAY_ID));
			return view;
		}

		@Override
		public void onResume() {
			super.onResume();
			if (ActivityUtilities.databaseChanged(getContext())) {
				Bundle args = getArguments();
				generateLayout(args.getString(GAME_TYPE), args.getLong(GAME_PLAY_ID));
			}
		}

		private void generateLayout(String gameType, long id) {
			dbHelper = new GamesDbHelper(getContext());
			ImageController imageController = new ImageController(getContext());

			Timber.d(gameType + " " + id);
			gamePlayData = null;
			this.gameType = gameType;
			switch (gameType) {
				case "b":
					gamePlayData = BoardGameDbUtility.getGamePlay(dbHelper, id);
					break;
				case "r":
					gamePlayData = RPGDbUtility.getGamePlay(dbHelper, id);
					break;
				case "v":
					gamePlayData = VideoGameDbUtility.getGamePlay(dbHelper, id);
					break;
			}
			gameName = gamePlayData.getGame().getName();

			((TextView) view.findViewById(R.id.textview_game_name)).setText(gamePlayData.getGame().getName());
			((TextView) view.findViewById(R.id.textview_game_date)).setText(gamePlayData.getDate()
																						.useShortMonth(true)
																						.toString());

			if (gamePlayData.getTimePlayed() > 0)
				((TextView) view.findViewById(R.id.textview_timeplayed))
						.setText(StringUtilities.convertMinutes(gamePlayData.getTimePlayed()));
			else view.findViewById(R.id.textview_timeplayed).setVisibility(View.GONE);

			String location = gamePlayData.getLocation();
			if (location != null && !location.equals(""))
				((TextView) view.findViewById(R.id.textview_location)).setText(location);
			else view.findViewById(R.id.textview_location).setVisibility(View.GONE);
			((TextView) view.findViewById(R.id.textview_game_notes)).setText(gamePlayData.getNotes() + "\n\n\n\n");

			try {
				Bitmap thumbnailBitmap;
				String thumbnailUrl = "";
				String game = gamePlayData.getGame().getName();
				switch (gameType) {
					case "b":
						thumbnailUrl = "http://" + BoardGameDbUtility.getThumbnailUrl(dbHelper, game);
						break;
					case "r":
						thumbnailUrl = "http://" + RPGDbUtility.getThumbnailUrl(dbHelper, game);
						break;
					case "v":
						thumbnailUrl = "http://" + VideoGameDbUtility.getThumbnailUrl(dbHelper, game);
						break;
				}

				thumbnailBitmap =
						imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1)).load();

				((ImageView) view.findViewById(R.id.imageview_avatar)).setImageBitmap(thumbnailBitmap);
			} catch (Exception e) {
			}

			setColors();
			colorComponents();

			dbHelper.close();
			imageController.close();
		}

		private void setColors() {
			if (Preferences.generatePalette(getActivity())) {
				String thumbnailUrl = null;
				switch (gameType) {
					case "b":
						thumbnailUrl = BoardGameDbUtility.getThumbnailUrl(dbHelper, gameName);
						break;
					case "r":
						thumbnailUrl = RPGDbUtility.getThumbnailUrl(dbHelper, gameName);
						break;
					case "v":
						thumbnailUrl = VideoGameDbUtility.getThumbnailUrl(dbHelper, gameName);
						break;
				}
				if (thumbnailUrl != null) {
					Timber.d(thumbnailUrl);
					ImageController imageController = new ImageController(getActivity()).setDirectoryName("thumbnails");
					Bitmap thumbnail =
							imageController.setFileName(thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1))
										   .load();
					imageController.close();
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
					if (Preferences.lightUI(getActivity()))
						swatch = swatchList.get(swatchList.size() - 1);
					else swatch = swatchList.get(0);

					if (thumbnail != null) {
						backgroundColor = swatch.getRgb();
						foregroundColor = swatch.getBodyTextColor();
					} else {
						backgroundColor = Preferences.getBackgroundColor(getContext());
						foregroundColor = Preferences.getForegroundColor(getContext());
					}
				} else {
					backgroundColor = Preferences.getBackgroundColor(getContext());
					foregroundColor = Preferences.getForegroundColor(getContext());
				}
			} else {
				backgroundColor = Preferences.getBackgroundColor(getContext());
				foregroundColor = Preferences.getForegroundColor(getContext());
			}
		}

		private void colorComponents() {
			((TextView) view.findViewById(R.id.textview_game_name)).setTextColor(foregroundColor);
			view.findViewById(R.id.textview_game_name).setBackgroundColor(backgroundColor);
			((TextView) view.findViewById(R.id.textview_game_date)).setTextColor(foregroundColor);
			view.findViewById(R.id.textview_game_date).setBackgroundColor(backgroundColor);
			((TextView) view.findViewById(R.id.textview_timeplayed)).setTextColor(foregroundColor);
			view.findViewById(R.id.textview_timeplayed).setBackgroundColor(backgroundColor);
			((TextView) view.findViewById(R.id.textview_location)).setTextColor(foregroundColor);
			view.findViewById(R.id.textview_location).setBackgroundColor(backgroundColor);
			((TextView) view.findViewById(R.id.textview_players)).setTextColor(foregroundColor);
			view.findViewById(R.id.textview_players).setBackgroundColor(backgroundColor);
			((TextView) view.findViewById(R.id.textview_notes)).setTextColor(foregroundColor);
			view.findViewById(R.id.textview_notes).setBackgroundColor(backgroundColor);
			((TextView) view.findViewById(R.id.textview_game_notes)).setTextColor(foregroundColor);
			view.findViewById(R.id.textview_game_notes).setBackgroundColor(backgroundColor);
			view.setBackgroundColor(backgroundColor);

			ViewUtilities.tintLayoutBackground(view.findViewById(R.id.relativelayout_game_play_top), foregroundColor);
			ViewUtilities.tintLayoutBackground(view.findViewById(R.id.listview_players), foregroundColor);
			ViewUtilities.tintLayoutBackground(view.findViewById(R.id.linearlayout_notes), foregroundColor);

			((ListView) view.findViewById(R.id.listview_players))
					.setAdapter(new PlayerDataAdapter(getActivity(),
													  gamePlayData.getOtherPlayers().values(),
													  !gameType.equals("r"),
													  backgroundColor,
													  foregroundColor));

		}
	}
}
