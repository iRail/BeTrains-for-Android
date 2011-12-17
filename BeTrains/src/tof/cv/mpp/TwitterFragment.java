package tof.cv.mpp;

import java.io.File;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.Utils.Utils;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TwitterFragment extends ListFragment{
//http://search.twitter.com/search.json?q=BETRAINS%20OR%20SNCB%20OR%20NMBS
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(getActivity());
		
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.fragment_twitter, null);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.loadTweets(getActivity(), getListView());
	}

		
		//TODO ActionBar
		//GDActionBar mActionBar = getGDActionBar();
		//mActionBar.addItem(R.drawable.ic_title_settings);
		// addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(R.drawable.ic_title_settings),R.id.action_bar_settings);
		//mActionBar.setTitle("Twitter");

	
	
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
                File file= new File(android.os.Environment.getExternalStorageDirectory(),
                		"/Android/data/BeTrains/Twitter");
                File[] files=file.listFiles();
                for(File f:files)
                    f.delete();
            } catch (Exception e) {
    		}

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
