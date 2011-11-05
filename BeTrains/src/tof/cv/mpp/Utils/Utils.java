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

import android.os.Environment;
import android.util.Log;

public class Utils {

	public static InputStream DownloadJsonFromUrlAndCacheToSd(String url,
			String dirName,String fileName) {

		InputStream source = retrieveStream(url);

		// Petite entourloupe pour éviter des soucis de InputSTream qui se ferme
		// apres la premiere utilisation.
		Utils test=new Utils();
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
	
	public static  InputStream retrieveStream(String url) {

		DefaultHttpClient client = new DefaultHttpClient();

		HttpGet getRequest = new HttpGet(url);

		try {

			HttpResponse getResponse = client.execute(getRequest);
			final int statusCode = getResponse.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("getClass().getSimpleName()", "Error " + statusCode
						+ " for URL " + url);
				return null;
			}

			HttpEntity getResponseEntity = getResponse.getEntity();
			Log.w("getClass().getSimpleName()", "No error for URL " + url);
			return getResponseEntity.getContent();

		} catch (IOException e) {
			getRequest.abort();
			Log.w("getClass().getSimpleName()"," Error for URL " + url, e);
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