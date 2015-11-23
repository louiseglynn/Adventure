package com.example.androidadventure;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginSignupActivity extends Activity {
	
	Button loginButton;
	Button signupButton;
	Button guestButton;
	String usernametxt;
	String passwordtxt;
	EditText password;
	EditText username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_signup);

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);

		loginButton = (Button) findViewById(R.id.login);
		signupButton = (Button) findViewById(R.id.signup);
		guestButton = (Button) findViewById(R.id.guest);

		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				usernametxt = username.getText().toString();
				passwordtxt = password.getText().toString();

				ParseUser.logInInBackground(usernametxt, passwordtxt, new LogInCallback() {

							@Override
							public void done(ParseUser user, ParseException e) {
								if (user != null) {
									
									// Save User details.

					    			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());		 
					    			Editor editor = preferences.edit();		 
					    			editor.putString("USER", user.getUsername());         
					    			editor.commit();
									
					    			//Toast.makeText(getApplicationContext(),"User : " +user.getUsername() ,Toast.LENGTH_SHORT).show();

									Intent intent = new Intent(LoginSignupActivity.this,Camera_Activity.class);
									startActivity(intent);
									finish();

								} else {

									Toast.makeText(getApplicationContext(),"This user doesn't exist. Please signup",Toast.LENGTH_SHORT).show();
								}
							}
						});
			}
		});

		
		
		
		guestButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(LoginSignupActivity.this,Map_Activity.class);
				startActivity(intent);
				finish();
			}
		});
		
		
		
		
		signupButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				usernametxt = username.getText().toString();
				passwordtxt = password.getText().toString();

				if (usernametxt.equals("") && passwordtxt.equals("")) {

					Toast.makeText(getApplicationContext(),"Please complete the sign up form",Toast.LENGTH_SHORT).show();

				} else {

					ParseUser user = new ParseUser();
					user.setUsername(usernametxt);
					user.setPassword(passwordtxt);
					user.signUpInBackground(new SignUpCallback() {

						@Override
						public void done(ParseException e) {
							if (e == null) {

								Toast.makeText(getApplicationContext(),"Successfully Signed up!",Toast.LENGTH_SHORT).show();

							} else {

								Toast.makeText(getApplicationContext(),"Sign up error", Toast.LENGTH_SHORT).show();

							}
						}
					});

				}
			}
		});
	}
	
	

}
