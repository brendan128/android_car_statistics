package com.bkapps.carapp.live;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.bkapps.carapp.R;
import com.bkapps.carapp.settings.SettingsActivity;

public class LiveActivity extends Activity implements ActionBar.TabListener {

	private static final String TAG = "LiveActivity";
	private static final boolean D = true;

	int tabInView = 1;

	private Boolean showSpeedfrag = true;
	private Boolean showRPMfrag = true;
	private Boolean showTEMPfrag = true;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	ActionBar actionBar;

	private SendTask mySendTask = null;
	private Boolean SendTaskON = false;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	String TabPlaceholderFragment1;
	String TabPlaceholderFragment2;
	String TabPlaceholderFragment3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live);

		// Set up the action bar.
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// if (savedInstanceState == null)
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}

	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		checkFrags();
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);

		// MOVED FROM MAIN #########################

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
				tabInView = position + 1;
				Log.v("TAB", String.valueOf(tabInView));
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		/*
		 * for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) { //
		 * Create a tab with text corresponding to the page title defined by //
		 * the adapter. Also specify this Activity object, which implements //
		 * the TabListener interface, as the callback (listener) for when //
		 * this tab is selected. actionBar.addTab(actionBar.newTab()
		 * .setText(mSectionsPagerAdapter.getPageTitle(i))
		 * .setTabListener(this)); }
		 */
		// ##################################################
		actionBar.removeAllTabs();

		if (showRPMfrag) {
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(0))
					.setTabListener(this));
			Log.v("ARRAY", "set one");
		}
		if (showSpeedfrag) {
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(1))
					.setTabListener(this));
			Log.v("ARRAY", "set two");
		}
		if (showTEMPfrag) {
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(2))
					.setTabListener(this));
			Log.v("ARRAY", "set three");
		}

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void checkFrags() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String[] default_array = getResources().getStringArray(R.array.empty_array);

		Set<String> default_checked = new HashSet<String>(Arrays.asList(default_array));
		Set<String> selections = preferences.getStringSet("number_checked", default_checked);
		// String[] selected= selections.toArray(new String[] {});
		/*
		 * for (int i = 0; i < selected.length ; i++){
		 * //System.out.println("\ntest" + i +" : " + selected[i]);
		 * Log.v("ARRAY","\ntest" + i +" : " + selected[i]);
		 */

		showRPMfrag = false;
		showSpeedfrag = false;
		showTEMPfrag = false;
		int count = 0;
		for (String s : selections) {
			if ("1".equals(s)) {
				Log.v("ARRAY", "its one");
				showRPMfrag = true;
				count++;
			} else if ("2".equals(s)) {
				Log.v("ARRAY", "its two");
				showSpeedfrag = true;
				count++;
			}
			if ("3".equals(s)) {
				Log.v("ARRAY", "its three");
				showTEMPfrag = true;
				count++;
			}
		}
		mSectionsPagerAdapter.setCount(count);
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
		if (SendTaskON) {
			// My AsyncTask is currently doing work in doInBackground()
			mySendTask.cancel(true);
		}
		if (mChatService != null)
			mChatService.stop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
		if (SendTaskON) {
			// My AsyncTask is currently doing work in doInBackground()
			mySendTask.cancel(true);
		}
		if (mChatService != null)
			mChatService.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.live, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
			/*
			 * case R.id.insecure_connect_scan: // Launch the DeviceListActivity
			 * to see devices and do scan serverIntent = new Intent(this,
			 * DeviceListActivity.class); startActivityForResult(serverIntent,
			 * REQUEST_CONNECT_DEVICE_INSECURE); return true;
			 */

		case R.id.stop_connect_scan:
			// Ensure this device is discoverable by others
			// mySendTask.cancel(true);  /////////////////newwwwwwwwwwwwwwwwwwwwwwwwww
			if (D)
				Log.v(TAG, "Stopping -- you pressed stop! ");
			if (SendTaskON) {
				// My AsyncTask is currently doing work in doInBackground()
				mySendTask.cancel(true);
			}
			if (mChatService != null)
				mChatService.stop();
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.action_settings:
			// Launch the settings activity
			Intent intent = new Intent();
			intent.setClass(LiveActivity.this, SettingsActivity.class);
			startActivityForResult(intent, 0);
			return true;
		}
		return false;
	}

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode + "   " + requestCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	protected void setupChat() {
		Log.d(TAG, "setupChat()");

		// mTextView = (TextView) findViewById(R.id.textView1);
		// mTextView.setText("0");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
//		mySendTask = new SendTask(); /////////////////newwwwwwwwwwwwwwwwwwwwwwwwww
	}

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
					// mConversationArrayAdapter.clear();
					mySendTask = new SendTask();
					mySendTask.execute();
					SendTaskON =true;
					break;
				case BluetoothChatService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					if (SendTaskON) {
						// My AsyncTask is currently doing work in doInBackground()
						mySendTask.cancel(true);
					}
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;

				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				readMessage = readMessage.replaceAll("(\\r|\\n)", "");
				readMessage = readMessage.replace(" ", "");
				Pattern pR = Pattern.compile("(410C)([A-F0-9]+)");
				Pattern pS = Pattern.compile("(410D)([A-F0-9]+)");
				Pattern pT = Pattern.compile("(4105)([A-F0-9]+)");
				Matcher mR = pR.matcher(readMessage);
				Matcher mS = pS.matcher(readMessage);
				Matcher mT = pT.matcher(readMessage);
				if (mR.find()) {
					readMessage = mR.group(2);
					String byteStrOne = readMessage.substring(0, 2);
					String byteStrTwo = readMessage.substring(2, 4);
					int a = Integer.parseInt(byteStrOne, 16);
					int b = Integer.parseInt(byteStrTwo, 16);
					b = (int) ((double) (a * 256 + b) / 4.0);
					respond(b, 0);
				} else if (mS.find()) {
					readMessage = mS.group(2);
					String byteStr = readMessage.substring(0, 2);
					int b = Integer.parseInt(byteStr, 16);
					respond(b, 1);
				} else if (mT.find()) {
					readMessage = mT.group(2);
					String byteStr = readMessage.substring(0, 2);
					int b = Integer.parseInt(byteStr, 16);
					b = b - 40;
					respond(b, 2);
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
						Toast.LENGTH_SHORT).show();

				if (SendTaskON) {
					// My AsyncTask is currently doing work in doInBackground()
					mySendTask.cancel(true);
				}

				break;
			}
		}
	};

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	/**
	 * @param value
	 * @param whichparamter
	 *            one for RPM, two for speed
	 */
	protected void respond(int value, int whichparamter) {
		String fragment1;
		Fragment fragmenttag = null;
//		Log.i("respond which", String.valueOf(whichparamter) + " and value: " + value);
		switch (whichparamter) {
		case 0: {
			if (!showRPMfrag) {
				break;
			}
			// Log.i("number zero", String.valueOf(value));
			try {
				fragment1 = getPlaceholderFragment1();
				fragmenttag = (PlaceholderFragment1) getFragmentManager().findFragmentByTag(
						fragment1);
				((PlaceholderFragment1) fragmenttag).changeText(value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		case 1: {
			if (!showSpeedfrag) {
				break;
			}
			// Log.i("number one", String.valueOf(value));
			try {
				fragment1 = getPlaceholderFragment2();
				fragmenttag = (PlaceholderFragment2) getFragmentManager().findFragmentByTag(
						fragment1);
				((PlaceholderFragment2) fragmenttag).changeText(value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		case 2: {
			if (!showTEMPfrag) {
				break;
			}
			// Log.i("number two", String.valueOf(value));
			try {
				fragment1 = getPlaceholderFragment3();
				fragmenttag = (PlaceholderFragment3) getFragmentManager().findFragmentByTag(
						fragment1);
				((PlaceholderFragment3) fragmenttag).changeText(value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		default:

		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private class SendTask extends AsyncTask<Void, Integer, Void> {
		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		protected Void doInBackground(Void... params) {
			// for (int i = 1; i < 5000; i++) {
			while (true) {
				sleep();
				publishProgress(3);// i * 10);
				if (isCancelled()) {
					// Log.e(TAG, "Exit the AsyncTask");
					break;
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (values[0] == 1) {
				sendMessage("ATE0" + "\r");
				// } else if ((values[0] % 2) == 0) {
			} else {
				if ((showRPMfrag) && (tabInView == 1))
					sendMessage("010C" + "\r");
				if ((showSpeedfrag) && (tabInView == 2))
					sendMessage("010D" + "\r");
				if ((showTEMPfrag) && (tabInView == 3))
					sendMessage("0105" + "\r");
				// sendMessage("010D" + "\r");
				// Log.i("send RPM",
				// String.valueOf(System.currentTimeMillis()));
				/*
				 * } else if (((values[0] % 2) != 0)&&((values[0] % 10)) {
				 * sendMessage("010D" + "\r"); } else if ((values[0] % 2) != 0)
				 */
			}
		}

		private void sleep() {
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	public void setPlaceholderFragment1(String t) {
		TabPlaceholderFragment1 = t;
	}

	public String getPlaceholderFragment1() {
		return TabPlaceholderFragment1;
	}

	public void setPlaceholderFragment2(String t) {
		TabPlaceholderFragment2 = t;
	}

	public String getPlaceholderFragment2() {
		return TabPlaceholderFragment2;
	}

	public void setPlaceholderFragment3(String t) {
		TabPlaceholderFragment3 = t;
	}

	public String getPlaceholderFragment3() {
		return TabPlaceholderFragment3;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		int numbertabs = 3;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			// return PlaceholderFragment.newInstance(position + 1);

			position = (!showRPMfrag) ? position++ : position;
			position = (!showSpeedfrag) ? position++ : position;
			Log.v("postion", String.valueOf(position));

			switch (position) {
			case 0:
				return new PlaceholderFragment1();
			case 1:
				return new PlaceholderFragment2();
			case 2:
				return new PlaceholderFragment3();
			default:
				return new PlaceholderFragment1();
			}

		}

		public void setCount(int t) {
			numbertabs = t;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return numbertabs;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment1 extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment1 newInstance(int sectionNumber) {
			PlaceholderFragment1 fragment = new PlaceholderFragment1();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment1() {
		}

		TextView textViewValue;
		TextView textView2;
		TextView textView3;
		SpeedometerView speedometer;
		private XYPlot aprHistoryPlot = null;
		private SimpleXYSeries azimuthHistorySeries = null;

		private static final int HISTORY_SIZE = 100;
		private Redrawer redrawer;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_live, container, false);
			/*
			 * TextView textView = (TextView) rootView
			 * .findViewById(R.id.section_label);
			 * textView.setText(Integer.toString(getArguments().getInt(
			 * ARG_SECTION_NUMBER)));
			 */
			textViewValue = (TextView) rootView.findViewById(R.id.tv_name2);

			String myTag = getTag();
			Log.e("myTag", myTag);

			((LiveActivity) getActivity()).setPlaceholderFragment1(myTag);

			speedometer = (SpeedometerView) rootView.findViewById(R.id.speedometer);
			// Add label converter
			speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
				@Override
				public String getLabelFor(double progress, double maxProgress) {
					return String.valueOf((int) Math.round(progress));
				}
			});

			// configure value range and ticks
			speedometer.setMaxSpeed(4000);
			speedometer.setMajorTickStep(1000);
			speedometer.setMinorTicks(500);

			// Configure value range colors
			speedometer.addColoredRange(1000, 2000, Color.GREEN);
			speedometer.addColoredRange(2000, 3000, Color.YELLOW);
			speedometer.addColoredRange(3000, 4000, Color.RED);

			speedometer.setLabelTextSize(34);

			speedometer.setSpeed(0, 800, 200);

			// setup the APR History plot:
			aprHistoryPlot = (XYPlot) rootView.findViewById(R.id.aprHistoryPlot);

			azimuthHistorySeries = new SimpleXYSeries("Speed");
			azimuthHistorySeries.useImplicitXVals();

			aprHistoryPlot.setRangeBoundaries(0, 4000, BoundaryMode.FIXED);
			aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);

			LineAndPointFormatter lineAndPointFormatter = new LineAndPointFormatter(Color.rgb(102,
					153, 0), null, null, null);
			// change the line width
			Paint paint = lineAndPointFormatter.getLinePaint();
			paint.setStrokeWidth(6);
			lineAndPointFormatter.setLinePaint(paint);
			aprHistoryPlot.addSeries(azimuthHistorySeries, lineAndPointFormatter);

			// aprHistoryPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
			aprHistoryPlot.setDomainStepValue(HISTORY_SIZE / 10);
			aprHistoryPlot.setTicksPerRangeLabel(3);
			// aprHistoryPlot.setDomainLabel("Sample Index");
			aprHistoryPlot.getDomainLabelWidget().pack();
			aprHistoryPlot.setRangeLabel("RPM");
			aprHistoryPlot.getRangeLabelWidget().pack();
			aprHistoryPlot.getLegendWidget().setVisible(false);

			aprHistoryPlot.setRangeValueFormat(new DecimalFormat("#"));
			aprHistoryPlot.setDomainValueFormat(new DecimalFormat("#"));

			redrawer = new Redrawer(Arrays.asList(new Plot[] { aprHistoryPlot }), 100, false);
			redrawer.start();

			return rootView;
		}

		@Override
		public void onDestroyView() {
			// TODO Auto-generated method stub
			super.onDestroyView();
			Log.e(TAG, "- Destroy draw -");
			redrawer.finish();
			speedometer.destroyDrawingCache();
		}

		public void changeText(int value) {
			// TODO Auto-generated method stub
			textViewValue.setText(String.valueOf(value));
			speedometer.setSpeed(value, 20, 10);

			if (azimuthHistorySeries.size() > HISTORY_SIZE) {

				azimuthHistorySeries.removeFirst();
			}
			azimuthHistorySeries.addLast(null, value);

		}

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment2 extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment2 newInstance(int sectionNumber) {
			PlaceholderFragment2 fragment = new PlaceholderFragment2();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment2() {
		}

		SpeedometerView speedometer;
		TextView textViewValue;
		private XYPlot aprHistoryPlot = null;
		private SimpleXYSeries azimuthHistorySeries = null;

		private static final int HISTORY_SIZE = 100;
		private Redrawer redrawer;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_live, container, false);
			/*
			 * TextView textView = (TextView) rootView
			 * .findViewById(R.id.section_label);
			 * textView.setText(Integer.toString(getArguments().getInt(
			 * ARG_SECTION_NUMBER)));
			 */
			textViewValue = (TextView) rootView.findViewById(R.id.tv_name2);

			String myTag = getTag();
			Log.e("myTag", myTag);

			((LiveActivity) getActivity()).setPlaceholderFragment2(myTag);

			speedometer = (SpeedometerView) rootView.findViewById(R.id.speedometer);
			// Add label converterhjj
			speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
				@Override
				public String getLabelFor(double progress, double maxProgress) {
					return String.valueOf((int) Math.round(progress));
				}
			});

			// configure value range and ticks
			speedometer.setMaxSpeed(300);
			speedometer.setMajorTickStep(30);
			speedometer.setMinorTicks(2);
			speedometer.setLabelTextSize(34);

			// Configure value range colors
			speedometer.addColoredRange(30, 140, Color.GREEN);
			speedometer.addColoredRange(140, 180, Color.YELLOW);
			speedometer.addColoredRange(180, 400, Color.RED);

			speedometer.setSpeed(0, 800, 200);

			// setup the APR History plot:
			aprHistoryPlot = (XYPlot) rootView.findViewById(R.id.aprHistoryPlot);

			azimuthHistorySeries = new SimpleXYSeries("Speed");
			azimuthHistorySeries.useImplicitXVals();

			LineAndPointFormatter lineAndPointFormatter = new LineAndPointFormatter(Color.rgb(102,
					153, 0), null, null, null);
			// change the line width
			Paint paint = lineAndPointFormatter.getLinePaint();
			paint.setStrokeWidth(6);
			lineAndPointFormatter.setLinePaint(paint);

			aprHistoryPlot.setRangeBoundaries(0, 200, BoundaryMode.FIXED);
			aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
			aprHistoryPlot.addSeries(azimuthHistorySeries, lineAndPointFormatter);

			// aprHistoryPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
			aprHistoryPlot.setDomainStepValue(HISTORY_SIZE / 10);
			aprHistoryPlot.setTicksPerRangeLabel(3);
			// aprHistoryPlot.setDomainLabel("Sample Index");
			aprHistoryPlot.getDomainLabelWidget().pack();
			aprHistoryPlot.setRangeLabel("Speed (kmph)");
			aprHistoryPlot.getRangeLabelWidget().pack();
			aprHistoryPlot.getLegendWidget().setVisible(false);

			aprHistoryPlot.setRangeValueFormat(new DecimalFormat("#"));
			aprHistoryPlot.setDomainValueFormat(new DecimalFormat("#"));

			redrawer = new Redrawer(Arrays.asList(new Plot[] { aprHistoryPlot }), 100, false);
			redrawer.start();

			return rootView;
		}

		@Override
		public void onDestroyView() {
			// TODO Auto-generated method stub
			super.onDestroyView();
			Log.e(TAG, "- Destroy draw -");
			redrawer.finish();
			speedometer.destroyDrawingCache();
		}

		public void changeText(int value) {
			// TODO Auto-generated method stub
			textViewValue.setText(String.valueOf(value));
			speedometer.setSpeed(value, 50, 10);

			if (azimuthHistorySeries.size() > HISTORY_SIZE) {

				azimuthHistorySeries.removeFirst();
			}
			azimuthHistorySeries.addLast(null, value);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment3 extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment3 newInstance(int sectionNumber) {
			PlaceholderFragment3 fragment = new PlaceholderFragment3();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment3() {
		}

		TextView textViewValue;
		SpeedometerView speedometer;
		private XYPlot aprHistoryPlot = null;
		private SimpleXYSeries azimuthHistorySeries = null;

		private static final int HISTORY_SIZE = 100;
		private Redrawer redrawer;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_live, container, false);
			/*
			 * TextView textView = (TextView) rootView
			 * .findViewById(R.id.section_label);
			 * textView.setText(Integer.toString(getArguments().getInt(
			 * ARG_SECTION_NUMBER)));
			 */
			String myTag = getTag();

			Log.e("myTag", myTag);

			((LiveActivity) getActivity()).setPlaceholderFragment3(myTag);

			/*
			 * if(getActivity().getResources().getConfiguration().orientation ==
			 * Configuration.ORIENTATION_PORTRAIT) {
			 */
			// code to do for Portrait Mode
			textViewValue = (TextView) rootView.findViewById(R.id.tv_name2);

			speedometer = (SpeedometerView) rootView.findViewById(R.id.speedometer);
			// Add label converter
			speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
				@Override
				public String getLabelFor(double progress, double maxProgress) {
					return String.valueOf((int) Math.round(progress));
				}
			});

			// configure value range and ticks
			speedometer.setMaxSpeed(300);
			speedometer.setMajorTickStep(50);
			speedometer.setMinorTicks(2);
			speedometer.setTemp(true);
			speedometer.setLabelTextSize(34);

			// Configure value range colors
			speedometer.addColoredRange(30, 140, Color.GREEN);
			speedometer.addColoredRange(140, 180, Color.YELLOW);
			speedometer.addColoredRange(180, 300, Color.RED);

			speedometer.setSpeed(50, 800, 200); // this means 0 degrees

			// setup the APR History plot:
			aprHistoryPlot = (XYPlot) rootView.findViewById(R.id.aprHistoryPlot);

			azimuthHistorySeries = new SimpleXYSeries("Speed");
			azimuthHistorySeries.useImplicitXVals();

			LineAndPointFormatter lineAndPointFormatter = new LineAndPointFormatter(Color.rgb(102,
					153, 0), null, null, null);
			// change the line width
			Paint paint = lineAndPointFormatter.getLinePaint();
			paint.setStrokeWidth(6);
			lineAndPointFormatter.setLinePaint(paint);

			aprHistoryPlot.setRangeBoundaries(-40, 200, BoundaryMode.FIXED);
			aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
			aprHistoryPlot.addSeries(azimuthHistorySeries, lineAndPointFormatter);

			// aprHistoryPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
			aprHistoryPlot.setDomainStepValue(HISTORY_SIZE / 10);
			aprHistoryPlot.setTicksPerRangeLabel(3);
			// aprHistoryPlot.setDomainLabel("Sample Index");
			aprHistoryPlot.getDomainLabelWidget().pack();
			aprHistoryPlot.setRangeLabel("Temp (°C)");
			aprHistoryPlot.getRangeLabelWidget().pack();
			aprHistoryPlot.getLegendWidget().setVisible(false);

			aprHistoryPlot.setRangeValueFormat(new DecimalFormat("#"));
			aprHistoryPlot.setDomainValueFormat(new DecimalFormat("#"));

			redrawer = new Redrawer(Arrays.asList(new Plot[] { aprHistoryPlot }), 100, false);
			redrawer.start();

			return rootView;
		}

		@Override
		public void onDestroyView() {
			// TODO Auto-generated method stub
			super.onDestroyView();
			Log.e(TAG, "- Destroy draw -");
			redrawer.finish();
			speedometer.destroyDrawingCache();
		}

		public void changeText(int value) {
			// TODO Auto-generated method stub
			textViewValue.setText(String.valueOf(value));
			int valueS = value + 50;
			if (value < 500) {
				speedometer.setSpeed(valueS, 50, 10);
			}
			if (azimuthHistorySeries.size() > HISTORY_SIZE) {

				azimuthHistorySeries.removeFirst();
			}
			azimuthHistorySeries.addLast(null, value);

		}
	}

}
