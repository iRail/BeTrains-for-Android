package tof.cv.mpp;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.rss.DownloadTrafficTask;
import tof.cv.mpp.rss.RSSFeed;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class TrafficFragment extends ListFragment {
	protected static final String TAG = "ActivityTraffic";
	private RSSFeed myRssFeed = null;
	private String lang;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(getActivity());
		return inflater.inflate(R.layout.fragment_traffic, null);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		// TODO: ActionBAr
		// GDActionBar mABar = getGDActionBar();
		// mABar.setTitle(getString(R.string.app_name));

		lang = this.getString(R.string.url_lang_2);
		if (settings.getBoolean("prefnl", false))
			lang = "nl";

		new DownloadTrafficTask(this).execute();

	}

	public String getLang() {
		return lang;
	}

	public RSSFeed getRssFeed() {
		return myRssFeed;
	}

	public void setRssFeed(RSSFeed rssFeed) {
		this.myRssFeed = rssFeed;
	}
	
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i("FragmentList", "Item clicked: " + id);
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this.getActivity());
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
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (android.R.id.home):
			// app icon in ActionBar is clicked; Go home
			Intent intent = new Intent(getActivity(), WelcomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
