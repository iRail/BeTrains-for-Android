package tof.cv.ui;

import greendroid.app.GDListActivity;
import greendroid.widget.GDActionBar;
import tof.cv.bo.DownloadTrafficTask;
import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import tof.cv.rss.RSSFeed;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class TrafficActivity extends GDListActivity {

	protected static final String TAG = "ActivityTraffic";
	private RSSFeed myRssFeed = null;	
	private String lang;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// setContentView(R.layout.activity_traffic);		
		SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(this);
		
		GDActionBar mABar = getGDActionBar();
		mABar.setTitle(getString(R.string.app_name));

		// TODO: language will be a parameter within the Intent.
		lang = this.getString(R.string.url_lang_2);
		if (settings.getBoolean("prefnl", false))
			lang = "nl";
		View home = (View) findViewById(R.id.gd_action_bar_home_item);
		home.setOnClickListener((new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(TrafficActivity.this,WelcomeActivity.class));
			}
		}));
		new DownloadTrafficTask(this).execute();

	}

	public String getLang(){
		return lang;
	}
	
	public RSSFeed getRssFeed(){
		return myRssFeed;
	}
	
	public void setRssFeed(RSSFeed rssFeed){
		this.myRssFeed=rssFeed;		
	}
	
	@Override
	public int createLayout() {
		return R.layout.activity_traffic;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setTitle(myRssFeed.getItem(position).getTitle());
		alertbox.setMessage(myRssFeed.getItem(position).getDescription());			
		// "\n\n"+myRssFeed.getItem(position).getPubdate());
		alertbox.setNeutralButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
		alertbox.show();
	}

}