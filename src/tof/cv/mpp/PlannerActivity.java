package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class PlannerActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Utils.setFullscreenIfNecessary(this);

		setContentView(R.layout.activity_planner);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = getIntent().getExtras();

		PlannerFragment fragment = (PlannerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragment);

		if (extras != null) {
			fragment.fromIntentArrivalStation=extras.getString("Arrival");
			fragment.fromIntentDepartureStation=extras.getString("Departure");
			fragment.fromIntent=true;
		} 
	}

}