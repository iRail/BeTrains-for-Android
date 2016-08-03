package tof.cv.mpp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;


import java.util.ArrayList;
import java.util.List;

import tof.cv.mpp.bo.StationLocation;
import tof.cv.mpp.bo.StationLocationApi;
import tof.cv.mpp.dummy.DummyContent;

public class NotifFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks {

    ArrayList<Geofence> mGeofences;
    ArrayList<StationLocation> stationList;

    public NotifFragment() {
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        mGeofences = new ArrayList<Geofence>();

        addLocationReceipe();
    }

    private void addLocationReceipe() {

        final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String langue = getString(R.string.url_lang);
        // There is a setting to force dutch when Android is in English.
        if (mPrefs.getBoolean("prefnl", false))
            langue = "nl";
        final String finalLangue = langue;

        if (mPrefs.getString("stations", "").length() > 1) {
            StationLocationApi cache = new Gson().fromJson(mPrefs.getString("stations", ""), StationLocationApi.class);
            stationList = cache.station;

            displayDialog();

            long delta = System.currentTimeMillis() - mPrefs.getLong("stationsDate", 0);
            if (delta > 10 * DateUtils.DAY_IN_MILLIS || !finalLangue.contentEquals(mPrefs.getString("stationsLan", ""))) {
                Ion.with(getActivity())
                        .load("http://api.irail.be/stations.php?format=json&lang=" + finalLangue)
                        .as(new TypeToken<StationLocationApi>() {
                        })
                        .setCallback(new FutureCallback<StationLocationApi>() {
                            @Override
                            public void onCompleted(Exception e, StationLocationApi apiList) {
                                if (apiList != null && apiList.station != null) {
                                    SharedPreferences.Editor ed = mPrefs.edit();
                                    Gson gson = new Gson();
                                    ed.putString("stations", gson.toJson(apiList));
                                    ed.putLong("stationsDate", System.currentTimeMillis());
                                    ed.putString("stationsLan", finalLangue);
                                    ed.apply();
                                }
                            }
                        });
            }
        } else
            Ion.with(getActivity())
                    .load("http://api.irail.be/stations.php?format=json&lang=" + finalLangue)
                    .as(new TypeToken<StationLocationApi>() {
                    })
                    .setCallback(new FutureCallback<StationLocationApi>() {
                        @Override
                        public void onCompleted(Exception e, StationLocationApi apiList) {
                            if (e != null && e.getMessage() != null)
                                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG);

                            if (apiList != null && apiList.station != null) {
                                SharedPreferences.Editor ed = mPrefs.edit();
                                Gson gson = new Gson();
                                ed.putString("stations", gson.toJson(apiList));
                                ed.putLong("stationsDate", System.currentTimeMillis());
                                ed.putString("stationsLan", finalLangue);
                                ed.apply();

                                stationList = apiList.station;
                            }
                            displayDialog();
                        }
                    });

    }

    private void displayDialog() {
        final AlertDialog.Builder db = new AlertDialog.Builder(getActivity());

        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(getActivity());

        AutoCompleteStationAdapter namesAdapter = new AutoCompleteStationAdapter(
                getActivity(),
                R.layout.autocomplete_item,
                R.id.lbl_name,
                stationList
        );
        autoCompleteTextView.setAdapter(namesAdapter);

        db.setTitle(R.string.notif_station);
        db.setView(autoCompleteTextView);
        db.setNegativeButton(R.string.cancel, null);
        final AlertDialog alert = db.show();

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("CVE",stationList.get(position).getName());
               alert.dismiss();
            }
        });




        //autoCompleteTextView.showDropDown();
    }

    public class AutoCompleteStationAdapter extends ArrayAdapter<StationLocation> {

        Context context;
        int textViewResourceId;
        List<StationLocation> items, tempItems, suggestions;

        public AutoCompleteStationAdapter(Context context, int resource, int textViewResourceId, List<StationLocation> items) {
            super(context, textViewResourceId, items);
            this.context = context;
            this.textViewResourceId = textViewResourceId;
            this.items = items;
            tempItems = new ArrayList<StationLocation>(items); // this makes the difference.
            suggestions = new ArrayList<StationLocation>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.autocomplete_item, parent, false);
            }
            StationLocation names = items.get(position);
            if (names != null) {
                TextView lblName = (TextView) view.findViewById(R.id.lbl_name);
                if (lblName != null)
                    lblName.setText(names.getName());
            }
            return view;
        }

        @Override
        public Filter getFilter() {
            return nameFilter;
        }

        /**
         * Custom Filter implementation for custom suggestions we provide.
         */
        Filter nameFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                String str = ((StationLocation) resultValue).getName();
                return str;
            }

            @Override
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    suggestions.clear();
                    for (StationLocation names : tempItems) {
                        if (names.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(names);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<StationLocation> filterList = (ArrayList<StationLocation>) results.values;
                if (results != null && results.count > 0) {
                    clear();
                    for (StationLocation names : filterList) {
                        add(names);
                        notifyDataSetChanged();
                    }
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new MyNotifRecipeAdapter(DummyContent.ITEMS));
        }
        return view;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
