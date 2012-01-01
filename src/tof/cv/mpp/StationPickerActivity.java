package tof.cv.mpp;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.Utils.FilterTextWatcher;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.AlphabeticalAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.viewpagerindicator.R;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;

public class StationPickerActivity extends FragmentActivity {

	MyAdapter mAdapter;

	ViewPager mPager;

	protected static final String[] TITLES = new String[] { "FAVOURITE",
			"BELGIUM", "EUROPE" };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Utils.setFullscreenIfNecessary(this);

		setContentView(R.layout.fragment_pref_picker);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mAdapter = new MyAdapter(getSupportFragmentManager());

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(mPager, 1);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (android.R.id.home):
			// app icon in ActionBar is clicked; Go home
			Intent intent = new Intent(this, PlannerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static class ArrayListFragment extends ListFragment {
		static int mNum;

		/**
		 * Create a new instance of CountingFragment, providing "num" as an
		 * argument.
		 */
		static ArrayListFragment newInstance(int num) {
			ArrayListFragment f = new ArrayListFragment();
			// Supply num input as an argument.
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);

			return f;
		}

		/**
		 * When creating, retrieve this instance's number from its arguments.
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			// Notifier au créateur de ActionBarSherlock que le mNum doit être
			// défini dans la onCreateView (cf Fragment lifeCycle)
			// mNum = getArguments() != null ? getArguments().getInt("num") : 1;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mNum = getArguments() != null ? getArguments().getInt("num") : 1;
			View v = null;
			if (mNum != 1)
				v = inflater.inflate(R.layout.fragment_station_list, container,
						false);
			else {
				v = inflater.inflate(R.layout.fragment_station_picker,
						container, false);
			}

			return v;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			String[] list = null;
			switch (mNum) {
			case 0:
				list = ConnectionMaker.LIST_OF_FAV_STATIONS;
				break;
			case 1:
				list = ConnectionMaker.LIST_OF_STATIONS;
				break;
			case 2:
				list = ConnectionMaker.LIST_OF_EURO_STATIONS;
				break;
			}

			getListView().setFastScrollEnabled(true);

			AlphabeticalAdapter a = new AlphabeticalAdapter(getActivity(), list);

			if (mNum == 1) {
				EditText filterText = (EditText) getActivity().findViewById(
						R.id.search_box);
				FilterTextWatcher filterTextWatcher = new FilterTextWatcher(a);
				if (filterText != null) {
					filterText.addTextChangedListener(filterTextWatcher);
					getListView().setTextFilterEnabled(true);
				}
			}

			this.setListAdapter(a);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			Bundle bundle = new Bundle();
			bundle.putString("GARE", l.getItemAtPosition(position).toString());
			Intent i = new Intent();
			i.putExtras(bundle);
			getActivity().setResult(RESULT_OK, i);
			getActivity().finish();
		}

	}

	public static class MyAdapter extends FragmentPagerAdapter implements
			TitleProvider {
		private int mCount = TITLES.length;

		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mCount;
		}

		@Override
		public Fragment getItem(int position) {
			return ArrayListFragment.newInstance(position);
		}

		@Override
		public String getTitle(int position) {
			return TITLES[position % TITLES.length];
		}
	}
}
