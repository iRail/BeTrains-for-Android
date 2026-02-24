package tof.cv.mpp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import tof.cv.mpp.adapter.TrafficAdapter;
import tof.cv.mpp.bo.Perturbations;

public class TrafficFragment extends Fragment {
    protected static final String TAG = "ActivityTraffic";
    private String lang;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_traffic, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        lang = this.getString(R.string.url_lang);
        if (settings.getBoolean("prefnl", false)) {
            lang = "nl";
        }

        // Hide recycler initially
        view.findViewById(R.id.recycler).setVisibility(View.GONE);

        String url = "https://api.irail.be/disturbances/?format=json&lang=" + lang;

        // Fetch data using HttpURLConnection (Ion is broken on SDK 36)
        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestProperty("User-Agent", "WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    Perturbations result = new Gson().fromJson(sb.toString(), Perturbations.class);
                    new Handler(Looper.getMainLooper()).post(() -> onDataLoaded(result));
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> onDataLoaded(null));
                }
                conn.disconnect();
            } catch (Exception ex) {
                new Handler(Looper.getMainLooper()).post(() -> onDataLoaded(null));
            }
        }).start();
    }

    private void onDataLoaded(Perturbations result) {
        if (getView() == null)
            return;

        try {
            if (result != null && result.disturbance != null) {
                RecyclerView recyclerView = getView().findViewById(R.id.recycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                TrafficAdapter adapter = new TrafficAdapter(getContext(), result);
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
                getView().findViewById(android.R.id.empty).setVisibility(View.GONE);
            } else if (result != null) {
                ((TextView) getView().findViewById(android.R.id.empty)).setText(R.string.issues_empty);
            } else {
                ((TextView) getView().findViewById(android.R.id.empty)).setText(R.string.check_connection);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_drawer_issues);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
