package com.codemonkey.bustogo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.codemonkey.bustogo.objects.Route;


@SuppressLint("NewApi") 
public class ActiveRouteAdapter extends BaseAdapter {

	Context context;
	Messenger msgr;
	
	public ActiveRouteAdapter(Context context, Messenger msgr) {
		this.context = context;
		this.msgr = msgr;
	}
	
	@Override
	public int getCount() {			
		return UFLBusService.routeList.size();
	}

	@Override
	public Object getItem(int position) {
		return UFLBusService.allRoutes.get(UFLBusService.routeList.get(position));
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return UFLBusService.routeList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup listItem;
		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			listItem = (ViewGroup) inflater.inflate(R.layout.list_item, null, false);
		}
		else
		{
			listItem = (ViewGroup) convertView;
		}
		
		final Route route = (Route) getItem(position);
		
		TextView color = ((TextView)listItem.findViewById(R.id.color));
		ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
		drawable.getPaint().setColor(route.getColor());
		color.setBackground(drawable);
		color.setText(""+route.getShortName());
		CheckBox check = ((CheckBox)listItem.findViewById(R.id.checkBus));
		check.setChecked(UFLBusService.checkedRoute[route.getInternalId()]);
		check.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
				boolean value = !UFLBusService.checkedRoute[route.getInternalId()];
				((CheckBox)v).setChecked(value);
				UFLBusService.checkedRoute[route.getInternalId()] = value;
				Message msg = Message.obtain();
				msg.obj=value?"showStops":"hideStops";
				try {
					msgr.send(msg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		((TextView)listItem.findViewById(R.id.routeName)).setText(route.getLongName());
		return listItem;
	}
	
}
