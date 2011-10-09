package tof.cv.ui;

import tof.cv.mpp.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class PlannerSettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private SharedPreferences mDefaultPrefs;
	private Context context = this;
	
	private ListPreference mplannerDAPref;

	static final String PREFIX = "Current: ";
	public static final String TAG = "PlannerSettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_planner_preferences);
		setContext(this);

		mDefaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		mplannerDAPref = (ListPreference) findPreference(getString(R.string.key_planner_da));
		mplannerDAPref.setOnPreferenceChangeListener(this);
		
		int choice = Integer.parseInt(mplannerDAPref.getValue());

		String[] plannerDA = getResources().getStringArray(
				R.array.planner_da_entries);
		mplannerDAPref.setSummary(plannerDA[choice-1]);


	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mplannerDAPref) {
			String v = (String) newValue;
			int choice = Integer.parseInt(v);
			mplannerDAPref.setValue(v);

			String[] plannerDA = getResources().getStringArray(
					R.array.planner_da_entries);
			mplannerDAPref.setSummary(plannerDA[choice-1]);
		}
		return true;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}


}
