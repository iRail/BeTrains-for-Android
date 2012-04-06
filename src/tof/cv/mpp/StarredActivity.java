package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class StarredActivity extends SherlockFragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.setFullscreenIfNecessary(this);
		
		setContentView(R.layout.activity_starred);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.home_btn_starred);	
	}

	
}