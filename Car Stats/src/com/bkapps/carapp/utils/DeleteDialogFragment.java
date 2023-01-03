package com.bkapps.carapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.bkapps.carapp.MainActivity;

public class DeleteDialogFragment extends AbstractDialogFragment {
	
	public static final String TITLE_DIALOG_TAG = "deleteDialog";
	private static final String POSITION_TAG = "position";
	
	public interface DelCaller {


		public void deleteDone(int position);
	  }
	
	
    public static DeleteDialogFragment newInstance(int position) {
    	Bundle bundle = new Bundle();
    	bundle.putInt(POSITION_TAG, position);
    	DeleteDialogFragment frag = new DeleteDialogFragment();
    	frag.setArguments(bundle);
        return frag;
    }
    
    private DelCaller caller;
    
    @Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    try {
	      caller = (DelCaller) activity;
	    } catch (ClassCastException e) {
	      throw new ClassCastException(
	          activity.toString() + " must implement " + DelCaller.class.getSimpleName());
	    }
	  }


	@Override
	protected Dialog createDialog() {
		
		final int position = getArguments().getInt(POSITION_TAG);
		String text = "";
		if (position==-5){
			text = "Delete all your records?";
		}
		else {
			text = "Delete file?";
		}
		String mytag = getTag();
		Log.v("tagdelete", "tag is : "+mytag);
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(text)
				.setPositiveButton("Do it",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// FIRE ZE MISSILES!
								
								caller.deleteDone(position);
//								refreshList();
							}
						})
				.setNegativeButton("cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}
	
/*	  @Override
	  public void onCancel(DialogInterface arg0) {
	    
	  }*/
}