package tof.cv.mpp;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.R;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.adapter.MenuAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;

public class WelcomeActivity extends FragmentActivity {
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
				setWelcomeContent();
				break;
			}
		} else {
			setWelcomeContent();
		}
	}
	
	public void setWelcomeContent(){
		setContentView(R.layout.activity_welcome);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		
		ViewPager mPager=(ViewPager) this.findViewById(R.id.pager);
		MenuAdapter adapter= new MenuAdapter(this);
		mPager.setAdapter(adapter);
		
		CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(mPager);
		indicator.setSnap(true);
		
	}

	public void onTwitClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new TwitterFragment());
		} else {
			startActivity(new Intent(this, TwitterActivity.class));
		}

	}

	public void onTrafficClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new TrafficFragment());
		} else {
			startActivity(new Intent(this, TrafficActivity.class));
		}
	}

	public void onStarredClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new StarredFragment());
		} else {
			startActivity(new Intent(this, StarredActivity.class));
		}
	}

	public void onClosestClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new ClosestFragment());
		} else {
			startActivity(new Intent(this, ClosestActivity.class));
		}
	}

	public void onPlannerClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new PlannerFragment());
		} else {
			startActivity(new Intent(this, PlannerActivity.class));
		}
	}

	public void onChatClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new ChatFragment());
		} else {
			startActivity(new Intent(this, ChatActivity.class));
		}
	}

	// Display the Fragment when the user does not want the dashboard as his
	// start screen.
	public void setFragment(Fragment fragment) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment, fragment);
		ft.commit();
	}

}