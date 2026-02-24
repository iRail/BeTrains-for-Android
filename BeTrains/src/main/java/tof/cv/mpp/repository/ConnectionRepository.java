package tof.cv.mpp.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import tof.cv.mpp.BuildConfig;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.bo.TrainComposition;

public class ConnectionRepository {

    private static final String USER_AGENT = "WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android";
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ConnectionRepository(Context context) {
        this.context = context;
    }

    public void searchConnections(String url, final OnConnectionsLoadedCallback callback) {
        clearCompositionCache();
        new Thread(() -> {
            try {
                String json = fetchUrl(url);
                Connections result = new Gson().fromJson(json, Connections.class);
                mainHandler.post(() -> callback.onLoaded(null, result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onLoaded(e, null));
            }
        }).start();
    }

    public void getTrainComposition(String vehicleId, final OnCompositionLoadedCallback callback) {
        TrainComposition.Composition.Segments.Segment.SegmentComposition cached = getCompositionFromCache(vehicleId);
        if (cached != null) {
            callback.onLoaded(null, cached);
            return;
        }

        String url = "https://api.irail.be/v1/composition/?id=" + vehicleId + "&format=json";
        new Thread(() -> {
            try {
                String json = fetchUrl(url);
                TrainComposition result = new Gson().fromJson(json, TrainComposition.class);

                if (result != null && result.composition != null &&
                        result.composition.segments != null &&
                        result.composition.segments.segment != null &&
                        !result.composition.segments.segment.isEmpty() &&
                        result.composition.segments.segment.get(0).composition != null) {

                    TrainComposition.Composition.Segments.Segment.SegmentComposition comp = result.composition.segments.segment
                            .get(0).composition;
                    cacheComposition(vehicleId, comp);
                    mainHandler.post(() -> callback.onLoaded(null, comp));
                } else {
                    mainHandler.post(() -> callback.onLoaded(null, null));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onLoaded(e, null));
            }
        }).start();
    }

    private String fetchUrl(String url) throws IOException {
        URL apiUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try {
            int code = conn.getResponseCode();
            if (code != 200) {
                // Read error body for details
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    BufferedReader er = new BufferedReader(new InputStreamReader(errorStream));
                    StringBuilder esb = new StringBuilder();
                    String eline;
                    while ((eline = er.readLine()) != null)
                        esb.append(eline);
                    er.close();
                    throw new IOException("HTTP " + code + ": " + esb);
                }
                throw new IOException("HTTP " + code);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private void clearCompositionCache() {
        File dir = new File(context.getCacheDir() + File.separator + "composition");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    private void cacheComposition(String trainId,
            TrainComposition.Composition.Segments.Segment.SegmentComposition composition) {
        try {
            File file = new File(
                    context.getCacheDir() + File.separator + "composition" + File.separator + trainId + ".cache");

            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(new Gson().toJson(composition).getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TrainComposition.Composition.Segments.Segment.SegmentComposition getCompositionFromCache(String trainId) {
        try {
            File file = new File(
                    context.getCacheDir() + File.separator + "composition" + File.separator + trainId + ".cache");

            if (!file.exists())
                return null;

            // Expire after 1 hour
            Calendar time = Calendar.getInstance();
            time.add(Calendar.HOUR, -1);
            if (new Date(file.lastModified()).before(time.getTime())) {
                file.delete();
                return null;
            }

            byte[] bytes = new byte[(int) file.length()];
            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            return new Gson().fromJson(new String(bytes),
                    TrainComposition.Composition.Segments.Segment.SegmentComposition.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnConnectionsLoadedCallback {
        void onLoaded(Exception e, Connections connections);
    }

    public interface OnCompositionLoadedCallback {
        void onLoaded(Exception e, TrainComposition.Composition.Segments.Segment.SegmentComposition composition);
    }
}
