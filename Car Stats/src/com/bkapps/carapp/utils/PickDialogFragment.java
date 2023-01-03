package com.bkapps.carapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.bkapps.carapp.R;

public class PickDialogFragment extends AbstractDialogFragment {
	
	public static final String TITLE_DIALOG_TAG = "pickDialog";
	private static final String POSITION_TAG = "position";
	
	public interface PickCaller {



		public void Delete(int position);
		public void send(int position);
		public void toEarth(int position);
		public void KMltoDrive(int position);
		public void sendGeoJson(int position);
	  }
	
	
    public static PickDialogFragment newInstance(int position) {
    	Bundle bundle = new Bundle();
    	bundle.putInt(POSITION_TAG, position);
    	PickDialogFragment frag = new PickDialogFragment();
    	frag.setArguments(bundle);
        return frag;
    }
    
    private PickCaller caller;
    
    @Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    try {
	      caller = (PickCaller) activity;
	    } catch (ClassCastException e) {
	      throw new ClassCastException(
	          activity.toString() + " must implement " + PickCaller.class.getSimpleName());
	    }
	  }


	@Override
	protected Dialog createDialog() {
		
		final int position = getArguments().getInt(POSITION_TAG);
		

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Pick one!").setItems(R.array.pick_array,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position
						// of the selected item
						
						switch (which) {
						case 0:
//							Intent intent = new Intent(getActivity(),
//									EditActivity.class);
//							String message = "abcforNOW";
//							intent.putExtra(EXTRA_MESSAGE, message);
//							startActivity(intent);
							caller.KMltoDrive(position);
							break;
						case 1:
							caller.send(position);
							break;
						case 2:
							caller.sendGeoJson(position);
							break;
						case 3:
	//						DeleteDialog(position);
							caller.Delete(position);

							break;
						case 4:
							caller.toEarth(position);
							break;
						}
					}
				});

           return builder.create();
	}


	
/*	  @Override
	  public void onCancel(DialogInterface arg0) {
	    
	  }*/
}