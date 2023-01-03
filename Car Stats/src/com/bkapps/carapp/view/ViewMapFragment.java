package com.bkapps.carapp.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bkapps.carapp.R;
import com.bkapps.carapp.R.drawable;
import com.bkapps.carapp.R.id;
import com.bkapps.carapp.R.layout;
import com.bkapps.carapp.utils.Tripp.Point;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ViewMapFragment extends Fragment {

	public ViewMapFragment() {
	}

	TextView tv;
	GoogleMap map;
	ImageView iv;
	
	int type;
	
	    private int getType() {
		return type;
	}

	private void setType(int type) {
		this.type = type;
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_view_map, container, false);

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		tv = (TextView) rootView.findViewById(R.id.tv_info);
		iv = (ImageView)rootView.findViewById(R.id.imageView1);

		
		return rootView;
	}

	public void loaded(int i) {
		setType(i);
		Log.v("Map", "in loaded *****");
		map.clear();

		ArrayList<Point> mypoints = ((ViewActivity) this.getActivity()).getMytrip().getPointslist();
		PointsFilesTask mytask = new PointsFilesTask();
		mytask.execute(mypoints);
		switch (getType()) {
        case 1:  iv.setBackgroundResource(R.drawable.map_speed);
                 break;
        case 2:  iv.setBackgroundResource(R.drawable.map_rpm);
        	break;
        case 3:  iv.setBackgroundResource(R.drawable.map_temp);
        	break;
        case 4:  iv.setBackgroundResource(R.drawable.map_load);
        	break;
        }

	}


	private class PointsFilesTask extends AsyncTask<ArrayList<Point>, Void, Pair> {
		protected Pair doInBackground(ArrayList<Point>... points) {
			Pair pair = new Pair();
			ArrayList<LatLng> listLatLng = new ArrayList<LatLng>();
			List<Integer> listcolor = new ArrayList<Integer>();
			int color = 0;
			for (Point temp : points[0]) {
				Log.v("Points", "Pt: " + temp.getLocation());
				String[] geo = temp.getLocation().split(",");
				if (getType()==1){
					double s = Double.parseDouble(temp.getSpeed());
					color = (s <= 50) ? ((s <= 25) ? 1 : 2 ): ((s <= 75) ? 3 : ((s <= 100) ? 4 : ((s <= 125) ? 5 : ((s <= 140) ? 6 : 7))));
				} else if (getType()==2) {
					double s = Double.parseDouble(temp.getRPM());
					color = (s <= 1000) ? ((s <= 500) ? 1 : 2 ): ((s <= 1500) ? 3 : ((s <= 2000) ? 4 : ((s <= 2500) ? 5 : ((s <= 3000) ? 6 : 7))));
				} else if (getType()==3) {
					double s = Double.parseDouble(temp.getTemp());
					color = (s <= 100) ? ((s <=50) ? 1 : 2 ): ((s <= 150) ? 3 : ((s <= 200) ? 4 : ((s <= 250) ? 5 : ((s <= 300) ? 6 : 7))));
				}  else if (getType()==4) {
					double s = Double.parseDouble(temp.getLoad());
					color = (s <= 40) ? ((s <=20) ? 1 : 2 ): ((s <= 60) ? 3 : ((s <= 70) ? 4 : ((s <= 80) ? 5 : ((s <= 90) ? 6 : 7))));
				}
				listcolor.add(color);
				Log.v("Points", "color: No." + color);

				LatLng p = new LatLng(Double.parseDouble(geo[0]), Double.parseDouble(geo[1]));
				listLatLng.add(p);
				// Escape early if cancel() is called
				if (isCancelled())
					break;
			}

			pair.list = listLatLng;
			pair.colors = listcolor;
			return pair;
		}

		protected void onPostExecute(Pair result) {
			ViewActivity.getMapfragment().loadedroute(result);
		}

	}

	public class Pair {
		public ArrayList<LatLng> list;
		public List<Integer> colors;
	}

	public void loadedroute(Pair result) {

		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LatLng marker : result.list) {
			builder.include(marker);
		}
		LatLngBounds bounds = builder.build();
		int padding = 18; // offset from edges of the map in pixels
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
		map.animateCamera(cu);

		int i = 0;
		LatLng temp = null;
		for (LatLng marker : result.list) {
			if (i == 0) {
				temp = marker;
			} else if ((result.colors.get(i) == 1)) {
				map.addPolyline(new PolylineOptions().add(temp, marker).width(10)
						.color(Color.rgb(0, 255, 255)));
				temp = marker;
			} else if ((result.colors.get(i) == 2)) {
				map.addPolyline(new PolylineOptions().add(temp, marker).width(10)
						.color(Color.rgb(0, 255, 33)));
				temp = marker;
			} else if ((result.colors.get(i) == 3)) {
				map.addPolyline(new PolylineOptions().add(temp, marker).width(10)
						.color(Color.rgb(255, 216, 0)));
				temp = marker;
			} else if ((result.colors.get(i) == 4)) {
				map.addPolyline(new PolylineOptions().add(temp, marker).width(10)
						.color(Color.rgb(255, 106, 0)));
				temp = marker;
			} else if ((result.colors.get(i) == 5)) {
				map.addPolyline(new PolylineOptions().add(temp, marker).width(10)
						.color(Color.rgb(127, 51, 0)));
				temp = marker;
			} else if ((result.colors.get(i) == 6)) {
				map.addPolyline(new PolylineOptions().add(temp, marker).width(10)
						.color(Color.RED));
				temp = marker;
			} else if ((result.colors.get(i) == 7)) {
				map.addPolyline(new PolylineOptions().add(temp, marker).width(10)
						.color(Color.BLACK));
				temp = marker;
			} 
			i++;
		}

		map.addMarker(new MarkerOptions().title("Start")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
				.position(result.list.get(0)));

		map.addMarker(new MarkerOptions().title("Finish").position(
				result.list.get(result.list.size() - 1)));

	}
}