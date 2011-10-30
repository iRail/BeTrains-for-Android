package tof.cv.mpp.Utils;

import tof.cv.mpp.WelcomeActivity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;

public class MyFragmentActivity extends FragmentActivity {
	
	//		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
	//if (settings.getBoolean("preffullscreen", false))
	//	ConnectionMaker.setFullscreen(getActivity());
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (android.R.id.home):
			// app icon in ActionBar is clicked; Go home
			Intent intent = new Intent(this, WelcomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
