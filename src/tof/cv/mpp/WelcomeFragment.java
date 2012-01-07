package tof.cv.mpp;

import java.io.File;

import tof.cv.mpp.adapter.MenuAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.CirclePageIndicator;

public class WelcomeFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_welcome, null);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getSupportActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		ViewPager mPager = (ViewPager) getActivity().findViewById(R.id.pager);
		MenuAdapter adapter = new MenuAdapter(getActivity());
		mPager.setAdapter(adapter);

		CirclePageIndicator indicator = (CirclePageIndicator) getActivity().findViewById(R.id.indicator);
		indicator.setViewPager(mPager);
		indicator.setSnap(true);
	}

	
    
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, 0, Menu.NONE, "Filter")
				.setIcon(R.drawable.ic_menu_preferences)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
		case (0):
			startActivity(new Intent(getActivity(), PreferenceActivity.class).putExtra("screen", PreferenceActivity.PAGE_TWITTER));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
}
