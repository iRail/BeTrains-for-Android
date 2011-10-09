package tof.cv.ui;

import tof.cv.misc.ListPreferenceMultiSelect;
import tof.cv.misc.MyUpdateService;
import tof.cv.misc.TimePickerPreference;
import tof.cv.mpp.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

public class NotificationActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	/** Called when the activity is first created. */
	
	SharedPreferences sharedP;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.notif);

		Intent svc = new Intent(this, MyUpdateService.class);
		startService(svc);
		
		sharedP=PreferenceManager.getDefaultSharedPreferences(this);
		
        for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
            initSummary(getPreferenceScreen().getPreference(i));
           }

	}

	@Override
	protected void onResume() {
		super.onResume();

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		 updatePrefSummary(findPreference(key));
	}
	
    private void initSummary(Preference p){
        if (p instanceof PreferenceCategory){
             PreferenceCategory pCat = (PreferenceCategory)p;
             for(int i=0;i<pCat.getPreferenceCount();i++){
                 initSummary(pCat.getPreference(i));
             }
         }else{
             updatePrefSummary(p);
         }

     }
    private void updatePrefSummary(Preference p){
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getKey().contentEquals("prefFreq"))
            	p.setSummary(editTextPref.getText()+"h"); 
            else
            	p.setSummary(editTextPref.getText()); 
        }
        if (p instanceof ListPreferenceMultiSelect) {
        	ListPreferenceMultiSelect listPreferenceMultiSelect = (ListPreferenceMultiSelect) p;
        	String rawval = listPreferenceMultiSelect.getValue();
            p.setSummary(displayItems(ListPreferenceMultiSelect.parseStoredValue(rawval),listPreferenceMultiSelect.getEntries())); 
        }
        if (p instanceof TimePickerPreference) {
        	TimePickerPreference timePickerPreference = (TimePickerPreference) p;
            String value=timePickerPreference.getTimeValue();
            if (value!=null)
            	p.setSummary(value); 
        }

    }
    
    private String displayItems(String[] array, CharSequence[] charSequences){
    	String toReturn="";
    	for (String s:array){
    		toReturn+=charSequences[Integer.valueOf(s)];
    		toReturn+=", ";
    		
    	}
    	return toReturn.substring(0, toReturn.length()-2);
    }

}