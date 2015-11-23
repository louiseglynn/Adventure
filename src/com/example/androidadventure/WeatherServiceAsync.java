package com.example.androidadventure;

import android.os.AsyncTask;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;





import android.os.AsyncTask;
import android.util.Log;


//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;



public class WeatherServiceAsync extends AsyncTask<String, Void, String> {
	
	private final Map_Activity WeatherActivity;
	
	// this constructor takes the activity as the parameter.
	// that way we can use the activity later to populate the weather value fields
	// on the screen
	    public WeatherServiceAsync(Map_Activity weatherActivity) {
	    	
	        this.WeatherActivity = weatherActivity;
	    }
	    @Override
	    
	    protected String doInBackground(String... urls) {
	        HttpURLConnection urlConnection = null;
	        String forecastJsonStr = null;


	// this weather service method will be called after the service executes.
	// it will run as a seperate process, and will populate the activity in the onPostExecute
	// method below

	        String response = "";
	// loop through the urls (there should only be one!) and call an http Get using the URL passed
	// to this service

	        ///
	        URL url = null;
	        try {
	            url = new URL(urls[0]);
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        }
	        try {
	            urlConnection = (HttpURLConnection) url.openConnection();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        try {
	            urlConnection.setRequestMethod("GET");
	        } catch (ProtocolException e) {
	            e.printStackTrace();
	        }
	        try {
	            urlConnection.connect();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        // Read the input stream into a String
	        InputStream inputStream = null;
	        try {
	            inputStream = urlConnection.getInputStream();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        forecastJsonStr = readStream(inputStream);
	        response = forecastJsonStr;
	        return response;
	    }

	    private String readStream(InputStream in) {
	        BufferedReader reader = null;
	        StringBuffer data = new StringBuffer("");
	        try {
	            reader = new BufferedReader(new InputStreamReader(in));
	            String line = "";
	            while ((line = reader.readLine()) != null) {
	                data.append(line);
	            }
	        } catch (IOException e) {
	         //   Log.e(LOG_TAG, "IOException");
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        return data.toString();
	    }
	    @Override
	    protected void onPostExecute(String result) {
	        String test = result;
	        try {
	        	
	// parse the json result returned from the service
	        	
	            JSONObject jsonResult = new JSONObject(test);

	// parse out the temperature from the JSON result
	            
	            double temperature = jsonResult.getJSONObject("main").getDouble("temp");
	            temperature = ConvertTemperatureToCelsius(temperature);

	// parse out the pressure from the JSON Result
	            
	            double pressure = jsonResult.getJSONObject("main").getDouble("pressure");

	// parse out the humidity from the JSON result
	            
	            double humidity = jsonResult.getJSONObject("main").getDouble("humidity");

	// parse out the description from the JSON result
	            
	            String description = jsonResult.getJSONArray("weather").getJSONObject(0).getString("description");
	            
	            // force upper-case of first character.
	            
	            String description2 = description.substring(0, 1).toUpperCase() + description.substring(1);

	            
	            this.WeatherActivity.displayWeather( description2, temperature, pressure, humidity);
	            
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
	    }

	    private double ConvertTemperatureToCelsius(double temperature) {
	        return (temperature - 273);
	    }

	

}
