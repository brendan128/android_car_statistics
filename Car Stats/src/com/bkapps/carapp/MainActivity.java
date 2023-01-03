package com.bkapps.carapp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bkapps.carapp.live.LiveActivity;
import com.bkapps.carapp.record.RecordActivity;
import com.bkapps.carapp.settings.SettingsActivity;
import com.bkapps.carapp.utils.DeleteDialogFragment;
import com.bkapps.carapp.utils.DeleteDialogFragment.DelCaller;
import com.bkapps.carapp.utils.EulaDialogFragment;
import com.bkapps.carapp.utils.EulaDialogFragment.EulaCaller;
import com.bkapps.carapp.utils.EulaUtils;
import com.bkapps.carapp.utils.MyFile;
import com.bkapps.carapp.utils.PickDialogFragment;
import com.bkapps.carapp.utils.PickDialogFragment.PickCaller;
import com.bkapps.carapp.view.ViewActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity implements EulaCaller, PickCaller, DelCaller {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mHomeDrawerTitles;

	private PlaceholderFragment placeholderfragment;
	private PlaceholderFragment1 placeholderfragment1;
	private PlaceholderFragment2 placeholderfragment2;
	private PlaceholderFragment3 placeholderfragment3;
	private InputFragment inputfragment;
	private FuelListFragment fuellistfragment;

	private Boolean Homeset0 = false;
	private Boolean Homeset1 = false;
	private Boolean Homeset2 = false;
	private Boolean Homeset3 = false;
	private Boolean Homeset4 = false;
	private Boolean Homeset5 = false;

	private int tabposition;
	ListViewTask task = null;
	Boolean listisempty = false;

	static final String KEY_SONG = "song"; // parent node
	static final String KEY_ID = "id";
	static final String KEY_TITLE = "title";
	static final String KEY_DATE = "date";
	static final String KEY_DURATION = "duration";
	static final String KEY_THUMB_URL = "thumb_url";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();
		mHomeDrawerTitles = getResources().getStringArray(R.array.Home_drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item,
				mHomeDrawerTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}
		showStartupDialogs();
		settabposition(0);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v("MAIN", "in OnResume");
		task = new ListViewTask(this);
		task.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, SettingsActivity.class);
			startActivityForResult(intent, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		// Fragment fragment = new PlaceholderFragment();
		/*
		 * Bundle args = new Bundle();
		 * args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		 * fragment.setArguments(args);
		 */
		settabposition(position);
		FragmentManager fragmentManager = getFragmentManager();
		Log.v("Info", "** creating PlaceholderFragment**" + position);

		switch (position) {
		case 0:
			if (!Homeset0) {
				placeholderfragment = new PlaceholderFragment();
				Homeset0 = true;
			}
			fragmentManager.beginTransaction().replace(R.id.content_frame, placeholderfragment)
					.commit();
			break;
		case 1:
			if (!Homeset1) {
				placeholderfragment1 = new PlaceholderFragment1();
				Homeset1 = true;
			}
			fragmentManager.beginTransaction().replace(R.id.content_frame, placeholderfragment1)
					.commit();
			break;
		case 2:
			if (!Homeset2) {
				inputfragment = new InputFragment();
				Homeset2 = true;
			}
			fragmentManager.beginTransaction().replace(R.id.content_frame, inputfragment).commit();
			break;
		case 3:
			if (!Homeset3) {
				fuellistfragment = new FuelListFragment();
				Homeset3 = true;
			}
			fragmentManager.beginTransaction().replace(R.id.content_frame, fuellistfragment)
					.commit();
			break;
		case 4:
			if (!Homeset4) {
				placeholderfragment3 = new PlaceholderFragment3();
				Homeset4 = true;
			}
			fragmentManager.beginTransaction().replace(R.id.content_frame, placeholderfragment3)
					.commit();
			break;
		case 5:
			if (!Homeset5) {
				placeholderfragment2 = new PlaceholderFragment2();
				Homeset4 = true;
			}
			fragmentManager.beginTransaction().replace(R.id.content_frame, placeholderfragment2)
					.commit();
			break;
		}
		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mHomeDrawerTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private void settabposition(int position) {
		// TODO Auto-generated method stub
		this.tabposition = position;

	}

	private int gettabposition() {
		// TODO Auto-generated method stub
		return this.tabposition;

	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private class ListViewTask extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
		private Context mContext;

		public ListViewTask(Context context) {
			mContext = context;
		}

		protected ArrayList<HashMap<String, String>> doInBackground(Void... position) {
			MyFile myfile = new MyFile(mContext);
			List<String[]> fileList = new LinkedList<String[]>();

			ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

			// String xml = parser.getXmlFromUrl(URL); // getting XML from URL
			// Document doc = parser.getDomElement(xml); // getting DOM element

			// NodeList nl = doc.getElementsByTagName(KEY_SONG);
			// looping through all song nodes <song>
			// for (int i = 0; i < nl.getLength(); i++) {

			// }
			try {
				fileList = myfile.listOfFilesDistance();

				// for (int i = 0; i < fileList.size(); i++) {
				for (String[] filedata : fileList) {
					// creating new HashMap
					HashMap<String, String> map = new HashMap<String, String>();
					// Element e = (Element) nl.item(i);
					// adding each child node to HashMap key => value
					map.put(KEY_ID, "ID1");// parser.getValue(e, KEY_ID));
					map.put(KEY_TITLE, filedata[0].toString()); // parser.getValue(e,
																// KEY_TITLE));
					map.put(KEY_DATE, filedata[1].toString()); // parser.getValue(e,
																// KEY_ARTIST));
					map.put(KEY_DURATION, filedata[2].toString()); // parser.getValue(e,
					// KEY_DURATION));
					// map.put(KEY_THUMB_URL, parser.getValue(e,
					// KEY_THUMB_URL));

					// adding HashList to ArrayList
					songsList.add(map);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return songsList;
		}

		protected void onPostExecute(ArrayList<HashMap<String, String>> mylist) {
			// setMytrip(mytripp);
			try {
				placeholderfragment.loaded(mylist);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * Shows start up dialogs.
	 */
	public void showStartupDialogs() {
		if (!EulaUtils.hasAcceptedEula(this)) {
			Fragment fragment = getFragmentManager().findFragmentByTag(
					EulaDialogFragment.EULA_DIALOG_TAG);
			if (fragment == null) {
				EulaDialogFragment.newInstance(false).show(getFragmentManager(),
						EulaDialogFragment.EULA_DIALOG_TAG);
			}
		}
	}

	public void onEulaDone() {
		// TODO Auto-generated method stub
		if (EulaUtils.hasAcceptedEula(this)) {
			// showStartupDialogs();
			Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
			if (listisempty) {
				Toast toast = Toast.makeText(this,
						"      Pretty empty here! \n You can record a new Trip!",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			return;
		}
		finish();
	}

	/*
	 * public void trace(String message){ Log.v("GpsTracker", message); }
	 */

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
			this.mylist = null;
		}

		int listsize = 0;

		private int getListsize() {
			return listsize;
		}

		private void setListsize(int listsize) {
			this.listsize = listsize;
		}

		MyFile myfile;
		String[] theNamesOfFiles = null;
		// XML node keys
		private ArrayList<HashMap<String, String>> mylist;
		ListView list;
		TextView tv_record_trip_see;
		LazyAdapter adapter;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			// TextView textView = (TextView)
			// rootView.findViewById(R.id.section_label);
			// textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

			// listView1 = (ListView) rootView.findViewById(R.id.listView1);
			list = (ListView) rootView.findViewById(R.id.listView1);
			tv_record_trip_see = (TextView) rootView.findViewById(R.id.tv_record_trip_see);
			tv_record_trip_see.setVisibility(View.GONE);
			/*
			 * String myTag = getTag();
			 * ((MainActivity)getActivity()).setPlaceholderFragment1(myTag);
			 * Log.v("TheTag", "tag is :"+myTag);
			 */

			Button button_live = (Button) rootView.findViewById(R.id.button_live);
			button_live.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(getActivity(), LiveActivity.class);
					startActivityForResult(intent, 90210);
				}
			});

			Button button_record = (Button) rootView.findViewById(R.id.button_record);
			button_record.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(getActivity(), RecordActivity.class);
					startActivityForResult(intent, 90210);
				}
			});

			refreshList();
			return rootView;
		}

		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			Log.v("FRAG", "in OnResume");
			refreshList();
		}

		private void refreshList() {
			Log.v("Info", "** refreshlist mainactivity **");

			if (getListsize() > 0) {
				// Getting adapter by passing xml data ArrayList
				adapter = new LazyAdapter(getActivity(), getMylist());
				tv_record_trip_see.setVisibility(View.GONE);
				list.setAdapter(adapter);
				Log.v("Info", "** is a list **");
			} else {
				((MainActivity) getActivity()).listisempty = true;
				tv_record_trip_see.setVisibility(View.VISIBLE);
				Log.v("Info", "** is NOT a list **");
			}

			list.setOnItemLongClickListener(new OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
					// TODO Auto-generated method stub
					Log.v("long clicked", "pos: " + pos);
					// TODO Auto-generated method stub
					PickDialogFragment.newInstance(pos).show(getFragmentManager(),
							PickDialogFragment.TITLE_DIALOG_TAG);
					return true;
				}
			});

			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent();
					intent.putExtra("positionkey", position);
					intent.putExtra("typekey", 1);
					intent.setClass(getActivity(), ViewActivity.class);
					startActivity(intent);

				}
			});

		}

		public void loaded(ArrayList<HashMap<String, String>> mylist) {
			// TODO Auto-generated method stub
			Log.v("Info", "went to loaded PLaceholderF");
			setMylist(mylist);
			setListsize(mylist.size());
			Log.v("info", "List size: " + mylist.size());
			Log.v("postionb4laoded", "Pos: " + ((MainActivity) getActivity()).gettabposition());
			if (((MainActivity) getActivity()).gettabposition() == 0) {
				// Getting adapter by passing xml data ArrayList

				adapter = new LazyAdapter(getActivity(), mylist);
				list.setAdapter(adapter);

				list.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
						// TODO Auto-generated method stub
						Log.v("long clicked", "pos: " + pos);
						// TODO Auto-generated method stub
						PickDialogFragment.newInstance(pos).show(getFragmentManager(),
								PickDialogFragment.TITLE_DIALOG_TAG);
						return true;
					}
				});

				list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Intent intent = new Intent();
						intent.putExtra("positionkey", position);
						intent.setClass(getActivity(), ViewActivity.class);
						startActivity(intent);

					}
				});

			}
			refreshList();

		}

		public void DeleteDialog(final int position) {

		}

		public ArrayList<HashMap<String, String>> getMylist() {
			return mylist;
		}

		public void setMylist(ArrayList<HashMap<String, String>> mylist) {
			this.mylist = mylist;
		}
	}


	@Override
	public void Delete(int position) {
		// TODO Auto-generated method stub
		Log.v("Delete", "position :" + position);
		/*
		 * DeleteDialogFragment.newInstance(position)
		 * .show(getFragmentManager(), DeleteDialogFragment.TITLE_DIALOG_TAG);
		 */
		DeleteDialogFragment.newInstance(position).show(getFragmentManager(),
				DeleteDialogFragment.TITLE_DIALOG_TAG);
	}

	@Override
	public void deleteDone(int position) {
		// TODO Auto-generated method stub
		MyFile myfile = new MyFile(this);
		myfile.deleteFilePosition(position);

		ListViewTask task2 = new ListViewTask(this);
		task2.execute();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment1 extends Fragment {

		public PlaceholderFragment1() {
		}

		public void refreshList() {
			MyFile filehelper = new MyFile(getActivity());
			String[] sums = filehelper.SummaryFuelList();
			tvAvg.setText(sums[0]);
			tvBest.setText(sums[1]);
			tvTotalkm.setText(sums[2]);
			tvAvgFP.setText(sums[3]);

		}

		TextView tvAvg;
		TextView tvBest;
		TextView tvTotalkm;
		TextView tvAvgFP;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_summary, container, false);
			tvAvg = (TextView) rootView.findViewById(R.id.tvAvg);
			tvBest = (TextView) rootView.findViewById(R.id.tvBest);
			tvTotalkm = (TextView) rootView.findViewById(R.id.tvTotalkm);
			tvAvgFP = (TextView) rootView.findViewById(R.id.tvAvgFP);
			refreshList();
			return rootView;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment2 extends Fragment {

		public PlaceholderFragment2() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_about, container, false);
			Button bt_email_dev = (Button) rootView.findViewById(R.id.bt_email_dev);
			bt_email_dev.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String[] TO = { "bkapps06@gmail.com" };
					Intent emailIntent = new Intent(Intent.ACTION_SEND);
					emailIntent.setData(Uri.parse("mailto:"));
					emailIntent.setType("text/plain");
					emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
					emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Car Stats Application issue");
					emailIntent.putExtra(Intent.EXTRA_TEXT, "");
					try {
						startActivity(Intent.createChooser(emailIntent, "Send mail..."));
						// Log.i("Finished sending email...", "");
					} catch (android.content.ActivityNotFoundException ex) {

					}
				}
			});
			return rootView;
		}
	}

	@Override
	public void toEarth(int position) {
		// TODO Auto-generated method stub
		MyFile myfile = new MyFile(this);
		File kmlfile = myfile.writeLogtokml(position);
		Intent map_intent = new Intent(Intent.ACTION_VIEW);
		map_intent.setClassName("com.google.earth", "com.google.earth.EarthActivity");
		map_intent.setDataAndType(Uri.fromFile(kmlfile), "application/vnd.google-earth.kml+xml");
		startActivity(map_intent);
	}
	
	@Override
	public void send(int position) {
		// TODO Auto-generated method stub

		Intent intent = new Intent();
		intent.putExtra("positionkey", position);
		intent.putExtra("KML", 1);
		intent.setClass(this, SendDriveActivity.class);
		startActivity(intent);

	}

	@Override
	public void KMltoDrive(int position) {
		Intent intent = new Intent();
		intent.putExtra("positionkey", position);
		intent.putExtra("KML", 0);
		intent.setClass(this, SendDriveActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void sendGeoJson(int position) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.putExtra("positionkey", position);
		intent.putExtra("KML", 2);
		intent.setClass(this, SendDriveActivity.class);
		startActivity(intent);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment3 extends Fragment {

		public PlaceholderFragment3() {
		}

		public void refreshList() {
			MyFile filehelper = new MyFile(getActivity());
			ArrayList<LatLng> listLatLng = filehelper.SummaryMapList();
			if (listLatLng.size() > 1) {
				ArrayList<String> listPrice = filehelper.SummaryPriceList();

				if (listLatLng.size() > 10) {
					LatLngBounds.Builder builder = new LatLngBounds.Builder();
					for (LatLng marker : listLatLng) {
						builder.include(marker);
					}
					LatLngBounds bounds = builder.build();
					int padding = 18; // offset from edges of the map in pixels
					CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
					map.animateCamera(cu);
				} else {
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(listLatLng.get(listLatLng.size()-1), 13));
				}
				int count = 0;
				for (LatLng marker : listLatLng) {
					map.addMarker(new MarkerOptions()
							.title(listPrice.get(count) + " €/l")
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
							.position(marker));
					count++;
				}
			}

		}

		GoogleMap map;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_map, container, false);

			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_main)).getMap();

			return rootView;
		}
		
		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			refreshList();
		}
	}



}
