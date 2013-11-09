package com.codemonkey.bustogo.objects;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Bus {
	private int id;
	private int agencyId;
	private int routeId;
	private int headingTo;
	private int segmentId;
	private LatLng position;
	private Location location;
	private int speed;
	
	public Bus(int id,int agency_id,int heading,int route_id, String positionStr,int speed) {
		this.id = id;
		this.agencyId = agency_id;
		this.headingTo = heading;
		this.routeId = route_id;
		positionStr = positionStr.replace("[", "");
		positionStr = positionStr.replace("]", "");
		String[] str = positionStr.split(",");
		this.position = new LatLng(Double.parseDouble(str[0]),Double.parseDouble(str[1]));
		Location preLoc = new Location("location");
		preLoc.setLatitude(position.latitude);
		preLoc.setLongitude(position.longitude);
		this.location = preLoc;
		this.speed = speed;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRouteId() {
		return routeId;
	}
	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}
	public int getHeadingTo() {
		return headingTo;
	}
	public void setHeadingTo(int headingTo) {
		this.headingTo = headingTo;
	}
	public int getSegmentId() {
		return segmentId;
	}
	public void setSegmentId(int segmentId) {
		this.segmentId = segmentId;
	}
	public LatLng getPosition() {
		return position;
	}
	public void setPosition(LatLng position) {
		Location preLoc = new Location("location");
		preLoc.setLatitude(position.latitude);
		preLoc.setLongitude(position.longitude);
		this.location = preLoc;
		this.position = position;
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	@Override
	public boolean equals(Object bus) {
		if(bus instanceof Bus && ((Bus) bus).id == id) {
			return true;
		}
		return false;
		
	}

	public Location getLocation() {
		return location;
	}
	
}
