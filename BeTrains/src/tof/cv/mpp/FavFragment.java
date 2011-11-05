package tof.cv.mpp;


import java.io.File;

import tof.cv.mpp.Utils.Tablet;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FavFragment extends ListFragment{
//http://search.twitter.com/search.json?q=BETRAINS%20OR%20SNCB%20OR%20NMBS
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//setContentView(R.layout.activity_twitter);
		Tablet.loadTweets(getActivity(),getListView());
		//GDActionBar mActionBar = getGDActionBar();
		//mActionBar.addItem(R.drawable.ic_title_settings);
		// addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(R.drawable.ic_title_settings),R.id.action_bar_settings);
		//mActionBar.setTitle("Twitter");

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_twitter, null);
	}
/*	
	public boolean onHandleActionBarItemClick(GDActionBarItem item, int position) {

		switch (position) {
		case 0:
			Intent i = new Intent(TwitterActivity.this,
					TwitterSettingsActivity.class);
			startActivity(i);
			break;

		default:
			return super.onHandleActionBarItemClick(item,position);
		}
		return true;
	}
	*/
    
    public void onDestroy() { 
            super.onDestroy(); 
            try {
                File file= new File(android.os.Environment.getExternalStorageDirectory(),"data/BeTrains");
                File[] files=file.listFiles();
                for(File f:files)
                    f.delete();
            } catch (Exception e) {
    		}

    } 
}
