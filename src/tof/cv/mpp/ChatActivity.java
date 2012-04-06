package tof.cv.mpp;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ChatActivity extends SherlockFragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Utils.setFullscreenIfNecessary(this);

		setContentView(R.layout.activity_chat);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.home_btn_chat);
		String trainId = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			trainId = extras.getString(DbAdapterConnection.KEY_NAME);
		}

		ChatFragment fragment = (ChatFragment) this.getSupportFragmentManager()
				.findFragmentById(R.id.fragment);
		fragment.trainId = trainId;
	}

}