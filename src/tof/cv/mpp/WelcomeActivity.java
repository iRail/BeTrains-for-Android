package tof.cv.mpp;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import tof.cv.search.SearchDatabase;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

public class WelcomeActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	Bundle savedInstanceState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		Utils.setFullscreenIfNecessary(this);
		this.savedInstanceState=savedInstanceState;
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
				break;
			case 3:
				setFragment(new StarredFragment());
				break;
			case 4:
				setFragment(new TrafficFragment());
				break;
			case 5:
				setFragment(new ClosestFragment());
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
		if (findViewById(R.id.istablet) != null) {
			setFragment(new ChatFragment());
		} else {
			startActivity(new Intent(this, MyPreferenceActivity.class)
					.putExtra("screen", MyPreferenceActivity.PAGE_GENERAL));
		}
	}

	public void onHelpClick(View v) {
		MyOtherAlertDialog.create(WelcomeActivity.this).show();
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
		
		if(savedInstanceState == null) 
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

			ft.add(R.id.fragment, fragment);
			ft.commit();
		}
		
		
	}

	public static class MyOtherAlertDialog {

		public static AlertDialog create(Context context) {

			String message = "";
			if (context.getString(R.string.url_lang).equalsIgnoreCase("en"))
				message = "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"
						+ "<strong>Cool application, how can I help?</strong>"
						+ "<br />"
						+ "Please share the news on:<br />"
						+ "<a href=\"http://touch.facebook.com/#/profile.php?id=238334902800\">Our Facebook Page</a><br />"
						+ "<a href=\"http://twitter.com/betrains\">Our Twitter</a><br />"
						+ "<br /><br />"
						+ "<strong>Who made this application?</strong>"
						+ "<br />"
						+ "<br /><a href=\"http://twitter.com/waza_be\">Christophe Versieux</a><br /><br />"
						+ "<br /><a href=\"https://twitter.com/#!/gmludo\">Ludovic Gasc</a><br /><br />";

			if (context.getString(R.string.url_lang).equalsIgnoreCase("fr"))
				message = "  <head>  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"  /> "
						+ "<title>UTF-8</title>  </head> <body>  "
						+ "<strong>Trop fort cette appli, comment vous aider?</strong>"
						+ "<br />"
						+ "Parlez de l'appli autour de vous!:<br />"
						+ "<a href=\"http://touch.facebook.com/#/profile.php?id=238334902800\">Notre page Facebook</a><br />"
						+ "<a href=\"http://twitter.com/betrains\">Notre Twitter</a><br />"
						+ "<br /><br />"
						+ "<strong>Qui en sont les auteurs?</strong>"
						+ "<br />"
						+ "<br /><a href=\"http://twitter.com/waza_be\">Christophe Versieux</a><br /><br />"
						+ "<br /><a href=\"https://twitter.com/#!/gmludo\">Ludovic Gasc</a><br /><br />";

			if (context.getString(R.string.url_lang).equalsIgnoreCase("nl"))
				message = "  <head>  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"  /> "
						+ "<strong>Schitterende App !! Hoe kan ik helpen ?</strong>"
						+ "<br />"
						+ "Verspreid het nieuws over deze app : <br />"
						+ "<a href=\"http://touch.facebook.com/#/profile.php?id=238334902800\">Onze facebook pagina</a><br />"
						+ "<a href=\"http://twitter.com/betrains\">Onze twitterpagina</a><br />"
						+ "<br /><br />"
						+ "<strong>Wie heeft deze app gemaakt ? </strong>"
						+ "<br />"
						+ "<br /><a href=\"http://twitter.com/waza_be\">Christophe Versieux</a><br /><br />"
						+ "<br /><a href=\"https://twitter.com/#!/gmludo\">Ludovic Gasc</a><br /><br />";

			WebView messageWv = new WebView(context);
			messageWv.loadData(message, "text/html", "utf-8");

			return new AlertDialog.Builder(context)
					.setTitle(R.string.btn_home_contact).setCancelable(true)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(messageWv).create();
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