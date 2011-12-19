package tof.cv.mpp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class ClosestActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_closest);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
}