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

import  android.appwidget.AppWidgetManager;
import  android.appwidget.AppWidgetProvider;
import  android.content.ContentProvider;
import  android.content.ContentValues;
import  android.content.Context;
import  android.content.Intent;
import  android.content.res.Resources;
import  android.database.Cursor;
import  android.database.MatrixCursor;
import  android.net.Uri;
import android.util.Log;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.Utils.UtilsWeb.VehicleStop;

import  java.util.ArrayList;

import java.util.ArrayList;

public class TrainDataProvider extends  ContentProvider {
    public  static  final  Uri CONTENT_URI =
            Uri.parse("content://tof.cv.mpp.widget.provider");


    /**
     * Generally, this data will be stored in an external and persistent location (ie. File,
     * Database, SharedPreferences) so that the data can persist if the process is ever killed.
     * For simplicity, in this sample the data will only be stored in memory.
     */
    public static  final  ArrayList<VehicleStop> sData = new  ArrayList<VehicleStop>();

    @Override
    public  boolean  onCreate() {
        // We are going to initialize the data provider with some default values


        //sData.add(new VehicleStop());


        return  true;
    }

    @Override
    public  synchronized  Cursor query(Uri uri, String[] projection, String selection,
                                       String[] selectionArgs, String sortOrder) {
        assert(uri.getPathSegments().isEmpty());

        // In this sample, we only query without any parameters, so we can just return a cursor to
        // all the weather data.
        final  MatrixCursor c = new  MatrixCursor(
                new  String[]{ DbAdapterConnection.KEY_STOP_STATUS, DbAdapterConnection.KEY_STOP_NAME, DbAdapterConnection.KEY_STOP_TIME });
        for  (int  i = 0; i < sData.size(); ++i) {
            final VehicleStop data = sData.get(i);
            //c.addRow(new  Object[]{ new  Integer(i), data.city, new  Integer(data.degrees) });
        }
        return  c;
    }

    @Override
    public  String getType(Uri uri) {
        return  "vnd.android.cursor.dir/vnd.betrains.trainwidget";
    }

    @Override
    public  Uri insert(Uri uri, ContentValues values) {
        // This example code does not support inserting
        return  null;
    }

    @Override
    public  int  delete(Uri uri, String selection, String[] selectionArgs) {
        // This example code does not support deleting
        return  0;
    }

    @Override
    public  synchronized  int  update(Uri uri, ContentValues values, String selection,
                                      String[] selectionArgs) {
        assert(uri.getPathSegments().size() == 1);
        Log.i("","***update");
        // In this sample, we only update the content provider individually for each row with new
        // temperature values.
        final  int  index = Integer.parseInt(uri.getPathSegments().get(0));
        final  MatrixCursor c = new  MatrixCursor(
                new  String[]{ DbAdapterConnection.KEY_STOP_STATUS, DbAdapterConnection.KEY_STOP_NAME, DbAdapterConnection.KEY_STOP_TIME });
        assert(0 <= index && index < sData.size());
        final VehicleStop data = sData.get(index);
        //data.getVehicleStop().get(0) = values.getAsInteger(Columns.TEMPERATURE);

        // Notify any listeners that the data backing the content provider has changed, and return
        // the number of rows affected.
        getContext().getContentResolver().notifyChange(uri, null);
        return  1;
    }

}