package com.cjboyett.boardgamestats.utility.data;

import android.content.Context;
import android.os.SystemClock;

import com.cjboyett.boardgamestats.data.TempDataManager;

import java.util.List;

import timber.log.Timber;

public class TimerUtility {
	public static String getElapsedTime(Context context) {
		TempDataManager tempDataManager = TempDataManager.getInstance(context);
		List<Long> times = tempDataManager.getTimer().toList();
		if (times.isEmpty() || times.get(0) == 0) return "00:00";
		else {
			long timerBase = times.get(0);
			long startTime = times.get(1);
			long stopTime = times.get(2);
			long diff = times.get(3);
			if (stopTime > startTime)
				return computeElapsedTimeString(diff);
/*
			else if (stopTime == 0)
				return computeElapsedTimeString(SystemClock.elapsedRealtime() - startTime);
*/
			else
				return computeElapsedTimeString(SystemClock.elapsedRealtime() - startTime + diff);
		}
	}

	private static String computeElapsedTimeString(long elapsedTime) {
		Timber.d(elapsedTime + "");
		long hours = elapsedTime / (60 * 60 * 1000);
		elapsedTime -= hours * (60 * 60 * 1000);
		long minutes = elapsedTime / (60 * 1000);
		elapsedTime -= minutes * (60 * 1000);
		long seconds = elapsedTime / 1000;

		String time = hours > 0 ? hours + ":" : "";
		time += minutes > 9 ? minutes + ":" : "0" + minutes + ":";
		time += seconds > 9 ? seconds : "0" + seconds;
		return time;
	}
}
