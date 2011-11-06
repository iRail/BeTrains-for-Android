package tof.cv.mpp.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import tof.cv.mpp.R;
import tof.cv.mpp.adapter.TweetItemAdapter;
import tof.cv.mpp.bo.Tweets;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

public class Utils {

	public static void loadTweets(final Activity a, final ListView l) {
		new Thread(new Runnable() {
			public void run() {
				try {
					String url = "http://search.twitter.com/search.json?q=BETRAINS";
					SharedPreferences mDefaultPrefs = PreferenceManager
							.getDefaultSharedPreferences(a);
					;
					if (mDefaultPrefs.getBoolean("mNMBS", a.getResources()
							.getBoolean(R.bool.nmbs)))
						url += "%20OR%20NMBS";

					if (mDefaultPrefs.getBoolean("mSNCB", a.getResources()
							.getBoolean(R.bool.sncb)))
						url += "%20OR%20SNCB";

					if (mDefaultPrefs.getBoolean("miRail", true))
						url += "%20OR%20irail";

					if (mDefaultPrefs.getBoolean("mNavetteurs", a
							.getResources().getBoolean(R.bool.navetteurs)))
						url += "%20OR%20navetteurs";
					
					InputStream is=Utils.DownloadJsonFromUrlAndCacheToSd(url,"/Android/data/BeTrains",null,a);
					Gson gson = new Gson();
					final Reader reader = new InputStreamReader(is);
					final Tweets tweets= gson.fromJson(reader,Tweets.class);
					
					a.runOnUiThread(new Thread(new Runnable() {
						public void run() {

							l.setAdapter(new TweetItemAdapter(a,
									R.layout.row_tweet, tweets.results));
						}
					}));
				} catch (Exception e) {
					e.printStackTrace();
					a.runOnUiThread(new Thread(new Runnable() {
						public void run() {
							TextView tv = (TextView) a.findViewById(R.id.fail);
							tv.setVisibility(View.VISIBLE);

						}
					}));

				}

			}
		}).start();

	}

	
	public static InputStream DownloadJsonFromUrlAndCacheToSd(String url,
			String dirName, String fileName, Context context) {

		InputStream source = retrieveStream(url, context);

		if (fileName==null)
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

	public static InputStream retrieveStream(String url, Context context) {

		DefaultHttpClient client = new DefaultHttpClient();

		HttpGet request = new HttpGet(url);

		// TODO: stocker la version pour ne pas faire un appel à chaque fois.
		String myVersion = "0.0";
		PackageManager manager = context.getPackageManager();
		try {
			myVersion = (manager.getPackageInfo(context.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		request.setHeader("User-Agent", "Waza_Be: BeTrains " + myVersion
				+ " for Android");

		Log.w("getClass().getSimpleName()", "URL TO CHECK " + url);
		
		try {
			HttpResponse response = client.execute(request);
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("getClass().getSimpleName()", "Error " + statusCode
						+ " for URL " + url);
				return null;
			}

			HttpEntity getResponseEntity = response.getEntity();
			Log.w("getClass().getSimpleName()", "Read the url:  " + url);
			return getResponseEntity.getContent();

		} catch (IOException e) {
			Log.w("getClass().getSimpleName()", " Error for URL " + url, e);
		}

		return null;

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
}