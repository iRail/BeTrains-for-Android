package tof.cv.mpp;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;


public class MyPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		boolean hasNoHeader = true;
		try {
			hasNoHeader = !hasHeaders();
		} catch (Error e) {
			e.printStackTrace();
		}

		/*if (hasNoHeader && getIntent().getExtras()==null) {
			this.setContentView(R.layout.activity_preference);
			addPreferencesFromResource(R.xml.activity_preferences);
			addPreferencesFromResource(R.xml.activity_planner_preferences);
		}*/


        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintResource(R.color.primarycolor);

	}


    protected boolean isValidFragment (String fragmentName) {
        return true;
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

        protected boolean isValidFragment (String fragmentName) {
            return true;
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
                        try {//Wrong number in previous app. Need to try/catch
                            preference.setSummary(((ListPreference) preference)
                                    .getEntries()[Integer.valueOf(newValue
                                    .toString()) - 1]);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
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
								.getEntries()[Integer.valueOf(newValue
								.toString()) - 1]);
						return true;
					}
				});
			}
		}
        protected boolean isValidFragment (String fragmentName) {
            return true;
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
        protected boolean isValidFragment (String fragmentName) {
            return true;
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
