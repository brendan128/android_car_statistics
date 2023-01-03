package com.bkapps.carapp;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bkapps.carapp.utils.LocationUtils;
import com.bkapps.carapp.utils.MyFile;
import com.bkapps.carapp.utils.Trip;

/**
 * A fragment that launches other parts of the demo application.
 */
public class InputFragment extends Fragment implements LocationListener {

	public EditText et_litres;
	public EditText et_price;
	public EditText et_kilometers;
	public TextView tv_showCal;
	public static TextView tv_Date;
	public ListView lv;
	public Button buttonNow;
	public Button buttonPick;
	public Button buttonCal;
	public Button bt_location_get;

	public static String datestring = "";
	public String currentDateTimeString = "";

	static int day = 0;
	static int month = 0;
	static int year = 0;
	static int setday = 0;
	static int setmonth = 0;
	static int setyear = 0;
	String location ="";



	String filename = "FuelData.dat";
	File file;
	
	public InputFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		System.out.println("done onCreate in Input Fragment");
		file = new File(getActivity().getFilesDir(), filename);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_input, container,
				false);
		System.out.println("done onCreateView in Input Fragment");
		init(rootView);
		return rootView;
	}

	public void onActivityCreated() {
		
		System.out.println("done onActivityCreated in Input Fragment");
	}
	
	private LocationManager locationManager;
	private String provider;
	Location cloc;
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// Get the location manager
	    locationManager = (LocationManager)getActivity().getSystemService(getActivity().LOCATION_SERVICE);
	    // Define the criteria how to select the locatioin provider -> use
	    // default
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    cloc = locationManager.getLastKnownLocation(provider);
	    locationManager.requestLocationUpdates(provider, 400, 1, this);
	    setLocation(LocationUtils.getLatLng(getActivity(), cloc));
	}
	


	  /* Remove the locationlistener updates when Activity is paused */
	  @Override
	public void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(this);
	  }
	
	public void init(View rootView) {
		et_litres = (EditText) rootView.findViewById(R.id.et1);
		et_price = (EditText) rootView.findViewById(R.id.et2);
		et_kilometers = (EditText) rootView.findViewById(R.id.et4);
		tv_showCal = (TextView) rootView.findViewById(R.id.tvshowCal);
		tv_Date = (TextView) rootView.findViewById(R.id.tvDate);
		buttonNow = (Button) rootView.findViewById(R.id.chooseNow);
		buttonPick = (Button) rootView.findViewById(R.id.chooseDate);
		buttonCal = (Button) rootView.findViewById(R.id.Cal1);
		bt_location_get = (Button) rootView.findViewById(R.id.bt_location_get);
		buttonNow.setOnClickListener(myhandler1);
		buttonPick.setOnClickListener(myhandler2);
		buttonCal.setOnClickListener(myhandler3);
		bt_location_get.setOnClickListener(myhandler4);
		System.out.println("done with initalize in inputfragment");
	}

	private void clearScreen() {
		// TODO Auto-generated method stub
		et_litres.setText("");
		et_price.setText("");
		et_kilometers.setText("");
		tv_Date.setText("Date:");
		datestring = "";
		currentDateTimeString = "";
	}

	View.OnClickListener myhandler1 = new View.OnClickListener() {
		public void onClick(View v) {
			System.out.println("you clicket hte first button");
			// it was the 1st button
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			DecimalFormat decim = new DecimalFormat("00");

			currentDateTimeString = decim.format(day) + "_"
					+ decim.format(month + 1) + "_" + String.valueOf(year);

			tv_Date = (TextView) getView().findViewById(R.id.tvDate);
			tv_Date.setText("Date: " + currentDateTimeString);
		}
	};

	View.OnClickListener myhandler2 = new View.OnClickListener() {
		public void onClick(View v) {
			// it was the 2nd button Pick
			DialogFragment newFragment = new DatePickerFragment();
			newFragment.show(getFragmentManager(), "datePicker");
			tv_Date = (TextView) getView().findViewById(R.id.tvDate);
		}
	};

	View.OnClickListener myhandler3 = new View.OnClickListener() {
		public void onClick(View v) {
			System.out.println("Save file ***************");
			// it was the 3rd button calculation
			MyFile filehelper = new MyFile(getActivity());
			ArrayList<Trip> TripList = new ArrayList<Trip>();
			try {
				TripList = filehelper.readFuelslist();

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Probably NO file");
			}
			
			if (TripList==null){
				TripList = new ArrayList<Trip>();
			}

			if (datestring == "" && currentDateTimeString == "") {
				Toast.makeText(getActivity().getBaseContext(),
						"Please give a Date!!", Toast.LENGTH_SHORT).show();
			} else if (et_litres.getText().toString().equals("")
					|| et_litres.getText().toString().equals("")
					|| et_kilometers.getText().toString().equals("")) {
				Toast.makeText(getActivity().getBaseContext(),
						"Please enter all the fields!!", Toast.LENGTH_SHORT).show();
			} else {

				Trip myTrip;
				if (datestring == "") {
					myTrip = new Trip(currentDateTimeString,
							Float.valueOf(et_litres.getText().toString()),
							Float.valueOf(et_price.getText().toString()),
							Float.valueOf(et_kilometers.getText().toString()));
					myTrip.setDay(day);
					myTrip.setMonth(month);
					myTrip.setYear(year);
//					myTrip.setLocation(getLocation());
					myTrip.setLocation("53.637891,15.900352");
				} else {
					myTrip = new Trip(datestring, Float.valueOf(et_litres.getText()
							.toString()), Float.valueOf(et_price.getText()
							.toString()), Float.valueOf(et_kilometers.getText()
							.toString()));
					myTrip.setDay(setday);
					myTrip.setMonth(setmonth);
					myTrip.setYear(setyear);
//					myTrip.setLocation(getLocation());
					myTrip.setLocation("53.637891,15.900352");
				}

				TripList.add(myTrip);

				try {
					filehelper.writeFuelslist(TripList);
					Toast.makeText(getActivity().getBaseContext(), "file saved",
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				DecimalFormat decim = new DecimalFormat("#0.00");
				tv_showCal.setText(decim.format(myTrip.getKmperlitre()) + " km/L");
				clearScreen();
			}
		}
	};
	
	

	View.OnClickListener myhandler4 = new View.OnClickListener() {
		public void onClick(View v) {
			setLocation(LocationUtils.getLatLng(getActivity(), cloc));
			bt_location_get.setText(getLocation());
		}
	};

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			DecimalFormat decim = new DecimalFormat("00");

			datestring = decim.format(day) + "_" + decim.format(month + 1)
					+ "_" + String.valueOf(year);

			tv_Date.setText("Date: " + datestring);
			setday = day;
			setmonth = month;
			setyear = year;
		}
	}
	
	private String getLocation() {
		return location;
	}

	private void setLocation(String location) {
		this.location = location;
	}

	@Override
	public void onLocationChanged(Location loc) {
		// TODO Auto-generated method stub
		cloc = loc;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	

}
