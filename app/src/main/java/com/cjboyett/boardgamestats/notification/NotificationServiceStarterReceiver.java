package com.cjboyett.boardgamestats.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Casey on 5/4/2016.
 */
public class NotificationServiceStarterReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Change reminder to keep persistent information on when it was last set
		// That way when the time is changed or the phone is rebooted this can reset the
		// reminder to still show at the correct time.
//		NotificationEventReceiver.setupReminder(context);
	}
}
