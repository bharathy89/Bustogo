package com.codemonkey.bustogo.objects;

import java.util.TreeSet;

public class ETAItem {
		private String routeShortName;
		private String routeName;
		private TreeSet<Integer> times =  new TreeSet<Integer>();
		private int color;
		
		public void addTime(int time) {
			times.add(time);
		}
		
		public String getETA() {
			String result = "";
			int count = 0;
			
			Integer[] timeArray = new Integer [times.size()];
			times.toArray(timeArray);
			if(times.size() > 0) {
				result = timeArray[0].toString();
				count++;
			} 
			int less = 2 < times.size() ? 2 : times.size();
			while(count < less) {
				if(less - count == 1) {
					result = result + " & ";
				}else if(less > 3) {
					result = result +", ";
				}
				result = result + timeArray[count];
				count++;
			}
			
			return result+" mins";
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