package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TwitterActivity extends SherlockFragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.setFullscreenIfNecessary(this);
		
		setContentView(R.layout.activity_twitter);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	


	
}