package com.cjboyett.boardgamestats.model;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Timer {
	private long timerBase;
	private long lastStartTime;
	private long lastStopTime;
	private long diff;

	public Timer() {
		this(0L, 0L, 0L, 0L);
	}

	public Timer(List<Long> times) {
		this(times.get(0), times.get(1), times.get(2), times.get(3));
	}

	public Timer(long timerBase, long lastStartTime, long lastStopTime, long diff) {
		this.timerBase = timerBase;
		this.lastStartTime = lastStartTime;
		this.lastStopTime = lastStopTime;
		this.diff = diff;
	}

	public long getTimerBase() {
		return timerBase;
	}

	public void setTimerBase(long timerBase) {
		this.timerBase = timerBase;
	}

	public long getLastStartTime() {
		return lastStartTime;
	}

	public void setLastStartTime(long lastStartTime) {
		this.lastStartTime = lastStartTime;
	}

	public long getLastStopTime() {
		return lastStopTime;
	}

	public void setLastStopTime(long lastStopTime) {
		this.lastStopTime = lastStopTime;
	}

	public long getDiff() {
		return diff;
	}

	public void setDiff(long diff) {
		this.diff = diff;
	}

	public void addToDiff(long dt) {
		diff += dt;
	}

	public void adjustDiffFromStopAndStart() {
		addToDiff(lastStopTime - lastStartTime);
	}

	public boolean isTimerRunning() {
		return lastStartTime > lastStopTime;
	}

	public List<Long> toList() {
		List<Long> list = new ArrayList<>();
		list.add(timerBase);
		list.add(lastStartTime);
		list.add(lastStopTime);
		list.add(diff);
		return list;
	}
}
