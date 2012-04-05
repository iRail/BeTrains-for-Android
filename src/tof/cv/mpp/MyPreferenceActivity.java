package tof.cv.mpp;

import java.util.List;

import tof.cv.mpp.Utils.Utils;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class MyPreferenceActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	// private MyPrefAdapter mAdapter;
	// private ViewPager mPager;
	public static int PAGE_GENERAL = 0;
	public static int PAGE_PLANNER = 1;
	public static int PAGE_TWITTER = 2;

	int page = 99;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Utils.setFullscreenIfNecessary(this);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
			page = extras.getInt("screen");

		// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)

		Boolean hasHeader = false;
		try {
			hasHeader = this.hasHeaders();
		} catch (NoSuchMethodError e) {

		}

		if (!hasHeader) {
			this.setContentView(R.layout.activity_preference);
			switch (page) {
			case 0:
				addPreferencesFromResource(R.xml.activity_preferences);
				break;
			case 1:
				addPreferencesFromResource(R.xml.activity_planner_preferences);
				break;
			case 2:
				addPreferencesFromResource(R.xml.activity_twitter_preferences);
				break;
			default:
				addPreferencesFromResource(R.xml.activity_preferences);
				addPreferencesFromResource(R.xml.activity_planner_preferences);
				addPreferencesFromResource(R.xml.activity_twitter_preferences);
				break;

			}
		}
		Preference pref = findPreference(getString(R.string.key_planner_da));
		if (pref != null) {
			pref.setSummary(((ListPreference) pref).getEntry());
			pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					preference.setSummary(((ListPreference) preference)
							.getEntries()[Integer.valueOf(newValue.toString()) - 1]);
					return true;
				}
			});
		}

		Preference pref2 = findPreference(getString(R.string.key_activity));
		if (pref2 != null) {
			pref2.setSummary(((ListPreference) pref2).getEntry());
			pref2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					preference.setSummary(((ListPreference) preference)
							.getEntries()[Integer.valueOf(newValue.toString()) - 1]);
					return true;
				}
			});
		}
		Preference pref3 = findPreference("prefPseudo");
		if (pref3 != null) {
			pref3.setSummary(((EditTextPreference) pref3).getText());
			pref3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					preference.setSummary((CharSequence) newValue);
					return true;
				}
			});
		}
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
			page = extras.getInt("screen");

		Log.i("", "onBuildHeaders" + page);

		if (page == 99)
			loadHeadersFromResource(R.xml.preference_headers, target);

		this.setContentView(R.layout.activity_preference);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

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
