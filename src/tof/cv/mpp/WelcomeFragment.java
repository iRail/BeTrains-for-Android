package tof.cv.mpp;

import tof.cv.mpp.adapter.MenuAdapter;
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
		setHasOptionsMenu(true);
		
		ViewPager mPager = (ViewPager) getActivity().findViewById(R.id.pager);
		
		MenuAdapter adapter = new MenuAdapter();
		mPager.setAdapter(adapter);

		CirclePageIndicator indicator = (CirclePageIndicator) getActivity().findViewById(R.id.indicator);
		indicator.setViewPager(mPager);
		indicator.setSnap(true);
	}
	
	  
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, 0, Menu.NONE, "Search")
				.setIcon(R.drawable.ic_menu_search)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (0):
			getActivity().onSearchRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
}
