package com.cjboyett.boardgamestats.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.model.achievements.Achievement;
import com.cjboyett.boardgamestats.model.achievements.AchievementManager;
import com.cjboyett.boardgamestats.view.adapter.AchievementRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends BaseAdActivity
{
	private View view;

	private AchievementManager achievementManager;

	public AchievementsActivity()
	{
		super("ca-app-pub-1437859753538305/1040750277");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		view = getLayoutInflater().inflate(R.layout.activity_achievements, null);
		setContentView(view);

		generateLayout();
		setColors();
		colorComponents();
	}

	@Override
	void generateLayout()
	{
		achievementManager = new AchievementManager(this);

		List<Achievement> achievementList = new ArrayList<>();
		for (Achievement achievement : achievementManager.getAchievements()) achievementList.add(achievement);

		RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerview_achievements);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(new AchievementRecyclerAdapter(this, achievementList));
	}

	void colorComponents()
	{
		view.setBackgroundColor(backgroundColor);
	}

}
