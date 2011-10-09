package tof.cv.bo;

import java.net.MalformedURLException;
import java.net.URL;

import tof.cv.adapters.TrafficAdapter;
import tof.cv.mpp.R;
import tof.cv.rss.RSSDocument;
import tof.cv.rss.RSSFeed;
import tof.cv.ui.TrafficActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.TextView;

public class DownloadTrafficTask extends AsyncTask<URL, Integer, Long> {

	private RSSFeed myRssFeed;
	private Context context;
	private LayoutInflater layoutInflater;
	private String lang;
	private TrafficActivity trafAct;

	public DownloadTrafficTask(TrafficActivity trafAct) {
		this.trafAct = trafAct;
		context = trafAct;
		myRssFeed = trafAct.getRssFeed();
		layoutInflater = trafAct.getLayoutInflater();
		lang = trafAct.getLang();
	}

	protected Long doInBackground(URL... params) {
		updateData();
		return null;
	}

	protected void onPostExecute(Long result) {
		if (myRssFeed != null) {
			if (myRssFeed.getList().size() > 0) {
				TrafficAdapter adapter = new TrafficAdapter(context,
						R.layout.row_rss, myRssFeed.getList(), layoutInflater,
						myRssFeed);
				trafAct.setListAdapter(adapter);
				trafAct.setRssFeed(myRssFeed);
			} else {
				TextView feedEmpty = (TextView) trafAct
						.findViewById(android.R.id.empty);
				feedEmpty.setText(trafAct.getString(R.string.txt_no_issue));
			}
		} else {
			TextView feedEmpty = (TextView) trafAct
					.findViewById(android.R.id.empty);
			feedEmpty.setText(trafAct.getString(R.string.txt_connection));
		}
	}

	private void updateData() {

		try {
			/*
			 * getting rss feed from the railtime.be website
			 */
			URL rssUrl = new URL(
					"http://www.railtime.be/website/RSS/RssInfoBar_" + lang
							+ ".xml");

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
