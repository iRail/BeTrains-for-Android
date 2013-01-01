package tof.cv.mpp.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;

import tof.cv.mpp.bo.Connections;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class Utils {

	final static String FILENAMECONN = "connections.txt";
	final static String DIRPATH = "/Android/data/BeTrains";

	public static String getHourFromDate(long dateFromAPI, boolean isDuration) {
		Date date;
		DateFormat dateFormat = new SimpleDateFormat("HH");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		try {
			if (isDuration) {
				date = new Date((Long.valueOf(dateFromAPI) - 3600) * 1000);
			} else {

				date = new Date((Long.valueOf(dateFromAPI)) * 1000);
				Log.i("", "getHourFromDate: " + date.toString());
			}
			return dateFormat.format(date);
		} catch (Exception e) {
			return "" + dateFromAPI;
		}

	}

	public static String getHourFromDate(String dateFromAPI, boolean isDuration) {

		return getHourFromDate(Long.valueOf(dateFromAPI), isDuration);

	}

	public static String getMinutsFromDate(String dateFromAPI,
			boolean isDuration) {
		Date date;
		DateFormat dateFormat = new SimpleDateFormat("mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		try {
			if (isDuration) {
				date = new Date((Long.valueOf(dateFromAPI) - 3600) * 1000);
			} else {

				date = new Date((Long.valueOf(dateFromAPI)) * 1000);
				Log.i("", "getMinutsFromDate: " + date.toString());
			}
			return dateFormat.format(date);
		} catch (Exception e) {
			return dateFromAPI;
		}

	}

	public static String getTimeFromDate(String dateFromAPI) {
		Date date;
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
		try {

			date = new Date((Long.valueOf(dateFromAPI)) * 1000);
			//Log.i("", "getMinutsFromDate: " + date.toString());
			return dateFormat.format(date);
		} catch (Exception e) {
			return dateFromAPI;
		}

	}

	public static String getTrainId(String train) {

		String[] array = train.split("\\.");

		if (array.length == 0)
			return train;
		else
			return array[array.length - 1];

	}

	public static String formatDate(String dateFromAPI, boolean isDuration,
			boolean isDelay) {

		return formatDate(Long.valueOf(dateFromAPI), isDuration, isDelay);

	}
	
	
	public static String formatDateWidget(Date dateFromAPI) {

		return formatDate(dateFromAPI,"HH:mm:ss");

	}

	public static String formatDate(long dateFromAPI, boolean isDuration,
			boolean isDelay) {
		// TODO: Lot of tweaks, need to be cleaned
		Date date;
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));

		if (dateFromAPI == 0)
			return "";
		try {
			if (isDuration) {

				if (isDelay)
					return "+" + dateFromAPI / 60 + "'";
				else
					date = new Date((dateFromAPI - 3600) * 1000);
			} else {
				date = new Date(dateFromAPI * 1000);
			}
			return dateFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return "" + dateFromAPI;
		}

	}

	public static Connections getCachedConnections() {
		try {
			File memory = Environment.getExternalStorageDirectory();
			File dir = new File(memory.getAbsolutePath() + DIRPATH);
			dir.mkdirs();
			File file = new File(dir, Utils.FILENAMECONN);
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			Gson gson = new Gson();
			final Reader reader = new InputStreamReader(is);
			return gson.fromJson(reader, Connections.class);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InputStream DownloadJsonFromUrlAndCacheToSd(String url,
			String dirName, String fileName, Context context) {

		InputStream source = UtilsWeb.retrieveStream(url, context);

		if (fileName == null)
			return source;

		// Petite entourloupe pour éviter des soucis de InputSTream qui se ferme
		// apres la premiere utilisation.
		Utils test = new Utils();
		CopyInputStream cis = test.new CopyInputStream(source);
		InputStream sourcetoReturn = cis.getCopy();
		InputStream sourceCopy = cis.getCopy();

		File memory = Environment.getExternalStorageDirectory();
		File dir = new File(memory.getAbsolutePath() + dirName);
		dir.mkdirs();
		File file = new File(dir, fileName);

		// Write to SDCard
		try {
			FileOutputStream f = new FileOutputStream(file);
			byte[] buffer = new byte[32768];
			int read;
			try {
				while ((read = sourceCopy.read(buffer, 0, buffer.length)) > 0) {
					f.write(buffer, 0, read);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sourcetoReturn;
	}

	public class CopyInputStream {
		private InputStream _is;
		private ByteArrayOutputStream _copy = new ByteArrayOutputStream();

		/**
    	 * 
    	 */
		public CopyInputStream(InputStream is) {
			_is = is;

			try {
				copy();
			} catch (IOException ex) {
				// do nothing
			}
		}

		private int copy() throws IOException {
			int read = 0;
			int chunk = 0;
			byte[] data = new byte[256];

			while (-1 != (chunk = _is.read(data))) {
				read += data.length;
				_copy.write(data, 0, chunk);
			}

			return read;
		}

		public InputStream getCopy() {
			return (InputStream) new ByteArrayInputStream(_copy.toByteArray());
		}
	}
	
	public void sendErrorMessage(){
		
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	public static String formatDate(Date d, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(d);
	}

	public static ArrayList<String> getFavFromDb(Activity context,
			DbAdapterConnection mDbHelper) {
		ArrayList<String> mArrayList = new ArrayList<String>();
		mDbHelper.open();
		Cursor mCursor = mDbHelper.fetchAllFavStations();
		mCursor.moveToFirst();

		// TODO: Bug: I have to add first item manually.. Why?
		if (!mCursor.isAfterLast())
			mArrayList.add(mCursor.getString(mCursor
			// .getColumnIndex(DbAdapterConnection.KEY_FAV_NAME)));
					.getColumnIndex(DbAdapterConnection.KEY_ROWID)));
		for (mCursor.moveToFirst(); mCursor.moveToNext(); mCursor.isAfterLast()) {
			// The Cursor is now set to the right position
			mArrayList.add(mCursor.getString(mCursor
			// .getColumnIndex(DbAdapterConnection.KEY_FAV_NAME)));
					.getColumnIndex(DbAdapterConnection.KEY_ROWID)));
		}
		return mArrayList;
	}

	public static void addAsStarred(String item, String item2, int type,
			Context context) {
		// TYPE 1 = Station
		// TYPE 2 = Vehicle
		// TYPE 3 = Trip
		DbAdapterConnection mDbHelper = new DbAdapterConnection(context);
		mDbHelper.open();
		mDbHelper.createFav(item, item2, type);
		mDbHelper.close();

	}
}