package tof.cv.mpp;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ChatActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Utils.setFullscreenIfNecessary(this);

		setContentView(R.layout.activity_chat);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		String trainId = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			trainId = extras.getString(DbAdapterConnection.KEY_NAME);
		}

		ChatFragment fragment = (ChatFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragment);
		fragment.trainId = trainId;
	}

}