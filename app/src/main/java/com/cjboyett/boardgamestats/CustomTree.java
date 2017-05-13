package com.cjboyett.boardgamestats;

import timber.log.Timber;

public class CustomTree extends Timber.DebugTree {
	private static final int MAX_LOG_LENGTH = 4000;
	@Override
	protected String createStackElementTag(StackTraceElement element) {
		return super.createStackElementTag(element) + "(" + element.getLineNumber() + ")";
	}
}
