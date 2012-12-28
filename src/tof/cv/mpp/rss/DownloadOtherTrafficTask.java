package tof.cv.mpp.rss;

import java.net.MalformedURLException;
import java.net.URL;

import tof.cv.mpp.R;
import tof.cv.mpp.TrafficFragment;
import tof.cv.mpp.adapter.TrafficAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

public class DownloadOtherTrafficTask extends AsyncTask<URL, Integer, Long> {

	private RSSFeed myRssFeed;
	private Context context;
	private LayoutInflater layoutInflater;
	private String lang;
	private TrafficFragment trafFrag;

	public DownloadOtherTrafficTask(TrafficFragment trafFrag2) {
		this.trafFrag = trafFrag2;
		context = trafFrag2.getActivity();
		myRssFeed = trafFrag2.getRssFeed();
		layoutInflater = trafFrag2.getActivity().getLayoutInflater();
		lang = trafFrag2.getLang();
	}

	protected Long doInBackground(URL... params) {
		updateData();
		return null;
	}

	protected void onPostExecute(Long result) {
		try {
			if (myRssFeed != null) {
				if (myRssFeed.getList().size() > 0) {
					Log.i("", myRssFeed.getList().get(0).getDescription());
					TrafficAdapter a = (TrafficAdapter) trafFrag
							.getListAdapter();
					if (a == null) {
						a = new TrafficAdapter(context, R.layout.row_rss,
								myRssFeed.getList(), layoutInflater, myRssFeed);
						trafFrag.setListAdapter(a);
					} else {
						for (RSSItem item : myRssFeed.itemList)
							a.add(item);

						a.notifyDataSetChanged();
					}

				} else {
					TextView feedEmpty = (TextView) trafFrag.getActivity()
							.findViewById(android.R.id.empty);
					feedEmpty
							.setText(trafFrag.getString(R.string.txt_no_issue));
				}
			} else {
				TextView feedEmpty = (TextView) trafFrag.getView()
						.findViewById(android.R.id.empty);
				feedEmpty.setText(trafFrag.getString(R.string.txt_connection));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateData() {

		try {
			/*
			 * getting rss feed from the railtime.be website
			 */
			URL rssUrl = new URL(
					"http://him-hari.b-rail.be/him/rss/export/rss_feed_" + lang.substring(0,1)
							+ "_8192.xml");

			RSSDocument rssDoc = new RSSDocument(rssUrl);
			myRssFeed = rssDoc.getRSSFeed();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
