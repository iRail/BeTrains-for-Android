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

package tof.cv.mpp.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Date;
import java.util.Random;

import tof.cv.mpp.InfoTrainActivity;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.WelcomeActivity;
import tof.cv.mpp.widget.TrainAppWidgetProvider;
import tof.cv.mpp.widget.TrainDataProvider;
import tof.cv.mpp.widget.TrainWidgetService;

/**
 * Our data observer just notifies an update for all weather widgets when it
 * detects a change.
 */
class CopyOfTrainDataProviderObserver extends ContentObserver {
	private AppWidgetManager mAppWidgetManager;
	private ComponentName mComponentName;

	CopyOfTrainDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
		super(h);
		mAppWidgetManager = mgr;
		mComponentName = cn;
	}

	@Override
	public void onChange(boolean selfChange) {
		// The data has changed, so notify the widget that the collection view
		// needs to be updated.
		// In response, the factory's onDataSetChanged() will be called which
		// will requery the
		// cursor for the new data.
		mAppWidgetManager.notifyAppWidgetViewDataChanged(
				mAppWidgetManager.getAppWidgetIds(mComponentName),
				R.id.weather_list);
	}
}

/**
 * The weather widget's AppWidgetProvider.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CopyOfTrainWidgetProvider extends AppWidgetProvider {

	public static String REFRESH_ACTION = "tof.cv.mpp.widget.action.REFRESH";
	public static String UPDATE_ACTION = "tof.cv.mpp.widget.action.UPDATE";

	private static Handler sWorkerQueue;
	private static TrainDataProviderObserver sDataObserver;

	String tid;
	String fromTo;
	String updateTime = "xx:xx";

	public CopyOfTrainWidgetProvider() {
		// Start the worker thread
		HandlerThread sWorkerThread = new HandlerThread(
				"TrainWidgetProvider-worker");
		sWorkerThread.start();
		sWorkerQueue = new Handler(sWorkerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				// display each item in a single line
				// txt.setText(txt.getText()+"Item "+System.getProperty("line.separator"));
			}
		};

	}

	@Override
	public void onEnabled(Context context) {
		// Register for external updates to the data to trigger an update of the
		// widget. When using
		// content providers, the data is often updated via a background
		// service, or in response to
		// user interaction in the main app. To ensure that the widget always
		// reflects the current
		// state of the data, we must listen for changes and update ourselves
		// accordingly.
		final ContentResolver r = context.getContentResolver();
		if (sDataObserver == null) {
			final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
			final ComponentName cn = new ComponentName(context,
					CopyOfTrainWidgetProvider.class);
			sDataObserver = new TrainDataProviderObserver(mgr, cn, sWorkerQueue);
			r.registerContentObserver(TrainDataProvider.CONTENT_URI, true,
					sDataObserver);
		}
	}

	@Override
	public void onReceive(final Context ctx, Intent intent) {
		final String action = intent.getAction();

		super.onReceive(ctx, intent);
		Log.i("", "***INTENT+" + intent.getAction());
		if (action.equals(REFRESH_ACTION) || action.equals(UPDATE_ACTION)) {
			// BroadcastReceivers have a limited amount of time to do work, so
			// for this sample, we
			// are triggering an update of the data on another thread. In
			// practice, this update
			// can be triggered from a background service, or perhaps as a
			// result of user actions
			// inside the main application.

			sWorkerQueue.removeMessages(0);
			sWorkerQueue.post(new Runnable() {
				@Override
				public void run() {

					DbAdapterConnection mDbHelper = new DbAdapterConnection(ctx);
					mDbHelper.open();
					Cursor mSTOPCursor = mDbHelper.fetchAllWidgetStops();

					if (mSTOPCursor.getCount() > 1) {
						mSTOPCursor.moveToPosition(0);

						tid = mSTOPCursor.getString(mSTOPCursor
								.getColumnIndex(DbAdapterConnection.KEY_STOP_NAME));
						fromTo = mSTOPCursor.getString(mSTOPCursor
								.getColumnIndex(DbAdapterConnection.KEY_STOP_STATUS));
						updateTime = mSTOPCursor.getString(mSTOPCursor
								.getColumnIndex(DbAdapterConnection.KEY_STOP_LATE));

						if (action.equals(REFRESH_ACTION)) {
/*
							UtilsWeb.Vehicle v =  UtilsWeb.getAPIvehicle(tid,ctx, 0);

							if (v.getVehicleStops().getVehicleStop().size() > 1) {
								mDbHelper.deleteAllWidgetStops();
								updateTime = Utils.formatDateWidget(new Date());
								mDbHelper.createWidgetStop(tid, "1",
										updateTime, fromTo);
								for (UtilsWeb.VehicleStop stop : v
										.getVehicleStops().getVehicleStop()) {
									mDbHelper.createWidgetStop(
											stop.getStation(),
											"" + stop.getTime(),
											stop.getDelay(), stop.getStatus());
								}
							}*/
						}

					}
					mDbHelper.close();

					final ContentResolver r = ctx.getContentResolver();
					final Cursor c = r.query(TrainDataProvider.CONTENT_URI,
							null, null, null, null);
					final int count = c.getCount();

					// We disable the data changed observer temporarily since
					// each of the updates
					// will trigger an onChange() in our data observer.
					if (sDataObserver != null)
						r.unregisterContentObserver(sDataObserver);
					for (int i = 0; i < count; ++i) {
						final Uri uri = ContentUris.withAppendedId(
								TrainDataProvider.CONTENT_URI, i);
						final ContentValues values = new ContentValues();
						values.put(DbAdapterConnection.KEY_STOP_TIME,
								new Random().nextInt(100));
						r.update(uri, values, null, null);
					}
					if (sDataObserver != null)
						r.registerContentObserver(
								TrainDataProvider.CONTENT_URI, true,
								sDataObserver);

					Message msg = new Message();
					Bundle b = new Bundle();
					b.putString("My Key", "My Value: ");
					msg.setData(b);

					sWorkerQueue.sendMessage(msg);
				}
			});
		}
		final AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
		final ComponentName cn = new ComponentName(ctx,
				CopyOfTrainWidgetProvider.class);

		Intent detailIntent = new Intent(ctx, WelcomeActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0,
				detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		final RemoteViews rv = new RemoteViews(ctx.getPackageName(),
				R.layout.widget_layout);
		rv.setOnClickPendingIntent(R.id.icon, pendingIntent);

		mgr.updateAppWidget(mgr.getAppWidgetIds(cn), rv);

		mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),
				R.id.weather_list);

		// Bind the click intent for the refresh button on the widget
		final Intent refreshIntent = new Intent(ctx, CopyOfTrainWidgetProvider.class);
		refreshIntent.setAction(CopyOfTrainWidgetProvider.REFRESH_ACTION);
		final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
				ctx, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);
		
		Log.i("", "HOPE");
		int[] ids = mgr.getAppWidgetIds(
						new ComponentName(ctx,
								TrainAppWidgetProvider.class));
		onUpdate(ctx, AppWidgetManager.getInstance(ctx), ids);
		// else if (action.equals(CLICK_ACTION)) {
		// Show a toast
		// final int appWidgetId =
		// intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
		// AppWidgetManager.INVALID_APPWIDGET_ID);
		// final String city = intent.getStringExtra(EXTRA_CITY_ID);
		// Toast.makeText(ctx, city, Toast.LENGTH_SHORT).show();
		// }

	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.i("", "UPDATE");
		DbAdapterConnection mDbHelper = new DbAdapterConnection(context);
		mDbHelper.open();
		Cursor mSTOPCursor = mDbHelper.fetchAllWidgetStops();

		if (mSTOPCursor.getCount() > 1) {
			mSTOPCursor.moveToPosition(0);

			tid = mSTOPCursor.getString(mSTOPCursor
					.getColumnIndex(DbAdapterConnection.KEY_STOP_NAME));
			fromTo = mSTOPCursor.getString(mSTOPCursor
					.getColumnIndex(DbAdapterConnection.KEY_STOP_STATUS));
			updateTime = mSTOPCursor.getString(mSTOPCursor
					.getColumnIndex(DbAdapterConnection.KEY_STOP_LATE));

		}
		mDbHelper.close();

		// Update each of the widgets with the remote adapter
		for (int appWidgetId : appWidgetIds) {
			// for (int i = 0; i < appWidgetIds.length; ++i)
			// Specify the service to provide data for the collection widget.
			// Note that we need to
			// embed the appWidgetId via the data otherwise it will be ignored.
			final Intent intent = new Intent(context, TrainWidgetService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			final RemoteViews rv = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			rv.setRemoteAdapter(appWidgetId, R.id.weather_list, intent);

			if (tid != null) {
				Log.i("", "tid2: " + tid + " (" + updateTime + ")");
				rv.setTextViewText(R.id.widget_title, fromTo);
				rv.setTextViewText(R.id.widget_sub, tid + " (" + updateTime
						+ ")");
			}

			// Set the empty view to be displayed if the collection is empty. It
			// must be a sibling
			// view of the collection view.
			rv.setEmptyView(R.id.weather_list, R.id.empty_view);

			// Bind the click intent for the refresh button on the widget
			final Intent refreshIntent = new Intent(context,
					CopyOfTrainWidgetProvider.class);
			refreshIntent.setAction(CopyOfTrainWidgetProvider.REFRESH_ACTION);
			final PendingIntent refreshPendingIntent = PendingIntent
					.getBroadcast(context, 0, refreshIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);

			// Bind the click intent for the icon button on the widget
			Intent detailIntent = new Intent(context, WelcomeActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			if (tid != null) {

				detailIntent = new Intent(context, InfoTrainActivity.class);
				detailIntent.putExtra("fromto", fromTo);
				detailIntent.putExtra("Name", tid);
				pendingIntent = PendingIntent.getActivity(context, 0,
						detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			}
			rv.setOnClickPendingIntent(R.id.icon, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, rv);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}