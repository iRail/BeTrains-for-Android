package tof.cv.mpp.Utils;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */

//NOTE: FAV_TYPE: 1=Station 2=Train 3=Trip 
public class DbAdapterConnection {

	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_FAV_NAME = "favname";
	public static final String KEY_FAV_NAMETWO = "favnametwo";
	public static final String KEY_FAV_TYPE = "favtype";
	public static final String KEY_STOP_NAME = "stopname";
	public static final String KEY_STOP_TIME = "stoptime";
	public static final String KEY_STOP_LATE = "stoplate";
	public static final String KEY_STOP_STATUS = "stopstatus";
	public static final String KEY_INFO_START = "infostart";
	public static final String KEY_INFO_STOP = "infostop";
	public static final String KEY_INFO_TID = "infotid";
	public static final String KEY_INFO_MESSAGE = "infomessage";
	public static final String KEY_ROWID = "_id";

	private static final String TAG = "BETRAINS";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */

	private static final String DATABASE_NAME = "betraindata";
	private static final String DATABASE_FAV_TABLE = "favorits";
	private static final String DATABASE_WIDGET_STOP_TABLE = "widget_stops";
	private static final String DATABASE_INFO_TABLE = "infotrain";
	
	/*
	 * Connection properties
	 */
	public final static String KEY_DEPARTURE = "departurestation";
	public final static String KEY_DEPARTTIME = "departtime";
	public final static String KEY_DEPARTUREDATE = "departdate";
	public final static String KEY_ARRIVAL = "arrivalstations";
	public final static String KEY_ARRIVALTIME = "arrivaltime";
	public final static String KEY_ARRIVALDATE = "arrivaldate";
	public final static String KEY_TRIPTIME = "triptime";
	public final static String KEY_DELAY_DEPARTURE = "delayd";
	public final static String KEY_DELAY_ARRIVAL = "delaya";
	public final static String KEY_TRAINS = "trains";
	public final static String KEY_DEPARTURE_PLATFORM = "departureplatform";
	public final static String KEY_ARRIVAL_PLATFORM = "arrivalplatform";
	public final static String KEY_DEPARTURE_COORD = "departurecoordinates";
	public final static String KEY_ARRIVAL_COORD = "arrivalcoordinates";
	public final static String KEY_ARRIVAL_STATUS = "arrivalstatus";
	public final static String KEY_DEPARTURE_STATUS = "departurestatus";
	public static final int DATABASE_VERSION = 14;

	
	/*
	 * Via properties
	 */
	public final static String KEY_VIA_ARRIVALPLATFORM = "arrivalplatform";
	public final static String KEY_VIA_ARRIVALTIME="arrivaltime";
	public final static String KEY_VIA_DEPARTUREPLATFORM = "departureplatform";
	public final static String KEY_VIA_DEPARTURETIME = "departuretime";
	public final static String KEY_VIA_TIMEBETWEEN = "timebetween";
	public final static String KEY_VIA_COORDINATES = "coordinates";
	public final static String KEY_VIA_STATIONNAME = "stationname";
	public final static String KEY_VIA_VEHICLE = "vehicle";
	public final static String KEY_VIA_DURATION = "duration";
	public final static String KEY_VIA_DELAY = "delay";
	public final static String KEY_VIA_ROWIDOFCONNECTION = "rowidofconnection";
	

	private static final String CREATE_FAV_DATABASE = "create table "
			+ DATABASE_FAV_TABLE + " (_id integer primary key autoincrement, "
			+ KEY_FAV_NAME + " text not null, " +KEY_FAV_NAMETWO + " text not null, "+ KEY_FAV_TYPE + " integer not null);";
	
	private static final String CREATE_WIDGET_STOP_DATABASE = "create table "
		+ DATABASE_WIDGET_STOP_TABLE
		+ " (_id integer primary key autoincrement, " + KEY_STOP_NAME
		+ " text not null, " + KEY_STOP_TIME + " text not null, "
		+ KEY_STOP_LATE + " text not null, " + KEY_STOP_STATUS
		+ " text not null);";

	private static final String CREATE_INFO_DATABASE = "create table "
			+ DATABASE_INFO_TABLE + " (_id integer primary key autoincrement, "
			+ KEY_INFO_START + " text not null, " + KEY_INFO_STOP
			+ " text not null, " + KEY_INFO_TID + " text not null, "
			+ KEY_INFO_MESSAGE + " text not null);";

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);			
		}

		
		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("CREATING DATABASE TABLES");
			db.execSQL(CREATE_FAV_DATABASE);
			db.execSQL(CREATE_WIDGET_STOP_DATABASE);
			db.execSQL(CREATE_INFO_DATABASE);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_FAV_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_WIDGET_STOP_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_INFO_TABLE);
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
	public DbAdapterConnection(Context ctx) {
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
	public DbAdapterConnection open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);		
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new connection using the connection properties provided. If the connection is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param title
	 *            the title of the note
	 * @param body
	 *            the body of the note
	 * @return rowId or -1 if failed
	 */


	public long createFav(String name, String nameTwo, int type) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_FAV_NAME, name);
		initialValues.put(KEY_FAV_NAMETWO, nameTwo);
		initialValues.put(KEY_FAV_TYPE, type);
		System.out.println("added fav " + name);
		return mDb.insert(DATABASE_FAV_TABLE, null, initialValues);

	}

	public long createWidgetStop(String name, String time, String delay, String status) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_STOP_NAME, name);
		initialValues.put(KEY_STOP_TIME, time);
		initialValues.put(KEY_STOP_LATE, delay);
		initialValues.put(KEY_STOP_STATUS, status);
		return mDb.insert(DATABASE_WIDGET_STOP_TABLE, null, initialValues);
	}
	

	/**
	 * Delete the connection with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */


	public boolean deleteFav(long rowId) {
		return mDb.delete(DATABASE_FAV_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public boolean deleteWidgetStop(long rowId) {
		return mDb.delete(DATABASE_WIDGET_STOP_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteInfo(long rowId) {
		return mDb.delete(DATABASE_INFO_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean deleteAllFav() {
		return mDb.delete(DATABASE_FAV_TABLE, null, null) > 0;
	}
	
	public boolean deleteAllWidgetStops() {
		Log.d(TAG,"deleteAllWidgetStops");
		return mDb.delete(DATABASE_WIDGET_STOP_TABLE, null, null) > 0;
	}

	public boolean deleteAllInfo() {
		return mDb.delete(DATABASE_INFO_TABLE, null, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */

	public Cursor fetchAllFav() {

		return mDb.query(DATABASE_FAV_TABLE, new String[] { KEY_ROWID,
				KEY_FAV_NAME, KEY_FAV_NAMETWO,KEY_FAV_TYPE }, null, null, null, null, null);
	}
	
	public Cursor fetchAllFavStations() {

		return mDb.query(DATABASE_FAV_TABLE, new String[] { KEY_ROWID,
				KEY_FAV_NAME, KEY_FAV_NAMETWO,KEY_FAV_TYPE }, KEY_FAV_TYPE + "=1", null, null, null, null);
	}
	
	public Cursor fetchAllWidgetStops() {
		
		Log.d(TAG,"fetchAllWidgetStops");

		return mDb.query(DATABASE_WIDGET_STOP_TABLE, new String[] { KEY_ROWID,
				KEY_STOP_NAME, KEY_STOP_TIME, KEY_STOP_LATE,
				KEY_STOP_STATUS }, null, null, null, null, null);
	}

	public Cursor fetchAllInfo() {

		return mDb.query(DATABASE_INFO_TABLE,
				new String[] { KEY_ROWID, KEY_INFO_START, KEY_INFO_STOP,
						KEY_INFO_TID, KEY_INFO_MESSAGE }, null, null, null,
				null, null);
	}

	/**
	 * Return a Cursor positioned at the connection that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */

	public Cursor fetchFav(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_FAV_TABLE, new String[] {
				KEY_ROWID, KEY_FAV_NAME, KEY_FAV_NAMETWO,KEY_FAV_TYPE }, KEY_ROWID + "=" + rowId, null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	
	public Cursor fetchWidgetStop(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_WIDGET_STOP_TABLE, new String[] {
				KEY_ROWID, KEY_STOP_NAME, KEY_STOP_TIME, KEY_STOP_LATE,
				KEY_STOP_STATUS }, KEY_ROWID + "=" + rowId, null, null, null,
				null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchInfo(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_INFO_TABLE, new String[] {
				KEY_ROWID, KEY_INFO_START, KEY_INFO_STOP, KEY_INFO_TID,
				KEY_INFO_MESSAGE }, KEY_ROWID + "=" + rowId, null, null, null,
				null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Update the connection using the details provided. The connection to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param rowId
	 *            id of note to update
	 * @param title
	 *            value to set note title to
	 * @param body
	 *            value to set note body to
	 * @return true if the note was successfully updated, false otherwise
	 */

	public boolean updateFav(long rowId, String name, String nameTwo, int type) {
		ContentValues args = new ContentValues();
		args.put(KEY_FAV_NAME, name);
		args.put(KEY_FAV_NAMETWO, nameTwo);
		args.put(KEY_FAV_TYPE, type);
		return mDb.update(DATABASE_FAV_TABLE, args, KEY_ROWID + "=" + rowId,
				null) > 0;
	}

	
	public boolean updateWidgetStop(long rowId, String name, String time,
			String late, String status) {
		ContentValues args = new ContentValues();
		args.put(KEY_STOP_NAME, name);
		args.put(KEY_STOP_TIME, time);
		args.put(KEY_STOP_LATE, late);
		args.put(KEY_STOP_STATUS, status);
		return mDb.update(DATABASE_WIDGET_STOP_TABLE, args, KEY_ROWID + "=" + rowId,
				null) > 0;
	}

	public boolean updateInfo(long rowId, String start, String stop,
			String tid, String message) {
		ContentValues args = new ContentValues();
		args.put(KEY_INFO_START, start);
		args.put(KEY_INFO_STOP, stop);
		args.put(KEY_INFO_TID, tid);
		args.put(KEY_INFO_MESSAGE, message);
		return mDb.update(DATABASE_INFO_TABLE, args, KEY_ROWID + "=" + rowId,
				null) > 0;
	}

}
