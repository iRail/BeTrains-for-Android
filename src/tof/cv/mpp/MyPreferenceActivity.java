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
import android.preference.PreferenceFragment;
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Utils.setFullscreenIfNecessary(this);

		boolean hasNoHeader = true;
		try {
			hasNoHeader = !hasHeaders();
		} catch (Error e) {
			e.printStackTrace();
		}

		if (hasNoHeader) {
			this.setContentView(R.layout.activity_preference);
			addPreferencesFromResource(R.xml.activity_preferences);
			addPreferencesFromResource(R.xml.activity_planner_preferences);
			addPreferencesFromResource(R.xml.activity_twitter_preferences);
		}

	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	/**
	 * This fragment shows the preferences for the first header.
	 */
	public static class Prefs1Fragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Make sure default values are applied. In a real app, you would
			// want this in a shared function that is used to retrieve the
			// SharedPreferences wherever they are needed.
			// PreferenceManager.setDefaultValues(getActivity(),
			// R.xml.advanced_preferences, false);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.activity_preferences);

			Preference pref2 = findPreference(getString(R.string.key_activity));
			if (pref2 != null) {
				pref2.setSummary(((ListPreference) pref2).getEntry());
				pref2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						preference.setSummary(((ListPreference) preference)
								.getEntries()[Integer.valueOf(newValue
								.toString()) - 1]);
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
	}

	/**
	 * This fragment shows the preferences for the second header.
	 */
	public static class Prefs2Fragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Can retrieve arguments from headers XML.
			Log.i("args", "Arguments: " + getArguments());

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.activity_planner_preferences);

			Preference pref = findPreference(getString(R.string.key_planner_da));
			if (pref != null) {
				pref.setSummary(((ListPreference) pref).getEntry());
				pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						preference.setSummary(((ListPreference) preference)
								.getEntries()[Integer.valueOf(newValue
								.toString()) - 1]);
						return true;
					}
				});
			}
		}
	}

	/**
	 * This fragment shows the preferences for the third header.
	 */
	public static class Prefs3Fragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Can retrieve arguments from headers XML.
			Log.i("args", "Arguments: " + getArguments());

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.activity_twitter_preferences);
		}
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
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
