package tof.cv.mpp;

import java.util.List;

import tof.cv.mpp.Utils.Utils;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.app.SherlockPreferenceActivity;
import android.support.v4.view.MenuItem;
import android.util.Log;

import com.viewpagerindicator.R;

public class MyPreferenceActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	// private MyPrefAdapter mAdapter;
	//private ViewPager mPager;
	public static int PAGE_GENERAL = 0;
	public static int PAGE_PLANNER = 1;
	public static int PAGE_TWITTER = 2;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Utils.setFullscreenIfNecessary(this);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.activity_preferences);
			addPreferencesFromResource(R.xml.activity_planner_preferences);
			addPreferencesFromResource(R.xml.activity_twitter_preferences);
			this.setContentView(R.layout.activity_preference);
		}
		
		Preference pref = findPreference(getString(R.string.key_planner_da));
		if (pref != null)
			pref.setSummary(((ListPreference) pref).getEntry());

		pref = findPreference(getString(R.string.key_activity));
		if (pref != null)
			pref.setSummary(((ListPreference) pref).getEntry());

		pref = findPreference("prefPseudo");
		if (pref != null)
			pref.setSummary(((EditTextPreference) pref).getText());

	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		Log.i("","onBuildHeaders");
		loadHeadersFromResource(R.xml.preference_headers, target);
		this.setContentView(R.layout.activity_preference);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.i("", "PRE-HC");
		if (key.contentEquals("prefPseudo")) {
			Preference pref = findPreference("prefPseudo");
			pref.setSummary(((EditTextPreference) pref).getText());
		}
		if (key.contentEquals(getString(R.string.key_activity))) {
			Preference pref = findPreference(getString(R.string.key_activity));
			pref.setSummary(((ListPreference) pref).getEntry());
		}
		if (key.contentEquals(getString(R.string.key_planner_da))) {
			Preference pref = findPreference(getString(R.string.key_planner_da));
			pref.setSummary(((ListPreference) pref).getEntry());
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (android.R.id.home):
			// app icon in ActionBar is clicked; Go home
			Intent intent = new Intent(this, WelcomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
