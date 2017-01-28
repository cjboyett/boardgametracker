package com.cjboyett.boardgamestats.view.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.cjboyett.boardgamestats.activity.AddGamePlayTabbedActivity;
import com.cjboyett.boardgamestats.data.TempDataManager;

/**
 * Created by Casey on 4/20/2016.
 */
public class WidgetAddGamePlayActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TempDataManager tempDataManager = TempDataManager.getInstance(getApplication());
		tempDataManager.clearTempGamePlayData();
		tempDataManager.clearTempPlayers();

		startActivity(new Intent(this, AddGamePlayTabbedActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
}
