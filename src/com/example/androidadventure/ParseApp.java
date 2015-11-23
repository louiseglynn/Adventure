package com.example.androidadventure;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import android.app.Application;

public class ParseApp extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Parse.enableLocalDatastore(this);
		Parse.initialize(this, "iuAta6uR75nLpg76S1EUY7gGGegh7xfzYxRbYplZ", "2UP5A8m9xmcp89GddKDje9H1DbUr58IofCyy4kkV");
		ParseUser.enableAutomaticUser();
		ParseACL defauAcl = new ParseACL();
		
		defauAcl.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defauAcl, true);
	}
	

}
