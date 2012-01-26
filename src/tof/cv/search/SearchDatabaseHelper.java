package tof.cv.search;

import java.io.IOException;

import tof.cv.mpp.Utils.ConnectionMaker;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SearchDatabaseHelper extends SQLiteOpenHelper {

	private final String TAG = "SearchDatabase";
	private final static String DATABASE_NAME = "trains";
	private final static int DATABASE_VERSION = 1;
	//private Context helperContext;
	private SQLiteDatabase mDatabase;
	public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2;
	private static final String FTS_VIRTUAL_TABLE = "FTSdictionary";
	private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
		+ FTS_VIRTUAL_TABLE + " USING fts3 (" + KEY_WORD + ", "
		+ KEY_DEFINITION + ");";
	/*
	 * Note that FTS3 does not support column constraints and thus, you cannot
	 * declare a primary key. However, "rowid" is automatically used as a unique
	 * identifier, so when making requests, we will use "_id" as an alias for
	 * "rowid"
	 */
	

	SearchDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		//helperContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		mDatabase = db;
		mDatabase.execSQL(FTS_TABLE_CREATE);
		loadDictionary();
	}

	/**
	 * Starts a thread to load the database table with words
	 */
	private void loadDictionary() {
		new Thread(new Runnable() {
			public void run() {
				try {
					loadWords();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	private void loadWords() throws IOException {
		Log.d(TAG, "Loading words...");
		for (String station : ConnectionMaker.LIST_OF_STATIONS) {
			addWord(station, "Station");
		}

		Log.d(TAG, "DONE loading words.");
	}

	/**
	 * Add a word to the dictionary.
	 * 
	 * @return rowId or -1 if failed
	 */
	public long addWord(String word, String definition) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_WORD, word);
		initialValues.put(KEY_DEFINITION, definition);

		return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
		onCreate(db);
	}

}
