package tof.cv.mpp.widget;

import  android.appwidget.AppWidgetManager;
import  android.content.Context;
import  android.content.Intent;
import  android.database.Cursor;
import android.util.Log;
import  android.widget.RemoteViews;
import  android.widget.RemoteViewsService;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
public  class  TrainWidgetService extends  RemoteViewsService {

    @Override
    public  RemoteViewsFactory onGetViewFactory(Intent intent) {
        return  new  StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

/**
 * This is the factory that will provide data to the collection widget.
 */
class  StackRemoteViewsFactory implements  RemoteViewsService.RemoteViewsFactory {
    private  Context mContext;
    private  Cursor mCursor;
    private  int  mAppWidgetId;
    private DbAdapterConnection mDbHelper;

    public  StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public  void  onCreate() {
        mDbHelper = new DbAdapterConnection(this.mContext);
    }

    public  void  onDestroy() {
        if  (mCursor != null) {
            mCursor.close();
        }
    }

    public  int  getCount() {
        return  mCursor.getCount()-1;
    }

    public  RemoteViews getViewAt(int  position) {

        Log.i("","getViewAt "+position);

        String station ="" ,delay=  "", time ="";

        if  (mCursor.moveToPosition(position+1)) {
            time = Utils
                    .getTimeFromDate(mCursor.getString(mCursor
                            .getColumnIndex(DbAdapterConnection.KEY_STOP_TIME)));
            Log.i("","time "+time);
            delay = mCursor.getString(mCursor
                    .getColumnIndex(DbAdapterConnection.KEY_STOP_STATUS));
            station = mCursor.getString(mCursor
                    .getColumnIndex(DbAdapterConnection.KEY_STOP_NAME));
        }

        // Return a proper item with the proper city and temperature.

        final  int  itemId = R.layout.row_info_train;
        RemoteViews rv = new  RemoteViews(mContext.getPackageName(), itemId);

        //rv.setTextViewText(R.id.time, Utils.formatDate(new Date(time*1000),"HH:mm"));
        rv.setTextViewText(R.id.time, time);
        rv.setTextViewText(R.id.station, station);
        rv.setTextViewText(R.id.delay, delay);

        return  rv;
    }
    public  RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return  null;
    }

    public  int  getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
        return  2;
    }

    public  long  getItemId(int  position) {
        return  position;
    }

    public  boolean  hasStableIds() {
        return  true;
    }

    public  void  onDataSetChanged() {
        // Refresh the cursor
        if  (mCursor != null) {
            mCursor.close();
        }
        Log.i("","***onDataSetChanged");
        mDbHelper.open();
        mCursor = mDbHelper.fetchAllWidgetStops();
    }
}