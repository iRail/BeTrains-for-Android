/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
	
  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package tof.cv.mpp.view;

import tof.cv.mpp.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StockPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	Activity a;
	SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		a = this.getActivity();
		Log.i("", "Activity1 " + a);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		preferences.registerOnSharedPreferenceChangeListener(this);

		int res = getActivity().getResources().getIdentifier(
				getArguments().getString("resource"), "xml",
				getActivity().getPackageName());
		addPreferencesFromResource(res);

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Crash on tablets but works everywhere else.
		// return inflater.inflate(R.layout.activity_preference, null);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
		preferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.i("", "PREFChanged1 " + this.getActivity());
		if (key.contentEquals("prefPseudo")) {
			Log.i("", "PseudoChanged1");
			Preference pref = findPreference("prefPseudo");
			pref.setSummary(((EditTextPreference) pref).getText());
		}
		if (key.contentEquals(a.getString(R.string.key_activity))) {
			Log.i("", "FirstChanged1");
			Preference pref = findPreference(a.getString(R.string.key_activity));
			pref.setSummary(((ListPreference) pref).getEntry());
		}
		if (key.contentEquals(a.getString(R.string.key_planner_da))) {
			Preference pref = findPreference(a
					.getString(R.string.key_planner_da));
			Log.i("", "PlannerChanged1" + pref);
			pref.setSummary(((ListPreference) pref).getEntry());
		}

	}
}
