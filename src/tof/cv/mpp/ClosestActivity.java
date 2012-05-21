package tof.cv.mpp;

import tof.cv.mpp.Activity.BeTrainsActivity;
import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;


public class ClosestActivity extends BeTrainsActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.index=5;
		
		super.onCreate(savedInstanceState);
		Utils.setFullscreenIfNecessary(this);
		setContentView(R.layout.activity_closest);
	}

	
}