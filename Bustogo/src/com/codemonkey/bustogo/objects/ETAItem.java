package com.codemonkey.bustogo.objects;

public class ETAItem {
		private String routeShortName;
		private String routeName;
		private String time="";
		private int color;
		
		public void addTime(String str) {
			if(time.equals("")) {
				time = time + str;
			} else {
				time = time +" and " +str;
			}
		}
		
		public String getETA() {
			return time+" mins";
		}

		

		public String getRouteShortName() {
			return routeShortName;
		}

		public void setRouteShortName(String routeShortName) {
			this.routeShortName = routeShortName;
		}

		public String getRouteName() {
			return routeName;
		}

		public void setRouteName(String routeName) {
			this.routeName = routeName;
		}

		public int getColor() {
			return color;
		}

		public void setColor(int color) {
			this.color = color;
		}
		
		
	}