package tof.cv.ui;

import greendroid.app.GDListActivity;
import greendroid.widget.GDActionBar;
import greendroid.widget.GDActionBarItem;
import greendroid.widget.NormalActionBarItem;

import java.io.File;

import tof.cv.bo.Tablet;
import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import tof.cv.search.SearchDatabase;
import uk.co.jasonfry.android.tools.ui.PageControl;
import uk.co.jasonfry.android.tools.ui.SwipeView;
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
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Toast;

public class WelcomeActivity extends GDListActivity {
	/** Called when the activity is first created. */

	final static int ACTIVITY_STATION = 1;
	final static int ACTIVITY_TRAIN = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(this);

		setContentView(R.layout.activity_welcome);

		PageControl mPageControl = (PageControl) findViewById(R.id.page_control);
		SwipeView mSwipeView = (SwipeView) findViewById(R.id.swipe_view);
		if (mSwipeView != null)
			mSwipeView.setPageControl(mPageControl);

		Intent mIntent = getIntent();

		if (Intent.ACTION_VIEW.equals(mIntent.getAction())) {
			// handles a click on a search suggestion; launches activity to show
			// word
			Cursor cursor = managedQuery(mIntent.getData(), null, null, null,
					null);
			cursor.moveToFirst();
			int iIndex = cursor.getColumnIndexOrThrow(SearchDatabase.KEY_ITEM);
			int tIndex = cursor.getColumnIndexOrThrow(SearchDatabase.KEY_TYPE);
			String type = cursor.getString(tIndex);
			if (type.contentEquals("Station"))
				launchStationActivity(cursor.getString(iIndex));
			else
				launchTrainActivity(cursor.getString(iIndex));

			finish();
		} else if (Intent.ACTION_SEARCH.equals(mIntent.getAction())) {
			showResults(mIntent.getStringExtra(SearchManager.QUERY));
			finish();
		}
		GDActionBar mABar = getGDActionBar();
		if (findViewById(R.id.istablet) != null) {
			Tablet.loadTweets(this, getListView());
			mABar.setVisibility(View.GONE);

		} else {

			View home = (View) findViewById(R.id.gd_action_bar_home_item);
			home.setVisibility(View.GONE);

			mABar.setTitle("BeTrains for Android");
			addActionBarItem(getGDActionBar().newActionBarItem(
					NormalActionBarItem.class).setDrawable(
					R.drawable.ic_title_search), R.id.action_bar_searchid);

		}

	}

	public boolean onHandleActionBarItemClick(GDActionBarItem item, int position) {

		switch (position) {
		case 0:
			onSearchRequested();
			break;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}
		return true;
	}

	private void showResults(String query) {
		try {
			Integer.valueOf(query);
			Intent i = new Intent(this, InfoTrainActivity.class);
			i.putExtra(ConnectionDbAdapter.KEY_TRAINS,
					getString(R.string.txt_train) + "  " + query);
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

		i.putExtra(ConnectionDbAdapter.KEY_TRAINS, trainNumber);

		startActivityForResult(i, ACTIVITY_TRAIN);
	}

	private void launchStationActivity(String station) {

		Intent i = new Intent(WelcomeActivity.this, InfoStationActivity.class);
		i.putExtra("gare_name", station);
		i.putExtra("gare_id", PlannerActivity.getStationNumber(station));
		startActivityForResult(i, ACTIVITY_STATION);
	}

	public void onTrafficClick(View v) {
		startActivity(new Intent(this, TrafficActivity.class));
	}

	public void onTwitPrefClick(View v) {
		startActivity(new Intent(this, TwitterSettingsActivity.class));
	}

	public void onChatClick(View v) {
		startActivity(new Intent(this, MessagesActivity.class));
	}

	public void onStarredClick(View v) {
		startActivity(new Intent(this, StarredActivity.class));
	}

	public void onSettingsClick(View v) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void onTwitClick(View v) {
		startActivity(new Intent(this, TwitterActivity.class));
	}

	public void onHelpClick(View v) {

		MyOtherAlertDialog.create(WelcomeActivity.this).show();

	}

	public void onClosestClick(View v) {

		startActivity(new Intent(this, GetClosestStationsActivity.class));

	}
	
	public void Notif(View view) {
		Intent myIntent = new Intent(WelcomeActivity.this,
				NotificationActivity.class);
		WelcomeActivity.this.startActivityForResult(myIntent,0);
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

			return new AlertDialog.Builder(context).setTitle(
					R.string.btn_home_help).setCancelable(true).setIcon(
					android.R.drawable.ic_dialog_info).setView(messageWv)
					.create();
		}
	}

	public void onPlannerClick(View v) {
		startActivity(new Intent(this, PlannerActivity.class));
	}

	/*
	 * @Override public boolean onHandleActionBarItemClick(int position) {
	 * 
	 * switch (position) {
	 * 
	 * case 0: onSearchRequested(); return true; case 1:
	 * vf.setInAnimation(AnimationHelper.inFromRightAnimation());
	 * vf.setOutAnimation(AnimationHelper.outToLeftAnimation()); vf.showNext();
	 * return true;
	 * 
	 * default: return super.onHandleActionBarItemClick(position); } }
	 */

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			File file = new File(android.os.Environment
					.getExternalStorageDirectory(), "data/BeTrains");
			File[] files = file.listFiles();
			for (File f : files)
				f.delete();
		} catch (Exception e) {
		}

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
			updateIntent = new Intent(Intent.ACTION_VIEW, Uri
					.parse("market://details?id=be.irail.liveboards"));
			startActivity(updateIntent);

		}

	}
	
	public void onNotifClick(View v) {

		Intent intent = new Intent("android.intent.action.MAIN");
		intent.setClassName("be.irail.liveboards",
				"be.irail.liveboards.NotificationActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();

			Intent updateIntent = null;
			updateIntent = new Intent(Intent.ACTION_VIEW, Uri
					.parse("market://details?id=be.irail.liveboards"));
			startActivity(updateIntent);

		}

	}

	public void onDonateClick(View v) {
		//
		String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=christophe%2eversieux%40gmail%2ecom&lc=BE&item_name=C%2e%20Versieux&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	// for the previous movement
	public static Animation inFromRightAnimation() {

		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(350);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	public static Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(350);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	// for the next movement
	public static Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(350);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}

	public static Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(350);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}

}