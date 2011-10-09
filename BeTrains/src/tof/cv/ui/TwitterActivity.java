package tof.cv.ui;


import greendroid.app.GDListActivity;
import greendroid.widget.GDActionBar;
import greendroid.widget.GDActionBarItem;
import greendroid.widget.NormalActionBarItem;

import java.io.File;

import tof.cv.bo.Tablet;
import tof.cv.mpp.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TwitterActivity extends GDListActivity{
//http://search.twitter.com/search.json?q=BETRAINS%20OR%20SNCB%20OR%20NMBS
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);
		Tablet.loadTweets(this,getListView());
		GDActionBar mActionBar = getGDActionBar();
		//mActionBar.addItem(R.drawable.ic_title_settings);
		 addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(R.drawable.ic_title_settings),R.id.action_bar_settings);
		mActionBar.setTitle("Twitter");

		View home = (View) findViewById(R.id.gd_action_bar_home_item);
		home.setOnClickListener((new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(TwitterActivity.this,WelcomeActivity.class));
			}
		}));

	}
	
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
	
    @Override 
    protected void onDestroy() { 
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
