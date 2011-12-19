package tof.cv.mpp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TwitterActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
}