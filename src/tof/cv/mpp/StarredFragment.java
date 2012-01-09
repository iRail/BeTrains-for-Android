package tof.cv.mpp;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.adapter.FavAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class StarredFragment extends ListFragment {
	protected static final String TAG = "StarredFragment";
	private static DbAdapterConnection mDbHelper;
	private Cursor mCursor ;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_starred, null);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		mDbHelper = new DbAdapterConnection(getActivity());

	}

	public void onResume() {
		super.onResume();
		registerForContextMenu(getListView());
		populateList();
	}
	
	public void populateList(){
		mDbHelper.open();
		mCursor = mDbHelper.fetchAllFav();
		FavAdapter fAdapter = new FavAdapter(getActivity(), mCursor);
		setListAdapter(fAdapter);
		mDbHelper.close();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		mCursor.moveToPosition(position);
		String item=mCursor.getString(mCursor
				.getColumnIndex(DbAdapterConnection.KEY_FAV_NAME));
		String itemTwo=mCursor.getString(mCursor
				.getColumnIndex(DbAdapterConnection.KEY_FAV_NAMETWO));
		int type=mCursor.getInt(mCursor
				.getColumnIndex(DbAdapterConnection.KEY_FAV_TYPE));
		Intent i;
		switch (type) {
		case 1:
			i = new Intent(getActivity(),
					InfoStationActivity.class);
			i.putExtra("Name", item);
			startActivity(i);
			break;
		case 2:
			i = new Intent(getActivity(),
					InfoTrainActivity.class);
			i.putExtra("Name",item);
			startActivity(i);
			break;
		case 3:
			i = new Intent(getActivity(),
					PlannerActivity.class);
			i.putExtra("Departure",item);
			i.putExtra("Arrival",itemTwo);
			startActivity(i);
		
			break;
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
