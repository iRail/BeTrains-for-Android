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
import tof.cv.mpp.R;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchEuroTrainStationActivity extends ListActivity {

	private ArrayList<String> elements;
	private MyIndexAdapter<String> adapter = null;
	private EditText filterText = null;
	public static SharedPreferences prefs;
	private static final int ADD_ID = 1;
	private TextWatcher filterTextWatcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setFullscreen();
		setNoTitle();
		setContentView(R.layout.getstation);

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		elements = new ArrayList<String>();
		for (int i = 0; i < ConnectionMaker.LIST_OF_EURO_STATIONS.length; i++) {

			elements.add(ConnectionMaker.LIST_OF_EURO_STATIONS[i]);

		}
		adapter = new MyIndexAdapter<String>(getApplicationContext(),
				R.layout.row_station_picker, elements);
		filterTextWatcher = new FilterTextWatcher(adapter);
		filterText = (EditText) findViewById(R.id.search_box);
		filterText.addTextChangedListener(filterTextWatcher);

		ListView myListView = getListView();
		registerForContextMenu(myListView);
		
		myListView.setAdapter(adapter);

		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);

		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);
		

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) l.getAdapter();
		BETrainsTabActivity.setStation(adapter.getItem(position));
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
			String Sname = adapter.getItem((int) menuInfo.id);
			ConnectionDbAdapter mDbHelper;
			mDbHelper = new ConnectionDbAdapter(this);
			mDbHelper.open();
			mDbHelper.createFav(Sname,"",1);
			mDbHelper.close();

			Toast.makeText(this, Sname + " Added to favorites",
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void setNoTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void onResume() {
		super.onResume();		
	}

}
