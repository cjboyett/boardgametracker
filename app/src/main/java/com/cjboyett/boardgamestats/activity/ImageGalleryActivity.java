package com.cjboyett.boardgamestats.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.data.DataManager;
import com.cjboyett.boardgamestats.data.games.GamesDbHelper;
import com.cjboyett.boardgamestats.data.games.board.BoardGameDbUtility;
import com.cjboyett.boardgamestats.data.games.rpg.RPGDbUtility;
import com.cjboyett.boardgamestats.data.games.video.VideoGameDbUtility;
import com.cjboyett.boardgamestats.view.adapter.ImageGalleryRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ImageGalleryActivity extends AppCompatActivity {
	private Activity activity = this;
	private View view;

	private RecyclerView recyclerView;
	private GridLayoutManager layoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_image_gallery, null);
		setContentView(view);

		generateLayout();
	}

	private void generateLayout() {
		recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_image_gallery);
		recyclerView.setHasFixedSize(true);

		layoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return position % 3 == 1 ? 2 : 1;
			}
		});
		recyclerView.setLayoutManager(layoutManager);

		final GamesDbHelper dbHelper = new GamesDbHelper(this);
		DataManager dataManager = DataManager.getInstance(getApplication());
		final List<String> games = dataManager.getAllGamesCombined();
		;
		final List<String> keys = new ArrayList<>();
		new AsyncTask<String, Void, Void>() {
			@Override
			protected Void doInBackground(String... params) {
				for (String game : games)

				{
					int l = game.length();
					String gameType = game.substring(l - 1);
					game = game.substring(0, l - 2);
					String key = "";
					switch (gameType) {
						case "b":
							key = BoardGameDbUtility.getThumbnailUrl(dbHelper, game);
							break;
						case "r":
							key = RPGDbUtility.getThumbnailUrl(dbHelper, game);
							break;
						case "v":
							key = VideoGameDbUtility.getThumbnailUrl(dbHelper, game);
							break;
					}
					if (!TextUtils.isEmpty(key) && key.lastIndexOf("/") != -1) {
						key = key.substring(key.lastIndexOf("/") + 1);
						keys.add(key);
						Timber.d(key);
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				ImageGalleryRecyclerAdapter recyclerAdapter = new ImageGalleryRecyclerAdapter(activity, keys);
				recyclerView.setAdapter(recyclerAdapter);
			}
		}.execute("");
	}
}
