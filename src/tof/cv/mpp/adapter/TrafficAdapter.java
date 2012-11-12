package tof.cv.mpp.adapter;

import java.util.List;

import tof.cv.mpp.R;
import tof.cv.mpp.rss.RSSFeed;
import tof.cv.mpp.rss.RSSItem;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


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
		
		int color1=0x00101010;
		int color2=0xfff5f5f5;
		
		if (position % 2 == 0) {
			listTitle.setBackgroundColor(color1);
			listPubdate.setBackgroundColor(color1);
		} else {
			listTitle.setBackgroundColor(color2);
			listPubdate.setBackgroundColor(color2);
		}

		return row;
	}
}
	

