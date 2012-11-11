package tof.cv.mpp;

import tof.cv.mpp.rss.DownloadOtherTrafficTask;
import tof.cv.mpp.rss.RSSFeed;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;

public class TrafficHimFragment extends SherlockListFragment {
	protected static final String TAG = "ActivityTraffic";
	private RSSFeed myRssFeed = null;
	private String lang;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_traffic_him, null);
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

		lang = this.getString(R.string.url_lang_for_him);
		if (settings.getBoolean("prefnl", false)) {
			lang = "n";
		}

		new DownloadOtherTrafficTask(this).execute();

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
		AlertDialog.Builder alertbox = new AlertDialog.Builder(
				this.getActivity());
		alertbox.setTitle(myRssFeed.getItem(position).getTitle());
		alertbox.setMessage(Html.fromHtml(myRssFeed.getItem(position).getDescription()));
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
