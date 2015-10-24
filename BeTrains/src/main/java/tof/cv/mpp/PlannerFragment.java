package tof.cv.mpp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import tof.cv.mpp.MyPreferenceActivity.Prefs2Fragment;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.adapter.ConnectionAdapter;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.view.DateTimePicker;

public class PlannerFragment extends ListFragment {

    boolean isDebug = false;
    public static final String SEARCH = "CgkI9Y3S0soCEAIQCQ";
    private static final int MENU_DT = 0;
    private static final int MENU_FAV = 1;
    private static final int MENU_PREF = 2;

    public Calendar mDate;

    public static String datePattern = "EEE dd MMM HH:mm";
    public static String abDatePattern = "EEE dd MMM";
    public static String abTimePattern = "HH:mm";

    int positionClicked;

    private static Connections allConnections = new Connections();

    private TextView tvDeparture;
    private TextView tvArrival;

    private ConnectionAdapter connAdapter;

    private String TAG = "BETRAINS";
    private Activity context;

    private static SharedPreferences settings;
    private SharedPreferences.Editor editor;

    public String fromIntentArrivalStation = null;
    public String fromIntentDepartureStation = null;
    public boolean fromIntent = false;

    // Second part need to be cleaned

    private static final int ACTIVITY_DISPLAY = 0;
    private static final int ACTIVITY_STOP = 1;
    private static final int ACTIVITY_STATION = 2;
    private static final int ACTIVITY_GETSTARTSTATION = 3;
    private static final int ACTIVITY_GETSTOPSTATION = 4;

    private void updateActionBar() {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(
                Utils.formatDate(mDate.getTime(), abDatePattern) + " - " + Utils.formatDate(mDate.getTime(), abTimePattern));
    }

    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planner, null);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this.setRetainInstance(true);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = settings.edit();
        context = this.getActivity();
        mDate = Calendar.getInstance();
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvDeparture = (TextView) getView().findViewById(R.id.tv_start);
        tvArrival = (TextView) getView().findViewById(R.id.tv_stop);
        getView().findViewById(R.id.progress).setVisibility(View.GONE);
        setAllBtnListener();
        String pStart = settings.getString("pStart", "MONS");
        try {
            pStart = getActivity().getIntent().getExtras().getString("Departure", pStart);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        String pStop = settings.getString("pStop", "TOURNAI");
        try {
            pStop = getActivity().getIntent().getExtras().getString("Arrival", pStop);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        fillStations(pStart, pStop);


       // getActivity().getSupportActionBar().setIcon(
        //        R.drawable.ab_planner);

        updateActionBar();


        Log.e("CVE", "Learned: " + !PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getBoolean("navigation_drawer_learned", false));
        Log.e("CVE","Layout: "+((WelcomeActivity) this.getActivity()).drawerLayout);

        if (!PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getBoolean("navigation_drawer_learned", false) && ((WelcomeActivity) this.getActivity()).drawerLayout != null)
            this.getView().findViewById(R.id.tuto).setVisibility(View.VISIBLE);

        final LinearLayout layout = (LinearLayout) getView().findViewById(R.id.Ly_Pannel_Sup);
        final ViewTreeObserver observer = layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    View fab = getView().findViewById(R.id.fab);

                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();

                    params.topMargin = layout.getHeight() - (fab.getHeight() / 2);

                    fab.setLayoutParams(params);
                } catch (Exception e) {
                   // e.printStackTrace();
                }
                //observer.removeGlobalOnLayoutListener(this);
            }
        });

    }

    public void doSearch() {
        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        mySearchThread(this.getActivity());
    }

    public void fillStations(String departure, String arrival) {
       // Log.e("", "fill " + departure + " - " + arrival + " - " + fromIntent);
        tvDeparture = (TextView) getView().findViewById(R.id.tv_start);
        tvArrival = (TextView) getView().findViewById(R.id.tv_stop);

        if (fromIntent) {
            fromIntent = false;
            tvDeparture.setText(fromIntentDepartureStation);
            tvArrival.setText(fromIntentArrivalStation);
            // mySearchThread();
        } else {
            if (departure != null && arrival != null) {
                tvDeparture.setText(departure);
                tvArrival.setText(arrival);
            }
        }

    }

    private void setAllBtnListener() {
        Button btnInvert = (Button) getView().findViewById(R.id.mybuttonInvert);
        btnInvert.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                fillStations(tvArrival.getText().toString(),
                        tvDeparture.getText().toString());
            }
        });

        Button btnSearch = (Button) getView().findViewById(R.id.mybuttonSearch);

        final FragmentActivity a = this.getActivity();

        btnSearch.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
                mySearchThread(a);

            }
        });

        tvDeparture.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),
                        StationPickerActivity.class);
                startActivityForResult(i, ACTIVITY_GETSTARTSTATION);
            }
        });

        tvArrival.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),
                        StationPickerActivity.class);
                startActivityForResult(i, ACTIVITY_GETSTOPSTATION);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(
                R.id.fab);
        fab.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                doSearch();
            }
        });
       /* Button btnInfoArrival = (Button) getView().findViewById(
                R.id.btn_info_arrival);
        btnInfoArrival.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                String station = tvArrival.getText().toString();
                Intent i = new Intent(getActivity(), InfoStationActivity.class);
                i.putExtra("Name", station);
                i.putExtra("Hour", mDate.get(Calendar.HOUR));
                i.putExtra("Minute", mDate.get(Calendar.MINUTE));
                startActivityForResult(i, ACTIVITY_STATION);

            }
        });
        Button btnInfoDeparture = (Button) getView().findViewById(
                R.id.btn_infos_departure);
        btnInfoDeparture.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String station = tvDeparture.getText().toString();
                Intent i = new Intent(getActivity(), InfoStationActivity.class);
                i.putExtra("Name", station);
                i.putExtra("Hour", mDate.get(Calendar.HOUR));
                i.putExtra("Minute", mDate.get(Calendar.MINUTE));
                startActivityForResult(i, ACTIVITY_STATION);

            }
        });*/

        Button btnAfter = (Button) getView().findViewById(R.id.mybuttonAfter);
        btnAfter.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
                mDate.add(Calendar.HOUR, 1);
                updateActionBar();
                mySearchThread(a);
            }
        });

        Button btnBefore = (Button) getView().findViewById(R.id.mybuttonBefore);
        btnBefore.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
                mDate.add(Calendar.HOUR, -1);
                updateActionBar();
                mySearchThread(a);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.add(Menu.NONE, MENU_DT, Menu.NONE, "Date/Time")
                .setIcon(R.drawable.ic_menu_time)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(Menu.NONE, MENU_FAV, Menu.NONE, "Add to Fav.")
                .setIcon(R.drawable.ic_menu_star)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu.add(Menu.NONE, MENU_PREF, Menu.NONE, "Settings")
                .setIcon(R.drawable.ic_menu_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (MENU_DT):
                showDateTimeDialog();
                return true;
            case (MENU_FAV):
                Utils.addAsStarred(tvDeparture.getText().toString(), tvArrival
                        .getText().toString(), 3, context);
                startActivity(new Intent(getActivity(), StarredActivity.class));
                return true;
            case (MENU_PREF):
                if (Build.VERSION.SDK_INT >= 11)
                    startActivity(new Intent(getActivity(),
                            MyPreferenceActivity.class).putExtra(
                            PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                            Prefs2Fragment.class.getName()));
                else {
                    startActivity(new Intent(getActivity(),
                            MyPreferenceActivity.class));
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getHeader(Connection c) {
        return c.getDeparture().getStation() + " - "
                + c.getArrival().getStation();

    }

    private void fillData() {

        if (allConnections != null && allConnections.connection != null) {
            connAdapter = new ConnectionAdapter(this.getActivity()
                    .getBaseContext(), R.layout.row_planner,
                    allConnections.connection
            );
            setListAdapter(connAdapter);
            registerForContextMenu(getListView());

        } else {
            allConnections = Utils.getCachedConnections();

            if (allConnections != null) {
                connAdapter = new ConnectionAdapter(this.getActivity()
                        .getBaseContext(), R.layout.row_planner,
                        allConnections.connection
                );
                setListAdapter(connAdapter);
                registerForContextMenu(getListView());
            } else {
                fillWithTips();
            }

        }

    }

    public void fillWithTips() {
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        // fill the map with data
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("tip", getString(R.string.tipa));
        map.put("title", getString(R.string.tipatitle));
        list.add(map);

        map = new HashMap<String, String>();
        map.put("tip", getString(R.string.tipb));
        map.put("title", getString(R.string.tipbtitle));
        list.add(map);

        map = new HashMap<String, String>();
        map.put("tip", getString(R.string.tipc));
        map.put("title", getString(R.string.tipctitle));
        list.add(map);

        map = new HashMap<String, String>();
        map.put("tip", getString(R.string.tipd));
        map.put("title", getString(R.string.tipdtitle));
        list.add(map);

        // Use a SimpleAdapter to display tips
        String[] from = {"tip", "title"};
        int[] to = {R.id.tiptitle, R.id.tiptext};
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), list,
                R.layout.row_tip, from, to);
        setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        positionClicked = position;

        try {

            Connection currentConnection = allConnections.connection
                    .get(positionClicked);
            FragmentManager fm = getActivity().getSupportFragmentManager();
            DialogViaFragment editNameDialog = new DialogViaFragment(
                    allConnections.connection.get(positionClicked));
            editNameDialog.show(fm, "fragment_edit_name");
            /*
            if (currentConnection.getVias() != null
					&& currentConnection.getVias().via.size() > 0) {

				FragmentManager fm = getActivity().getSupportFragmentManager();
				DialogViaFragment editNameDialog = new DialogViaFragment(
						allConnections.connection.get(positionClicked));
				editNameDialog.show(fm, "fragment_edit_name");

			} else {
				Intent i = new Intent(getActivity(), InfoTrainActivity.class);
				i.putExtra("Name", currentConnection.getDeparture()
						.getVehicle());
				i.putExtra("fromto", tvDeparture.getText().toString() + " - "
						+ tvArrival.getText().toString());
				i.putExtra("Hour", mDate.get(Calendar.HOUR));
				i.putExtra("Minute", mDate.get(Calendar.MINUTE));
				startActivity(i);
			}*/

        } catch (Exception e) {
            e.printStackTrace();
            // noDataClick(positionClicked);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //Log.d(TAG, "requestCode is: " + requestCode);

        switch (requestCode) {
            case ACTIVITY_DISPLAY:
                fillData();
                break;
            case ACTIVITY_STOP:
                fillData();
                break;

            case ACTIVITY_GETSTARTSTATION:
                if (intent != null) {
                    String gare = intent.getStringExtra("GARE");
                    if (!gare.contentEquals("")) {
                        tvDeparture.setText(gare);
                        editor.putString("pStart", gare);
                        editor.commit();
                    }
                }

                break;

            case ACTIVITY_GETSTOPSTATION:
                if (intent != null) {
                    String gare = intent.getStringExtra("GARE");
                    if (!gare.contentEquals("")) {
                        tvArrival.setText(gare);
                        editor.putString("pStop", gare);
                        editor.commit();
                    }
                }
                break;

            default:
                break;

        }

    }

    public void onPause() {
        super.onPause();
        String start = tvDeparture.getText().toString();
        String stop = tvArrival.getText().toString();
        if (!start.contentEquals("") && !start.contentEquals("")) {
            editor.putString("pStart", start);
            editor.putString("pStop", stop);
            editor.commit();
        }

    }

    private void mySearchThread(final Activity a) {
        Runnable trainSearch = new Runnable() {
            public void run() {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                int score = sp.getInt("searchGame", 0) + 1;
                sp.edit().putInt("searchGame", score).commit();
                makeApiRequest();
                if (a != null)
                    a.runOnUiThread(new Runnable() {
                        public void run() {
                            //
                            try {
                                fillData();
                                getView().findViewById(R.id.progress).setVisibility(View.GONE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

            }
        };

        Thread thread = new Thread(null, trainSearch, "MyThread");
        thread.start();
    }

    public void makeApiRequest() {

        String myStart;
        String myArrival;
        myStart = tvDeparture.getText().toString();
        myArrival = tvArrival.getText().toString();

        String langue = getString(R.string.url_lang);
        // There is a setting to force dutch when Android is in English.
        if (settings.getBoolean("prefnl", false))
            langue = "NL";

        String dA = "depart";
        if (settings.getString(context.getString(R.string.key_planner_da), "1")
                .contentEquals("2"))
            dA = "arrive";

        allConnections = UtilsWeb.getAPIConnections(
                "" + (mDate.get(Calendar.YEAR) - 2000),
                "" + (mDate.get(Calendar.MONTH) + 1),
                "" + mDate.get(Calendar.DAY_OF_MONTH),
                Utils.formatDate(mDate.getTime(), "HH"),
                Utils.formatDate(mDate.getTime(), "mm"), langue, myStart,
                myArrival, dA, getActivity());

        if (allConnections == null) {
            //Log.e(TAG, "API failure!!!");
            if (getActivity() != null)
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), R.string.txt_error,
                                Toast.LENGTH_LONG).show();
                    }
                });

        }
    }

    public void onResume() {
        super.onResume();

        try {
            fillData();
        } catch (Exception e) {
            //Log.i(TAG, "Impossible to fill Data:\n" + e.getMessage());
            e.printStackTrace();
        }

    }

    public void noDataClick(int position) {

        // TODO If there were no results, launch browser to check connection.

		/*
         * Intent i = new Intent(this, InfoTrainActivity.class);
		 * 
		 * i.putExtra("fromto", tvDeparture.getText().toString() + " - " +
		 * tvArrival.getText().toString());
		 * 
		 * i.putExtra(ConnectionDbAdapter.KEY_TRAINS, c.getString(c
		 * .getColumnIndexOrThrow(ConnectionDbAdapter.KEY_TRAINS)));
		 * 
		 * startActivity(i);
		 */
    }

    private void showDateTimeDialog() {

        final DateTimePicker mDateTimeDialog = new DateTimePicker(
                (Context) getActivity(), this);

        final String timeS = android.provider.Settings.System.getString(
                getActivity().getContentResolver(),
                android.provider.Settings.System.TIME_12_24);
        final boolean is24h = !(timeS == null || timeS.equals("12"));

        mDateTimeDialog.setIs24HourView(is24h);

        mDateTimeDialog.show();
    }
}
