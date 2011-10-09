package tof.cv.ui;

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.savedInstanceState
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;

import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.mpp.R;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SearchFavTrainStationActivity extends ListActivity {

	private final String[] LISTE_GARES = new String[] { "'A", "B", "C" };
	public final String[] LISTE_GARES_TO_W = new String[] { "'A", "B", "C" };
	public static ArrayList<String> favList = new ArrayList<String>();
	private static final int REMOVE_ID = 1;
	private Cursor mCursor;
	private ConnectionDbAdapter mDbHelper;
	private ArrayList<String> elements;

	// MyIndexerAdapter<String> adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.getfavstation);

		elements = new ArrayList<String>();
		for (int i = 0; i < LISTE_GARES.length; i++) {

			elements.add(LISTE_GARES[i]);

		}

		registerForContextMenu(getListView());

		mDbHelper = new ConnectionDbAdapter(this);
			}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mCursor.moveToPosition(position);
		BETrainsTabActivity.setStation(mCursor.getString(mCursor
				.getColumnIndex(ConnectionDbAdapter.KEY_FAV_NAME)));
		BETrainsTabActivity.close();

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			BETrainsTabActivity.setStation("");
			BETrainsTabActivity.close();
			return true;
		default:
			return false;
		}
	}

	private void fillData() {
		// Get all of the rows from the database and create the item list
		mDbHelper.open();
		mCursor = mDbHelper.fetchAllFavStations();

		if (mCursor.getCount() == 0)
			Toast.makeText(this,
					"To add a favorite, long-click on a station name.",
					Toast.LENGTH_LONG).show();

		startManagingCursor(mCursor);

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] { ConnectionDbAdapter.KEY_FAV_NAME };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { R.id.station };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter favs = null;

		favs = new SimpleCursorAdapter(this,
				R.layout.row_station_picker, mCursor, from, to);

		/*
		 * ListView myListView = getListView(); adapter = new
		 * MyIndexerAdapter<String>( getApplicationContext(),
		 * android.R.layout.simple_list_item_1, elements);
		 * myListView.setAdapter(adapter);
		 */

		setListAdapter(favs);
		mDbHelper.close();
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, REMOVE_ID, 0, "Remove");
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case REMOVE_ID:
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.open();
			mDbHelper.deleteFav(menuInfo.id);
			fillData();
			mDbHelper.close();
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}


	public void onResume() {
		super.onResume();

		fillData();

	}

}
