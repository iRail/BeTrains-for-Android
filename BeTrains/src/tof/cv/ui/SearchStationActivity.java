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

import tof.cv.adapters.MyIndexAdapter;
import tof.cv.handlers.FilterTextWatcher;
import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.misc.ConnectionMaker;
import tof.cv.misc.LocationDbHelper;
import tof.cv.mpp.R;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchStationActivity extends ListActivity {

	private ArrayList<String> elements;
	private EditText filterText = null;
	public static SharedPreferences prefs;
	private Cursor stationsCursor = null;
	private static final int ADD_ID = 1;
	private static final String TAG = "BETRAINS";
	private MyIndexAdapter<String> mIndexAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.getstation);
		elements = new ArrayList<String>();
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		ListView myListView = getListView();
		registerForContextMenu(myListView);

		/*
		 * 
		 * myListView.setAdapter(adapter);
		 */

		LocationDbHelper mDbHelper = new LocationDbHelper(this);
		mDbHelper.open();
		stationsCursor = mDbHelper.fetchAllLocations();
		filterText = (EditText) findViewById(R.id.search_box);
		/*if (stationsCursor.getCount() != 0) {
			stationsCursor.moveToFirst();
			while(!stationsCursor.isLast()){
				elements.add(stationsCursor.getString(stationsCursor.getColumnIndex(LocationDbHelper.KEY_STATION_NAME)));
				stationsCursor.move(1);	
			}
			elements.add(stationsCursor.getString(stationsCursor.getColumnIndex(LocationDbHelper.KEY_STATION_NAME)));
		} else*/
			for (int i = 0; i < ConnectionMaker.LIST_OF_STATIONS.length; i++) {
				
		elements.add(ConnectionMaker.LIST_OF_STATIONS[i]);
			}

		mIndexAdapter = new MyIndexAdapter<String>(getApplicationContext(),
				R.layout.row_station_picker, elements);
		FilterTextWatcher filterTextWatcher = new FilterTextWatcher(
				mIndexAdapter);
		filterText.addTextChangedListener(filterTextWatcher);
		this.setListAdapter(mIndexAdapter);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		if(android.os.Build.VERSION.SDK_INT<11)
			lv.setFastScrollEnabled(true);

		
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		ArrayAdapter<String> adapter = extracted(l);
		BETrainsTabActivity.setStation(adapter.getItem(position));
		BETrainsTabActivity.close();

	}

	@SuppressWarnings("unchecked")
	private ArrayAdapter<String> extracted(ListView l) {
		return (ArrayAdapter<String>) l.getAdapter();
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
			sName = mIndexAdapter.getItem((int) menuInfo.id);
			ConnectionDbAdapter mDbHelper;
			mDbHelper = new ConnectionDbAdapter(this);
			mDbHelper.open();
			mDbHelper.createFav(sName, "", 1);
			mDbHelper.close();

			Toast.makeText(this, sName + " Added to favorites",
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

}
