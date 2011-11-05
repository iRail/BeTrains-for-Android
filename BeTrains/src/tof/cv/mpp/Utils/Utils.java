package tof.cv.mpp.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

public class Utils {

	public static InputStream DownloadJsonFromUrlAndCacheToSd(String url,
			String dirName, String fileName, Context context) {

		InputStream source = retrieveStream(url, context);

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

		//TODO: stocker la version pour ne pas faire un appel à chaque fois.
		String myVersion = "0.0";
		PackageManager manager = context.getPackageManager();
		try {
			myVersion = (manager.getPackageInfo(context.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		request.setHeader("User-Agent", "Waza_Be: BeTrains " + myVersion
				+ " for Android");

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