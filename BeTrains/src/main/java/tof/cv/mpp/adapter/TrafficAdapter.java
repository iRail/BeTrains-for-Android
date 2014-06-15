package tof.cv.mpp.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tof.cv.mpp.R;
import tof.cv.mpp.rss.RSSFeed;
import tof.cv.mpp.rss.RSSItem;


public class TrafficAdapter extends ArrayAdapter<RSSItem>{

	private LayoutInflater myLayoutInflater;
	private RSSFeed myRssFeed;
	
	public TrafficAdapter(Context context, int textViewResourceId,List<RSSItem> list,LayoutInflater layoutInflater,RSSFeed rssFeed) {
		super(context, textViewResourceId, list);
		this.myLayoutInflater = layoutInflater;
		this.myRssFeed = rssFeed;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		// return super.getView(position, convertView, parent);

		View row = convertView;

		if (row == null) {			
			row = myLayoutInflater.inflate(R.layout.row_rss, parent, false);
		}

		TextView listTitle = (TextView) row.findViewById(R.id.listtitle);
		listTitle.setText(myRssFeed.getList().get(position).getTitle());

		TextView listPubdate = (TextView) row
				.findViewById(R.id.listpubdate);
		listPubdate.setText(myRssFeed.getList().get(position).getPubdate());
		
		TextView message = (TextView) row
				.findViewById(R.id.message);
		message.setText(Html.fromHtml(myRssFeed.getList().get(position).getDescription()));

		return row;
	}
}
	

