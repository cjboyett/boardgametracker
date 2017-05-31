package com.cjboyett.boardgamestats.conductor.collection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.cjboyett.boardgamestats.activity.base.BasePresenter;
import com.cjboyett.boardgamestats.model.games.board.Link;
import com.cjboyett.boardgamestats.model.games.board.Poll;
import com.cjboyett.boardgamestats.model.games.board.Result;
import com.cjboyett.boardgamestats.model.games.board.Results;
import com.cjboyett.boardgamestats.model.games.board.SimpleGame;
import com.cjboyett.boardgamestats.model.games.board.SimpleGameList;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import timber.log.Timber;

public class GameDataPresenter extends BasePresenter<GameDataView> {
	Context context;

	public GameDataPresenter(Context context) {
		this.context = context;
	}

	public void loadGame(String id) {
		Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.boardgamegeek.com/xmlapi2/")
												  .client(new OkHttpClient())
												  .addConverterFactory(
														  SimpleXmlConverterFactory.create())
												  .build();
		BoardgameClient boardgameClient = retrofit.create(BoardgameClient.class);
		Call<SimpleGameList> call = boardgameClient.getSimpleGame(id);
		call.enqueue(new Callback<SimpleGameList>() {
			@Override
			public void onResponse(Call<SimpleGameList> call, Response<SimpleGameList> response) {
				SimpleGame simpleGame = response.body().getSimpleGameList().get(0);
				getView().populateView(simpleGame);
			}

			@Override
			public void onFailure(Call<SimpleGameList> call, Throwable t) {
				Timber.e(t);
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	public void loadThumbnail(String thumbnailUrl) {
		new AsyncTask<String, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(String... strings) {
				Bitmap bitmap = null;
				try {
					bitmap = Picasso.with(context).load(thumbnailUrl).get();
				} catch (IOException e) {
					Timber.e(e);
				}
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {
				getView().populateThumbnail(bitmap);
			}
		}.execute(thumbnailUrl);
	}

	@SuppressLint("StaticFieldLeak")
	public void loadImage(String imageUrl) {
		new AsyncTask<String, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(String... strings) {
				Bitmap bitmap = null;
				try {
					bitmap = Picasso.with(context).load(imageUrl).resize(512, 512).onlyScaleDown().get();
				} catch (IOException e) {
					Timber.e(e);
				}
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {
				getView().populateToolbarImage(bitmap);
			}
		}.execute(imageUrl);
	}

	public interface BoardgameClient {
		@GET("thing?boardgame")
		Call<SimpleGameList> getSimpleGame(
				@Query("id") String id
		);
	}
}
