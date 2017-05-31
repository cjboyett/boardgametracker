package com.cjboyett.boardgamestats.conductor.collection;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.model.games.Game;
import com.cjboyett.boardgamestats.model.games.board.SimpleGame;
import com.cjboyett.boardgamestats.utility.Preferences;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GameDataController2 extends BaseController implements GameDataView {
	@BindView(R.id.textview_game_name)
	TextView textGameName;

	@BindView(R.id.imageview_thumbnail)
	ImageView imageThumbnail;

	@BindView(R.id.textview_description_label)
	TextView getTextGameDescriptionLabel;

	@BindView(R.id.textview_game_description)
	TextView textGameDescription;

	@BindView(R.id.textview_extras_label)
	TextView getTextGameExtrasLabel;

	@BindView(R.id.listview_game_extras)
	ListView listGameExtras;

	private View view;
	private GameDataPresenter presenter;

	private String gameName, gameType, thumbnailUrl, gameId;
	private Game game;

	private int backgroundColor, foregroundColor, hintTextColor;


	public GameDataController2() {
	}

	public GameDataController2(String gameName, String gameType, String thumbnailUrl, String gameId) {
		this.gameName = gameName;
		this.gameType = gameType;
		this.thumbnailUrl = thumbnailUrl;
		this.gameId = gameId;
	}

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.activity_game_data2, container, false);
		ButterKnife.bind(this, view);
		presenter = new GameDataPresenter(view.getContext());
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		presenter.attachView(this);
		setColors();
		colorComponents();
		presenter.loadGame(gameId);
	}

	@Override
	protected void onDetach(@NonNull View view) {
		presenter.detachView();
		getToolbarImage().setImageBitmap(null);
		super.onDetach(view);
	}

	@Override
	protected void setColors() {
		if (Preferences.generatePalette(getActivity())) {
			foregroundColor = Preferences.getGeneratedForegroundColor(getActivity());
			backgroundColor = Preferences.getGeneratedBackgroundColor(getActivity());
			hintTextColor = Preferences.getHintTextColor(getActivity());
		} else {
			foregroundColor = Preferences.getForegroundColor(getActivity());
			backgroundColor = Preferences.getBackgroundColor(getActivity());
			hintTextColor = Preferences.getHintTextColor(getActivity());
		}
	}

	private void colorComponents() {
		view.setBackgroundColor(backgroundColor);
		textGameName.setTextColor(foregroundColor);
		getTextGameDescriptionLabel.setTextColor(foregroundColor);
		textGameDescription.setTextColor(foregroundColor);
		getTextGameExtrasLabel.setTextColor(foregroundColor);
	}

	@Override
	public void populateView(SimpleGame simpleGame) {
		textGameName.setText(simpleGame.getPrimaryName());
		textGameDescription.setText(simpleGame.getDescription());
		presenter.loadThumbnail(simpleGame.getThumbnail());
		presenter.loadImage(simpleGame.getImage());
	}

	@Override
	public void populateThumbnail(Bitmap thumbnail) {
		imageThumbnail.setImageBitmap(thumbnail);
	}

	@Override
	public void populateToolbarImage(Bitmap image) {
		getToolbarImage().setImageBitmap(image);
	}
}
