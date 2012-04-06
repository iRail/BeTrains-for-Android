package tof.cv.mpp;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import tof.cv.search.SearchDatabase;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class WelcomeActivity extends SherlockFragmentActivity {
	/** Called when the activity is first created. */
	Bundle savedInstanceState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		Utils.setFullscreenIfNecessary(this);
		this.savedInstanceState = savedInstanceState;
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			// handles a click on a search suggestion; launches activity to show
			// word
			Cursor cursor = managedQuery(getIntent().getData(), null, null,
					null, null);
			cursor.moveToFirst();
			int iIndex = cursor.getColumnIndexOrThrow(SearchDatabase.KEY_ITEM);
			int tIndex = cursor.getColumnIndexOrThrow(SearchDatabase.KEY_TYPE);
			String type = cursor.getString(tIndex);
			if (type.contentEquals("Station"))
				launchStationActivity(cursor.getString(iIndex));
			else
				launchTrainActivity(cursor.getString(iIndex));

			finish();
		} else if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			showResults(getIntent().getStringExtra(SearchManager.QUERY));
			finish();
		}

		// Je vérifie si c'est lancé depuis le Launcher pour activer le bon
		// fragment
		setContentView(R.layout.activity_welcome);
		if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			switch (Integer.valueOf(settings.getString("Activitypref", "1"))) {
			case 1:
				setFragment(new PlannerFragment());
				getSupportActionBar().setIcon(R.drawable.home_btn_planner);
				break;
			case 3:
				setFragment(new StarredFragment());
				getSupportActionBar().setIcon(R.drawable.home_btn_starred);
				break;
			case 4:
				setFragment(new TrafficFragment());
				getSupportActionBar().setIcon(R.drawable.home_btn_traffic);
				break;
			case 5:
				setFragment(new ClosestFragment());
				getSupportActionBar().setIcon(R.drawable.home_btn_closest);
				break;
			default:
				setFragment(new WelcomeFragment());
				break;
			}
		} else {
			setFragment(new WelcomeFragment());
		}
	}

	public void onTwitClick(View v) {
		if (findViewById(R.id.istablet) != null) {
			setFragment(new TwitterFragment());
		} else {
			startActivity(new Intent(this, TwitterActivity.class));
		}

	}

	public void onTrafficClick(View v) {
		if (findViewById(R.id.istablet) != null) {

			setFragment(new TrafficFragment());
		} else {
			startActivity(new Intent(this, TrafficActivity.class));
		}
	}

	public void onStarredClick(View v) {
		if (findViewById(R.id.istablet) != null) {
			setFragment(new StarredFragment());
		} else {
			startActivity(new Intent(this, StarredActivity.class));
		}
	}

	public void onClosestClick(View v) {
		if (findViewById(R.id.istablet) != null) {
			setFragment(new ClosestFragment());
		} else {
			startActivity(new Intent(this, ClosestActivity.class));
		}
	}

	public void onPlannerClick(View v) {
		if (findViewById(R.id.istablet) != null) {
			setFragment(new PlannerFragment());
		} else {
			startActivity(new Intent(this, PlannerActivity.class));
		}
	}

	public void onChatClick(View v) {
		if (findViewById(R.id.istablet) != null) {
			setFragment(new ChatFragment());
		} else {
			startActivity(new Intent(this, ChatActivity.class));
		}
	}

	public void onSettingsClick(View v) {
			startActivity(new Intent(this, MyPreferenceActivity.class));
	}

	public void onHelpClick(View v) {
		Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.about_dialog);
		dialog.setTitle("About us");

		LinearLayout profile1 = (LinearLayout) dialog
				.findViewById(R.id.profil1);
		profile1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent profileIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://plus.google.com/117418174673875366560"));
				startActivity(profileIntent);

			}
		});

		LinearLayout profile2 = (LinearLayout) dialog
				.findViewById(R.id.profil2);
		profile2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent profileIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://twitter.com/#!/GMLudo"));
				startActivity(profileIntent);
			}
		});

		LinearLayout profile3 = (LinearLayout) dialog
				.findViewById(R.id.profil3);
		profile3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent profileIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://twitter.com/#!/uncorse"));
				startActivity(profileIntent);
			}
		});

		dialog.getWindow().getAttributes().width = LayoutParams.FILL_PARENT;
		dialog.show();
	}

	public void onIrailClick(View v) {

		Intent intent = new Intent("android.intent.action.MAIN");
		intent.setClassName("be.irail.liveboards",
				"be.irail.liveboards.WelcomeActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();

			Intent updateIntent = null;
			updateIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=be.irail.liveboards"));
			startActivity(updateIntent);

		}

	}

	// Display the Fragment when the user does not want the dashboard as his
	// start screen.
	public void setFragment(Fragment fragment) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();

			ft.add(R.id.fragment, fragment);
			ft.commit();
		}

	}

	private void showResults(String query) {
		try {
			Integer.valueOf(query);
			Intent i = new Intent(this, InfoTrainActivity.class);
			i.putExtra(DbAdapterConnection.KEY_NAME, query);
			startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.search_wrong_string,
					Toast.LENGTH_LONG).show();
		}

	}

	private void launchTrainActivity(String trainNumber) {
		Intent i = new Intent(this, InfoTrainActivity.class);

		i.putExtra("fromto", getString(R.string.app_name));

		i.putExtra(DbAdapterConnection.KEY_NAME, trainNumber);

		startActivity(i);
	}

	private void launchStationActivity(String station) {

		Intent i = new Intent(WelcomeActivity.this, InfoStationActivity.class);
		i.putExtra(DbAdapterConnection.KEY_NAME, station);
		startActivity(i);
	}
}