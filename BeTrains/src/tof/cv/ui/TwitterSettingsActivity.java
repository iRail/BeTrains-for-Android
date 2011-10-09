package tof.cv.ui;

import tof.cv.mpp.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

public class TwitterSettingsActivity extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.activity_twitter_preferences);
		

	}


	
}
