package com.cjboyett.boardgamestats.model.firebase;

/**
 * Created by Casey on 5/25/2016.
 */
public class FirebaseUserData {
	public String name;
	public String email;

	public FirebaseUserData() {
	}

	public FirebaseUserData(String name, String email) {
		this.name = name;
		this.email = email;
	}

	@Override
	public String toString() {
		return "[" + name + ", " + email + "]";
	}
}
