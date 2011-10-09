package tof.cv.misc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocationDbHelper {

	public static final String KEY_STATION_NAME = "stationname";
	public static final String KEY_STATION_LAT = "lat";
	public static final String KEY_STATION_LON = "long";
	public static final String KEY_STATION_DIS = "distance";
	public static final String KEY_STATION_ID = "stationid";
	public static final String KEY_ROWID = "_id";


	private static final String TAG = "BETRAINS";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */

	private static final String DATABASE_NAME = "stationslocation";
	private static final String DATABASE_LOC_TABLE = "locationtable";


	public static final int DATABASE_VERSION = 1;

	private static final String CREATE_DATABASE = "create table " + DATABASE_LOC_TABLE +"("+ KEY_ROWID  + " integer primary key autoincrement, "+ KEY_STATION_NAME+ " text not null, "+ KEY_STATION_ID+ " text not null, "  + KEY_STATION_LAT  + " integer not null, "
	+ KEY_STATION_LON   + " integer not null, " +  KEY_STATION_DIS + " double not null);";
	
	
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);			
		}

		
		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("creating database tables");
			db.execSQL(CREATE_DATABASE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_LOC_TABLE);
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public LocationDbHelper(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the connections database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public LocationDbHelper open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);		
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createStationLocation(String name, String id, int lat, int lon, double dist) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_STATION_NAME, name);
		initialValues.put(KEY_STATION_LAT, lat );
		initialValues.put(KEY_STATION_LON, lon );
		initialValues.put(KEY_STATION_DIS, dist  );
		initialValues.put(KEY_STATION_ID, id  );

		return mDb.insert(DATABASE_LOC_TABLE, null, initialValues);
	}

	public boolean deleteLocation(long rowId) {
		return mDb.delete(DATABASE_LOC_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}


	public boolean deleteAllLocations() {
		return mDb.delete(DATABASE_LOC_TABLE, null, null) > 0;
	}


	public Cursor fetchAllLocations() {	
		return mDb.query(DATABASE_LOC_TABLE, new String[] { KEY_ROWID, KEY_STATION_NAME, KEY_STATION_ID,
				KEY_STATION_LAT,KEY_STATION_LON, KEY_STATION_DIS}, null, null, null, null, null);
	}
	
	public Cursor fetchAllLocationsWithDistance() {	
		return mDb.query(DATABASE_LOC_TABLE, new String[] { KEY_ROWID, KEY_STATION_NAME,KEY_STATION_ID,
				KEY_STATION_LAT,KEY_STATION_LON, KEY_STATION_DIS}, null, null, null, null, KEY_STATION_DIS+" ASC");
	}

	
	public Cursor fetchLocation(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_LOC_TABLE, new String[] {
				KEY_ROWID, KEY_STATION_NAME,KEY_STATION_ID,
				KEY_STATION_LAT,KEY_STATION_LON, KEY_STATION_DIS }, KEY_ROWID + "=" + rowId,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	
	public boolean updateLocation(long rowId,String name,String id, int lat, int lon, double dist) {
		ContentValues args = new ContentValues();
		args.put(KEY_STATION_NAME, name);
		args.put(KEY_STATION_LAT, lat );
		args.put(KEY_STATION_LON, lon );
		args.put(KEY_STATION_DIS, dist  );
		args.put(KEY_STATION_ID, id  );
		return mDb.update(DATABASE_LOC_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	
}
