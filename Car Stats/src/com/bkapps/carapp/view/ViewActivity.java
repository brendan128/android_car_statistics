package com.bkapps.carapp.view;

import java.util.ArrayList;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bkapps.carapp.R;
import com.bkapps.carapp.R.id;
import com.bkapps.carapp.R.layout;
import com.bkapps.carapp.R.menu;
import com.bkapps.carapp.R.string;
import com.bkapps.carapp.utils.MyFile;
import com.bkapps.carapp.utils.Tripp;

public class ViewActivity extends Activity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private int positionNum;
	private int typeNum;
	private Tripp mytrip = new Tripp(null, null, null);

	private static ViewMapFragment mapfragment;
	private static ViewDetailFragment detailfragment;
	private static ViewChartFragment chartfragment;

	ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);

		// Set up the action bar.
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		setPositionNum(intent.getIntExtra("positionkey", 0));
		setTypeNum(intent.getIntExtra("typekey", 1));
		// detailfragment = new DetailMapFragment();
		setMapfragment(new ViewMapFragment());
		setDetailfragment(new ViewDetailFragment());
		setChartfragment(new ViewChartFragment());



	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));

		}

		mViewPager.setCurrentItem(1, false);

		switch (getTypeNum()) {
		case 1:
			actionBar.setTitle("  SPEED ");
			break;
		case 2:
			actionBar.setTitle("   RPM ");
			break;
		case 3:
			actionBar.setTitle(" TEMPERATURE ");
			break;
		case 4:
			actionBar.setTitle("  LOAD % ");
			break;
		}
			
		GsonTask task = new GsonTask(this);
		Log.v("Detail", "in resumne *****");
		task.execute(getPositionNum());

	}
	
/*	@Override
	protected void onSaveInstanceState(Bundle outState) {
	   FragmentManager manager = getFragmentManager();
	   manager.putFragment(outState,"chartfrag", getChartfragment());
	   manager.putFragment(outState,"mapfrag", getMapfragment());
	   manager.putFragment(outState,"detfrag", getDetailfragment());
	}  
	
	@Override
	protected void onRestoreInstanceState(Bundle inState) {
	   instantiateFragments(inState);
	}  
	
	private void instantiateFragments(Bundle inState) {
		   FragmentManager manager = getFragmentManager();
		   FragmentTransaction transaction = manager.beginTransaction();

		   if (inState != null) {
			   setChartfragment((ViewChartFragment) manager.getFragment(inState, "chartfrag"));
			   setDetailfragment((ViewDetailFragment) manager.getFragment(inState, "detfrag"));
			   setMapfragment(mapfragment)((ViewChartFragment) manager.getFragment(inState, "chartfrag"));
		      mMyFragment = (MyFragment) manager.getFragment(inState, MyFragment.TAG);
		   } else {
		      mMyFragment = new MyFragment();
		      transaction.add(R.id.fragment, mMyFragment, MyFragment.TAG);
		      transaction.commit();
		   }
		}  
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intent = getIntent();
		switch (id) {
		case (R.id.Speed_view):
			intent.putExtra("typekey", 1);
			finish();
			startActivity(intent);
			break;
		case (R.id.RPM_view):
			intent.putExtra("typekey", 2);
			finish();
			startActivity(intent);
			break;
		case (R.id.TEMP_view):
			intent.putExtra("typekey", 3);
			finish();
			startActivity(intent);
			break;
		case (R.id.LOAD_view):
			intent.putExtra("typekey", 4);
			finish();
			startActivity(intent);
			break;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			break;

		default:
			break;
		}
		return true;
	}

	private class GsonTask extends AsyncTask<Integer, Void, Tripp> {
		private Context mContext;

		public GsonTask(Context context) {
			mContext = context;
		}

		protected Tripp doInBackground(Integer... position) {
			MyFile myfile = new MyFile(mContext);
			Tripp mytripp = new Tripp(null, null, null);
			try {
				mytripp = myfile.readFilePosition(position[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return mytripp;
		}

		protected void onPostExecute(Tripp mytripp) {
			setMytrip(mytripp);
			getDetailfragment().loaded();
			getMapfragment().loaded(getTypeNum());
			getChartfragment().loaded(getTypeNum());
			
			setTitle(mytripp.getName());
		}
	}

	ArrayList<String> spinnerlist;
	ArrayAdapter<String> spinneradapter;

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	public int getPositionNum() {
		return positionNum;
	}

	public void setPositionNum(int positionNum) {
		this.positionNum = positionNum;
	}

	public Tripp getMytrip() {
		return mytrip;
	}

	public void setMytrip(Tripp mytrip) {
		this.mytrip = mytrip;
	}

	public static ViewMapFragment getMapfragment() {
		return mapfragment;
	}

	public void setMapfragment(ViewMapFragment mapfragment) {
		ViewActivity.mapfragment = mapfragment;
	}

	public static ViewDetailFragment getDetailfragment() {
		return detailfragment;
	}

	public void setDetailfragment(ViewDetailFragment detailfragment) {
		ViewActivity.detailfragment = detailfragment;
	}

	public static ViewChartFragment getChartfragment() {
		return chartfragment;
	}

	public void setChartfragment(ViewChartFragment chartfragment) {
		ViewActivity.chartfragment = chartfragment;
	}

	public int getTypeNum() {
		return typeNum;
	}

	public void setTypeNum(int typeNum) {
		this.typeNum = typeNum;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			Log.v("Detail", "sectionpager  *****");
			switch (position) {
			case 0:
				return getDetailfragment();
				// case 0: return new DetailFragment();
			case 1:
				return getMapfragment();
			case 2:
				return getChartfragment();
			default:
				return detailfragment;
			}
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_details).toUpperCase(l);
			case 1:
				return getString(R.string.title_map).toUpperCase(l);
			case 2:
				return getString(R.string.title_chart).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class DetailMapFragment extends Fragment {

		public DetailMapFragment() {
		}

		private TextView tv_name;
		private TextView tv_date;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_details, container, false);
			tv_name = (TextView) rootView.findViewById(R.id.tv_name2);
			tv_date = (TextView) rootView.findViewById(R.id.tv_date);

			return rootView;
		}

		public void loaded() {
			// TODO Auto-generated method stub
			Log.v("Detail", "in loaded *****");
			tv_name.setText(((ViewActivity) this.getActivity()).getMytrip().getName());
			tv_date.setText(((ViewActivity) this.getActivity()).getMytrip().getDate());

		}

	}

}
