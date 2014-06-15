package tof.cv.mpp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;


public class CompensationActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compensation);
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