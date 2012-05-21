package tof.cv.mpp;

import tof.cv.mpp.Activity.BeTrainsActivity;
import tof.cv.mpp.Utils.Utils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;

public class TrafficActivity extends BeTrainsActivity {
	/** Called when the activity is first created. */

	protected static final String[] TITLES = new String[] { "RAILTIME",
			"B-RAIL" };
	MenuAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.index=2;
		super.onCreate(savedInstanceState);

		Utils.setFullscreenIfNecessary(this);

		setContentView(R.layout.activity_traffic);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.home_btn_traffic);

		mAdapter = new MenuAdapter(getSupportFragmentManager());

		ViewPager mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(mPager);

	}

	public static class MenuAdapter extends FragmentPagerAdapter implements
			TitleProvider {
		private int mCount = TITLES.length;

		public MenuAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mCount;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new TrafficFragment();
			case 1:
				return new TrafficHimFragment();
			}
			return null;
		}

		@Override
		public String getTitle(int position) {
			return TITLES[position % TITLES.length];
		}
	}

}