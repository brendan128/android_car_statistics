package com.bkapps.carapp.record;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bkapps.carapp.R;
import com.bkapps.carapp.utils.LocationUtils;
import com.bkapps.carapp.utils.MyFile;
import com.bkapps.carapp.utils.Tripp;
import com.bkapps.carapp.utils.Tripp.Point;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class RecordActivity extends Activity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private static final String TAG = "RecordActivity";
	private static final boolean D = true;
	String fileName = "";
	private static RecordFragment recordfragment;

	// A request to connect to Location Services
	private LocationRequest mLocationRequest;
	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;
	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;
	// Handle to a SharedPreferences editor
	SharedPreferences.Editor mEditor;
	boolean mUpdatesRequested = false;

	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothRecordService mChatService = null;
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

	private Tripp mytrip = null;
	private GetLocTask getLoctask;

	// private SendTask getSendTask;

	public Tripp getMytrip() {
		return mytrip;
	}

	public void setMytrip(Tripp mytrip) {
		this.mytrip = mytrip;
	}

	private double RPM = 0;
	private double SPEED = 0;
	private double TEMP = 0;
	private double AIR = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);

		setContentView(R.layout.activity_record);
		setRecordfragment(new RecordFragment());

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.containerRecord, recordfragment)
					.commit();
		}

		if (!((LocationManager) this.getSystemService(Context.LOCATION_SERVICE))
				.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// prompt user to enable gps
			displayPromptForEnablingGPS(this);
		} else {
			// gps is enabled
		}

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		mUpdatesRequested = false;
		mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		mEditor = mPrefs.edit();
		mLocationClient = new LocationClient(this, this, this);

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

	public static void displayPromptForEnablingGPS(final Activity activity) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		final String message = "Enable GPS " + " to find current location. Click OK to go to"
				+ " the location services settings.";

		builder.setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int id) {
				activity.startActivity(gpsOptionsIntent);
				d.dismiss();
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int id) {
				d.cancel();
			}
		});
		builder.create().show();
	}

	public static RecordFragment getRecordfragment() {
		return recordfragment;
	}

	public void setRecordfragment(RecordFragment recordfragment) {
		RecordActivity.recordfragment = recordfragment;
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v("WHERE", "onStop()");
		// If the client is connected
		// if (mLocationClient.isConnected()) {
		// stopPeriodicUpdates();
		// }
		// // After disconnect() is called, the client is considered "dead".
		// mLocationClient.disconnect();
		// if (mChatService != null)
		// mChatService.stop();
		// recordfragment.lostConnectionRecord("N");

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.v("WHERE", " onPause()");
		// // Save the current setting for updates
		// mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED,
		// mUpdatesRequested);
		// mEditor.commit();
		// recordfragment.lostConnectionRecord("N");
	}

	@Override
	public void onStart() {
		super.onStart();
		mLocationClient.connect();
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
	protected void onDestroy() {
		Log.v("WHERE", " onDestroy()");
		super.onDestroy();
		if (mChatService != null)
			mChatService.stop();

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v("WHERE", " onResume()");
		// If the app already has a setting for getting location updates, get it
		if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
			mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

			// Otherwise, turn off location updates until requested
		} else {
			mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
			mEditor.commit();
		}

		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothRecordService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (D)
			Log.i(TAG, "onActivityResult: request: " + requestCode + " result: " + resultCode);
		// Choose what to do based on the request code
		switch (requestCode) {
		// If the request code matches the code sent in onConnectionFailed
		case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:
			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK: {
				// Log the result
				Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
				// Display the result
				break;
			}
			// If any other result was returned by Google Play services
			default: {
				// Log the result
				Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
				// Display the result
				break;
			}
			}

		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(intent, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(intent, false);
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
		default:
			// Report that this Activity received an unknown requestCode
			Log.d(LocationUtils.APPTAG,
					getString(R.string.unknown_activity_request_code, requestCode));
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(DeviceListRecordActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	protected void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothRecordService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothRecordService.STATE_CONNECTED) {
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
			// Log.i(TAG, "mHandler: msg: " + msg.what + " result: " +
			// msg.arg1);
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothRecordService.STATE_CONNECTED:
					// setStatus(getString(R.string.title_connected_to,
					// mConnectedDeviceName));
					// mConversationArrayAdapter.clear();
					// mySendTask = new SendTask();
					// mySendTask.execute();
					setTitle("CONNECTED to OBD");
					recordfragment.startrecord();
					setProgressBarIndeterminateVisibility(false);
					break;
				case BluetoothRecordService.STATE_CONNECTING:
					// setStatus(R.string.title_connecting);
					setProgressBarIndeterminateVisibility(true);
					setTitle("CONNECTING..");
					break;
				case BluetoothRecordService.STATE_LISTEN:
					recordfragment.lostConnectionRecord("L");
					setTitle("LISTENING.. ");
					setProgressBarIndeterminateVisibility(false);
				case BluetoothRecordService.STATE_NONE:
					// setStatus(R.string.title_not_connected);
					recordfragment.lostConnectionRecord("N");
					setTitle("NOT CONNECTED to OBD");
					setProgressBarIndeterminateVisibility(false);

					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// Log.i(TAG, "writeMessage " + writeMessage);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;
				//
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				Log.i(TAG, "readMessage " + readMessage);
				readMessage = readMessage.replaceAll("(\\r|\\n)", "");
				readMessage = readMessage.replace(" ", "");
				Pattern pR = Pattern.compile("(410C)([A-F0-9]+)");
				Matcher mR = pR.matcher(readMessage);

				if (mR.find()) {
					readMessage = mR.group(2);
					CharSequence cs1 = "int";

					if (readMessage.contains("NO DATA")) {
						setRPM(0);
					} else {
						String byteStrOne = readMessage.substring(0, 2);
						String byteStrTwo = readMessage.substring(2, 4);
						int a = Integer.parseInt(byteStrOne, 16);
						int b = Integer.parseInt(byteStrTwo, 16);
						b = (int) ((double) (a * 256 + b) / 4.0);
						setRPM(b);
						break;
					}
				} // else
				Pattern pS = Pattern.compile("(410D)([A-F0-9]+)");
				Matcher mS = pS.matcher(readMessage);
				if (mS.find()) {
					readMessage = mS.group(2);
					if (readMessage.contains("NO DATA")) {
						setRPM(0);
					} else {
						String byteStr = readMessage.substring(0, 2);
						int b = Integer.parseInt(byteStr, 16);
						setSpeed(b);
						break;
					}
				} // else
				Pattern pT = Pattern.compile("(4105)([A-F0-9]+)");
				Matcher mT = pT.matcher(readMessage);
				if (mT.find()) {
					readMessage = mT.group(2);
					if (readMessage.contains("NO DATA")) {
						setRPM(0);
					} else {
						String byteStr = readMessage.substring(0, 2);
						int b = Integer.parseInt(byteStr, 16);
						b = b - 40;
						// TEMP = b;
						setTemp(b);
						break;
					}
				}
				Pattern pA = Pattern.compile("(4104)([A-F0-9]+)");
				Matcher mA = pA.matcher(readMessage);
				if (mA.find()) {
					readMessage = mA.group(2);
					if (readMessage.contains("NO DATA")) {
						setRPM(0);
					} else {
						String byteStr = readMessage.substring(0, 2);
						int b = Integer.parseInt(byteStr, 16);
						b = b * 100 / 255;
						// TEMP = b;
						setAir(b);
						break;
					}
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

				// if (mySendTask.getStatus() == AsyncTask.Status.RUNNING) {
				// // My AsyncTask is currently doing work in doInBackground()
				// mySendTask.cancel(true);
				// }

				break;
			}
		}
	};

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			// Log.d(LocationUtils.APPTAG,
			// getString(R.string.play_services_available));

			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
				// DialogFragment newFragment =
				// MyAlertDialogFragment.newInstance(
				// "hjow are ya now?");
				// newFragment.show(getFragmentManager(), "dialog");
			}
			return false;
		}
	}

	public Location getLocation() {

		// If Google Play Services is available
		if (servicesConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();
			return currentLocation;
		}
		return null;
	}

	@SuppressLint("NewApi")
	public void getAddress() {

		// In Gingerbread and later, use Geocoder.isPresent() to see if a
		// geocoder is available.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
			// No geocoder is present. Issue an error message
			Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
			return;
		}

		if (servicesConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();

			// Turn the indefinite activity indicator on
			recordfragment.mActivityIndicator.setVisibility(View.VISIBLE);

			// Start the background task
			(new RecordActivity.GetAddressTask(this)).execute(currentLocation);
		}
	}

	/**
	 * Invoked by the "Start Updates" button Sends a request to start location
	 * updates
	 * 
	 * @param v
	 *            The view object associated with this method, in this case a
	 *            Button.
	 */
	public void startUpdates(View v) {
		mUpdatesRequested = true;

		if (servicesConnected()) {
			startPeriodicUpdates();
		}
	}

	/**
	 * Invoked by the "Stop Updates" button Sends a request to remove location
	 * updates request them.
	 * 
	 * @param v
	 *            The view object associated with this method, in this case a
	 *            Button.
	 */
	public void stopUpdates(View v) {
		mUpdatesRequested = false;

		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		// placeholderFragment1.mConnectionStatus.setText(R.string.connected);
		// placeholderFragment1.mConnectionStatusBool =true;

		if (mUpdatesRequested) {
			startPeriodicUpdates();
		}
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// recordfragment.mConnectionStatus.setText(R.string.disconnected);
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */

			} catch (IntentSender.SendIntentException e) {

				// Log the error
				e.printStackTrace();
			}
		} else {

			// If no resolution is available, display a dialog to the user with
			// the error.
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	/**
	 * Report location updates to the UI.
	 * 
	 * @param location
	 *            The updated location.
	 */
	@Override
	public void onLocationChanged(Location location) {

		// Report to the UI that the location was updated
		// mConnectionStatus.setText(R.string.location_updated);

		// In the UI, set the latitude and longitude to the value received
		// mLatLng.setText(LocationUtils.getLatLng(this, location));
	}

	/**
	 * In response to a request to start updates, send a request to Location
	 * Services
	 */
	private void startPeriodicUpdates() {

		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		// mConnectionState.setText(R.string.location_requested);
	}

	/**
	 * In response to a request to stop updates, send a request to Location
	 * Services
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
		// mConnectionState.setText(R.string.location_updates_stopped);
	}

	/**
	 * An AsyncTask that calls getFromLocation() in the background. The class
	 * uses the following generic types: Location - A
	 * {@link android.location.Location} object containing the current location,
	 * passed as the input parameter to doInBackground() Void - indicates that
	 * progress units are not used by this subclass String - An address passed
	 * to onPostExecute()
	 */
	protected class GetAddressTask extends AsyncTask<Location, Void, String> {

		// Store the context passed to the AsyncTask when the system
		// instantiates it.
		Context localContext;

		// Constructor called by the system to instantiate the task
		public GetAddressTask(Context context) {

			// Required by the semantics of AsyncTask
			super();

			// Set a Context for the background task
			localContext = context;
		}

		/**
		 * Get a geocoding service instance, pass latitude and longitude to it,
		 * format the returned address, and return the address to the UI thread.
		 */
		@Override
		protected String doInBackground(Location... params) {
			/*
			 * Get a new geocoding service instance, set for localized
			 * addresses. This example uses android.location.Geocoder, but other
			 * geocoders that conform to address standards can also be used.
			 */
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());
			// Get the current location from the input parameter list
			Location location = params[0];
			// Create a list to contain the result address
			List<Address> addresses = null;
			// Try to get an address for the current location. Catch IO or
			// network problems.
			try {
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);

				// Catch network or other I/O problems.
			} catch (IOException exception1) {

				// Log an error and return an error message
				Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));

				// print the stack trace
				exception1.printStackTrace();
				return "";

				// Catch incorrect latitude or longitude values
			} catch (IllegalArgumentException exception2) {

				// Construct a message containing the invalid arguments
				String errorString = getString(R.string.illegal_argument_exception,
						location.getLatitude(), location.getLongitude());
				// Log the error and print the stack trace
				Log.e(LocationUtils.APPTAG, errorString);
				exception2.printStackTrace();
				return "";
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {

				// Get the first address
				Address address = addresses.get(0);

				// Format the first line of address
				String addressText = getString(R.string.address_output_string,

				// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",

						// Locality is usually a city
						address.getLocality());
				// address.getCountryName());

				// Return the text
				return addressText;

				// If there aren't any addresses, post a message
			} else {
				// return getString(R.string.no_address_found);
				return "";
			}
		}

		/**
		 * A method that's called once doInBackground() completes. Set the text
		 * of the UI element that displays the address. This method runs on the
		 * UI thread.
		 */
		@Override
		protected void onPostExecute(String address) {

			// Turn off the progress bar
			recordfragment.mActivityIndicator.setVisibility(View.GONE);
			Log.v("myAddress", "ad is :" + address);
			recordfragment.changeName(address);
		}

	}

	/**
	 * Show a dialog returned by Google Play services for the connection error
	 * code
	 * 
	 * @param errorCode
	 *            An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode) {

		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this,
				LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
		}
	}

	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog
		 *            An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class RecordFragment extends Fragment {

		String fileName = null;

		private int running = 0;

		boolean mConnectionStatusBool = false;
		private EditText et_Name;
		private TextView tv_Filename;
		private TextView tv_name;

		// Handles to UI widgets
		private TextView mLocation;
		private TextView mSpeed;
		private TextView mRPM;
		private TextView mTemp;
		private TextView mAir;
		private TextView tv_info;
		private ProgressBar mActivityIndicator;
		// Button button_stop;
		Button button_start;
		Button button_getAddress;
		Spinner freqSpinner;
		ProgressBar mProgress;
		private int frequency;
		MyOnItemSelectedListener selecetlistener;

		public RecordFragment() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_record, container, false);
			mProgress = (ProgressBar) rootView.findViewById(R.id.progressBarStart);
			tv_info = (TextView) rootView.findViewById(R.id.tv_info);
			tv_info.setText("\n\n     HOW TO RECORD\n 1. Please ensure the OBD device is paired.\n 2.Fill in a name for your Trip above. \n    (address button will find your address!)\n 3. Select frequency of recording. \n 4. Start your Engine!\n 5. Press START RECORD! \n    (Speed > 5 km/h to make Trip!)");
			mAir = (TextView) rootView.findViewById(R.id.tv_air);
			mSpeed = (TextView) rootView.findViewById(R.id.tv_Speed);
			mRPM = (TextView) rootView.findViewById(R.id.tv_rpm);
			mTemp = (TextView) rootView.findViewById(R.id.tv_temp);
			tv_name = (TextView) rootView.findViewById(R.id.tv_name);
			mProgress.setVisibility(View.GONE);
			mSpeed.setText("");
			mSpeed.setVisibility(View.GONE);
			mRPM.setText("");
			mRPM.setVisibility(View.GONE);
			mTemp.setText("");
			mTemp.setVisibility(View.GONE);
			mAir.setText("");
			mAir.setVisibility(View.GONE);
			mLocation = (TextView) rootView.findViewById(R.id.lat_lng);
			mLocation.setText("");
			mLocation.setVisibility(View.GONE);

			tv_Filename = (TextView) rootView.findViewById(R.id.tv_filename);
			tv_Filename.setText("");
			tv_Filename.setVisibility(View.GONE);

			et_Name = (EditText) rootView.findViewById(R.id.et_name);
			freqSpinner = (Spinner) rootView.findViewById(R.id.spinner1);
			// Create an ArrayAdapter using the string array and a default
			// spinner layout
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
					R.array.record_frequency_array, android.R.layout.simple_spinner_item);
			// Specify the layout to use when the list of choices appears
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			freqSpinner.setAdapter(adapter);
			SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
			int position = sharedPref.getInt("NEWPOS", 2);
			freqSpinner.setSelection(position);
			selectedFreq(position);

			selecetlistener = new MyOnItemSelectedListener();
			freqSpinner.setOnItemSelectedListener(selecetlistener);

			mActivityIndicator = (ProgressBar) rootView.findViewById(R.id.address_progress);

			button_getAddress = (Button) rootView.findViewById(R.id.button_address_get);
			button_getAddress.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((RecordActivity) getActivity()).getAddress();
				}
			});

			/*
			 * button_stop = (Button) rootView.findViewById(R.id.button_stop);
			 * button_stop.setOnClickListener(new View.OnClickListener() {
			 * 
			 * @Override public void onClick(View v) {
			 * 
			 * Intent serverIntent = new Intent(getActivity(),
			 * DeviceListRecordActivity.class);
			 * startActivityForResult(serverIntent,
			 * REQUEST_CONNECT_DEVICE_SECURE); } });
			 */

			button_start = (Button) rootView.findViewById(R.id.button_start);
			button_start.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_record, 0, 0,
					0);
			button_start.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (running == 2) {
						StopNow();
					} else {
						StartNow();
					}
				}
			});

			Log.v("running", "running :" + running);
			return rootView;
		}

		private String text = null;

		public String getText() {
			return text;
		}

		private void setText(String text) {
			this.text = text;
		}

		/**
		 * in here the request is made to connect to bluetooth else
		 * startrecord() can be used without connection
		 */
		public void StartNow() {

			setText(et_Name.getText().toString().trim());
			if ((text.isEmpty() || text.length() < 2 || text.equals("") || text == null)) {
				running = 0;
			} else {
				running = 1;
			}

			switch (running) {
			case 0:
				Toast.makeText(getActivity(), "Give name!", Toast.LENGTH_SHORT).show();
				break;
			case 1:
				tv_info.setVisibility(View.GONE);
				try {
					((RecordActivity) getActivity()).ConnectToOBD();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				Toast.makeText(getActivity(), "RUNNING!", Toast.LENGTH_SHORT).show();
				break;
			}
		}

		public void startrecord() {
			Log.e(TAG, "IN startrecord() device connected ..");
			fileName = text.replace(" ", "");
			fileName = fileName.replaceAll("[^\\x00-\\x7F]", "");
			Log.v("filename", "msg: " + fileName);
			this.fileName = fileName + ".json";
			if (checkIfAlready(fileName)) {
				running = 0;
				Log.v("running", "running :" + running);
				Toast.makeText(getActivity(), "Name already used!", Toast.LENGTH_SHORT).show();

			} else {

				running = 2;

				((RecordActivity) getActivity()).startGetLocTask(fileName);
				tv_Filename.setText("FILENAME: " + fileName + "\n");
				tv_Filename.setVisibility(View.VISIBLE);
				// et_Name.setEnabled(false);
				// freqSpinner.setEnabled(false);
				et_Name.setVisibility(View.GONE);
				freqSpinner.setVisibility(View.GONE);
				tv_name.setVisibility(View.GONE);
				button_getAddress.setVisibility(View.GONE);
				mSpeed.setVisibility(View.GONE);
				tv_info.setVisibility(View.GONE);

				// mProgress.setVisibility(View.VISIBLE);
				mAir.setVisibility(View.VISIBLE);
				mSpeed.setVisibility(View.VISIBLE);
				mRPM.setVisibility(View.VISIBLE);
				mTemp.setVisibility(View.VISIBLE);
				mLocation.setVisibility(View.VISIBLE);

				button_start.setBackgroundColor(Color.RED);
				button_start.setText("STOP");
				button_start.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_stop, 0,
						0, 0);
			}
		}

		public void lostConnectionRecord(String string) {
			Log.e(TAG, "lostConnectionRecord string:" + string);
			StopNow();

		}

		private Boolean checkIfAlready(String text) {
			// TODO Auto-generated method stub
			MyFile myfile = new MyFile(getActivity());
			return myfile.checkName(text);
		}

		public void StopNow() {
			Log.e(TAG, "IN stopNow().");

			if (running == 2) {
				button_start.setBackgroundResource(R.drawable.roundedbutton);
				// button_start.refreshDrawableState();
				button_start.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_record,
						0, 0, 0);
				button_start.setText("START RECORD");
				try {
					((RecordActivity) getActivity()).stopGetLocTask();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				running = 0;

				Log.v("running", "running :" + running);
				et_Name.setText("");
				// et_Name.setEnabled(true);
				// freqSpinner.setEnabled(true);
				et_Name.setVisibility(View.VISIBLE);
				freqSpinner.setVisibility(View.VISIBLE);
				tv_name.setVisibility(View.VISIBLE);
				button_getAddress.setVisibility(View.VISIBLE);

				// mProgress.setVisibility(View.GONE);
				mAir.setVisibility(View.GONE);
				mSpeed.setVisibility(View.GONE);
				mRPM.setVisibility(View.GONE);
				mTemp.setVisibility(View.GONE);
				mLocation.setVisibility(View.GONE);
				tv_Filename.setVisibility(View.GONE);
			}
		}

		public void selectedFreq(int position) {
			// TODO Auto-generated method stub
			SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt("NEWPOS", position);
			editor.commit();
			switch (position) {
			case 1:
				setFrequency(2000);
				break;
			case 2:
				setFrequency(3000);
				break;
			case 3:
				setFrequency(5000);
				break;
			case 4:
				setFrequency(8000);
				break;
			case 5:
				setFrequency(10000);
				break;
			case 6:
				setFrequency(20000);
				break;
			}
		}

		public void updateAll(Point progress) {
			// TODO Auto-generated method stub
			mLocation.setText("Location: " + progress.getLocation());
			mSpeed.setText("Speed: " + progress.getSpeed() + " km/h");
			mRPM.setText("RPM: " + progress.getRPM() + " rpm");
			mTemp.setText("Temp: " + progress.getTemp() + " °C");
			mAir.setText("Load: " + progress.getLoad() + " %");
		}

		public void changeName(String address) {
			// TODO Auto-generated method stub
			et_Name.setText(address);
		}

		public int getFrequency() {
			return frequency;
		}

		public void setFrequency(int frequency) {
			this.frequency = frequency;
		}
	}

	public synchronized void setTemp(int temp) {
		TEMP = temp;
	}

	public synchronized double getTemp() {
		return TEMP;
	}

	public synchronized void setRPM(int rpm) {
		RPM = rpm;
	}

	public synchronized double getRPM() {
		return RPM;
	}

	public synchronized void setSpeed(int speed) {
		SPEED = speed;
	}

	public synchronized double getSpeed() {
		return SPEED;
	}

	public synchronized void setAir(int air) {
		AIR = air;
	}

	public synchronized double getAir() {
		return AIR;
	}

	private class GetLocTask extends AsyncTask<String, Point, Void> {

		private Context mContext;

		public GetLocTask(Context context) {
			mContext = context;
		}

		protected Void doInBackground(final String... names) {

			Calendar c = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
			String date = df.format(c.getTime());
			setMytrip(new Tripp(recordfragment.getText(), date));
			Tripp.Point point;
			double distance = 0;
			double avgRPM = 0;
			double avgSpeed = 0;
			double avgTemp = 0;
			setRPM(0);
			setTemp(0);
			setSpeed(0);
			setAir(0);
			Location cloc_last = null;
			Location cloc = null;
			long time = 0;
			long start = System.currentTimeMillis();
			Boolean startspeed = false;
			
			ArrayList<Point> pointlist = new ArrayList<Point>();
//			((RecordActivity) mContext).sendMessage("AT Z" + "\r");
//			sleep(1000);
//			((RecordActivity) mContext).sendMessage("AT SP 0" + "\r");
//			sleep(1000);
			Log.v("task", "all sent");
			
			while (true) {
				// int n = rand.nextInt(150) + 1;
				((RecordActivity) mContext).sendMessage("010C" + "\r");
				sleep(200);
				((RecordActivity) mContext).sendMessage("010D" + "\r");
				sleep(200);
				((RecordActivity) mContext).sendMessage("0105" + "\r");
				sleep(200);
				((RecordActivity) mContext).sendMessage("0104" + "\r");
				sleep(200);
				((RecordActivity) mContext).sendMessage("010C" + "\r");

				cloc = getLocation();
				try {
					double alt = cloc.getAltitude() + 100; // just to be sure !!
					String loc = LocationUtils.getLatLng(mContext, cloc);
					if (cloc_last != null) {
						distance = distance + cloc_last.distanceTo(cloc);
					}
					cloc_last = cloc;
					String speed = String.valueOf(getSpeed());
					String rpm = String.valueOf(getRPM());
					String temp = String.valueOf(getTemp());
					String load = String.valueOf(getAir());
					String alte = String.valueOf(alt);
					// Log.i("task", "S R T " + speed + " " + rpm + " " + temp);
					point = mytrip.new Point(loc, speed, alte, rpm, temp, load);
					publishProgress(point);
					if (startspeed) {
						pointlist.add(point);
						mytrip.setPointslist(pointlist);
						avgRPM = avgRPM + getRPM();
						avgSpeed = avgSpeed + getSpeed();
						avgTemp = avgTemp + getTemp();
					} else if (getSpeed() > 5) {
						startspeed = true;
						pointlist.add(point);
						mytrip.setPointslist(pointlist);
						avgRPM = avgRPM + getRPM();
						avgSpeed = avgSpeed + getSpeed();
						avgTemp = avgTemp + getTemp();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			sleep(recordfragment.getFrequency() - 800);
				// Escape early if cancel() is called
				if (isCancelled()) {
					if (startspeed) {
						// Log.v("pointlist", "complete is : " +
						// mytrip.getPointlistSize());
						double size = mytrip.getPointlistSize();
						mytrip.setDistance(String.format("%.2f", distance / 1000));
						mytrip.setFrequency((String.valueOf(recordfragment.getFrequency())));
						mytrip.setAvgRPM(String.format("%.2f", avgRPM / size));
						mytrip.setAvgSpeed(String.format("%.2f", avgSpeed / size));
						mytrip.setAvgTemp(String.format("%.2f", avgTemp / size));
						// Log.i("task", "AvgS R T " + mytrip.getAvgSpeed() +
						// " " + mytrip.getAvgRPM() + " " +
						// mytrip.getAvgTemp());
						long finish = System.currentTimeMillis();
						time = finish - start;
						mytrip.setTime((String.format(
								"%d min, %d sec",
								TimeUnit.MILLISECONDS.toMinutes(time),
								TimeUnit.MILLISECONDS.toSeconds(time)
										- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
												.toMinutes(time)))));

						new Thread(new Runnable() {
							public void run() {
								MyFile file = new MyFile(mContext);
								file.saveFile(names[0], getMytrip());
							}
						}).start();
						break;
					}
					break;
				}
			}
			return null;
		}

		protected void onProgressUpdate(Point... progress) {
			recordfragment.updateAll(progress[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			recordfragment.StopNow();
			
		}

		private void sleep(int time) {
			try {
				Thread.sleep(time); // 10 seconds
			} catch (InterruptedException e) {
				Log.e("trouble", e.toString());
			}
		}
	}

	public void ConnectToOBD() {
		// TODO Auto-generated method stub
		Intent serverIntent = new Intent(this, DeviceListRecordActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
	}

	public void stopGetLocTask() {
		Log.e(TAG, "IN stopGetLocTask() ");
		mUpdatesRequested = false;
		getLoctask.cancel(true);
		// getSendTask.cancel(true);
		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
//		Toast.makeText(this, "File Saved: ", Toast.LENGTH_SHORT).show();

	}

	public void startGetLocTask(String filename) {
		Log.e(TAG, "IN startGetLocTask() ");
		mUpdatesRequested = true;

		if (servicesConnected()) {
			startPeriodicUpdates();
		}

		getLoctask = new GetLocTask(this);
		getLoctask.execute(filename);
		// getSendTask = new SendTask();
		// getSendTask.execute();

	}

	//
	// private class SendTask extends AsyncTask<Void, Integer, Void> {
	// /**
	// * The system calls this to perform work in a worker thread and delivers
	// * it the parameters given to AsyncTask.execute()
	// */
	// protected Void doInBackground(Void... params) {
	// // for (int i = 1; i < 5000; i++) {
	// // sendMessage("ATE0" + "\r");
	// while (true) {
	// sleep(recordfragment.getFrequency()-1000);
	// publishProgress(1);
	//
	// // sendMessage("010C" + "\r");
	// // sleep(400);
	// // sendMessage("010D" + "\r");
	// // sleep(400);
	// // sendMessage("0105" + "\r");
	// // publishProgress(2);
	// // sleep(400);
	// // publishProgress(3);
	// if (isCancelled()) {
	// // Log.e(TAG, "Exit the AsyncTask");
	// break;
	// }
	// }
	// return null;
	// }
	//
	// @Override
	// protected void onProgressUpdate(Integer... values) {
	// sendMessage("010C" + "\r");
	// }
	//
	// private void sleep(int time) {
	// try {
	// Thread.sleep(time);
	// } catch (InterruptedException e) {
	// Log.e(TAG, e.toString());
	// }
	// }
	// }
}
