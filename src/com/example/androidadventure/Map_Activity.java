package com.example.androidadventure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
//import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.MapFragment;



//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.parse.GetDataCallback;
//import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.FindCallback;
//import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
//import com.parse.ParseUser;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//-- http://www.androidhive.info/2015/02/android-location-api-using-google-play-services/

//-- http://www.androidhive.info/2013/09/android-working-with-camera-api/ 

public class Map_Activity extends Activity implements 
ConnectionCallbacks, OnConnectionFailedListener {
	
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
	
	private GoogleApiClient mGoogleApiClient;
	      
	private GoogleMap map;
	
	private Location mLastLocation;
	
	private Button btnSignOut, btnPullParse;
	
	private TextView weatherView, distanceView;
	
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();
    
    private ArrayList<ParseGeoPoint> mGeoPoint = new ArrayList<ParseGeoPoint>();
    
    private HashMap<Marker, MyMarker> mMarkersHashMap;
    
	final String nl = System.getProperty("line.separator");
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.maps);
		

		try {
			
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment)).getMap();
			map.setMyLocationEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(true);  //-- false to disable
			map.getUiSettings().setZoomControlsEnabled(true);      //-- false to disable	
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	    
        //-- First we need to check availability of play services
		
        if (checkPlayServices()) {
 
            //-- Building the GoogleApi client
        	
            buildGoogleApiClient();    
            mGoogleApiClient.connect();
        }
        
        PullFromParse();
        
		weatherView = (TextView) findViewById(R.id.weather);	
		distanceView = (TextView) findViewById(R.id.distance);

	}
	
    protected synchronized void buildGoogleApiClient() {
       
    	mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }
    
	private void PullFromParse() {
		
	    
		//-- 14/11/2015 care the MyMarker objects, each one with its own data.	
		
		mMarkersHashMap = new HashMap<Marker, MyMarker>();
		
		ParseQuery<ParseObject> query=ParseQuery.getQuery("Photograph");
	    
		query.findInBackground(new FindCallback<ParseObject>() {
	    	public void done(List<ParseObject> pList, ParseException e) {
	        	
	            if (e == null) {

	            	//Toast.makeText(getApplicationContext(), pList.size() +" records returned", Toast.LENGTH_LONG).show();
	            	
	            	for ( ParseObject p : pList ){
	            		String user = p.get("User").toString();
	            		String location = p.get("Location").toString();
	            		
	            		String date = p.get("Date").toString();
	            		
	            		Bitmap bmp;
	            		ParseGeoPoint gp = p.getParseGeoPoint("GeoPoint");
	            		
	            		mGeoPoint.add(gp);
	            		
						ParseFile fileObject = (ParseFile) p.get("Photo");
						byte[] data;
						
						if ( fileObject != null) {                         
							try {
								
								//-- 14/11/2015 Start recording the Marker information.	
										
								data = fileObject.getData();
								bmp = BitmapFactory.decodeByteArray(data, 0,data.length);							      
			            		
			            		mMyMarkersArray.add(new MyMarker(user + nl +nl +location + nl +nl +date  ,bmp,gp.getLatitude(),gp.getLongitude()));
							}
							catch (ParseException e2) {
			
								Toast.makeText(getApplicationContext(), "There was an exception generated ...", Toast.LENGTH_LONG).show();  
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
						}
						//-- all details obtained at this point.
						
						//-- plotMarkers(mMyMarkersArray);
	            		
	            	}	
	            	
					//-- all details obtained at this point.
					
					plotMarkers(mMyMarkersArray);
	            }
	        }
	    });
	}

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        
    	Toast.makeText(this, "Failed to connect...", Toast.LENGTH_SHORT).show();
        
    }

    @Override
    public void onConnected(Bundle arg0) {
        
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient);
        
        if (mLastLocation != null) {
        	
        	//-- move map to last known position.
        	
        	 LatLng postion = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        	 
        	 CameraPosition cameraPosition = new CameraPosition.Builder().target(postion).tilt(25).zoom(12).bearing(0).build(); 
        	 
        	 map.moveCamera(CameraUpdateFactory.newLatLng(postion)); 
        	 
        	 map.moveCamera( CameraUpdateFactory.newCameraPosition(cameraPosition));
        	
        	 map.setOnMarkerClickListener(new OnMarkerClickListener()
        	 { 
        		// boolean show = true;
        		 
                 @Override
                 public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker)
                 {
                		 
                	 weatherView.setText("");
                	 distanceView.setText("");
                	 
                	 marker.showInfoWindow();
                     return true;
                 }
        	 });
        }
    }
    
    //-- http://www.rogcg.com/blog/2014/04/20/android-working-with-google-maps-v2-and-custom-markers
    
    private void plotMarkers(ArrayList<MyMarker> markers)
    {
        if(markers.size() > 0)
        {
            for (MyMarker myMarker : markers)
            {

                // Create user marker with custom icon and other options
            	
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));
                
                Marker currentMarker = map.addMarker(markerOption);
                
                mMarkersHashMap.put(currentMarker, myMarker);

                map.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
                
                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
                {
                    @Override
                    public void onInfoWindowClick(Marker marker)
                    {
                    	
                    	String id = marker.getId();
                    	
                    	int i = Integer.valueOf( id = id.substring(1, id.length()) );
                    			
                    	ParseGeoPoint gp = mGeoPoint.get(i);
                    	
                    	String lat = Double.toString(gp.getLatitude());
                    	
                    	String lng = Double.toString(gp.getLongitude());
                    	
                    	
                    	try {
                    		
                    		RetrieveWeather( lat, lng );
                    	}
                    	catch (Exception e ){
                    		
                    	}
                    	
                    	//Toast.makeText(getApplicationContext(), "Index[ " +i +" ] Lat - " +gp.getLatitude() +", Lng - " +gp.getLongitude(), Toast.LENGTH_LONG).show();
                    	
                    	LatLng origin = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    	
                    	LatLng dest = new LatLng(gp.getLatitude(),gp.getLongitude() );
                    	
                    	  // Getting URL to the Google Directions API
                    	
                        String url = getDirectionsUrl(origin, dest);
     
                        DownloadTask downloadTask = new DownloadTask();
     
                        // Start downloading json data from Google Directions API
                        
                        downloadTask.execute(url);    
                    	                    	
                    }
                });
                
           
            }
        }
    }
 
    private void RetrieveWeather( String lat, String lng ) throws IOException{

        String url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lng+"&appid=2de143494c0b295cca9337e1e96b00e0";
        WeatherServiceAsync task = new WeatherServiceAsync(this);
        task.execute(url);
    }
    

    
    private String getDirectionsUrl(LatLng origin,LatLng dest){
    	      
        String str_origin = "origin="+origin.latitude+","+origin.longitude; //-- Origin of route
 
        String str_dest = "destination="+dest.latitude+","+dest.longitude; //-- Destination of route
 
        String sensor = "sensor=false";  //-- Sensor enabled
 
        String parameters = str_origin+"&"+str_dest+"&"+sensor+",&mode=walking";  //-- Building the parameters to the web service : mode=Walking mode=Bicycling mode=Driving.
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;       
        return url;
    }
    
    //-- A method to download JSON data from Url 
    
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
 
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
 
            // Connecting to url
            urlConnection.connect();
 
            // Reading data from url
            iStream = urlConnection.getInputStream();
 
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
 
            StringBuffer sb  = new StringBuffer();
 
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
 
            data = sb.toString();
 
            br.close();
 
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    
    
 //-- Fetches data from Url passed
    
    private class DownloadTask extends AsyncTask<String, Void, String>{
 
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
 
            // For storing data from web service
            String data = "";
 
            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
 
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
 
            ParserTask parserTask = new ParserTask();
 
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }
 
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
 
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
 
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
 
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
 
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
 
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
  
            String distance = "";
            String duration = "";
 
            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
 
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){ 
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
 
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
 
                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }
                }
            }
            
            displayDistance( distance, duration );
        }
    }

    
    
    
    public void displayWeather( String description, double temperature, double pressure, double humidity ){
    	
    	temperature =Double.parseDouble(new DecimalFormat("##.#").format(temperature));
    	String DEGREE  = "\u00b0";
    	weatherView.setText("Weather: " +description +" - Temp: " +temperature +DEGREE +"C" +nl +"Pressure: " +pressure +"hPa - Humidity: " +humidity +"%" );
    }
    
    public void displayDistance( String distance, String duration ){
    	
    	distanceView.setText("Distance: " +distance +" - Duration: " +duration +" < Walking >" );
    }
    
    private void displayLocation(){

    	mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
 
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            
        	Toast.makeText(this, "Location... latitude : " +latitude +", longitude : " +longitude, Toast.LENGTH_SHORT).show();
 
        } else {
 
        	Toast.makeText(this, "No location available ... " ,Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onConnectionSuspended(int arg0) {
        
    	Toast.makeText(this, "Connection suspended...", Toast.LENGTH_SHORT).show();
    }
    
    

     
     //-- Method to verify google play services on the device
    
    private boolean checkPlayServices() {
        
    	int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    
    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        public MarkerInfoWindowAdapter()
        {
        }

        @Override
        public View getInfoWindow(Marker marker)
        {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker)
        {
            View v  = getLayoutInflater().inflate(R.layout.infowindow_layout, null);

            MyMarker myMarker = mMarkersHashMap.get(marker);

            ImageView markerIcon = (ImageView) v.findViewById(R.id.marker_icon);

            TextView markerLabel = (TextView)v.findViewById(R.id.marker_label);
            
            markerIcon.setImageBitmap(myMarker.getmIcon());

            markerLabel.setText(myMarker.getmLabel());

            return v;
        }
    }
    
    
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
