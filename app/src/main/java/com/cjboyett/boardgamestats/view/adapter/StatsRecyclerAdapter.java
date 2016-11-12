package com.cjboyett.boardgamestats.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.model.stats.Statistic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 4/21/2016.
 */
public class StatsRecyclerAdapter extends RecyclerView.Adapter<StatsRecyclerAdapter.ViewHolder>
{
	private List<Statistic> statsList;
	private int backgroundColor, foregroundColor, hintTextColor;

	public StatsRecyclerAdapter(List<Statistic> statsList, int backgroundColor, int foregroundColor, int hintTextColor)
	{
		this.statsList = new ArrayList<>(statsList);
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
		this.hintTextColor = hintTextColor;
	}

	@Override
	public StatsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View linearLayout = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.simple_linear_layout, parent, false);
		ViewHolder viewHolder = new ViewHolder(linearLayout);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position)
	{
		final View view = statsList.get(position).getView();
		if (position > 0)
		{
			((TextView) view.findViewById(R.id.textview_title)).setText(statsList.get(position).getTitle());
			((TextView) view.findViewById(R.id.textview_title)).setTextColor(foregroundColor);
			((TextView) view.findViewById(R.id.textview_stats)).setTextColor(foregroundColor);
			((LinearLayout) holder.view).addView(view);
			holder.view.findViewById(R.id.textview_title).setBackgroundColor(backgroundColor);
			holder.view.findViewById(R.id.textview_stats).setBackgroundColor(backgroundColor);

			((TextView) view.findViewById(R.id.textview_more_stats)).setTextColor(hintTextColor);
			holder.view.findViewById(R.id.textview_more_stats)
			           .setBackgroundColor(backgroundColor);

			((TextView) view.findViewById(R.id.textview_fewer_stats)).setTextColor(hintTextColor);
			holder.view.findViewById(R.id.textview_fewer_stats)
			           .setBackgroundColor(backgroundColor);

			if (statsList.get(position).hasMoreStats())
			{
				view.findViewById(R.id.textview_more_stats).setVisibility(View.VISIBLE);
				view.findViewById(R.id.textview_fewer_stats).setVisibility(View.VISIBLE);
			}
			view.findViewById(R.id.textview_more_stats).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					statsList.get(position).getMoreStats();

/*
					if (statsList.get(position).hasMoreStats()) ((TextView)view.findViewById(R.id.textview_more_stats)).setText("More...");
					else ((TextView)view.findViewById(R.id.textview_more_stats)).setText("");

					if (statsList.get(position).hasFewerStats()) ((TextView)view.findViewById(R.id.textview_fewer_stats)).setText("Less...");
					else ((TextView)view.findViewById(R.id.textview_fewer_stats)).setText("");
*/
				}
			});

			if (statsList.get(position).hasFewerStats()) view.findViewById(R.id.textview_fewer_stats).setVisibility(View.VISIBLE);
			view.findViewById(R.id.textview_fewer_stats).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					statsList.get(position).getFewerStats();

/*
					if (statsList.get(position).hasMoreStats()) ((TextView)view.findViewById(R.id.textview_more_stats)).setText("More...");
					else ((TextView)view.findViewById(R.id.textview_more_stats)).setText("");

					if (statsList.get(position).hasFewerStats()) ((TextView)view.findViewById(R.id.textview_fewer_stats)).setText("Less...");
					else ((TextView)view.findViewById(R.id.textview_fewer_stats)).setText("");
*/
				}
			});
		}
	}

	@Override
	public int getItemCount()
	{
		return statsList.size();
	}

	@Override
	public void onViewRecycled(ViewHolder holder)
	{
		((LinearLayout)holder.view).removeAllViews();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public View view;

		public ViewHolder(View view)
		{
			super(view);
			this.view = view;
		}
	}
}
