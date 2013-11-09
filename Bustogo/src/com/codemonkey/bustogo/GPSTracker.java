package com.codemonkey.bustogo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class GPSTracker extends Service implements LocationListener{

	private final Context mContext;
	boolean isGPSEnabled = false;
	boolean canGetLocation = false;

	Location location;
	double latitude;
	double longitude;

	protected LocationManager locationManager;

	Marker myLoc;

	public GPSTracker(Context context){

		this.mContext = context;
		locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
	}

	public void updateMarker(Marker myLoc) {
		this.myLoc = myLoc;
	}

	public Location getLocation(){
	
		if(location == null) {
			
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);		
			String best = locationManager.getBestProvider(new Criteria(), true);
			location = locationManager.getLastKnownLocation(best);
			return location;
		}
		return location;
	}

	

	protected void onStart() {
		
	}

	protected void onPause(){
		locationManager.removeUpdates(this);
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		Location temp = this.location;
		this.location = location;
		float rotation = temp.bearingTo(location);
		if(myLoc != null) {
			myLoc.setPosition(new LatLng(latitude, longitude));
			myLoc.setRotation(rotation);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}