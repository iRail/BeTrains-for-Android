package tof.cv.mpp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import tof.cv.mpp.MyPreferenceActivity.Prefs3Fragment;

public class TwitterFragment extends ListFragment {
	// http://search.twitter.com/search.json?q=BETRAINS%20OR%20SNCB%20OR%20NMBS

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_twitter, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		//UtilsWeb.loadTweets(getActivity(), getListView());

		//getActivity().getActionBar().setIcon(R.drawable.ab_twit);
        getActivity().getActionBar().setTitle("Twitter");
        getActivity().getActionBar().setSubtitle(null);
	}

	public void onDestroy() {
		super.onDestroy();
		try {
			File file = new File(
					android.os.Environment.getExternalStorageDirectory(),
					"/Android/data/BeTrains/Twitter");
			File[] files = file.listFiles();
			for (File f : files)
				f.delete();
		} catch (Exception e) {
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.txt_filter)
				.setIcon(R.drawable.ic_menu_preferences)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (0):
			if (Build.VERSION.SDK_INT >= 11)
				startActivity(new Intent(getActivity(),
						MyPreferenceActivity.class).putExtra(
						PreferenceActivity.EXTRA_SHOW_FRAGMENT,
						Prefs3Fragment.class.getName()));
			else {
				startActivity(new Intent(getActivity(),
						MyPreferenceActivity.class));
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
