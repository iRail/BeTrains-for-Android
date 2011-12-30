package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class InfoTrainActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.setFullscreenIfNecessary(this);
		
		setContentView(R.layout.activity_info_train);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle bundle = this.getIntent().getExtras();
		String name = bundle.getString("Name");
		getSupportActionBar().setTitle(name+" infos:");
		
		InfoTrainFragment fragment = (InfoTrainFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
		fragment.displayInfo(name);
	}

	
}