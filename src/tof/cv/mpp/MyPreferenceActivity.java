package tof.cv.mpp;

import java.util.List;

import tof.cv.mpp.Utils.Utils;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.viewpagerindicator.R;

public class MyPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	//private MyPrefAdapter mAdapter;
	private ViewPager mPager;
	public static int PAGE_GENERAL = 0;
	public static int PAGE_PLANNER = 1;
	public static int PAGE_TWITTER = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Utils.setFullscreenIfNecessary(this);
		
		 if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
				addPreferencesFromResource(R.xml.activity_preferences);
				addPreferencesFromResource(R.xml.activity_planner_preferences);
				addPreferencesFromResource(R.xml.activity_twitter_preferences);			 
		 }

			try {
				Preference pref = findPreference("prefPseudo");
				pref.setSummary(((EditTextPreference) pref).getText());

				pref = findPreference(getString(R.string.key_activity));
				pref.setSummary(((ListPreference) pref).getEntry());
			} catch (Exception e) {
			}		

		//setContentView(R.layout.fragment_tab_picker);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		
		//This way is buggy
		/*mPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new MyPrefAdapter(getSupportFragmentManager(), this);
		mPager.setAdapter(mAdapter);

		TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
		titleIndicator.setViewPager(mPager);
		titleIndicator.setFooterIndicatorStyle(IndicatorStyle.Underline);

		mPager.setCurrentItem(this.getIntent().getExtras().getInt("screen"));*/
	}
	
	@Override
	  public void onBuildHeaders(List<Header> target) {
	    loadHeadersFromResource(R.xml.preference_headers, target);
	  }
/*
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

	public class MyPrefAdapter extends FragmentPagerAdapter implements
			TitleProvider {

		PreferenceListFragment[] fragments;
		String[] titles;

		public MyPrefAdapter(FragmentManager fm, Context context) {
			super(fm);
			fragments = new PreferenceListFragment[3];
			fragments[0] = new PreferenceListFragment(
					R.xml.activity_preferences);
			fragments[1] = new PreferenceListFragment(
					R.xml.activity_planner_preferences);
			fragments[2] = new PreferenceListFragment(
					R.xml.activity_twitter_preferences);

			titles = new String[] { "GENERAL", "PLANNER", "TWITTER" };
		}

		@Override
		public Fragment getItem(int position) {
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

		static PrefFragment newInstance(int num) {
			PrefFragment f = new PrefFragment();
			// Supply num input as an argument.
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);
			return f;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_station_list,
					container, false);
			mNum = getArguments() != null ? getArguments().getInt("num") : 1;
			return v;
		}
	}

	List<PreferenceScreen> rootPref=new ArrayList<PreferenceScreen>() ;

	@Override
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		rootPref.add(root);
		Toast.makeText(getBaseContext(), root.getSharedPreferences().toString(), Toast.LENGTH_LONG).show();
		root.getSharedPreferences().registerOnSharedPreferenceChangeListener(
				this);
		// General Prefs
		try {
			Preference pref = root.findPreference("prefPseudo");
			pref.setSummary(((EditTextPreference) pref).getText());

			pref = root.findPreference(getString(R.string.key_activity));
			pref.setSummary(((ListPreference) pref).getEntry());
		} catch (Exception e) {
		}

		// Planner Prefs
		try {
			Preference pref = root
					.findPreference(getString(R.string.key_planner_da));
			pref.setSummary(((ListPreference) pref).getEntry());
		} catch (Exception e) {
		}

		// Twitter Prefs
		try {
		} catch (Exception e) {
		}

	}
*/
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.i("","X");
		if(key.contentEquals("prefPseudo")){
			Preference pref = findPreference("prefPseudo");
			pref.setSummary(((EditTextPreference) pref).getText());
		}
		if(key.contentEquals(getString(R.string.key_activity))){
			Log.i("","B");
			Preference pref = findPreference(getString(R.string.key_activity));
			pref.setSummary(((ListPreference) pref).getEntry());
		}
		if(key.contentEquals(getString(R.string.key_planner_da))){
			Log.i("","C");
			Preference pref = findPreference(getString(R.string.key_planner_da));
			pref.setSummary(((ListPreference) pref).getEntry());
		}

	}


}
