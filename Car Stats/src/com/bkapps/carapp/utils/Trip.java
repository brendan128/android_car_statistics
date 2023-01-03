package com.bkapps.carapp.utils;

import java.io.Serializable;
import java.text.DecimalFormat;


public class Trip implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float litres;
	private float price;
	private String date;
	private float kilometers;
	private float kmperlitre;
	private int day;
	private int month;
	private int year;
	private String location;
	
	
	// stupid example for transient
	//transient private Thread myThread;

	public Trip(String date, float litres, float price,  float kilometers) {

		
		this.litres = litres;
		this.price = price;
		this.date = date;
		this.kilometers = kilometers;
		//this.myThread = new Thread();
		this.kmperlitre = (kilometers / litres);
	}
	
	public Trip() {
		this.litres = 0;
		this.price = 0;
		this.date = "default_date";
		this.kilometers = 0;
		this.kmperlitre = (kilometers / litres);
	}

	public float getlitres() {
		return litres;
	}

	public void setlitres(float litres) {
		this.litres = litres;
	}
	
	public float getprice() {
		return price;
	}

	public void setprice(float price) {
		this.price = price;
	}
	
	public String getdate() {
		return date;
	}

	public void setdate(String date) {
		this.date = date;
	}
	
	public float getkilometers() {
		return kilometers;
	}

	public void setkilometers(float kilometers) {
		this.kilometers = kilometers;
	}

	@Override
	public String toString() {
		DecimalFormat Moneydecim = new DecimalFormat("00.00");
		DecimalFormat Kilodecim  = new DecimalFormat("#00.00");
		
		return ("Date:"+date + "  km/L:"+Kilodecim.format(getKmperlitre()) + "  €/L:"+Moneydecim.format(getprice()) ) ;
	}

	public float getKmperlitre() {
		//return kmperlitre;
		return kilometers / litres;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}


}