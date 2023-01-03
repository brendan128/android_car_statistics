package com.bkapps.carapp.record;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class MyOnItemSelectedListener implements OnItemSelectedListener {

	public MyOnItemSelectedListener() {
		// TODO Auto-generated constructor stub
		
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		//Toast.makeText(parent.getContext(), "Selected Country : " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
//		selectcaller.selectedFreq(position);
		RecordActivity.getRecordfragment().selectedFreq(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

}
