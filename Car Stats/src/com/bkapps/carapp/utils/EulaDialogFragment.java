package com.bkapps.carapp.utils;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.widget.TextView;

import com.bkapps.carapp.R;

public class EulaDialogFragment extends AbstractDialogFragment {

	  public interface EulaCaller {


	    public void onEulaDone();
	  }

	  public static final String EULA_DIALOG_TAG = "eulaDialog";
	  private static final String KEY_HAS_ACCEPTED = "hasAccepted";
	  private static final String GOOGLE_URL = "m.google.com";
	  private static final String KOREAN = "ko";

	  /**
	   * Creates a new instance of {@link EulaDialogFragment}.
	   * 
	   * @param hasAccepted true if the user has accepted the eula.
	   */
	  public static EulaDialogFragment newInstance(boolean hasAccepted) {
	    Bundle bundle = new Bundle();
	    bundle.putBoolean(KEY_HAS_ACCEPTED, hasAccepted);

	    EulaDialogFragment eulaDialogFragment = new EulaDialogFragment();
	    eulaDialogFragment.setArguments(bundle);
	    return eulaDialogFragment;
	  }

	  private EulaCaller caller;

	  @Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    try {
	      caller = (EulaCaller) activity;
	    } catch (ClassCastException e) {
	      throw new ClassCastException(
	          activity.toString() + " must implement " + EulaCaller.class.getSimpleName());
	    }
	  }

	  @Override
	  protected Dialog createDialog() {
	    boolean hasAccepted = getArguments().getBoolean(KEY_HAS_ACCEPTED);

	    SpannableString message = new SpannableString(getEulaText());
	    Linkify.addLinks(message, Linkify.WEB_URLS);

	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setMessage(message)
	        .setTitle(R.string.menu_help_eula);

	    if (hasAccepted) {
	      builder.setPositiveButton(R.string.generic_ok, null);
	    } else {
	      builder.setNegativeButton(R.string.eula_decline, new DialogInterface.OnClickListener() {
	          @Override
	        public void onClick(DialogInterface dialog, int which) {
	          caller.onEulaDone();
	        }
	      }).setOnKeyListener(new DialogInterface.OnKeyListener() {
	          @Override
	        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
	          if (keyCode == KeyEvent.KEYCODE_SEARCH) {
	            return true;
	          }
	          return false;
	        }
	      }).setPositiveButton(R.string.eula_accept, new DialogInterface.OnClickListener() {
	          @Override
	        public void onClick(DialogInterface dialog, int which) {
	          EulaUtils.setAcceptedEula(getActivity());
	          caller.onEulaDone();
	        }
	      });
	    }   
	    return builder.create();
	  }

	  @Override
	  public void onStart() {
	    super.onStart();   
	    TextView textView = (TextView) getDialog().findViewById(android.R.id.message);
	    textView.setMovementMethod(LinkMovementMethod.getInstance());
	    textView.setTextAppearance(getActivity(), R.style.TextSmall);
	  }
	  
	  @Override
	  public void onCancel(DialogInterface arg0) {
	    caller.onEulaDone();
	  }

	  /**
	   * Gets the EULA text.
	   */
	  private String getEulaText() {
	    String tos = getString(R.string.eula_date) + "\n\n" + getString(R.string.eula_body, GOOGLE_URL)
	        + "\n\n" + getString(R.string.eula_footer, GOOGLE_URL) + "\n\n"
	        + getString(R.string.eula_copyright_year);
	 //   boolean isKorean = getResources().getConfiguration().locale.getLanguage().equals(KOREAN);
	/*    if (isKorean) {
	      tos += "\n\n" + getString(R.string.eula_korean);
	    }*/
	    return tos;
	  }
	}
