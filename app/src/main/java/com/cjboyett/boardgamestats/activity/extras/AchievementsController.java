package com.cjboyett.boardgamestats.activity.extras;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.conductor.base.BaseController;
import com.cjboyett.boardgamestats.model.achievements.Achievement;
import com.cjboyett.boardgamestats.model.achievements.AchievementManager;
import com.cjboyett.boardgamestats.view.adapter.AchievementRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AchievementsController extends BaseController {
	private View view;

	private AchievementManager achievementManager;

	@NonNull
	@Override
	protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
		view = inflater.inflate(R.layout.content_achievements, container, false);
		return view;
	}

	@Override
	protected void onAttach(@NonNull View view) {
		super.onAttach(view);
		generateLayout();
		setColors();
		colorComponents();

	}

	protected void generateLayout() {
		achievementManager = new AchievementManager(getActivity());

		List<Achievement> achievementList = new ArrayList<>();
		for (Achievement achievement : achievementManager.getAchievements()) achievementList.add(achievement);

		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_achievements);
		LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(new AchievementRecyclerAdapter(getActivity(), achievementList));
	}

	protected void colorComponents() {
		view.setBackgroundColor(backgroundColor);
	}
}
