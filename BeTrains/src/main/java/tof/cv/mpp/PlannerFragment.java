package tof.cv.mpp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import tof.cv.mpp.MyPreferenceActivity.Prefs2Fragment;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.ConnectionAdapter;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.bo.LogCVE;
import tof.cv.mpp.view.DateTimePicker;

public class PlannerFragment extends ListFragment {

    boolean isDebug = false;
    public static final String SEARCH = "CgkI9Y3S0soCEAIQCQ";
    private static final int MENU_DT = 0;
    private static final int MENU_FAV = 1;
    private static final int MENU_PREF = 2;
    private static final int MENU_FAV_ADD = 3;

    public Calendar mDate;

    public static String datePattern = "EEE dd MMM HH:mm";
    public static String abDatePattern = "EEE dd MMM";
    public static String abTimePattern = "HH:mm";

    private int positionClicked;

    private static Connections allConnections = new Connections();

    private TextView tvDeparture;
    private TextView tvArrival;

    private ConnectionAdapter connAdapter;

    private String TAG = "BETRAINS";
    private Activity context;

    private static SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private String fromIntentArrivalStation = null;
    private String fromIntentDepartureStation = null;
    private boolean fromIntent = false;

    // Second part need to be cleaned

    private static final int ACTIVITY_DISPLAY = 0;
    private static final int ACTIVITY_STOP = 1;
    private static final int ACTIVITY_STATION = 2;
    private static final int ACTIVITY_GETSTARTSTATION = 3;
    private static final int ACTIVITY_GETSTOPSTATION = 4;

    private void updateActionBar() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(
                Utils.formatDate(mDate.getTime(), abDatePattern) + " - " + Utils.formatDate(mDate.getTime(), abTimePattern));
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

        updateActionBar();

        if (!PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getBoolean("navigation_drawer_learned", false) && ((WelcomeActivity) this.getActivity()).drawerLayout != null && !getResources().getBoolean(R.bool.tablet_layout))
            this.getView().findViewById(R.id.tuto).setVisibility(View.VISIBLE);

        if (getActivity().getIntent().hasExtra("Departure") && getActivity().getIntent().hasExtra("Arrival"))
            doSearch();

        BottomAppBar bap = getActivity().findViewById(R.id.bar);

        bap.setHideOnScroll(true);
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
        TextView btnInvert = (TextView) getView().findViewById(R.id.mybuttonInvert);
        btnInvert.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                fillStations(tvArrival.getText().toString(),
                        tvDeparture.getText().toString());
            }
        });

        final FragmentActivity a = this.getActivity();

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

        FloatingActionButton fab = getActivity().findViewById(
                R.id.fab);
        fab.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                doSearch();
            }
        });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

       /* menu.add(Menu.NONE, MENU_DT, Menu.NONE, R.string.action_change_datetime)
                .setIcon(R.drawable.ic_menu_time)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);*/
        menu.add(Menu.NONE, MENU_FAV, Menu.NONE, R.string.action_goto_favorites)
                .setIcon(R.drawable.ic_menu_star)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(Menu.NONE, MENU_FAV_ADD, Menu.NONE, R.string.action_add_to_favorites)
                .setIcon(R.drawable.ic_menu_star_add)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu.add(Menu.NONE, MENU_PREF, Menu.NONE, R.string.action_settings)
                .setIcon(R.drawable.ic_menu_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (MENU_DT):
                showDateTimeDialog();
                return true;
            case (MENU_FAV_ADD):
                Utils.addAsStarred(tvDeparture.getText().toString(), tvArrival
                        .getText().toString(), 3, context);
                startActivity(new Intent(getActivity(), StarredActivity.class));
                return true;
            case (MENU_FAV):
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

    private void fillData(final String url) {
        BottomAppBar bap = getActivity().findViewById(R.id.bar);
        if (allConnections != null && allConnections.connection != null) {
            connAdapter = new ConnectionAdapter(this.getActivity()
                    .getBaseContext(), R.layout.row_planner,
                    allConnections.connection
            );
            setListAdapter(connAdapter);
            registerForContextMenu(getListView());
            PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit().putString("cached", new Gson().toJson(allConnections)).commit();
            if (bap.getMenu().size() == 0)
                bap.replaceMenu(R.menu.appbar);
        } else {
            Log.e("CVE", "NULL");

            if (url != null && url.length() > 0) {
                // Linkify the message
                final SpannableString s = new SpannableString(getString(R.string.msg_api_error) + " - " + url);
                Linkify.addLinks(s, Linkify.ALL);

                final AlertDialog d = new AlertDialog.Builder(getContext())
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .setTitle(R.string.msg_api_error_title)
                        .setMessage(s)
                        .create();

                d.show();
                // Make the textview clickable. Must be called after show()
                ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
            }


            allConnections = Utils.getCachedConnections(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("cached", ""));
            if (allConnections != null) {
                connAdapter = new ConnectionAdapter(this.getActivity()
                        .getBaseContext(), R.layout.row_planner,
                        allConnections.connection
                );
                setListAdapter(connAdapter);
                registerForContextMenu(getListView());
                if (bap.getMenu().size() == 0)
                    bap.inflateMenu(R.menu.appbar);
            } else {
                fillWithTips();
            }

        }
        bap.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.appbar_prev:
                        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        mDate.add(Calendar.HOUR, -1);
                        updateActionBar();
                        mySearchThread(getActivity());
                        break;
                    case R.id.appbar_next:
                        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        mDate.add(Calendar.HOUR, 1);
                        updateActionBar();
                        mySearchThread(getActivity());
                        break;
                    /*case R.id.appbar_mix:
                        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        fillStations(tvArrival.getText().toString(),
                                tvDeparture.getText().toString());
                        mySearchThread(getActivity());
                        break;*/
                }
                return false;
            }
        });
        bap.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimeDialog();
            }
        });
    }

    public void fillWithTips() {
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        // fill the map with data
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("tip", getString(R.string.intro_tip_a_title));
        map.put("title", getString(R.string.intro_tip_a));
        list.add(map);

        map = new HashMap<String, String>();
        map.put("tip", getString(R.string.intro_tip_b_title));
        map.put("title", getString(R.string.intro_tip_b));
        list.add(map);

        map = new HashMap<String, String>();
        map.put("tip", getString(R.string.intro_tip_c_title));
        map.put("title", getString(R.string.intro_tip_c));
        list.add(map);

        map = new HashMap<String, String>();
        map.put("tip", getString(R.string.intro_tip_d_title));
        map.put("title", getString(R.string.intro_tip_d));
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
                fillData("");
                break;
            case ACTIVITY_STOP:
                fillData("");
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

    //DatabaseReference ref;
    private void mySearchThread(final Activity a) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int score = sp.getInt("searchGame", 0) + 1;
        sp.edit().putInt("searchGame", score).commit();

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

        String year = "" + (mDate.get(Calendar.YEAR) - 2000);
        String month = "" + (mDate.get(Calendar.MONTH) + 1);
        String day = "" + mDate.get(Calendar.DAY_OF_MONTH);
        String hour = Utils.formatDate(mDate.getTime(), "HH");
        String minutes = Utils.formatDate(mDate.getTime(), "mm");


        if (day.length() == 1)
            day = "0" + day;

        if (month.length() == 1)
            month = "0" + month;
        if (month.contentEquals("13"))
            month = "01";

        String url = "";
        //ref = FirebaseDatabase.getInstance().getReference().child("log").getRef();
        try {
            url = URLEncoder.encode(myArrival, "UTF-8") + "&from=" + URLEncoder.encode(myStart, "UTF-8") + "&date=" + day + month
                    + year + "&time=" + hour + minutes + "&timeSel="
                    + dA + "&lang=" + langue
                    + "&typeOfTransport=train&format=json&fast=true&alerts=true";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            url = myArrival + "&from=" + myStart + "&date=" + day + month
                    + year + "&time=" + hour + minutes + "&timeSel="
                    + dA + "&lang=" + langue
                    + "&typeOfTransport=train&format=json&fast=true&alerts=true";
            // ref.push().setValue(new LogCVE("CATCH",e.getMessage(),url,""));
        }


        url = url.replace(" ", "%20");


        url = "https://api.irail.be/connections.php?to=" + url;
        // ref.push().setValue(new LogCVE("URL","",url,""));
        Log.v(TAG, url);
        Log.e("CVE", "Search " + url);
        final String finalUrl = url;
        Ion.with(this).load(url).setTimeout(4500).userAgent("WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android").as(new TypeToken<Connections>() {
        }).setCallback(new FutureCallback<Connections>() {
            @Override
            public void onCompleted(Exception e, Connections result) {
                if (e != null) {
                    //ref.push().setValue(new LogCVE("NULL","",finalUrl,""));
                    e.printStackTrace();
                }

                allConnections = result;
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

                try {

                    fillData(finalUrl);
                    getView().findViewById(R.id.progress).setVisibility(View.GONE);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });


    }


    public void onResume() {
        super.onResume();
        try {
            fillData("");
        } catch (Exception e) {
            //Log.i(TAG, "Impossible to fill Data:\n" + e.getMessage());
            e.printStackTrace();
        }

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
