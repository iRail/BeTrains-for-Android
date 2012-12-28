package tof.cv.mpp;

import tof.cv.mpp.rss.DownloadTrafficTask;
import tof.cv.mpp.rss.RSSFeed;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;

public class TrafficFragment extends SherlockListFragment {
	protected static final String TAG = "ActivityTraffic";
	private RSSFeed myRssFeed = null;
	private String lang;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_traffic, null);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		lang = this.getString(R.string.url_lang);
		if (settings.getBoolean("prefnl", false)) {
			lang = "nl";
		}

		new DownloadTrafficTask(this).execute();
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getSherlockActivity().getSupportActionBar().setIcon(R.drawable.ab_traffic);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.btn_home_traffic);
		getSherlockActivity().getSupportActionBar().setSubtitle(null);
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

	/*@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("FragmentList", "Item clicked: " + id);
		
		TrafficAdapter a=(TrafficAdapter) this.getListAdapter();
		
		AlertDialog.Builder alertbox = new AlertDialog.Builder(
				this.getActivity());
		alertbox.setTitle(a.getItem(position).getTitle());
		alertbox.setMessage(Html.fromHtml(a.getItem(position).getDescription()));
		// "\n\n"+myRssFeed.getItem(position).getPubdate());
		alertbox.setNeutralButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
		alertbox.show();
	}*/

}
