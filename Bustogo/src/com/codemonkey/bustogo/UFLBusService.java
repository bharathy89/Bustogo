package com.codemonkey.bustogo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.codemonkey.bustogo.objects.Bus;
import com.codemonkey.bustogo.objects.BusStop;
import com.codemonkey.bustogo.objects.ETAItem;
import com.codemonkey.bustogo.objects.Route;
import com.codemonkey.bustogo.objects.Segment;
import com.google.android.gms.maps.model.LatLng;

public class UFLBusService extends IntentService{

	public UFLBusService() {
		super("UFLBusService");
		// TODO Auto-generated constructor stub
	}

	private final int agencyID = 116;

	private static final String agency="ufl";

	private final int updateInterval = 5000;

	private final int announcementInterval = 60000;

	public volatile static HashMap<Integer, Bus> allBuses = new HashMap<Integer, Bus>();

	public volatile static HashMap<Integer, Route> allRoutes = new HashMap<Integer, Route>();
	
	private static ArrayList<Integer> activeRoute = new ArrayList<Integer>();
	
	public volatile static ArrayList<Integer> routeList = new ArrayList<Integer>(); 
	
	public volatile static boolean[] checkedRoute;
	
	public static HashMap<Integer, BusStop> busStops = new HashMap<Integer, BusStop>();
	
	Messenger msger;
	
	private boolean routeFetched = false; 
	private boolean stopsFetched = false;
	private boolean segmentsFetched = false; 

	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//sendRouteRequest();
		//sendSetupRequest();
		//sendStopsRequest();
	}

	public static HttpResponse makeRequest(String path) throws Exception 
	{
		//instantiates httpclient to make request
		DefaultHttpClient httpclient = new DefaultHttpClient();
		//url with the post data
		HttpGet httget = new HttpGet(path);
		return httpclient.execute(httget);
	}

	public HashMap<Integer, Bus> getAllBuses() {
		return allBuses;
	}

	public void sendActiveRouteRequestService() {
		
		TimerTask doAsynchronousTask;
	    final Handler handler = new Handler();
	    Timer timer = new Timer();

	    doAsynchronousTask = new TimerTask() {

	        @Override
	        public void run() {

	            new Thread(new Runnable() {
	                public void run() {
	                    
	                	try {
	                		if(isNetworkAvailable()){
	                			 if(routeFetched) {
	                				HttpResponse response = makeRequest("http://feeds.transloc.com/mobile/2/update?agencies="+agency);
	                				Log.d("TAG",response.toString());
	                				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
	                				StringBuilder builder = new StringBuilder();
	                				for (String line = null; (line = reader.readLine()) != null;) {
	                					builder.append(line).append("\n");
	                				}
	                				JSONTokener tokener = new JSONTokener(builder.toString());
	                				JSONObject finalResult = new JSONObject(tokener);
	                				JSONArray active_routes = finalResult.getJSONArray("active_routes");
	                				boolean same = addActiveRoutes(active_routes);
	                				JSONArray vehicles = finalResult.getJSONArray("vehicles");
	                				addToAllBuses(vehicles);
	                				Message msg = Message.obtain();
	                				if (same)
	                					msg.obj="update";
	                				else
	                					msg.obj="update-all";
	                				msger.send(msg);
	                				Log.d("TAG","active routes fetched");
	                			} else {
	                				
	                				sendRouteRequest();
	                			
	                			}
	                		} 
	    				} catch (JSONException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				} catch (Exception e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}
	                }
	            }).start();

	        }

	    };

	    timer.schedule(doAsynchronousTask, 0, updateInterval);
		
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private boolean addActiveRoutes(JSONArray active_route) {
		int count = 0;
		activeRoute.clear();
		while(count < active_route.length()) {
			try {
				int routeId = Integer.parseInt(active_route.getString(count));
				if(allRoutes.containsKey(routeId)) {
					activeRoute.add(routeId);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			count++;
		}
		boolean same = true;
		ArrayList<Integer> placeHolder = new ArrayList<Integer>();
		if(activeRoute.size() == routeList.size()) {
			for(Integer i : routeList) {
				if(!activeRoute.contains(i)) {
					same = false;
					placeHolder.clear();
					placeHolder.addAll(activeRoute);
					break;
				}else {
					placeHolder.add(i);
				}
			}
		}else {
			same = false;
			placeHolder.addAll(activeRoute);
		}
		routeList = placeHolder;
		Collections.sort(routeList,new RouteComparator());
		return same;
	}
	
	
	class RouteComparator implements Comparator<Integer> {

		@Override
		public int compare(Integer lhs, Integer rhs) {
			
			if (allRoutes.get(lhs).getShortNameInInt() < allRoutes.get(rhs).getShortNameInInt()) {
				return -1;
			} else if (allRoutes.get(lhs).getShortNameInInt() == allRoutes.get(rhs).getShortNameInInt()) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	private void addToAllBuses(JSONArray vehicles) {
		int count = 0;
		while(count < vehicles.length()) {
			JSONObject JSONbus;
			try {
				JSONbus = vehicles.getJSONObject(count);
				Bus bus = new Bus(JSONbus.getInt("id"),JSONbus.getInt("agency_id"),
					JSONbus.getInt("heading"),JSONbus.getInt("route_id"),
					JSONbus.getString("position"), JSONbus.getInt("speed"));
				allBuses.put(bus.getId(), bus);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			count++;
		}
	}

	public void sendRouteRequest() {
		new Thread(new Runnable() {
			public void run() {
				try {

					HttpResponse response = makeRequest("http://feeds.transloc.com/mobile/2/routes?agencies="+agency);
					Log.d("TAG",response.toString());
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					StringBuilder builder = new StringBuilder();
					for (String line = null; (line = reader.readLine()) != null;) {
						builder.append(line).append("\n");
					}
					JSONTokener tokener = new JSONTokener(builder.toString());
					JSONObject finalResult = new JSONObject(tokener);
					JSONArray routes = finalResult.getJSONArray("routes");
					addToAllRoutes(routes);
					Log.d("TAG","routes fetched");
					sendStopsRequest();
					routeFetched = true;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void sendStopsRequest() {
		new Thread(new Runnable() {
			public void run() {
				try {

					HttpResponse response = makeRequest("http://feeds.transloc.com/mobile/2/stops?agencies="+agency);
					Log.d("TAG",response.toString());
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					StringBuilder builder = new StringBuilder();
					for (String line = null; (line = reader.readLine()) != null;) {
						builder.append(line).append("\n");
					}
					JSONTokener tokener = new JSONTokener(builder.toString());
					JSONObject finalResult = new JSONObject(tokener);
					JSONArray stops = finalResult.getJSONArray("stops");
					addToAllStops(stops);
					Message msg = Message.obtain();			
					msg.obj="updateStops";	
    				msger.send(msg);
					Log.d("TAG","stops fetched");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	

	private void addToAllStops(JSONArray stops) {
		int count = 0;
		busStops.clear();
		while(count < stops.length()) {
			JSONObject JSONStop;
			try {
				JSONStop = stops.getJSONObject(count);
				BusStop stop = new BusStop(JSONStop.getInt("code"), 
					JSONStop.getString("description"), 
					JSONStop.getInt("id"), 
					JSONStop.getString("name"),
					JSONStop.getString("position"));
				busStops.put(stop.getId(),stop);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			count++;
		}
		Log.d("TAG", "no of stops : "+busStops.size());
	}
	
	private void addToAllRoutes(JSONArray routes) {
		int count = 0;
		checkedRoute = new boolean[routes.length()];
		while(count < routes.length()) {
			JSONObject JSONRoute;
			try {
				JSONRoute = routes.getJSONObject(count);
				Route route = new Route(count, 116, 
					JSONRoute.getInt("id"), new BigInteger("ff"+JSONRoute.getString("color"), 16).intValue(), 
					JSONRoute.getString("short_name"), 
					JSONRoute.getString("long_name"));
				allRoutes.put(route.getRouteId(), route);
				checkedRoute[route.getInternalId()] = false;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			count++;
		}
	}

	private void addToAllSegments(JSONArray segments) {
		int count = 0;
		while(count < segments.length()) {
			JSONObject JSONSegment;
			try {
				JSONSegment = segments.getJSONObject(count);
				Segment segment = new Segment();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			count++;
		}
	}
	
	public void sendSetupRequest() {
		new Thread(new Runnable() {
			public void run() {
				try {

					HttpResponse response = makeRequest("http://feeds.transloc.com/mobile/2/setup?agencies="+agency);
					Log.d("TAG",response.toString());
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					StringBuilder builder = new StringBuilder();
					for (String line = null; (line = reader.readLine()) != null;) {
						builder.append(line).append("\n");
					}
					JSONTokener tokener = new JSONTokener(builder.toString());
					JSONObject finalResult = new JSONObject(tokener);
					JSONArray segments = finalResult.getJSONArray("segments");
					JSONArray routes = finalResult.getJSONArray("agencies").getJSONObject(0).getJSONArray("routes");
					addToAllSegments(segments);
					addToAllRoutes(routes);
					Log.d("TAG","setup fetched");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void sendAnouncementsRequestService() {
		TimerTask doAsynchronousTask;
	    final Handler handler = new Handler();
	    Timer timer = new Timer();

	    doAsynchronousTask = new TimerTask() {

	        @Override
	        public void run() {

	            new Thread(new Runnable() {
	                public void run() {
	                    
	                	try {

					HttpResponse response = makeRequest("http://feeds.transloc.com/mobile/2/announcements?agencies="+agency+"&contents=1");
					Log.d("TAG",response.toString());
					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					StringBuilder builder = new StringBuilder();
					for (String line = null; (line = reader.readLine()) != null;) {
						builder.append(line).append("\n");
					}
					JSONTokener tokener = new JSONTokener(builder.toString());

	                	} catch (JSONException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				} catch (Exception e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}
	                }
	            }).start();

	        }

	    };

	    timer.schedule(doAsynchronousTask, 0, announcementInterval);
	}
	
	public static HashMap<Integer, ETAItem> getETA(int stopId) {
		HttpResponse response;
		try {
			response = makeRequest("http://feeds.transloc.com/mobile/2/arrivals?agencies="+agency+"&stop_id="+stopId);
			Log.d("TAG",response.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			JSONTokener tokener = new JSONTokener(builder.toString());
			JSONObject finalResults = new JSONObject(tokener);
			JSONArray arrivals = finalResults.getJSONArray("arrivals");
			return addETA(arrivals);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}
	
	public static HashMap<Integer, ETAItem> addETA(JSONArray arrivals) {
		HashMap<Integer, ETAItem> routeToArrival = new HashMap<Integer, ETAItem>();
		int count =0;
		long sysTime = System.currentTimeMillis();
		while(count < arrivals.length()) {
			try {
				ETAItem etaItem;
				int routeId = arrivals.getJSONObject(count).getInt("route_id");
				if(routeToArrival.get(routeId) == null) {
					etaItem = new ETAItem();
					etaItem.setRouteShortName(allRoutes.get(routeId).getShortName());
					etaItem.setRouteName(allRoutes.get(routeId).getLongName());
					etaItem.setColor(allRoutes.get(routeId).getColor());
					routeToArrival.put(routeId, etaItem);
				} else {
					etaItem = routeToArrival.get(routeId);
				}
				double time = Math.ceil((arrivals.getJSONObject(count).getInt("timestamp") - sysTime) / 60 );
				etaItem.addTime(""+time);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
		}
		return routeToArrival;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		msger = (Messenger)intent.getExtras().get("MESSENGER");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		sendActiveRouteRequestService();
	}
	
	public static boolean isChecked(Integer key) {
		int internalId = allRoutes.get(key).getInternalId();
		return checkedRoute[internalId];
	}
	
	public static String getRouteName(Integer key) {
		String shortName ="";
		if(allRoutes.get(key) != null) {
			shortName = allRoutes.get(key).getShortName();
		}
		return shortName;
	}
	
	private List<LatLng> decodePoly(String encoded) {

	    List<LatLng> poly = new ArrayList<LatLng>();
	    int index = 0, len = encoded.length();
	    int lat = 0, lng = 0;

	    while (index < len) {
	        int b, shift = 0, result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        LatLng p = new LatLng((int) (((double) lat /1E5)* 1E6), (int) (((double) lng/1E5   * 1E6)));
	        poly.add(p);
	    }

	    return poly;
	}
}
