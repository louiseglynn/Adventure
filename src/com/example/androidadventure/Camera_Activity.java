package com.example.androidadventure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.GoogleMap;
//import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import android.app.Activity;
//import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
//import android.location.LocationManager;
import android.net.Uri;
//import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
//import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
//import android.widget.TextView;
import android.widget.Toast;
//import android.location.Criteria;
import android.location.Location;
//import android.location.LocationManager;
import android.location.Address;
import android.location.Geocoder;


public class Camera_Activity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	
//-- Activity request codes
	
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;

	private static final String IMAGE_DIRECTORY_NAME = "Adventure Gallery"; //-- directory name to store captured images

	private Uri fileUri; //-- file Uri to store image ( we will save photo to a folder on phone as well as parse.com

	private ParseFile file; //-- image file that will be saved to Parse.com
	
	private ImageView imgPreview;
	private Button btnCapturePicture,btnSavePicture,btnSignOut,btnViewPhototMap;
	
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
	private GoogleApiClient mGoogleApiClient;	
	private Location mLastLocation;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_camera2);	
	
		imgPreview = (ImageView) findViewById(R.id.imgPreview);
		btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
		btnSavePicture = (Button) findViewById(R.id.btnSavePicture);
		btnSignOut = (Button)findViewById(R.id.btnSignOut);
		btnViewPhototMap = (Button)findViewById(R.id.btnViewPhotoMap);
	
		//-- Only want to save photo under certain circumstances.
		
		btnSavePicture.setEnabled( false );
		
        //-- First we need to check availability of play services
		
        if (checkPlayServices()) {
 
            //-- Building the GoogleApi client
        	
            buildGoogleApiClient();    
            mGoogleApiClient.connect();
        }
		
		
		//-- Sign out of Parse / leave Application.

		btnSignOut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				ParseUser.logOut();
				finish();
				
			}
		});
        
		
		//-- View ALL Photo markers on Google Map.

		btnViewPhototMap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(Camera_Activity.this, Map_Activity.class);
				startActivity(intent);
				finish();
			}
		});
        
			
		//-- Capture image button click event

		btnCapturePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				captureImage(); //-- capture picture
			}
		});

		
		//-- Save image button click event

		btnSavePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				saveImage(); //-- capture picture
			}
		});
		
		
        
		//-- Checking camera availability
				
		if ( !isDeviceSupportCamera() ) {
			Toast.makeText(getApplicationContext(), "Sorry! Your device doesn't support camera", Toast.LENGTH_LONG).show();
			
			finish(); //-- will close the application if the device does't have camera
		}
		
		
	}
	

	
   private boolean checkPlayServices() {  //-- Method to verify google play services on the device
       
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
   
    protected synchronized void buildGoogleApiClient() {
        
    	mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }
	
    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        
    	Toast.makeText(this, "Failed to connect to 'Google Play Services' ...", Toast.LENGTH_SHORT).show();  
    }
    
    @Override
    public void onConnectionSuspended(int arg0) {
        
    	//Toast.makeText(this, "Connection suspended...", Toast.LENGTH_SHORT).show();
    }
    
    
	protected void onDestroy() {
		
		super.onPause();
	}
	
	protected void onResume(Bundle outState ){
        
		super.onResume();
		
	}
	
	protected void onPause( Bundle outState ){
				
		super.onPause();
	}
    
    @Override
    public void onConnected(Bundle arg0) {
    	
    	//-- Shared Pref's could be used to store previous photo and display in Image Preview.
    	
        // SharedPreferences sp = getSharedPreferences("AppSharedPref", 0);       		
        // fileUri = Uri.parse(sp.getString("ImagePath", ""));
        
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient);        
        previewCapturedImage();
    }
  
    
	private boolean isDeviceSupportCamera() {   //-- Checking device has camera hardware or not
		
		if ( getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ) {
			
			return true; //-- this device has a camera
		} else {
			
			return false; //-- no camera on this device
		}
	}

	//-- Capturing Camera Image will launch camera application request image capture
	 
	private void captureImage() {
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		
		SharedPreferences sp = getSharedPreferences("AppSharedPref", 0); // Open SharedPreferences with name AppSharedPref
	    Editor editor = sp.edit();
	    editor.putString("ImagePath", fileUri.toString()); // Store selectedImagePath with key "ImagePath". This key will be then used to retrieve data.         
	    editor.commit();
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE); //-- start the image capture Intent
	}

	 //-- Here we store the file Uri as it will be null after returning from camera application

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable("file_uri", fileUri); //-- save file url in bundle as it will be null on screen orientation changes
	}


	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
				
		fileUri = savedInstanceState.getParcelable("file_uri"); //-- get the file uri
	}

	//-- Receiving activity result method will be called after closing the camera
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) { //-- if the result is capturing Image
			if (resultCode == RESULT_OK) {
				
				previewCapturedImage();
				
			} else if (resultCode == RESULT_CANCELED) { //-- user cancelled Image capture
				
				Toast.makeText(getApplicationContext(),"User cancelled image capture", Toast.LENGTH_SHORT).show();
			} 
			else { //-- failed to capture image
				
				Toast.makeText(getApplicationContext(),"Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
			}
		} 
	}
	
	
	private void saveImage(){
				 
		Calendar newDate = Calendar.getInstance();
		String date = newDate.get(Calendar.DAY_OF_MONTH) +"/" +newDate.get(Calendar.MONTH) +"/" +newDate.get(Calendar.YEAR);
		//Toast.makeText(getApplicationContext(),"date - " +date, Toast.LENGTH_SHORT).show();
		
		file.saveInBackground();	  	 
		ParseUser currentUser = ParseUser.getCurrentUser();	  
		String name = currentUser.getUsername();		  
		String location = getGeoCode( mLastLocation.getLatitude(),mLastLocation.getLongitude() );		  
		ParseGeoPoint point = new ParseGeoPoint((Double)mLastLocation.getLatitude(),(Double)mLastLocation.getLongitude());	
		
		ParseObject dataObject = new ParseObject("Photograph");			  
		dataObject.put("User", name );	  
		dataObject.put("Location",location );  
		dataObject.put("GeoPoint", point);		 
		dataObject.put("Photo", file );
		dataObject.put("Date", date);	 
		dataObject.saveInBackground();
		
		//-- Don't need to save the same picture twice.
		  
		btnSavePicture.setEnabled( false );			    
		btnSavePicture.setTextColor(Color.BLACK);
		
		Toast.makeText(getApplicationContext(),"Details Saved ...", Toast.LENGTH_SHORT).show();
	}


	//-- Display image from a path to ImageView

	private void previewCapturedImage() {
		try {

			imgPreview.setVisibility(View.VISIBLE);		
			
			BitmapFactory.Options options = new BitmapFactory.Options();	
			
			options.inSampleSize = 8; //-- down-sizing image as it throws OutOfMemory Exception for larger images
			
			final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),options);
			
			imgPreview.setImageBitmap(bitmap);	
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();	
			
			bitmap.compress(Bitmap.CompressFormat.PNG, 3, stream);	
			
			byte[] image = stream.toByteArray();
	
			file = new ParseFile("snap.png",image);
				
			mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient); //-- Get location details.
			  
			  if( mLastLocation == null ) {
					
				  Toast.makeText(getApplicationContext(),"No location obtained ...", Toast.LENGTH_SHORT).show();
			  }
			  else {
				  
				  btnSavePicture.setEnabled( true );			  
				  btnSavePicture.setTextColor(Color.RED);
			  }
		}	  		
		catch (NullPointerException e) {
			e.printStackTrace();
		}
	}


	//-- Return address for given latitude / longitude. 
	
	public String getGeoCode( double lat, double lng){
		
		Geocoder geocoder = new Geocoder( this, Locale.getDefault() );
		List<Address> addresses = null;
		
		  try {							
				addresses = geocoder.getFromLocation(lat, lng, 1);
		  } catch ( IOException e1 ) {
					e1.printStackTrace();
		  } catch ( IllegalArgumentException e2 ) {
					e2.printStackTrace();				
		  }
		  
		  
		  if ( addresses != null && addresses.size() > 0 ) {	  

			  Address address = addresses.get(0);  
			  
			  
			  if ( address.getLocality() != null ) {
				  
				  return ( address.getAddressLine(0) +" " +address.getLocality() +" " +address.getCountryName() );
			  }
			  else {
				  return ( address.getAddressLine(0) +" " +address.getCountryName() );
			  }
		  }
		  return( "No address");
	}
	



	//-- Creating file uri to store image/video

	public Uri getOutputMediaFileUri(int type) {
		
		return Uri.fromFile(getOutputMediaFile(type));
	}


	//-- Returning Image.

	private static File getOutputMediaFile(int type) {
		
		// External SD card location
		File mediaStorageDir = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
	
		if ( !mediaStorageDir.exists() ) {  //-- Create the storage directory if it does not exist
			if ( !mediaStorageDir.mkdirs() ) {
				
				Log.d(IMAGE_DIRECTORY_NAME, "Failed to create : "+ IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		//-- Create a media file name ( Can be expanded to include Video in the future.
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
		File mediaFile;
		
		if (type == MEDIA_TYPE_IMAGE) {
			
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		} else {
			return null;
		}
		return mediaFile;
	}
}
