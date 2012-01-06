/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Modifications:
 * -Connect to VLC server instead of media service
 * -Listen for VLC status events
 * -Schedule status updates for time at which current track is expected to end
 */

package tof.cv.widget;

import java.util.ArrayList;

import tof.cv.mpp.PlannerActivity;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.Utils.UtilsWeb.VehicleStops;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class TrainAppWidgetProvider extends AppWidgetProvider {

	public static final String TRAIN_WIDGET_UPDATE = "BETRAIN_WIDGET_UPDATE";
	private ArrayList<VehicleStops> listOfTrainStops = new ArrayList<VehicleStops>();
	private DbAdapterConnection mDbHelper;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// To prevent any ANR timeouts, we perform the update in a service
		final int N = appWidgetIds.length;
		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = null;
			views = new RemoteViews(context.getPackageName(), R.layout.widget);

			// When user click on the train
			Intent intent = new Intent(context, PlannerActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);
			views.setOnClickPendingIntent(R.id.lancemoi, pendingIntent);

			// When user click on the center of the widget to update
			intent = new Intent(context, TrainAppWidgetProvider.class);
			intent.setAction("Update");
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.montrain, pendingIntent);

			// When user click on the next arrow
			intent = new Intent(context, TrainAppWidgetProvider.class);
			intent.setAction("Next");
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.next_train, pendingIntent);

			// When user click on the previous arrow
			intent = new Intent(context, TrainAppWidgetProvider.class);
			intent.setAction("Previous");
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.previous_train, pendingIntent);

			update(context, views);
			// Tell the AppWidgetManager to perform an update on the current App
			// Widget
			appWidgetManager.updateAppWidget(appWidgetId, views);

		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		final String action = intent.getAction();

		TrainService service = TrainService.getInstance(context);
		listOfTrainStops = service.getAllStops();
		int currentPos = service.getCurrentPos();

		if (action.equals(TRAIN_WIDGET_UPDATE)) {
			int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
					new ComponentName(context, TrainAppWidgetProvider.class));
			onUpdate(context, AppWidgetManager.getInstance(context), ids);
		}
		if (action.equals("android.appwidget.action.APPWIDGET_UPDATE")) {
			int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
					new ComponentName(context, TrainAppWidgetProvider.class));
			onUpdate(context, AppWidgetManager.getInstance(context), ids);
		}

		if (action.contentEquals("Update")) {
			try {

				mDbHelper = new DbAdapterConnection(context);
				mDbHelper.open();
				Cursor mSTOPCursor = mDbHelper.fetchAllWidgetStops();

				if (mSTOPCursor.getCount() > 1)
					//TODO: fix it!
					UtilsWeb.getAPIvehicle(" vehicle" , context);
				else
					Toast.makeText(context, R.string.wid_empty,
							Toast.LENGTH_LONG).show();
				int[] ids = AppWidgetManager.getInstance(context)
						.getAppWidgetIds(
								new ComponentName(context,
										TrainAppWidgetProvider.class));

				onUpdate(context, AppWidgetManager.getInstance(context), ids);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				mDbHelper.close();
			}

		}

		if (action.contentEquals("Previous")) {
			// System.out.println("P"+currentPos);

			mDbHelper = new DbAdapterConnection(context);
			try {
				mDbHelper.open();

				Cursor mSTOPCursor = mDbHelper.fetchAllWidgetStops();
				mSTOPCursor.moveToPosition(0);
				int id = mSTOPCursor.getInt(mSTOPCursor.getColumnIndex("_id"));

				String tid = mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_NAME));
				int pos = Integer.valueOf(mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_TIME)));
				String fromto = mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_STATUS));

				if (pos > 1)
					pos--;
				mDbHelper.updateWidgetStop(id, tid, "" + pos, "", fromto);

				int[] ids = AppWidgetManager.getInstance(context)
						.getAppWidgetIds(
								new ComponentName(context,
										TrainAppWidgetProvider.class));
				onUpdate(context, AppWidgetManager.getInstance(context), ids);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				mDbHelper.close();
			}
		}

		if (action.contentEquals("Next")) {

			mDbHelper = new DbAdapterConnection(context);
			try {
				mDbHelper.open();

				Cursor mSTOPCursor = mDbHelper.fetchAllWidgetStops();

				int total = mSTOPCursor.getCount();

				mSTOPCursor.moveToPosition(0);

				int id = mSTOPCursor.getInt(mSTOPCursor.getColumnIndex("_id"));

				String tid = mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_NAME));
				int pos = Integer.valueOf(mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_TIME)));
				String fromto = mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_STATUS));

				;
				if (pos < total - 1)
					pos++;
				mDbHelper.updateWidgetStop(id, tid, "" + pos, "", fromto);

				if (currentPos > 0)
					service.setCurrentPos(currentPos - 1);
				int[] ids = AppWidgetManager.getInstance(context)
						.getAppWidgetIds(
								new ComponentName(context,
										TrainAppWidgetProvider.class));
				onUpdate(context, AppWidgetManager.getInstance(context), ids);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				mDbHelper.close();
			}
		}

		this.getClass().getName();

	}

	public void update(Context context, RemoteViews updateViews) {

		mDbHelper = new DbAdapterConnection(context);

		try {
			mDbHelper.open();

			Cursor mSTOPCursor = mDbHelper.fetchAllWidgetStops();
			int size = mSTOPCursor.getCount();

			Log.d("BETRAINS", "** " + size);

			if (size > 1) {
				mSTOPCursor.moveToPosition(0);
				String tid = mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_NAME));
				int pos = Integer.valueOf(mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_TIME)));
				String fromto = mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_STATUS));

				updateViews.setViewVisibility(R.id.next_train, View.VISIBLE);
				updateViews
						.setViewVisibility(R.id.previous_train, View.VISIBLE);

				updateViews.setTextViewText(R.id.tid, tid.replace(" ", ""));

				updateViews.setTextViewText(R.id.text1, fromto);

				mSTOPCursor.moveToPosition(pos);

				String time = Utils
						.getTimeFromDate(mSTOPCursor.getString(mSTOPCursor
								.getColumnIndex(DbAdapterConnection.KEY_STOP_TIME)));
				String late = mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_STATUS));
				String station = mSTOPCursor.getString(mSTOPCursor
						.getColumnIndex(DbAdapterConnection.KEY_STOP_NAME));
				updateViews.setTextViewText(R.id.text2, Html.fromHtml(station));
				updateViews.setTextViewText(R.id.text3, time);

				updateViews.setTextViewText(R.id.text4, late);

			} else {

				updateViews.setViewVisibility(R.id.next_train, View.GONE);
				updateViews.setViewVisibility(R.id.previous_train, View.GONE);

				updateViews.setTextViewText(R.id.tid, "BETrains");

				updateViews.setTextViewText(R.id.text1, "Widget");

				updateViews.setTextViewText(R.id.text2, "Add your train.");

				updateViews.setTextViewText(R.id.text3, "");

				updateViews.setTextViewText(R.id.text4, "");

			}
			ComponentName thisWidget = new ComponentName(context,
					TrainAppWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			manager.updateAppWidget(thisWidget, updateViews);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			mDbHelper.close();
		}
	}

	public void setListOfTrainStops(ArrayList<VehicleStops> listOfTrainStops) {
		this.listOfTrainStops = listOfTrainStops;
	}

	public ArrayList<VehicleStops> getListOfTrainStops() {
		return listOfTrainStops;
	}

}