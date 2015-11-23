package com.example.androidadventure;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import com.parse.GetDataCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.FindCallback;

import com.parse.ParseFile;
import com.parse.ParseGeoPoint;

public class Welcome extends Activity {

	Button logout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		String struser = currentUser.getUsername().toString();
		
		TextView txtUser = (TextView) findViewById(R.id.txtuser);
		txtUser.setText("You are logged in as " + struser);
		
		logout = (Button) findViewById(R.id.logout);
		
		logout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ParseUser.logOut();
				finish();
			}
		});
	}
}