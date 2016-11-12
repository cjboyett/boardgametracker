package com.cjboyett.boardgamestats.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.model.games.GamePlayData;
import com.cjboyett.boardgamestats.utility.ActivityUtilities;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.data.StringUtilities;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;
import com.cjboyett.boardgamestats.view.adapter.PlayerDataAdapter;

public class GamePlayDataActivity extends BaseAdActivity
{
	private Activity activity = this;
	private View view;

	private FloatingActionButton fab, editFab, deleteFab;
	private boolean fabsShowing = false;

	private int backgroundColor, foregroundColor;

	private GamePlayData gamePlayData;
	private GamesDbHelper dbHelper;

	public GamePlayDataActivity()
	{
		super("");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_game_play_data, null);
		setContentView(view);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		dbHelper = new GamesDbHelper(this);

		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		dbHelper = new GamesDbHelper(this);
		if (ActivityUtilities.databaseChanged(this)) generateLayout();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		dbHelper.close();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (dbHelper != null) dbHelper.close();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		ActivityUtilities.exitDown(this);
	}

	@Override
	protected void setColors()
	{
		if (Preferences.generatePalette(this))
		{
			backgroundColor = Preferences.getGeneratedBackgroundColor(this);
			foregroundColor = Preferences.getGeneratedForegroundColor(this);
		}
		else
		{
			backgroundColor = Preferences.getBackgroundColor(this);
			foregroundColor = Preferences.getForegroundColor(this);
		}
	}

	void colorComponents()
	{
		((TextView) view.findViewById(R.id.textview_game_name)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_game_name).setBackgroundColor(backgroundColor);
		((TextView)view.findViewById(R.id.textview_game_date)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_game_date).setBackgroundColor(backgroundColor);
		((TextView)view.findViewById(R.id.textview_timeplayed)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_timeplayed).setBackgroundColor(backgroundColor);
		((TextView)view.findViewById(R.id.textview_location)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_location).setBackgroundColor(backgroundColor);
		((TextView)view.findViewById(R.id.textview_players)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_players).setBackgroundColor(backgroundColor);
		((TextView)view.findViewById(R.id.textview_notes)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_notes).setBackgroundColor(backgroundColor);
		((TextView)view.findViewById(R.id.textview_game_notes)).setTextColor(foregroundColor);
		view.findViewById(R.id.textview_game_notes).setBackgroundColor(backgroundColor);
		view.setBackgroundColor(backgroundColor);

		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.relativelayout_game_play_top), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.listview_players), foregroundColor);
		ViewUtilities.tintLayoutBackground(view.findViewById(R.id.linearlayout_notes), foregroundColor);
	}

	void generateLayout()
	{
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				toggleFabs();
			}
		});

		editFab = (FloatingActionButton) findViewById(R.id.fab_edit_game_play);
		editFab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				toggleFabs();
				long gamePlayId = getIntent().getLongExtra("ID", -1l);
				startActivity(new Intent(view.getContext(), AddGamePlayTabbedActivity.class)
						              .putExtra("GAME", gamePlayData.getGame().getName())
						              .putExtra("TYPE", getIntent().getStringExtra("TYPE"))
						              .putExtra("ID", gamePlayId)
						              .putExtra("EXIT", "DOWN"));
				ActivityUtilities.exitUp(activity);
			}
		});

		deleteFab = (FloatingActionButton) findViewById(R.id.fab_delete_game_play);
		deleteFab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				final View finalView = view;
				toggleFabs();
				AlertDialog dialog = new ViewUtilities.DialogBuilder(view.getContext())
						.setTitle("Delete Game Play")
						.setMessage("Are you sure you want to delete this game play?")
						.setPositiveButton("Delete", new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								String gameType = getIntent().getStringExtra("TYPE");
								switch (gameType)
								{
									case "b":
										BoardGameDbUtility.deleteGamePlay(dbHelper, getIntent().getLongExtra("ID", -1l));
										break;
									case "r":
										RPGDbUtility.deleteGamePlay(dbHelper, getIntent().getLongExtra("ID", -1l));
										break;
									case "v":
										VideoGameDbUtility.deleteGamePlay(dbHelper, getIntent().getLongExtra("ID", -1l));
										break;
								}
								ActivityUtilities.setDatabaseChanged(activity, true);
								onBackPressed();
							}
						})
						.setNegativeButton("Cancel", null)
						.create();
				dialog.show();
			}
		});

		editFab.hide();
		deleteFab.hide();

		findViewById(R.id.fab_invisible).setScaleY(0.7f);
		findViewById(R.id.fab_invisible).setScaleX(0.7f);

		String gameType = getIntent().getStringExtra("TYPE");
		switch (gameType)
		{
			case "b":
				gamePlayData = BoardGameDbUtility.getGamePlay(dbHelper, getIntent().getLongExtra("ID", -1l));
				break;
			case "r":
				gamePlayData = RPGDbUtility.getGamePlay(dbHelper, getIntent().getLongExtra("ID", -1l));
				break;
			case "v":
				gamePlayData = VideoGameDbUtility.getGamePlay(dbHelper, getIntent().getLongExtra("ID", -1l));
				break;
		}

		((TextView)view.findViewById(R.id.textview_game_name)).setText(gamePlayData.getGame().getName());
		((TextView)view.findViewById(R.id.textview_game_date)).setText(gamePlayData.getDate().toString());

		if (gamePlayData.getTimePlayed() > 0)
			((TextView)view.findViewById(R.id.textview_timeplayed))
					.setText(StringUtilities.convertMinutes(gamePlayData.getTimePlayed()));
		else view.findViewById(R.id.textview_timeplayed).setVisibility(View.GONE);

		String location = gamePlayData.getLocation();
		if (location != null && !location.equals(""))
			((TextView)view.findViewById(R.id.textview_location)).setText(location);
		else view.findViewById(R.id.textview_location).setVisibility(View.GONE);
		((TextView)view.findViewById(R.id.textview_game_notes)).setText(gamePlayData.getNotes() + "\n\n\n");

		((ListView)view.findViewById(R.id.listview_players))
				.setAdapter(new PlayerDataAdapter(this, gamePlayData.getOtherPlayers().values(), !getIntent().getStringExtra("TYPE").equals("r")));

		try
		{
			Bitmap thumbnailBitmap = getIntent().getParcelableExtra("BITMAP");
			((ImageView)findViewById(R.id.imageview_avatar)).setImageBitmap(thumbnailBitmap);
		}
		catch (Exception e)
		{
		}

		ActivityUtilities.setDatabaseChanged(this, false);
	}

	private void toggleFabs()
	{
		if (fabsShowing)
		{
			fab.setScaleY(1);
			editFab.hide();
			deleteFab.hide();
			fabsShowing = false;
		}
		else
		{
			fab.setScaleY(-1f);
			editFab.show();
			deleteFab.show();
			fabsShowing = true;
		}
	}

}
