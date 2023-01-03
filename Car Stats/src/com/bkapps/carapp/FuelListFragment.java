package com.bkapps.carapp;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bkapps.carapp.utils.MyFile;
import com.bkapps.carapp.utils.Trip;

public class FuelListFragment extends Fragment {
		
		
		Button buttonD;
		ListView lv;
		
		public FuelListFragment() {
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_list_fuel, container, false);
			System.out.println("done onCreateView in Data Fragment");
			buttonD = (Button) rootView.findViewById(R.id.buttonDelete);
			buttonD.setOnClickListener(myhandlerD);
			lv = (ListView) rootView.findViewById(R.id.listView1);
			DataSetList();
			return rootView;
		}
		
		View.OnClickListener myhandlerD = new View.OnClickListener() {
			public void onClick(View v) {
				// it was the 1st button
				new AlertDialog.Builder(getActivity())
				.setTitle("Delete List")
				.setMessage("Are you sure you want to delete all your Fuel data?")
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// continue with delete
								try {
									MyFile filehelper = new MyFile(getActivity());
									filehelper.deleteFuelslist();
									Toast.makeText(getActivity().getBaseContext(),
											"All data deleted!",
											Toast.LENGTH_SHORT).show();
									DataSetList();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
							}
						}).show();
			}
		};
		
		@Override
		public void onPause() {
			super.onPause();
		}
		
		public void DataSetList() {
			MyFile filehelper = new MyFile(getActivity());
			try {
				ArrayList<Trip> TripList = filehelper.readFuelslist();
				ArrayList<String> STrips = new ArrayList<String>();
				for (Trip oneTrip : TripList) {
					STrips.add(oneTrip.toString());
				}

				ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_list_item_1, STrips);
				lv.setAdapter(arrayAdapter);
				lv.setLongClickable(true);
				lv.setOnItemLongClickListener(new OnItemLongClickListener() {
				    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				        //Do some
				    	final int pos = position;
				    	new AlertDialog.Builder(getActivity())
						.setTitle("Delete List")
						.setMessage("Delete this fuel logging?")
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// continue with delete
										try {
											MyFile filehelper = new MyFile(getActivity());
											filehelper.deleteFuelslist(pos);
											Toast.makeText(getActivity().getBaseContext(),
													"deleted!",
													Toast.LENGTH_SHORT).show();
											DataSetList();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								})
						.setNegativeButton(android.R.string.no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing
									}
								}).show();
				    	
				        return true;
				    }
				});

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getActivity().getBaseContext(), "No data yet!!\nAdd a New Fuel logging", Toast.LENGTH_LONG)
						.show();
				lv.setAdapter(null);
			}
		}
	}
