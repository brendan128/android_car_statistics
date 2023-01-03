package com.bkapps.carapp.view;

import com.bkapps.carapp.R;
import com.bkapps.carapp.R.id;
import com.bkapps.carapp.R.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ViewDetailFragment extends Fragment {

	public ViewDetailFragment() {
	}

	private TextView tv_name;
	private TextView tv_date;
	private TextView tv_distance;
	private TextView tv_time;
	private TextView tv_avg_rpm;
	private TextView tv_avg_speed;
	private TextView tv_avg_temp;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_view_details, container, false);
		tv_name = (TextView) rootView.findViewById(R.id.tv_name2);
		tv_date = (TextView) rootView.findViewById(R.id.tv_date);
		tv_time = (TextView) rootView.findViewById(R.id.tv_time);
		tv_distance = (TextView) rootView.findViewById(R.id.tv_fileheader);
		tv_avg_rpm = (TextView) rootView.findViewById(R.id.tv_avg_rpm);
		tv_avg_speed = (TextView) rootView.findViewById(R.id.tv_avg_speed);
		tv_avg_temp = (TextView) rootView.findViewById(R.id.tv_avg_temp);

		return rootView;
	}

	public void loaded() {
		// TODO Auto-generated method stub
		Log.v("Detail", "in loaded *****");
		tv_name.setText(((ViewActivity) this.getActivity()).getMytrip().getName());
		tv_date.setText(((ViewActivity) this.getActivity()).getMytrip().getDate());
		tv_distance.setText(((ViewActivity) this.getActivity()).getMytrip().getDistance()+ " km");
		tv_avg_rpm.setText(((ViewActivity) this.getActivity()).getMytrip().getAvgRPM()+ " rpm");
		tv_avg_speed.setText(((ViewActivity) this.getActivity()).getMytrip().getAvgSpeed()+" km/h");
		tv_avg_temp.setText(((ViewActivity) this.getActivity()).getMytrip().getAvgTemp()+" °C");
		tv_time.setText(((ViewActivity) this.getActivity()).getMytrip().getTime());

	}

}