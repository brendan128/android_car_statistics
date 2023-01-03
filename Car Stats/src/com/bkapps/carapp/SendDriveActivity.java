package com.bkapps.carapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.bkapps.carapp.utils.BaseDriveActivity;
import com.bkapps.carapp.utils.MyFile;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

/**
 * @author Brendan
 * use 0 as an Intent for kml
 * use 1 to send json
 * use 2 to send Geojson
 *
 */
public class SendDriveActivity extends BaseDriveActivity {

	private static final int REQUEST_CODE_CREATOR = 2;

	@Override
	public void onConnected(Bundle connectionHint) {
		super.onConnected(connectionHint);
		// create new contents resource
		Drive.DriveApi.newContents(getGoogleApiClient()).setResultCallback(newContentsCallback);
	}

	ResultCallback<ContentsResult> newContentsCallback = new ResultCallback<ContentsResult>() {
		@Override
		public void onResult(ContentsResult result) {

			// Get an output stream for the contents.
			OutputStream outputStream = result.getContents().getOutputStream();
			File f = null;
			IntentSender intentSender = null;
			String filename;
			switch (getTypeFile()) {
			case 0:
				f = myfile.writeLogtokml(getPositionNum());
				try {
					byte[] readbyte = readBytesFromFile(f);
					outputStream.write(readbyte);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					Log.i("TAG", "Unable to write file contents.");
				}
				filename = f.getName();

				MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
						.setMimeType("application/vnd.google-earth.kml+xml").setTitle(filename).build();
				intentSender = Drive.DriveApi.newCreateFileActivityBuilder()
						.setInitialMetadata(metadataChangeSet).setInitialContents(result.getContents())
						.build(getGoogleApiClient());
				break;
			case 1:
				f = myfile.sendFilePosition(getPositionNum());
				try {
					byte[] readbyte = readBytesFromFile(f);
					outputStream.write(readbyte);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					Log.i("TAG", "Unable to write file contents.");
				}
				filename = f.getName();

				MetadataChangeSet metadataChangeSet1 = new MetadataChangeSet.Builder()
						.setMimeType("text/plain").setTitle(filename).build();
				intentSender = Drive.DriveApi.newCreateFileActivityBuilder()
						.setInitialMetadata(metadataChangeSet1).setInitialContents(result.getContents())
						.build(getGoogleApiClient());
				break;
			case 2:	
				f = myfile.writeLogtoGeojson(getPositionNum());
				try {
					byte[] readbyte = readBytesFromFile(f);
					outputStream.write(readbyte);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					Log.i("TAG", "Unable to write file contents.");
				}
				filename = f.getName();

				MetadataChangeSet metadataChangeSet11 = new MetadataChangeSet.Builder()
						.setMimeType("text/plain").setTitle(filename).build();
				intentSender = Drive.DriveApi.newCreateFileActivityBuilder()
						.setInitialMetadata(metadataChangeSet11).setInitialContents(result.getContents())
						.build(getGoogleApiClient());
				break;
			}

			

			try {
				startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
			} catch (SendIntentException e) {
				Log.w("Problems", "Unable to send intent", e);
			}
		}
	};

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		Log.v("onActivityResult", "requestCode " + requestCode + " resultCode :" + resultCode);
		switch (requestCode) {

		case REQUEST_CODE_CREATOR:
			// Called after a file is saved to Drive.
			if (resultCode == RESULT_OK) {
				Log.i("Problems", "File successfully saved.");
				showMessage("Completed Successfully");
				// Just start the camera again for another photo.
				finish();
			}
			if (resultCode == RESULT_CANCELED) {
				Log.i("Problems", "File Cancelled ");
				showMessage("Cancelled");
				// Just start the camera again for another photo.
				finish();
			}
			break;
		case 1:
			if (resultCode == 0) {
			finish();
			break;
			}
		}
	}

	private int positionNum;
	private int TypeFile;
	MyFile myfile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_drive);
		Intent intent = getIntent();
		setPositionNum(intent.getIntExtra("positionkey", 0));
		setTypeFile(intent.getIntExtra("KML", 0));
		myfile = new MyFile(this);
	}

	public int getPositionNum() {
		return positionNum;
	}

	public void setPositionNum(int positionNum) {
		this.positionNum = positionNum;
	}

	public int getTypeFile() {
		return TypeFile;
	}

	public void setTypeFile(int i) {
		TypeFile = i;
	}

	public static byte[] readBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			throw new IOException("Could not completely read file " + file.getName()
					+ " as it is too long (" + length + " bytes, max supported "
					+ Integer.MAX_VALUE + ")");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

}
