package tof.cv.ui;

import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.mpp.R;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;

public class BETrainsTabActivity extends TabActivity {

	private static String station;
	private static Activity monthis;
	public static Cursor mFavCursor;
	public static int mFavNumber = 1;
	private static ConnectionDbAdapter mDbHelper;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		monthis = this;

		mDbHelper = new ConnectionDbAdapter(this);
		mDbHelper.open();

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		
		if (settings.getBoolean("preffullscreen", false))
			setFullscreen();
		
		TabHost host = getTabHost();
		host.setup();

		host.addTab(host
				.newTabSpec("All")
				.setIndicator("",
						this.getResources().getDrawable(R.drawable.icon))
				.setContent(new Intent(this, SearchStationActivity.class)));
		host.addTab(host
				.newTabSpec("Europe")
				.setIndicator("",
						this.getResources().getDrawable(R.drawable.europe))
				.setContent(new Intent(this, SearchEuroTrainStationActivity.class)));
		
		host.addTab(host
				.newTabSpec("Favorites")
				.setIndicator("",
						this.getResources().getDrawable(R.drawable.star))
				.setContent(new Intent(this, SearchFavTrainStationActivity.class)));
		
		host.setBackgroundColor(Color.WHITE);

		mDbHelper.close();
		
		

		

				
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			System.out.println("**Back");
			Bundle bundle = new Bundle();
			bundle.putString("GARE", getStation());
			Intent mIntent = new Intent();
			mIntent.putExtras(bundle);
			setResult(RESULT_OK, mIntent);
			finish();
			return true;
		default:
			return false;

		}
	}

	public static void close() {
		System.out.println("**Quit");
		Bundle bundle = new Bundle();
		bundle.putString("GARE", getStation());
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		mDbHelper.close();
		monthis.setResult(RESULT_OK, mIntent);
		monthis.finish();

	}

	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public static void setStation(String station) {
		BETrainsTabActivity.station = station;
	}

	public static String getStation() {
		return station;
	}

}
