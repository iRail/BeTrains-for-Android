package tof.cv.mpp;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.Utils.MyFragmentActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

public class WelcomeActivity extends MyFragmentActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(this);

		settings = PreferenceManager.getDefaultSharedPreferences(this);

		// Je vérifie si c'est lancé depuis le Launcher pour activer le bon
		// fragment
		if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			switch (Integer.valueOf(settings.getString("Activitypref", "1"))) {
			case 1:
				setContentView(R.layout.activity_welcome_with_fragment);
				setFragment(new PlannerFragment());
				break;
			case 3:
				// i = new Intent(WelcomeActivity.this, StarredActivity.class);
				// finish();
				// startActivity(i);
				break;
			case 4:
				// i = new Intent(WelcomeActivity.this, TrafficActivity.class);
				// finish();
				// startActivity(i);
				break;
			case 5:
				// i = new Intent(WelcomeActivity.this,
				// GetClosestStationsActivity.class);
				// finish();
				// startActivity(i);
				break;
			case 6:
				// i = new Intent(WelcomeActivity.this, MessagesActivity.class);
				// finish();
				// startActivity(i);
				break;
			default:
				setContentView(R.layout.activity_welcome);
				getSupportActionBar().setDisplayHomeAsUpEnabled(false);
				break;
			}
		} else {
			setContentView(R.layout.activity_welcome);
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}

	}

	public void onTwitClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new TwitterFragment());
		} else {
			Log.i("tag", "NULL");
			startActivity(new Intent(this, TwitterActivity.class));
		}

	}

	public void onPlannerClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new PlannerFragment());
		} else {
			Log.i("tag", "NULL");
			startActivity(new Intent(this, PlannerActivity.class));
		}
	}

	//Display the Fragment when the user does not want the dashboard as his start screen.
	public void setFragment(Fragment fragment) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment, fragment);
		ft.commit();
	}
	
	
}