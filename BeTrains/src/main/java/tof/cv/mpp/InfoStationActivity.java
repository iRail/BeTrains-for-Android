package tof.cv.mpp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public class InfoStationActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info_station);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle bundle = this.getIntent().getExtras();
		long timestamp = bundle.getLong("timestamp")*1000;
		String name = bundle.getString("Name");
        getActionBar().setTitle(name+" infos:");
		
		InfoStationFragment fragment = (InfoStationFragment)getFragmentManager().findFragmentById(R.id.fragment);
		fragment.displayInfo(name,timestamp);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
	         Intent intent = new Intent(this, WelcomeActivity.class);            
	         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
	         startActivity(intent);  
	         finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
}