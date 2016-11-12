package com.cjboyett.boardgamestats.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Casey on 4/22/2016.
 */
public class ChooseThemeAdapter extends BaseAdapter
{
	private List<Integer[]> palettes;

	public ChooseThemeAdapter(List<Integer[]> palettes)
	{
		this.palettes = new ArrayList<>(palettes);
	}

	@Override
	public int getCount()
	{
		return palettes.size();
	}

	@Override
	public Object getItem(int position)
	{
		return palettes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view;
		if (convertView == null)
		{
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item_theme, null);
		}
		else view = convertView;

		Integer[] palette = palettes.get(position);
		view.findViewById(R.id.textview_sample_background).setBackgroundColor(palette[0]);
		view.findViewById(R.id.textview_sample_foreground).setBackgroundColor(palette[1]);
		view.findViewById(R.id.textview_sample_text).setBackgroundColor(palette[0]);
		((TextView)view.findViewById(R.id.textview_sample_text)).setTextColor(palette[1]);

		return view;
	}
}
