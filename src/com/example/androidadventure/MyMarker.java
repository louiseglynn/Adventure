package com.example.androidadventure;

import android.graphics.Bitmap;

public class MyMarker
{
    private String mLabel;
    private Bitmap mIcon;
    private Double mLatitude;
    private Double mLongitude;

    public MyMarker( String label, Bitmap icon, Double latitude, Double longitude)
    {

        this.mLabel = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
    }
    
    public String getmLabel()
    {
        return mLabel;
    }

    public void setmLabel(String mLabel)
    {
        this.mLabel = mLabel;
    }

    public Bitmap getmIcon()
    {
        return mIcon;
    }

    public void setmIcon(Bitmap icon)
    {
        this.mIcon = icon;
    }

    public Double getmLatitude()
    {
        return mLatitude;
    }

    public void setmLatitude(Double mLatitude)
    {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude()
    {
        return mLongitude;
    }

    public void setmLongitude(Double mLongitude)
    {
        this.mLongitude = mLongitude;
    }
}
