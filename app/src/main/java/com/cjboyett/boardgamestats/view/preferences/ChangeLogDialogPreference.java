package com.cjboyett.boardgamestats.view.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cjboyett.boardgamestats.R;
import com.cjboyett.boardgamestats.model.changelog.ChangeLog;

/**
 * Created by Casey on 4/29/2016.
 */
public class ChangeLogDialogPreference extends DialogPreference {
	Context context;

	public ChangeLogDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;
		setPositiveButtonText("Close");
		setNegativeButtonText("");
	}

	@Override
	protected View onCreateDialogView() {
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_fragment_changelog, null);
		((ListView) view.findViewById(R.id.listview_changelog)).setAdapter(new CustomAdapter(context,
																							 R.layout.list_item_changelog_entry,
																							 ChangeLog.entries));
		return view;
	}

	private class CustomAdapter extends ArrayAdapter<ChangeLog.LogEntry> {
		ChangeLog.LogEntry[] entries;

		public CustomAdapter(Context context, int resource, ChangeLog.LogEntry[] objects) {
			super(context, resource, objects);
			entries = objects;
		}

		@Override
		public int getCount() {
			return entries.length;
		}

		@Override
		public ChangeLog.LogEntry getItem(int position) {
			return entries[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null)
				view = LayoutInflater.from(context).inflate(R.layout.list_item_changelog_entry, null);
			else
				view = convertView;

			ChangeLog.LogEntry entry = getItem(position);
			((TextView) view.findViewById(R.id.textview_version)).setText("Version " + entry.version);
			((TextView) view.findViewById(R.id.textview_date)).setText(entry.date.getMonthDayAndYear());
			((TextView) view.findViewById(R.id.textview_log_entry)).setText(entry.entry);

			return view;
		}
	}
}
