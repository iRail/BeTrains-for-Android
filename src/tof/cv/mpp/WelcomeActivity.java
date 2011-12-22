package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.MenuAdapter;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.webkit.WebView;

import com.viewpagerindicator.CirclePageIndicator;

public class WelcomeActivity extends FragmentActivity {
	/** Called when the activity is first created. */
	//TEST CVE
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		Utils.setFullscreenIfNecessary(this);

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
	
	public void onSettingsClick(View v) {
		if (findViewById(R.id.fragment) != null) {
			setFragment(new ChatFragment());
		} else {
			startActivity(new Intent(this, SettingsActivity.class));
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
			updateIntent = new Intent(Intent.ACTION_VIEW, Uri
					.parse("market://details?id=be.irail.liveboards"));
			startActivity(updateIntent);

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

}