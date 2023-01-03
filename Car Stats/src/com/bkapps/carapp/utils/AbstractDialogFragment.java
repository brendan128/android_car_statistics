package com.bkapps.carapp.utils;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;




public abstract class AbstractDialogFragment extends DialogFragment {

		  @Override
		  public Dialog onCreateDialog(Bundle savedInstanceState) {
		    final Dialog dialog = createDialog();
		    dialog.setOnShowListener(new DialogInterface.OnShowListener() {

		        @Override
		      public void onShow(DialogInterface dialogInterface) {
		        DialogUtils.setDialogTitleDivider(getActivity(), dialog);
		      }
		    });
		    return dialog;
		  }

		  protected abstract Dialog createDialog();
}


