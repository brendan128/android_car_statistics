package com.bkapps.carapp.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.bkapps.carapp.R;
import com.bkapps.carapp.utils.DeleteDialogFragment;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		
		// Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

	
	Preference button = (Preference)findPreference("button");
	button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
	                @Override
	                public boolean onPreferenceClick(Preference arg0) { 
	                    //code for what you want it to do   
	                	
	                	DeleteDialogFragment.newInstance(-5).show(getFragmentManager(),
	            				DeleteDialogFragment.TITLE_DIALOG_TAG);
	                    return true;
	                }
	            });
	}


}
