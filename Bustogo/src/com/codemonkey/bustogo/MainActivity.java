package com.codemonkey.bustogo;

/*  SlideOutWindow is created by Edward Akoto on 12/31/12.
 *  Email akotoe@aua.ac.ke
 * 	Free for modification and distribution
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codemonkey.bustogo.objects.Bus;
import com.codemonkey.bustogo.objects.BusStop;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/***
 * This activity is the has multiple functionalities. 
 * It has the slideOut Menu and map fragment.
 * 
 * 
 * @author bharatyarlagadda
 *
 */
@SuppressLint("NewApi") 
public class MainActivity extends Activity{

	//Declare
	private LinearLayout slidingPanel;
	private boolean isExpanded;
	private DisplayMetrics metrics;	
	private RelativeLayout headerPanel;
	private LinearLayout menuPanel;
	private int panelWidth;
	private ImageView menuViewButton;
	private ImageButton findMyLoc;
	private ListView active_route_list;
	private BaseAdapter activeListAdapter;

	FrameLayout.LayoutParams menuPanelParameters;
	FrameLayout.LayoutParams slidingPanelParameters;
	LinearLayout.LayoutParams headerPanelParameters ;
	LinearLayout.LayoutParams listViewParameters;

	private GoogleMap map;
	private GPSTracker gps;
	private Marker myLoc;
	private Map<Integer, Marker> markerMap = new HashMap<Integer, Marker>();
	private Map<Integer, Marker> stopMarkerMap = new HashMap<Integer, Marker>();
	private LocationManager locationManager ;
	private Marker markerForInfoWindowUpdate;
	
	// This handler is used to communicate between the server the activity.
	// This is used to synchronize the changes of map objects.
	Handler mapUpdates=new Handler() { 
		@Override 
		public void handleMessage(Message msg) { 

			if(msg.obj.toString().equals("update") || msg.obj.toString().equals("update-all")) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						int count = 0;
						if(UFLBusService.allBuses.size() < 1) {
							return;
						}
						final Object[] busArr = UFLBusService.allBuses.values().toArray();
						while(count < busArr.length) {
							if(busArr[count] instanceof Bus) {
								Bus bus = (Bus)busArr[count];
								if(markerMap.get(bus.getId())== null) {
									
									Marker marker = map.addMarker(new MarkerOptions().position(bus.getPosition()).
										icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)).rotation(0));
									markerMap.put(bus.getId(), marker);
									marker.setSnippet("speed : "+bus.getSpeed()+" Mph");
								}
								count++;
								Marker marker = markerMap.get(bus.getId());
								LatLng prevltlg = marker.getPosition();
								Location preLoc = new Location("prev");
								preLoc.setLatitude(prevltlg.latitude);
								preLoc.setLongitude(prevltlg.longitude);
								float angle = preLoc.bearingTo(bus.getLocation());
								marker.setPosition(bus.getPosition());
								marker.setRotation(angle);
								marker.setTitle(UFLBusService.getRouteName(bus.getRouteId()));
								marker.setVisible(UFLBusService.isChecked(bus.getRouteId()));
							}
						}		
					}
				});
				if(msg.obj.toString().equals("update-all")) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							activeListAdapter.notifyDataSetChanged();
						}

					});
				}
			} else if(msg.obj.toString().equals("updateStops")) {
				runOnUiThread(new Runnable() {
					int count = 0;
					
					@Override
					public void run() {
						if(UFLBusService.busStops.size() < 1) {
							return;
						}
						Object[] stopArr = UFLBusService.busStops.values().toArray();
						while(count < stopArr.length) {
							if(stopArr[count] instanceof BusStop) {
								BusStop stop = (BusStop)stopArr[count];
								if(stopMarkerMap.get(stop.getId())== null) {
									
									Marker marker = map.addMarker(new MarkerOptions().position(stop.getPosition()).
										icon(BitmapDescriptorFactory.fromResource(R.drawable.stop)).rotation(0));
									marker.setTitle("Stop "+stop.getId());
									marker.setVisible(false);
									stopMarkerMap.put(stop.getId(), marker);
								}
							}
							count++;
						}
						
					}

				});
			}
		}
	}; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layer_stack);
		gps = new GPSTracker(MainActivity.this);
		//Initialize
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		panelWidth = (int) ((metrics.widthPixels)*0.80);

		headerPanel = (RelativeLayout) findViewById(R.id.header);
		headerPanelParameters = (LinearLayout.LayoutParams) headerPanel.getLayoutParams();
		headerPanelParameters.width = metrics.widthPixels;
		headerPanel.setLayoutParams(headerPanelParameters);

		menuPanel = (LinearLayout) findViewById(R.id.menuPanel);
		menuPanelParameters = (FrameLayout.LayoutParams) menuPanel.getLayoutParams();
		menuPanelParameters.width = panelWidth;
		menuPanel.setLayoutParams(menuPanelParameters);

		slidingPanel = (LinearLayout) findViewById(R.id.slidingPanel);
		slidingPanelParameters = (FrameLayout.LayoutParams) slidingPanel.getLayoutParams();
		slidingPanelParameters.width = metrics.widthPixels;
		slidingPanel.setLayoutParams(slidingPanelParameters);
		locationManager = (LocationManager) MainActivity.this.getSystemService(LOCATION_SERVICE);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		map.getUiSettings().setRotateGesturesEnabled(false);
		
		Location loc = gps.getLocation();
		LatLng ltlg = new LatLng(loc.getLatitude(), loc.getLongitude());
		myLoc = map.addMarker(new MarkerOptions().position(ltlg)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.myloc)));
		
		gps.updateMarker(myLoc);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(ltlg, 15));

		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
		map.getUiSettings().setZoomControlsEnabled(false);
		
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			@Override
			public void onCameraChange(CameraPosition arg0) {
				// TODO Auto-generated method stub
				if(arg0.zoom < 15) {
					for (Marker marker : stopMarkerMap.values())
					{
						marker.setVisible(false);
					}
				}else {
					for (Marker marker : stopMarkerMap.values())
					{
						marker.setVisible(true);
					}
				}
			}
		});

		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				String stopStr = marker.getTitle();
				if(stopStr.startsWith("Stop")) {
					String[] value = stopStr.split(" ");
					final BusStop stop = UFLBusService.busStops.get(Integer.parseInt(value[1]));

					AsyncTask fetchETA = new AsyncTask< Object, Void, HashMap<Integer, Integer> >(){

						@Override
						protected void onPostExecute(HashMap<Integer, Integer> result) {
							map.setInfoWindowAdapter(new BusStopInfoWindowAdapter(stop, result));
						}

						@Override
						protected HashMap<Integer, Integer> doInBackground(
								Object... params) {
							HashMap<Integer, Integer> result = UFLBusService.getETA(stop.getId());
							return result;
						}

					};
					fetchETA.execute();		
				}
				return false;
			}
		});

		findMyLoc = (ImageButton) findViewById(R.id.findMyLoc);
		findMyLoc.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
					buildAlertMessageNoGps();
				}
				
				
				Location loc = gps.getLocation();
				LatLng ltlg = new LatLng(loc.getLatitude(), loc.getLongitude());
				myLoc.setPosition(ltlg);
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(ltlg, 15));
			}
		});

		//Slide the Panel	
		menuViewButton = (ImageView) findViewById(R.id.menuViewButton);
		menuViewButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!isExpanded){
					isExpanded = true;   		    				        		

					//Expand
					new ExpandAnimation(slidingPanel, panelWidth,
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, 0.80f, 0, 0.0f, 0, 0.0f);		    			         	    
				}else{
					isExpanded = false;

					//Collapse
					new CollapseAnimation(slidingPanel,panelWidth,
							TranslateAnimation.RELATIVE_TO_SELF, 0.80f,
							TranslateAnimation.RELATIVE_TO_SELF, 0.0f, 0, 0.0f, 0, 0.0f);


				}         	   
			}
		});

		active_route_list = (ListView) findViewById(R.id.active_route_list);
		activeListAdapter = new ActiveRouteAdapter(this, new Messenger(mapUpdates));
		active_route_list.setAdapter(activeListAdapter);
		active_route_list.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> listView, View item, int position, long id) 
			{
			
			}
		});
		
		Intent mServiceIntent = new Intent(this, UFLBusService.class);
		ServiceConnection mConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "Bus service connection", Toast.LENGTH_LONG);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, "Lost Bus service connection", Toast.LENGTH_LONG);
			}
			
		};
		bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
		mServiceIntent.putExtra("MESSENGER",new Messenger(mapUpdates));
		startService(mServiceIntent);
		//mapUpdates.postDelayed(updateMarker, 10000);
	}
	
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
		.setCancelable(false).setIcon(R.drawable.ic_launcher)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog,final int id) {
				Intent gpsEnable = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(gpsEnable);
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog,final int id) {
				dialog.cancel();
			}
		});
		final AlertDialog alert = builder.create();
		alert.show();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//mapUpdates.removeCallbacks(updateMarker);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}
	
	class BusStopInfoWindowAdapter implements InfoWindowAdapter {
		
		private View view;
		private TextView stopCode;
		private TextView stopName;
		private LinearLayout infoLayout;
		private BusStop stop;
		private HashMap<Integer, Integer> result;

		public BusStopInfoWindowAdapter(BusStop stop, HashMap<Integer, Integer> result) {
			view = getLayoutInflater().inflate(R.layout.busstop_info,
					null);
			stopCode = (TextView) view.findViewById(R.id.stopCode);
			stopName = (TextView) view.findViewById(R.id.stopName);
			infoLayout = (LinearLayout) view.findViewById(R.id.infoLayout);
			this.stop = stop;
			this.result = result;
		}

		@Override
		public View getInfoContents(final Marker marker) {
			return null;
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			// TODO Auto-generated method stub

			if( result != null) {
				for(int x : result.keySet()) {
					TextView addItem = new TextView(MainActivity.this);
					addItem.setText("route : "+x+" time : "+result.get(x));
					infoLayout.addView(addItem);
				}
			}

			stopCode.setText(""+stop.getStopCode());
			stopName.setText(stop.getStopName());
			markerForInfoWindowUpdate = marker;				
			return view;

		}
		
	}
	
}







