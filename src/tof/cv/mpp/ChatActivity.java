package tof.cv.mpp;

import tof.cv.mpp.Utils.DbAdapterConnection;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ChatActivity extends SherlockFragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		String trainId = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			trainId = extras.getString(DbAdapterConnection.KEY_NAME);
		}

		ChatFragment fragment = (ChatFragment) this.getSupportFragmentManager()
				.findFragmentById(R.id.fragment);
		if (trainId != null)
			fragment.trainId = getString(R.string.txt_train) + " " + trainId.replaceAll(getString(R.string.txt_train)+ " ","");
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