package com.cjboyett.boardgamestats.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.model.achievements.Achievement;
import com.cjboyett.boardgamestats.utility.Preferences;
import com.cjboyett.boardgamestats.utility.view.ViewUtilities;

import java.util.List;

/**
 * Created by Casey on 9/17/2016.
 */
public class AchievementRecyclerAdapter extends RecyclerView.Adapter<AchievementRecyclerAdapter.ViewHolder> {
	private List<Achievement> achievementList;
	private Context context;

	private static int backgroundColor, foregroundColor;

	public AchievementRecyclerAdapter(Context context, List<Achievement> achievementList) {
		this.context = context;
		this.achievementList = achievementList;

		backgroundColor = Preferences.getBackgroundColor(context);
		foregroundColor = Preferences.getForegroundColor(context);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item_achievement, null);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.setAchievement(achievementList.get(position));
	}

	@Override
	public int getItemCount() {
		return achievementList.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private View view;

		public ViewHolder(View itemView) {
			super(itemView);
			view = itemView;
		}

		public void setAchievement(Achievement achievement) {
			((TextView) view.findViewById(R.id.textview_achievement_title)).setText(achievement.getTitle());
			((TextView) view.findViewById(R.id.textview_achievement_description)).setText(achievement.getDescription());
			((TextView) view.findViewById(R.id.textview_achievement_experience)).setText(
					achievement.getExperience() + " Exp");

			((TextView) view.findViewById(R.id.textview_achievement_title)).setTextColor(foregroundColor);
			((TextView) view.findViewById(R.id.textview_achievement_description)).setTextColor(foregroundColor);
			((TextView) view.findViewById(R.id.textview_achievement_experience)).setTextColor(foregroundColor);

			ViewUtilities.tintLayoutBackground(view, foregroundColor);

			if (achievement.isCompleted())
				ViewUtilities.tintImageView((AppCompatImageView) view.findViewById(R.id.imageview_achievement_icon),
											Color.rgb(255, 215, 0));
			else ViewUtilities.tintImageView((AppCompatImageView) view.findViewById(R.id.imageview_achievement_icon),
											 Color.DKGRAY);
		}

		@Override
		public void onClick(View v) {

		}
	}
}
