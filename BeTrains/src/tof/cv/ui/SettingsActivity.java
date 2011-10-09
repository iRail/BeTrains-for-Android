package tof.cv.ui;

import tof.cv.mpp.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private SharedPreferences mDefaultPrefs;
	private CheckBoxPreference mFirstM;
	private EditTextPreference mPseudo;
	private ListPreference mActivityPref;
	private Context context = this;

	static final String PREFIX = "Current: ";
	public static final String TAG = "PrefSkin.java";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_preferences);
		setContext(this);

		mDefaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		mPseudo = (EditTextPreference) findPreference("prefPseudo");
		mPseudo.setOnPreferenceChangeListener(this);
		String total = mDefaultPrefs.getString("prefPseudo", "Anonymous");
		mPseudo.setSummary(total);
		
		mActivityPref = (ListPreference) findPreference(getString(R.string.key_activity));
		mActivityPref.setOnPreferenceChangeListener(this);
		
		int choice = Integer.parseInt(mActivityPref.getValue());
		String[] prefActivity = getResources().getStringArray(
				R.array.activity_entries);
		mActivityPref.setSummary(prefActivity[choice-1]);

	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {

		if (preference == mPseudo) {
			String v = (String) newValue;
			mPseudo.setSummary(v);

		}
		
		if (preference == mActivityPref) {
			String v = (String) newValue;
			int choice = Integer.parseInt(v);
			mActivityPref.setValue(v);

			String[] prefActivity = getResources().getStringArray(
					R.array.activity_entries);
			mActivityPref.setSummary(prefActivity[choice-1]);
		}

		return true;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public void setmFirstM(CheckBoxPreference mFirstM) {
		this.mFirstM = mFirstM;
	}

	public CheckBoxPreference getmFirstM() {
		return mFirstM;
	}
}
