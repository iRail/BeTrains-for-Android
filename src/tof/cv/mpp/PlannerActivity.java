package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class PlannerActivity extends SherlockFragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Utils.setFullscreenIfNecessary(this);

		setContentView(R.layout.activity_planner);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.home_btn_planner);

		Bundle extras = getIntent().getExtras();

		PlannerFragment fragment = (PlannerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragment);

		if (extras != null) {
			fragment.fromIntentArrivalStation = extras.getString("Arrival");
			fragment.fromIntentDepartureStation = extras.getString("Departure");
			fragment.fromIntent = true;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}