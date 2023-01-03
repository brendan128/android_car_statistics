package com.bkapps.carapp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.bkapps.carapp.utils.Tripp.Point;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class MyFile {

	private List<LatLng> points;
	// private MainActivity context;
	private File dataDir;
	private File dataKmlDir;
	private File dataFuelDir;
	private String filename;
	private Context context;

	public MyFile(Context context) {
		this.context = context;
		this.points = new ArrayList<LatLng>();
		if (this.hasSdCard()) {
			this.dataDir = new File(this.getDir()); // log one
			this.dataKmlDir = new File(this.getKMLDir()); // Kml one
			this.dataFuelDir = new File(this.getFuelDir()); // Fuel one
			// context.trace("data dir : " + dataDir.getPath());
		} else {
			// context.trace("SD Card not Found");
			// new
			// AlertDialog.Builder(context).setMessage("this app uses SD Card")
			// .setPositiveButton("OK", null).show();
		}
		/*
		 * try { this.loadLog(); } catch (Exception e) { Log.e("GpsTracker",
		 * e.getMessage()); }
		 */
	}

	public String getDir() {
		if (!this.hasSdCard())
			return "";
		dataDir = new File(Environment.getExternalStorageDirectory(), context.getPackageName()
				+ "/log");
		if (!dataDir.exists())
			dataDir.mkdirs();
		return dataDir.getPath();
	}

	public boolean hasSdCard() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public String getKMLDir() {
		if (!this.hasSdCard())
			return "";
		dataKmlDir = new File(Environment.getExternalStorageDirectory(), context.getPackageName()
				+ "/Kml");
		if (!dataKmlDir.exists())
			dataKmlDir.mkdirs();
		return dataKmlDir.getPath();
	}

	public String getFuelDir() {
		if (!this.hasSdCard())
			return "";
		dataFuelDir = new File(Environment.getExternalStorageDirectory(), context.getPackageName()
				+ "/Fuel");
		if (!dataFuelDir.exists())
			dataFuelDir.mkdirs();
		return dataFuelDir.getPath();
	}

	public List<LatLng> getPoints() {
		return this.points;
	}

	public int size() {
		return this.points.size();
	}

	public LatLng getPoint(int index) {
		return this.points.get(index);
	}

	public void add(double lat, double lon) {

		LatLng p = new LatLng(lat, lon);

		this.points.add(p);
		try {
			saveLog(p);
		} catch (Exception e) {
			Log.e("GpsTracker", e.getMessage());
		}
	}

	public LatLng lastPoint() {
		return this.points.get(this.points.size() - 1);
	}

	/*
	 * public Boolean writePointsToFile() throws Exception { writeLogtokml();
	 * return true;
	 * 
	 * }
	 */

	/**
	 * @param position
	 * @param fileKmlname
	 * @param useName
	 *            - if its true it will use the name you provided
	 * @return
	 */
	// public void writeLogtokml(int position, String fileKmlname, Boolean
	// useName) {
	public File writeLogtokml(int position) {
		if (dataKmlDir == null) {
			return null;
		}
		Tripp mytripp = new Tripp(null, null, null);
		mytripp = readFilePosition(position);
		ArrayList<Point> mypoints = mytripp.getPointslist();
		// if (useName==false) {
		String fileKmlname = nameFromFilePosition(position);
		// }
		// p.getYear()+"-"+p.getMonth()+"-"+p.getDay());
		File f = new File(this.dataKmlDir, fileKmlname + ".kml");
		// OutputStream os = new FileOutputStream(f, true);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(f);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		BufferedWriter outWriter = new BufferedWriter(fileWriter);

		try {
			outWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "\n <kml xmlns=\"http://www.opengis.net/kml/2.2\">" + "\n <Document>" + "\n"
					+ "<name>"
					+ mytripp.getName()
					+ "</name>"
					+ "\n"
					+ "<description>" 
					+ "Average Speed "+mytripp.getAvgSpeed() +""+ "\n"
					+ "Average Speed "+mytripp.getAvgRPM() +""+ "\n"
					+ "Average Speed "+mytripp.getAvgTemp() +""+ "\n"

					+ "</description>"
					+ "\n"
					+ "<Style id=\"yellowLineGreenPoly\">"
					+ "\n"
					+ "<LineStyle>"
					+ "\n"
					+ "<color>5014F0FF</color>"
					+ "\n"
					+ "<width>11</width>"
					+ "\n"
					+ "</LineStyle>"
					+ "\n"
					+ "<PolyStyle>"
					+ "\n"
					+ "<color>501400FF</color>"
					+ "\n"
					+ "</PolyStyle>"
					+ "\n"
					+ "</Style>"
					+ "\n"
					+ "<Placemark>"
					+ "\n"
					+ "<name>Absolute Extruded</name>"
					+ "\n"
					+ "<description>Transparent green wall with yellow outlines</description>"
					+ "\n"
					+ "<styleUrl>#yellowLineGreenPoly</styleUrl>"
					+ "\n"
					+ "<LineString>"
					+ "\n"
					+ "<extrude>1</extrude>"
					+ "\n"
					+ "<tessellate>2</tessellate>"
					+ "\n"
					+ "<altitudeMode>absolute</altitudeMode>" + "\n" + "<coordinates>" + "\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (Point temp : mypoints) {
			String[] geo = temp.getLocation().split(",");
			LatLng p = new LatLng(Double.parseDouble(geo[0]), Double.parseDouble(geo[1]));
			double lat = p.latitude;
			double lon = p.longitude;
			// String alt = temp.getAltitude();
			String alt = temp.getSpeed();
			double value = Double.parseDouble(alt);
			double valuehere = (value * 5)+20;
			String str = new String(lon + "," + lat + "," + String.format("%.2f", valuehere) + "\n");
			try {
				outWriter.write(str);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			outWriter.write("</coordinates>" + "\n" + "</LineString>" + "\n" + "</Placemark>"
					+ "\n" + "</Document>" + "\n" + "</kml>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			outWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return f;
	}

	public File writeLogtoGeojson(int position) {
		if (dataKmlDir == null) {
			return null;
		}
		Tripp mytripp = new Tripp(null, null, null);
		mytripp = readFilePosition(position);
		ArrayList<Point> mypoints = mytripp.getPointslist();
		// if (useName==false) {
		String fileKmlname = nameFromFilePosition(position);
		// }
		// p.getYear()+"-"+p.getMonth()+"-"+p.getDay());
		File f = new File(this.dataKmlDir, fileKmlname + ".geojson");
		// OutputStream os = new FileOutputStream(f, true);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(f);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		BufferedWriter outWriter = new BufferedWriter(fileWriter);

		try {
			outWriter.write(
					
					
					 "{"+ "\n"
  +"\"type\": \"FeatureCollection\","+ "\n"
  +"\"features\": ["+ "\n"
    +"{"+ "\n"
      +"\"type\": \"Feature\","+ "\n"
      +"\"geometry\": {"+ "\n"
        +"\"type\": \"LineString\","+ "\n"
        +"\"coordinates\": ["+ "\n"
        
        
					);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int count = 0;
		for (Point temp : mypoints) {
			String[] geo = temp.getLocation().split(",");
			LatLng p = new LatLng(Double.parseDouble(geo[0]), Double.parseDouble(geo[1]));
			double lat = p.latitude;
			double lon = p.longitude;
//			String alt = temp.getAltitude();

			if ((mypoints.size()-1)!=count){
				String str1 = new String("["+lon + "," + lat + "]" + ",\n");
				try {
					outWriter.write(str1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String str2 = new String("["+lon + "," + lat + "]" + "\n");
				try {
					outWriter.write(str2);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			count++;
		}
		try {
			outWriter.write(
			        "]"+ "\n"
			         +   "},"+ "\n"
			          +  "\"properties\": {"+ "\n"
			           +   "\"Name\":\"" +mytripp.getName()+"\","+ "\n"
			            +  "\"Des1\":\"" +mytripp.getAvgSpeed()+" km/h\""+","+ "\n"
			            +  "\"Des2\":\"" +mytripp.getAvgRPM()+" rpm\""+","+ "\n"
			            +  "\"Des3\":\"" +mytripp.getAvgTemp()+" degrees\""+""+ "\n"
			          +  "}"+ "\n"
			         + "}"+ "\n"
			        +"]"+ "\n"
			     + "}"+ "\n"
			        );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			outWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return f;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void writeFuelslist(ArrayList<Trip> tripList) {
		if (dataFuelDir == null) {
			return;
		}
		File f = new File(this.dataFuelDir, "FuelData.dat");
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(outputStream);
			oos.writeObject(tripList);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Trip> readFuelslist() {
		if (dataFuelDir == null) {
			return null;
		}
		File f = new File(this.dataFuelDir, "FuelData.dat");
		if (f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				ObjectInputStream ois = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				ArrayList<Trip> TripList = (ArrayList<Trip>) ois.readObject();
				ois.close();
				return TripList;
			} catch (Exception ex) {
				ArrayList<Trip> TripList = new ArrayList<Trip>();
				ex.printStackTrace();
				return TripList;
			}
		}
		return null;
	}

	public void deleteFuelslist() {
		if (dataFuelDir == null) {
			return;
		}
		File f = new File(this.dataFuelDir, "FuelData.dat");
		if (f.exists()) {
			f.delete();
		}
	}

	public void deleteFuelslist(int position) {
		if (dataFuelDir == null) {
			return;
		}
		File f = new File(this.dataFuelDir, "FuelData.dat");
		if (f.exists()) {
			ArrayList<Trip> TripList = readFuelslist();
			TripList.remove(position);
			writeFuelslist(TripList);
		}
	}

	public String[] SummaryFuelList() {
		if (dataFuelDir == null) {
			return null;
		}
		double totalKm = 0;
		double AvgKmL = 0;
		double BestKml = 0;
		double AvgP = 0;
		int count = 0;
		File f = new File(this.dataFuelDir, "FuelData.dat");
		if (f.exists()) {
			ArrayList<Trip> TripList = readFuelslist();
			for (Trip oneTrip : TripList) {
				AvgKmL = AvgKmL + oneTrip.getKmperlitre();
				BestKml = (BestKml > oneTrip.getKmperlitre()) ? BestKml : oneTrip.getKmperlitre();
				totalKm = totalKm + oneTrip.getkilometers();
				AvgP = AvgP + oneTrip.getprice();
				count++;
			}
			AvgKmL = AvgKmL / count;
			AvgP = AvgP / count;
		}
		return new String[] { String.format("%.2f", AvgKmL) + " km/l",
				String.format("%.2f", BestKml) + " km/l", String.format("%.2f", totalKm) + " km",
				String.format("%.2f", AvgP) + " €/l" };
	}

	public ArrayList<LatLng> SummaryMapList() {
		if (dataFuelDir == null) {
			return null;
		}
		ArrayList<LatLng> listLatLng = new ArrayList<LatLng>();
		File f = new File(this.dataFuelDir, "FuelData.dat");
		if (f.exists()) {
			ArrayList<Trip> TripList = readFuelslist();
			for (Trip oneTrip : TripList) {
				String[] geo = oneTrip.getLocation().split(",");
				// Log.v("FILEMAN",
				// " "+Double.parseDouble(geo[0])+","+Double.parseDouble(geo[1]));
				LatLng p = new LatLng(Double.parseDouble(geo[0]), Double.parseDouble(geo[1]));
				listLatLng.add(p);
			}
		}
		return listLatLng;
	}

	public ArrayList<String> SummaryPriceList() {
		if (dataFuelDir == null) {
			return null;
		}
		ArrayList<String> listPrice = new ArrayList<String>();
		File f = new File(this.dataFuelDir, "FuelData.dat");
		if (f.exists()) {
			ArrayList<Trip> TripList = readFuelslist();
			for (Trip oneTrip : TripList) {
				listPrice.add(String.format("%.2f", oneTrip.getprice()));
			}
		}
		return listPrice;
	}

	private void saveLog(LatLng p) throws Exception {
		if (dataDir == null)
			return;
		File f = new File(this.dataDir, this.filename + ".json");

		try {
			OutputStream os = new FileOutputStream(f, true); // append mode
			double lat = p.latitude;
			double lon = p.longitude;
			String str = new String(lat + "," + lon + "\n");
			os.write(str.getBytes());
			os.close();
		} catch (Exception e) {
			throw e;
		}
	}

	public String readLog() {
		if (dataDir == null)
			return null;

		File file = new File(this.dataDir, this.filename + ".json");
		StringBuilder text = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text.toString();
	}

	public boolean loadLog() throws Exception {
		if (dataDir == null)
			return false;

		File f = new File(this.dataDir, this.filename + ".json");

		try {
			InputStream is = new FileInputStream(f);
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			is.close();
			String[] lines = new String(bytes).split("\n");
			List<LatLng> points = new ArrayList<LatLng>();
			for (String line : lines) {
				LatLng p = parse(line);
				points.add(p);
			}
			if (points.size() > 0) {
				this.points = points;
				return true;
			}
			return false;
		} catch (Exception e) {
			throw e;
		}

	}

	public static LatLng parse(String log) throws Exception {
		String[] items = log.split("[    ]*,[    ]*");
		if (items.length < 2)
			throw new Exception("log format error");
		else { //
			LatLng p;
			double lat = Double.parseDouble(items[0]);
			double lon = Double.parseDouble(items[1]);
			p = new LatLng(lat, lon);
			return p;
		}
	}

	public String[] listOfFiles() {
		File[] filelist = this.dataDir.listFiles();
		String[] theNamesOfFiles = new String[filelist.length];
		for (int i = 0; i < theNamesOfFiles.length; i++) {
			theNamesOfFiles[i] = filelist[i].getName();
			Log.v("Files", filelist[i].getName());
		}
		return theNamesOfFiles;
	}

	public void deleteFilePosition(int position) {
		// TODO Auto-generated method stub
		File[] filelist = this.dataDir.listFiles();
		int count = 0;
		for (File f : filelist) {
			if (count == position) {
				f.delete();
				break;
			}
			count++;
		}

	}

	public void deleteAllFiles() {
		// TODO Auto-generated method stub
		File[] filelist = this.dataDir.listFiles();
		for (File f : filelist) {
			f.delete();
		}

	}

	public String nameFromFilePosition(int position) {
		File[] filelist = this.dataDir.listFiles();
		int count = 0;
		String name = "NoName";
		for (File f : filelist) {
			if (count == position) {
				name = f.getName();
				String[] parts = name.split(".js");
				return parts[0];
			}
			count++;
		}
		return name;

	}

	public File sendFilePosition(int position) {
		// TODO Auto-generated method stub
		File[] filelist = this.dataDir.listFiles();
		int count = 0;
		for (File f : filelist) {
			if (count == position) {
				return f;
			}
			count++;
		}
		return null;

	}

	public void saveFile(String filename, Tripp t) {
		this.filename = filename;

		if (dataDir == null)
			return;
		File f = new File(this.dataDir, this.filename);

		try {
			OutputStream os = new FileOutputStream(f, true); // append mode
			// double lat = p.latitude/1E6;
			Gson gson = new Gson();
			os.write(gson.toJson(t).getBytes());
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * No used coz of one below this
	 */
	public List<String[]> listOfFilesDate() {
		File[] filelist = this.dataDir.listFiles();
		String[] theNamesOfFiles = new String[filelist.length];
		String[] theDatesOfFiles = new String[filelist.length];
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		List<String[]> fileList = new LinkedList<String[]>();
		for (int i = 0; i < theNamesOfFiles.length; i++) {
			theNamesOfFiles[i] = filelist[i].getName();
			theDatesOfFiles[i] = sdf.format(filelist[i].lastModified());
			fileList.add(new String[] { theNamesOfFiles[i], theDatesOfFiles[i] });
			Log.v("Files", filelist[i].getName());
		}

		// return theNamesOfFiles;
		return fileList;
	}

	public List<String[]> listOfFilesDistance() {
		File[] filelist = this.dataDir.listFiles();
		String[] theNamesOfFiles = new String[filelist.length];
		String[] theDatesOfFiles = new String[filelist.length];
		String[] theDistanceOfFiles = new String[filelist.length];
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		List<String[]> fileList = new LinkedList<String[]>();
		Tripp mytripp = new Tripp(null, null, null);
		Gson gson = new Gson();
		for (int i = 0; i < theNamesOfFiles.length; i++) {
			// mytripp = readFilePosition(i);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(filelist[i]));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mytripp = gson.fromJson(br, Tripp.class);

			if (mytripp.getDistance() != null) {
				theDistanceOfFiles[i] = mytripp.getDistance();// mytripp.getDistance();
			} else {
				theDistanceOfFiles[i] = "0";
			}
			theNamesOfFiles[i] = filelist[i].getName();
			theDatesOfFiles[i] = sdf.format(filelist[i].lastModified());
			fileList.add(new String[] { theNamesOfFiles[i], theDatesOfFiles[i],
					theDistanceOfFiles[i] });
		}
		// return theNamesOfFiles;
		return fileList;
	}

	public Tripp readFilePosition(int position) {
		// TODO Auto-generated method stub
		File[] filelist = this.dataDir.listFiles();

		Gson gson = new Gson();

		Tripp trippObj = new Tripp(null, null, null);
		int count = 0;
		for (File f : filelist) {
			Log.v("readfile", "file " + f.getName());
			if (count == position) {
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(f));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				trippObj = gson.fromJson(br, Tripp.class);
				return trippObj;
				// break;
			}
			count++;
		}
		return null;
	}

	public Boolean checkName(String text) {
		text = text.trim();
		// TODO Auto-generated method stub
		File[] filelist = this.dataDir.listFiles();
		Boolean checkname = false;
		for (File f : filelist) {
			Log.v("INFO", "name: " + f.getName() + " text: " + text);
			if ((String.valueOf(f.getName()) == (text)) || (f.getName().equals(text))) {
				checkname = true;
				Log.v("INFO", "name: TRUE");
				break;
			}
		}
		return checkname;
	}

}
