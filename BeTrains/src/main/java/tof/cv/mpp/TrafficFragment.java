package tof.cv.mpp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.ListFragment;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import tof.cv.mpp.adapter.TrafficAdapter;
import tof.cv.mpp.bo.Perturbations;


public class TrafficFragment extends ListFragment {
    protected static final String TAG = "ActivityTraffic";
    private String lang;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_traffic, null);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        lang = this.getString(R.string.url_lang);
        if (settings.getBoolean("prefnl", false)) {
            lang = "nl";
        }

        String url = "https://api.irail.be/disturbances/?format=json&lang=" + lang;
        Log.e("CVE", url);
        Ion.with(this).load(url).as(new TypeToken<Perturbations>() {
        }).setCallback(new FutureCallback<Perturbations>() {
            @Override
            public void onCompleted(Exception e, Perturbations result) {

                try {
                    Log.e("CVE", "" + result);
                    if (result != null) {
                        if (result.disturbance != null) {
                            TrafficAdapter adapter = new TrafficAdapter(getActivity(),
                                    R.layout.row_rss, result, getLayoutInflater());
                            setListAdapter(adapter);
                        } else
                            ((TextView) getView().findViewById(android.R.id.empty)).setText(R.string.issues_empty);


                    } else
                        ((TextView) getView().findViewById(android.R.id.empty)).setText(R.string.check_connection);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }
        });

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
