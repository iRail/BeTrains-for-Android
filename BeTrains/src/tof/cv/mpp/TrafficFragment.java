package tof.cv.mpp;


import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.rss.DownloadTrafficTask;
import tof.cv.mpp.rss.RSSFeed;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TrafficFragment extends ListFragment{
	protected static final String TAG = "ActivityTraffic";
	private RSSFeed myRssFeed = null;	
	private String lang;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(getActivity());
		return inflater.inflate(R.layout.fragment_traffic, null);
	}

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(getActivity());

		//TODO: ActionBAr
		//GDActionBar mABar = getGDActionBar();
		//mABar.setTitle(getString(R.string.app_name));

		// TODO: language will be a parameter within the Intent.
		lang = this.getString(R.string.url_lang_2);
		if (settings.getBoolean("prefnl", false))
			lang = "nl";
		
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
	
//TODO
/*	@Override
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
	}*/
}
