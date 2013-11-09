package com.codemonkey.bustogo.objects;

import com.google.android.gms.maps.model.LatLng;

public class BusStop {
	private int stopCode;
	private String description;
	private int id;
	private String stopName;
	private LatLng position;
	
	public BusStop(int stopCode, String description, int id, String stopName, String positionStr) {
		positionStr = positionStr.replace("[", "");
		positionStr = positionStr.replace("]", "");
		String[] str = positionStr.split(",");
		this.position = new LatLng(Double.parseDouble(str[0]),Double.parseDouble(str[1]));
		this.stopCode = stopCode;
		this.description = description;
		this.id = id;
		this.stopName = stopName;
	}
	
	public int getStopCode() {
		return stopCode;
	}

	public void setStopCode(int stopCode) {
		this.stopCode = stopCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	public LatLng getPosition() {
		return position;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}
	
}
