package com.codemonkey.bustogo.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import com.google.android.gms.maps.model.LatLng;

public class Route {
	private int agencyId;
	private int internalId = 0;
	private LatLng bounds[] = null;
	private int color;
	private int routeId;
	private String shortName;
	private String longName;
	private int type;
	private ArrayList<Bus> activeBuses = new ArrayList<Bus>();
	private boolean isActive = false;	
	private HashMap<Integer, Segment> routeSegments = new HashMap<Integer, Segment>();
	private static int A = -6;
	private static int B = -5;
	private static int C = -4;
	private static int D = -3;
	private static int E = -2;
	private static int F = -1;
	
	public Route(int internalId, int agencyId,int routeId, int color, String shortName, String longName) {
		this.agencyId = agencyId;
		this.routeId = routeId;
		this.color = color;
		this.shortName = shortName;
		this.longName = longName;
		this.internalId = internalId;
	}
	
	public int getAgencyId() {
		return agencyId;
	}
	
	public int getInternalId() {
		return internalId;
	}
	
	public void setAgencyId(int agencyId) {
		this.agencyId = agencyId;
	}
	public LatLng[] getBounds() {
		return bounds;
	}
	public void setBounds(LatLng[] bounds) {
		this.bounds = bounds;
	}
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
	public int getRouteId() {
		return routeId;
	}
	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortname) {
		this.shortName = shortname;
	}
	public String getLongName() {
		return longName;
	}
	public void setLongName(String longName) {
		this.longName = longName;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public int getShortNameInInt() {
		int name = 0;
		try {
			name = Integer.parseInt(shortName);
		} catch(NumberFormatException e) {
			if(shortName.equals("A")) {
				name = A;
			}else if (shortName.equals("B")) {
				name = B;
			}else if (shortName.equals("C")) {
				name = C;
			}else if (shortName.equals("D")) {
				name = D;
			}else if (shortName.equals("E")) {
				name = E;
			}else if (shortName.equals("F")) {
				name = F;
			}
		}
		return name;
	}
	
	public ArrayList<Bus> getActiveBusesForRoute() {
		return activeBuses;
	}
	public void addActiveBus(Bus bus) {
		this.activeBuses.add(bus);
	}
	
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
