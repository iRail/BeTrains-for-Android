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

import greendroid.app.GDListActivity;
import greendroid.widget.GDActionBar;
import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class StarredActivity extends GDListActivity {

	private static SharedPreferences settings;

	private String TAG = "StarredActivity";
	private ConnectionDbAdapter mDbHelper;
	private FavouriteAdapter fAdapter;
	private Cursor mCursor;
	
	private static final int REMOVE_ID = 1;

	private StarredActivity context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(this);

		// setActionBarContentView(R.layout.activity_starred);

		GDActionBar mABar = getGDActionBar();
		mABar.setTitle(getString(R.string.btn_home_starred));

		mDbHelper = new ConnectionDbAdapter(context);
		View home = (View) findViewById(R.id.gd_action_bar_home_item);
		home.setOnClickListener((new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(StarredActivity.this,WelcomeActivity.class));
			}
		}));

		registerForContextMenu(getListView());
		

	}

	public void fillFavorites() {
		
		mCursor = mDbHelper.fetchAllFav();
		if (mCursor.getCount() == 0){

			if (Integer.valueOf(settings.getString("Activitypref", "1"))==3){
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("Activitypref", "1");
				editor.commit();
			}
				
			ConnectionMaker.createAlertDialogAndFinish(getString(R.string.fav_no_fav_title),getString(R.string.fav_no_fav_body),context);
		}
			
		startManagingCursor(mCursor);

		this.fAdapter = new FavouriteAdapter(context, mCursor);
		setListAdapter(this.fAdapter);
	}

	public class FavouriteAdapter extends CursorAdapter {

		private final LayoutInflater mInflater;

		public FavouriteAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = LayoutInflater.from(context);

		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			View rowView = view;
			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
			TextView nameTv = (TextView) rowView.findViewById(R.id.firstLine);
			TextView typeTv = (TextView) rowView.findViewById(R.id.secondLine);
			int nameColumn = cursor
					.getColumnIndex(ConnectionDbAdapter.KEY_FAV_NAME);
			int nameTwoColumn = cursor
			.getColumnIndex(ConnectionDbAdapter.KEY_FAV_NAMETWO);
			int typeColumn = cursor
					.getColumnIndex(ConnectionDbAdapter.KEY_FAV_TYPE);

			int type = cursor.getInt(typeColumn);
			switch (type) {
			case 1:
				typeTv.setText(getString(R.string.txt_station));
				imageView.setImageResource(R.drawable.ic_fav_station);
				nameTv.setText(cursor.getString(nameColumn));
				break;
			case 2:
				typeTv.setText(getString(R.string.txt_train));
				imageView.setImageResource(R.drawable.ic_fav_train);
				nameTv.setText(cursor.getString(nameColumn));
				break;
			case 3:
				typeTv.setText(getString(R.string.txt_trip));
				imageView.setImageResource(R.drawable.ic_fav_map);
				nameTv.setText(cursor.getString(nameColumn)+" - "+cursor.getString(nameTwoColumn));
				break;
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.row_favorite, parent, false);
			bindView(v, context, cursor);
			return v;
		}
	}

	@Override
	public int createLayout() {
		return R.layout.activity_starred;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mCursor.moveToPosition(position);
		String item=mCursor.getString(mCursor
				.getColumnIndex(ConnectionDbAdapter.KEY_FAV_NAME));
		String itemTwo=mCursor.getString(mCursor
				.getColumnIndex(ConnectionDbAdapter.KEY_FAV_NAMETWO));
		int type=mCursor.getInt(mCursor
				.getColumnIndex(ConnectionDbAdapter.KEY_FAV_TYPE));
		Intent i;
		switch (type) {
		case 1:
			i = new Intent(StarredActivity.this,
					InfoStationActivity.class);
			i.putExtra("gare_name", item);
			int stationId = PlannerActivity.getStationNumber(item);
			i.putExtra("gare_id", stationId);
			startActivity(i);
			break;
		case 2:
			i = new Intent(StarredActivity.this,
					InfoTrainActivity.class);
			i.putExtra(ConnectionDbAdapter.KEY_TRAINS,item);
			startActivity(i);
			break;
		case 3:
			i = new Intent(StarredActivity.this,
					PlannerActivity.class);
			i.putExtra("Departure",item);
			i.putExtra("Arrival",itemTwo);
			startActivity(i);
		
			break;
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, REMOVE_ID, 0, R.string.txt_remove);
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case REMOVE_ID:
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteFav(menuInfo.id);
			fillFavorites();
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}
	
	public void onResume() {
		super.onResume();
		mDbHelper.open();
		fillFavorites();
	}
	public void onPause() {
		super.onPause();
		mDbHelper.close();
	}


}
