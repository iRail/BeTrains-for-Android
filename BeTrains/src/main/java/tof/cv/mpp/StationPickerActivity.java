package tof.cv.mpp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;

import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.ListFragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.FilterTextWatcher;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.IndexAdapter;
import tof.cv.mpp.bo.StationLocation;
import tof.cv.mpp.bo.StationLocationApi;

public class StationPickerActivity extends AppCompatActivity implements
        ViewPager.OnPageChangeListener {

    MyAdapter mAdapter;
    ViewPager mPager;
    private static final int ADD_ID = 1;
    private static final int REMOVE_ID = 2;
    private static final int ADD_EUROPE_ID = 3;
    static StationFavListFragment f;

    private static DbAdapterConnection mDbHelper;

    protected static String[] TITLES;

    // , "EUROPE"
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        // getSupportFragmentManager().putFragment(outState,
        // StationFavListFragment.class.getName(), f);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_tab_picker);

        TITLES =  new String[]{
                getString(R.string.station_picker_title_belgium),
                getString(R.string.station_picker_title_favorite)};

        setSupportActionBar((Toolbar) findViewById(R.id.my_awesome_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(this);
        mDbHelper = new DbAdapterConnection(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class EuroStationListFragment extends StationListFragment {

        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenuInfo menuInfo) {
            // super.onCreateContextMenu(menu, v, menuInfo);
            menu.add(0, ADD_EUROPE_ID, 0, R.string.action_add_to_favorites);
        }

        @Override
        public boolean onContextItemSelected(android.view.MenuItem item) {
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            switch (item.getItemId()) {
                case ADD_EUROPE_ID:
                    String sName = (String) getListAdapter().getItem(
                            (int) menuInfo.id);
                    Utils.addAsStarred(sName, "", 1, getActivity());
                    return true;
                default:
                    return super
                            .onContextItemSelected((android.view.MenuItem) item);
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = null;
            v = inflater.inflate(R.layout.fragment_station_list, container,
                    false);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            String[] list = ConnectionMaker.LIST_OF_EURO_STATIONS;

            getListView().setFastScrollEnabled(true);
            registerForContextMenu(getListView());

            ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, list);

            this.setListAdapter(a);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            // System.out.println("DO NOTHING!");

        }

    }

    public static class StationListFragment extends ListFragment implements
            OnScrollListener {

        ArrayList<StationLocation> stationList;

        /**
         * When creating, retrieve this instance's number from its arguments.
         */

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // mNum = getArguments() != null ? getArguments().getInt("num") : 1;
            View v = null;
            v = inflater.inflate(R.layout.fragment_station_picker, container,
                    false);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String langue = getString(R.string.url_lang);
            // There is a setting to force dutch when Android is in English.
            if (mPrefs.getBoolean("prefnl", false))
                langue = "nl";
            final String finalLangue = langue;

            if (mPrefs.getString("stations", "").length() > 1) {
                StationLocationApi cache = new Gson().fromJson(mPrefs.getString("stations", ""), StationLocationApi.class);
                stationList = cache.station;
                refreshList(finalLangue.contentEquals("en"));
                long delta = System.currentTimeMillis() - mPrefs.getLong("stationsDate", 0);
                Log.e("CVE","Vide");
                //Force update
                if (delta > 10 * DateUtils.DAY_IN_MILLIS || !finalLangue.contentEquals(mPrefs.getString("stationsLan", ""))) {
                    Log.e("CVE","cas1");
                    Ion.with(getActivity())
                            .load("https://api.irail.be/stations.php?format=json&lang="+finalLangue).setTimeout(1200)
                            .as(new TypeToken<StationLocationApi>() {
                            })
                            .setCallback(new FutureCallback<StationLocationApi>() {
                                @Override
                                public void onCompleted(Exception e, StationLocationApi apiList) {
                                    Log.e("CVE"+apiList,""+apiList);
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
                        .load("https://api.irail.be/stations.php?format=json&lang="+finalLangue).setTimeout(1200)
                                .as(new TypeToken<StationLocationApi>() {
                                })
                                .setCallback(new FutureCallback<StationLocationApi>() {
                                    @Override
                                    public void onCompleted(Exception e, StationLocationApi apiList) {
                                        if (e != null && e.getMessage() != null)
                                            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG);

                                        Log.e("CVE"+apiList,""+apiList);

                                        if (apiList != null && apiList.station != null) {
                                            SharedPreferences.Editor ed = mPrefs.edit();
                                            Gson gson = new Gson();
                                            ed.putString("stations", gson.toJson(apiList));
                                            ed.putLong("stationsDate", System.currentTimeMillis());
                                            ed.putString("stationsLan", finalLangue);
                                            ed.apply();

                                            stationList = apiList.station;
                                        }

                                        refreshList(finalLangue.contentEquals("en"));
                                    }
                                });


        }

        private void refreshList(boolean standart) {

            ArrayList<String> list = new ArrayList<>();
            if (stationList == null) {
                list =new ArrayList<>(Arrays.asList(ConnectionMaker.LIST_OF_STATIONS));
            } else {

                list.clear();
                for (StationLocation aStation : stationList) {
                    list.add(standart?aStation.getStation():aStation.getName());
                }
            }

            Collections.sort(list);

            getListView().setFastScrollEnabled(true);
            registerForContextMenu(getListView());

            IndexAdapter a = new IndexAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, list);


            EditText filterText = (EditText) getActivity().findViewById(
                    R.id.search_box);
            FilterTextWatcher filterTextWatcher = new FilterTextWatcher(a);
            if (filterText != null) {
                filterText.addTextChangedListener(filterTextWatcher);
                getListView().setTextFilterEnabled(true);
            }

            getListView().setOnScrollListener(this);

            this.setListAdapter(a);
        }


        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Bundle bundle = new Bundle();
            bundle.putString("GARE", l.getItemAtPosition(position).toString());
            Intent i = new Intent();
            i.putExtras(bundle);
            getActivity().setResult(RESULT_OK, i);
            getActivity().finish();
        }

        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.add(0, ADD_ID, 0, R.string.action_add_to_favorites);

        }

        @Override
        public boolean onContextItemSelected(android.view.MenuItem item) {
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            switch (item.getItemId()) {
                case ADD_ID:
                    String sName = (String) getListAdapter().getItem(
                            (int) menuInfo.id);
                    Utils.addAsStarred(sName, "", 1, getActivity());
                    return true;
                default:
                    return super
                            .onContextItemSelected((android.view.MenuItem) item);
            }

        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
/*
            if (mReady && mDialogText != null && firstVisibleItem >0) {
                try {
                    char firstLetter = view.getItemAtPosition(firstVisibleItem)
                            .toString().charAt(0);

                    if (!mShowing && firstLetter != mPrevLetter) {
                        mShowing = true;
                        mDialogText.setVisibility(View.VISIBLE);
                    }
                    mDialogText.setText(((Character) firstLetter).toString());
                    mHandler.removeCallbacks(mRemoveWindow);
                    mHandler.postDelayed(mRemoveWindow, 1000);
                    mPrevLetter = firstLetter;
                } catch (Exception e) {

                }

            }
*/
        }

    }

    public static class StationFavListFragment extends ListFragment {

        Cursor mCursor;

        /**
         * Create a new instance of CountingFragment, providing "num" as an
         * argument.
         */
        static StationFavListFragment newInstance() {
            f = new StationFavListFragment();
            return f;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v;
            v = inflater.inflate(R.layout.fragment_station_list, container,
                    false);
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            updateList();
            registerForContextMenu(getListView());
        }

        public void updateList() {
            mDbHelper.open();
            try {

                mCursor = mDbHelper.fetchAllFavStations();

                String[] from = {DbAdapterConnection.KEY_FAV_NAME};
                int[] to = {android.R.id.text1};

                SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                        getActivity(), android.R.layout.simple_list_item_1,
                        mCursor, from, to);
                setListAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mDbHelper.close();
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Bundle bundle = new Bundle();

            mCursor.moveToPosition(position);

            bundle.putString("GARE", mCursor.getString(mCursor
                    .getColumnIndex(DbAdapterConnection.KEY_FAV_NAME)));

            Intent i = new Intent();
            i.putExtras(bundle);
            getActivity().setResult(RESULT_OK, i);
            getActivity().finish();
        }

        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.add(0, REMOVE_ID, 0, R.string.remove);
        }

        @Override
        public boolean onContextItemSelected(android.view.MenuItem item) {
            switch (item.getItemId()) {
                case REMOVE_ID:
                    AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                            .getMenuInfo();
                    mDbHelper.open();
                    Log.i("", "ID " + menuInfo.id);
                    mDbHelper.deleteFav(menuInfo.id);
                    mDbHelper.close();

                    updateList();
                    return true;
                default:
                    return super
                            .onContextItemSelected((android.view.MenuItem) item);
            }

        }
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        private int mCount = TITLES.length;


        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new StationListFragment();
                case 1:
                    return StationFavListFragment.newInstance();
                case 2:
                    return new EuroStationListFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position % TITLES.length];
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        // Log.i("","SCROLLED "+position);

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 1) {
            f.updateList();
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Log.i("","CHANGED "+state);

    }

}
