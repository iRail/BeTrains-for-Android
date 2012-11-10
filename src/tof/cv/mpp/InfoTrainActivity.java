package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class InfoTrainActivity extends SherlockFragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.setFullscreenIfNecessary(this);
		
		setContentView(R.layout.activity_info_train);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle bundle = this.getIntent().getExtras();
		long timestamp = bundle.getLong("timestamp")*1000;
		String name = bundle.getString("Name").replaceAll("[^0-9]+", "");
		String fromTo = bundle.getString("fromto");
		getSupportActionBar().setTitle(name+" infos:");
		
		InfoTrainFragment fragment = (InfoTrainFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
		fragment.displayInfo(name,fromTo,timestamp);
	}

	
}