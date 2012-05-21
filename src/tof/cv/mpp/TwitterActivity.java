package tof.cv.mpp;

import tof.cv.mpp.Activity.BeTrainsActivity;
import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;

public class TwitterActivity extends BeTrainsActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.index=1;
		
		super.onCreate(savedInstanceState);
		
		Utils.setFullscreenIfNecessary(this);
		setContentView(R.layout.activity_twitter);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.home_btn_twit);
	}
	


	
}