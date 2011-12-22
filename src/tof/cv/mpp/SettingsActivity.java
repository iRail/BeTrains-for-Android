package tof.cv.mpp;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.view.PreferenceListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.R;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
import com.viewpagerindicator.TitleProvider;

public class SettingsActivity extends FragmentActivity{

	MyPrefAdapter mAdapter;
	ViewPager mPager;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.setFullscreenIfNecessary(this);
		
		setContentView(R.layout.fragment_station_picker);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new MyPrefAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mAdapter);
        
        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titleIndicator.setViewPager(mPager);
        titleIndicator.setFooterIndicatorStyle(IndicatorStyle.Underline);
        
        mPager.setCurrentItem(1);
        

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (android.R.id.home):
			// app icon in ActionBar is clicked; Go home
			Intent intent = new Intent(this, PlannerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class MyPrefAdapter extends FragmentPagerAdapter implements TitleProvider{


		PreferenceListFragment[] fragments;
	    String[] titles;
	    
	    public MyPrefAdapter(FragmentManager fm, Context context) {
	        super(fm);
	        fragments = new PreferenceListFragment[3];
	        fragments[0] = new PreferenceListFragment(R.xml.activity_preferences);
	        fragments[1] = new PreferenceListFragment(R.xml.activity_planner_preferences);
	        fragments[2] = new PreferenceListFragment(R.xml.activity_twitter_preferences);
	        
	        titles = new String[] { "GENERAL",
	        		"PLANNER", "TWITTER" };
	    }

	    @Override
	    public Fragment getItem(int position){
	        return fragments[position];
	    }

	    @Override
	    public int getCount() {
	        return fragments.length;
	    }

	    @Override
	    public String getTitle(int position) {
	        return titles[position];
	    }


	    
	}
	
	public static class PrefFragment extends PreferenceFragment {
		static int mNum;

		/**
		 * Create a new instance of CountingFragment, providing "num" as an
		 * argument.
		 */
		static PrefFragment newInstance(int num) {
			PrefFragment f = new PrefFragment();
			// Supply num input as an argument.
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);

			return f;
		}

		/**
		 * When creating, retrieve this instance's number from its arguments.
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			// Notifier au créateur de ActionBarSherlock que le mNum doit être défini dans la onCreateView (cf Fragment lifeCycle)
			//mNum = getArguments() != null ? getArguments().getInt("num") : 1;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_station_list,
					container, false);
			mNum = getArguments() != null ? getArguments().getInt("num") : 1;
			return v;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			String[] list = null;
			switch (mNum) {
			case 0:
				list = ConnectionMaker.LIST_OF_FAV_STATIONS;
				break;
			case 1:
				list = ConnectionMaker.LIST_OF_STATIONS;
				break;
			case 2:
				list = ConnectionMaker.LIST_OF_EURO_STATIONS;
				break;
			}

			//this.setListAdapter(new ArrayAdapter<String>(getActivity(),
			//		android.R.layout.simple_list_item_1, list));
		}
		

	}
   

}
