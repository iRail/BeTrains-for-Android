package tof.cv.mpp;

import java.util.ArrayList;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.FilterTextWatcher;
import tof.cv.mpp.Utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBar.LayoutParams;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;

public class StationPickerActivity extends FragmentActivity {

	MyAdapter mAdapter;
	ViewPager mPager;
	private static final int ADD_ID = 1;
	private static final int REMOVE_ID = 2;

	private static DbAdapterConnection mDbHelper;

	protected static final String[] TITLES = new String[] { "FAVOURITE",
			"BELGIUM", "EUROPE" };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Utils.setFullscreenIfNecessary(this);
		setContentView(R.layout.fragment_tab_picker);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mAdapter = new MyAdapter(getSupportFragmentManager());

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(mPager, 1);

		mDbHelper = new DbAdapterConnection(this);

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

	public static class StationListFragment extends ListFragment implements
			OnScrollListener {
		static int mNum;
		private char mPrevLetter = '\'';
		private TextView mDialogText;
		private boolean mShowing;
		private boolean mReady;

		private final class RemoveWindow implements Runnable {
			public void run() {
				removeWindow();
			}
		}

		private void removeWindow() {
			if (mShowing) {
				mShowing = false;
				mDialogText.setVisibility(View.INVISIBLE);
			}
		}

		private RemoveWindow mRemoveWindow = new RemoveWindow();
		Handler mHandler = new Handler();
		private WindowManager mWindowManager;

		/**
		 * Create a new instance of CountingFragment, providing "num" as an
		 * argument.
		 */
		static StationListFragment newInstance(int num) {
			StationListFragment f = new StationListFragment();
			// Supply num input as an argument.
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);

			return f;
		}

		@Override
		public void onResume() {
			super.onResume();
			mReady = true;
		}

		@Override
		public void onPause() {
			super.onPause();
			removeWindow();
			mReady = false;
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if (mWindowManager != null)
				mWindowManager.removeView(mDialogText);
			mReady = false;
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

			String[] list = {};
			switch (mNum) {
			case 1:
				list = ConnectionMaker.LIST_OF_STATIONS;
				break;
			case 2:
				list = ConnectionMaker.LIST_OF_EURO_STATIONS;
				break;
			}

			getListView().setFastScrollEnabled(true);
			registerForContextMenu(getListView());

			ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, list);

			if (mNum == 1) {

				mWindowManager = (WindowManager) getActivity()
						.getSystemService(Context.WINDOW_SERVICE);

				LayoutInflater inflate = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mDialogText = (TextView) inflate.inflate(
						R.layout.list_position, null);
				mDialogText.setVisibility(View.INVISIBLE);

				mHandler.post(new Runnable() {

					public void run() {
						WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT,
								WindowManager.LayoutParams.TYPE_APPLICATION,
								WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
										| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
								PixelFormat.TRANSLUCENT);
						mWindowManager.addView(mDialogText, lp);
					}
				});

				EditText filterText = (EditText) getActivity().findViewById(
						R.id.search_box);
				FilterTextWatcher filterTextWatcher = new FilterTextWatcher(a);
				if (filterText != null) {
					filterText.addTextChangedListener(filterTextWatcher);
					getListView().setTextFilterEnabled(true);
				}
			}

			getListView().setOnScrollListener(this);

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

		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.add(0, ADD_ID, 0, "Favorite");
		}

		public boolean onContextItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case ADD_ID:
				AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
						.getMenuInfo();
				String sName = "";
				sName = (String) getListAdapter().getItem((int) menuInfo.id);
				mDbHelper.open();
				mDbHelper.createFav(sName, "", 1);
				mDbHelper.close();

				return true;
			default:
				return super.onContextItemSelected(item);
			}

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (mReady && mDialogText != null) {
				char firstLetter = view.getItemAtPosition(firstVisibleItem)
						.toString().charAt(0);

				if (!mShowing && firstLetter != mPrevLetter) {
					mShowing = true;
					mDialogText.setVisibility(View.VISIBLE);
				}
				mDialogText.setText(((Character) firstLetter).toString());
				mHandler.removeCallbacks(mRemoveWindow);
				mHandler.postDelayed(mRemoveWindow, 1000);
				mPrevLetter = firstLetter;
			}

		}

	}

	public static class StationFavListFragment extends ListFragment {
		static int mNum;
		private TextView mDialogText;
		private boolean mShowing;

		private void removeWindow() {
			if (mShowing) {
				mShowing = false;
				mDialogText.setVisibility(View.INVISIBLE);
			}
		}

		Handler mHandler = new Handler();
		private WindowManager mWindowManager;

		/**
		 * Create a new instance of CountingFragment, providing "num" as an
		 * argument.
		 */
		static StationFavListFragment newInstance(int num) {
			StationFavListFragment f = new StationFavListFragment();
			// Supply num input as an argument.
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);

			return f;
		}

		@Override
		public void onPause() {
			super.onPause();
			removeWindow();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			if (mWindowManager != null)
				mWindowManager.removeView(mDialogText);
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
			v = inflater.inflate(R.layout.fragment_station_list, container,
					false);
			return v;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			String[] list = {};
			switch (mNum) {
			case 0:
				mDbHelper.open();
				Cursor mCursor = mDbHelper.fetchAllFavStations();
				ArrayList<String> mArrayList = new ArrayList<String>();
				getActivity().startManagingCursor(mCursor);

				// TODO: Bug: I have to add first item manually.. Why?
				mCursor.moveToFirst();
				if (!mCursor.isAfterLast())
					mArrayList.add(mCursor.getString(mCursor
							.getColumnIndex(DbAdapterConnection.KEY_FAV_NAME)));

				for (mCursor.moveToFirst(); mCursor.moveToNext(); mCursor
						.isAfterLast()) {
					// The Cursor is now set to the right position
					mArrayList.add(mCursor.getString(mCursor
							.getColumnIndex(DbAdapterConnection.KEY_FAV_NAME)));
				}

				list = mArrayList.toArray(new String[mArrayList.size()]);
				mDbHelper.close();
				break;
			case 1:
				list = ConnectionMaker.LIST_OF_STATIONS;
				break;
			case 2:
				list = ConnectionMaker.LIST_OF_EURO_STATIONS;
				break;
			}

			getListView().setFastScrollEnabled(true);
			registerForContextMenu(getListView());

			ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, list);

			if (mNum == 1) {

				mWindowManager = (WindowManager) getActivity()
						.getSystemService(Context.WINDOW_SERVICE);

				LayoutInflater inflate = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mDialogText = (TextView) inflate.inflate(
						R.layout.list_position, null);
				mDialogText.setVisibility(View.INVISIBLE);

				mHandler.post(new Runnable() {

					public void run() {
						WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT,
								WindowManager.LayoutParams.TYPE_APPLICATION,
								WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
										| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
								PixelFormat.TRANSLUCENT);
						mWindowManager.addView(mDialogText, lp);
					}
				});

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

		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.add(0, REMOVE_ID, 0, "Remove");
		}

		public boolean onContextItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case REMOVE_ID:
				// TODO
				AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
						.getMenuInfo();
				String sName = "";
				sName = (String) getListAdapter().getItem((int) menuInfo.id);

				mDbHelper.open();
				mDbHelper.deleteFav(sName);
				mDbHelper.close();
				return true;
			default:
				return super.onContextItemSelected(item);
			}

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
			if (position == 0)
				return StationFavListFragment.newInstance(position);
			return StationListFragment.newInstance(position);
		}

		@Override
		public String getTitle(int position) {
			return TITLES[position % TITLES.length];
		}
	}
}
