package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class TrafficActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.setFullscreenIfNecessary(this);
		
		setContentView(R.layout.activity_traffic);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
}