package com.bkapps.carapp.utils;

import java.util.ArrayList;

public class Tripp {
	private String Name;
	private String Date;
	/**
	 * Distance in metres
	 */
	private String Distance;
	private String Time;
	private String frequency;
	private String avgRPM;
	private String avgSpeed;
	private String avgTemp;
	private ArrayList<Point> Pointslist;


	public Tripp(String name, String date, ArrayList<Point> pointslist ) {
		this.setName(name);
		this.setDate(date);
		this.setPointslist(pointslist);
	}
	
	public Tripp(String name, String date) {
		this.setName(name);
		this.setDate(date);
	}
	
	public Tripp(String name) {
		this.setName(name);
	}

	public class Point {
		
		private String Location;
		private String Speed;
		private String RPM;
		private String Temp;
		private String Altitude;
		private String Load;
		
		public Point() {
			// TODO Auto-generated constructor stub
		}
		
		public Point(String location, String speed, String altitude, String rpm, String temp) {
			this.setLocation(location);
			this.setSpeed(speed);
			this.setAltitude(altitude);
			this.setRPM(rpm);
			this.setTemp(temp);
		}
		
		
		public Point(String location, String speed, String altitude) {
			this.setLocation(location);
			this.setSpeed(speed);
			this.setAltitude(altitude);
		}
		
		public Point(String location, String speed) {
			this.setLocation(location);
			this.setSpeed(speed);
		}

		public Point(String location, String speed, String altitude, String rpm, String temp, String load) {
			this.setLocation(location);
			this.setSpeed(speed);
			this.setAltitude(altitude);
			this.setRPM(rpm);
			this.setTemp(temp);
			this.setLoad(load);
		}

		public String getLocation() {
			return Location;
		}
		public void setLocation(String location) {
			this.Location = location;
		}
		public String getSpeed() {
			return Speed;
		}
		public void setSpeed(String speed) {
			this.Speed = speed;
		}

		public String getAltitude() {
			return Altitude;
		}

		public void setAltitude(String altitude) {
			Altitude = altitude;
		}

		public String getRPM() {
			return RPM;
		}

		public void setRPM(String rPM) {
			RPM = rPM;
		}

		public String getTemp() {
			return Temp;
		}

		public void setTemp(String temp) {
			Temp = temp;
		}

		public String getLoad() {
			return Load;
		}

		public void setLoad(String load) {
			Load = load;
		}

	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		this.Name = name;
	}

	public ArrayList<Point> getPointslist() {
		return Pointslist;
	}

	public void setPointslist(ArrayList<Point> pointslist) {
		Pointslist = pointslist;
	}
	
	public int getPointlistSize() {
		// TODO Auto-generated method stub
		return Pointslist.size();
	}

	public String getDate() {
		return Date;
	}

	public void setDate(String date) {
		Date = date;
	}

	public String getDistance() {
		return Distance;
	}

	public void setDistance(String distance) {
		Distance = distance;
	}

	public String getTime() {
		return Time;
	}

	public void setTime(String time) {
		Time = time;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getAvgRPM() {
		return avgRPM;
	}

	public void setAvgRPM(String avgRPM) {
		this.avgRPM = avgRPM;
	}

	public String getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(String avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	public String getAvgTemp() {
		return avgTemp;
	}

	public void setAvgTemp(String avgTemp) {
		this.avgTemp = avgTemp;
	}
	

}