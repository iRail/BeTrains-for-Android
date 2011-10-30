package tof.cv.mpp;

import tof.cv.mpp.Utils.MyFragmentActivity;
import android.os.Bundle;

public class PlannerActivity extends MyFragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_planner);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
}