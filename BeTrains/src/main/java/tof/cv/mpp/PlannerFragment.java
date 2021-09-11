package tof.cv.mpp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Objects;

import tof.cv.mpp.MyPreferenceActivity.Prefs2Fragment;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.ConnectionAdapter;
import tof.cv.mpp.adapter.TipAdapter;
import tof.cv.mpp.bo.Alert;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.view.DateTimePicker;

public class PlannerFragment extends Fragment {

    RecyclerView recyclerView;

    //boolean isDebug = false;
    private static final int MENU_DT = 0;
    private static final int MENU_FAV = 1;
    private static final int MENU_PREF = 2;
    private static final int MENU_FAV_ADD = 3;

    public Calendar mDate;

    public static String datePattern = "EEE dd MMM HH:mm";
    public static String abDatePattern = "EEE dd MMM";
    public static String abTimePattern = "HH:mm";

    private static Connections allConnections = new Connections();

    private TextView tvDeparture;
    private TextView tvArrival;

    private static SharedPreferences settings;
    private SharedPreferences.Editor editor;


    private static final int ACTIVITY_DISPLAY = 0;
    private static final int ACTIVITY_STOP = 1;

    ActivityResultLauncher<Intent> arrivalActivityLauncher;
    ActivityResultLauncher<Intent> departureActivityResultLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planner, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arrivalActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        assert result.getData() != null;
                        String gare = result.getData().getStringExtra("GARE");
                        assert gare != null;
                        if (!gare.contentEquals("")) {
                            tvArrival.setText(gare);
                            editor.putString("pStop", gare);
                            editor.commit();
                        }
                    }
                });

        departureActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        assert result.getData() != null;
                        String gare = result.getData().getStringExtra("GARE");
                        assert gare != null;
                        if (!gare.contentEquals("")) {
                            tvDeparture.setText(gare);
                            editor.putString("pStart", gare);
                            editor.commit();
                        }
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = settings.edit();
        mDate = Calendar.getInstance();
        setHasOptionsMenu(true);

        tvDeparture = getView().findViewById(R.id.tv_start);
        tvArrival = getView().findViewById(R.id.tv_stop);
        recyclerView = getView().findViewById(R.id.recyclerview);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        setAllBtnListener();

        String pStart = settings.getString("pStart", "Mons");
        String pStop = settings.getString("pStop", "Tournai");

        if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null)
            try {
                pStart = getActivity().getIntent().getExtras().getString("Departure", pStart);
                pStop = getActivity().getIntent().getExtras().getString("Arrival", pStop);
            } catch (Exception e) {
                e.printStackTrace();
            }


        fillStations(pStart, pStop);

        try {
            fillData("");
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateActionBar();

        if (getActivity().getIntent().hasExtra("Departure") && getActivity().getIntent().hasExtra("Arrival"))
            doSearch();

        ((BottomAppBar) getActivity().findViewById(R.id.bar)).setHideOnScroll(true);
    }

    public void doSearch() {
        if (getView().findViewById(R.id.progress) != null)
            getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        mySearchThread(this.getActivity());
    }

    public void fillStations(String departure, String arrival) {
        tvDeparture = requireView().findViewById(R.id.tv_start);
        tvArrival = requireView().findViewById(R.id.tv_stop);

        if (departure != null && arrival != null) {
            tvDeparture.setText(departure);
            tvArrival.setText(arrival);
        }
    }

    private void setAllBtnListener() {
        TextView btnInvert = requireView().findViewById(R.id.mybuttonInvert);
        btnInvert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fillStations(tvArrival.getText().toString(),
                        tvDeparture.getText().toString());
            }
        });

        tvDeparture.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(),
                    StationPickerActivity.class);

            departureActivityResultLauncher.launch(i);

        });

        tvArrival.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(),
                    StationPickerActivity.class);

            arrivalActivityLauncher.launch(i);
        });

        FloatingActionButton fab = getActivity().findViewById(
                R.id.fab);
        fab.setOnClickListener(v -> doSearch());


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
                        .getText().toString(), 3, getActivity());
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
            ArrayList<Alert> singleAlert = checkSingleAlert(allConnections);
            ConnectionAdapter connAdapter = new ConnectionAdapter(allConnections.connection, getActivity(), singleAlert);
            recyclerView.setAdapter(connAdapter);


            PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit().putString("cached", new Gson().toJson(allConnections)).commit();
            if (bap.getMenu().size() == 0)
                bap.replaceMenu(R.menu.appbar);
        } else {
            if (url != null && url.length() > 0) {
                Log.e("CVE", "PAS DE RESULTATS");

                String message = getString(R.string.txt_error);
                if (allConnections.message != null & allConnections.message.length() > 0) {
                    message = allConnections.message;
                }
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }


            allConnections = Utils.getCachedConnections(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("cached", ""));
            //allConnections.connection.get(0).removeAlerts();

            if (allConnections != null) {
                ConnectionAdapter connAdapter = new ConnectionAdapter(allConnections.connection, getActivity(), checkSingleAlert(allConnections));
                recyclerView.setAdapter(connAdapter);
                //registerForContextMenu(getListView());
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
                        if (getView().findViewById(R.id.progress) != null)
                            getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        mDate.add(Calendar.HOUR, -1);
                        updateActionBar();
                        mySearchThread(getActivity());
                        break;
                    case R.id.appbar_next:
                        if (getView().findViewById(R.id.progress) != null)
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

    private ArrayList<Alert> checkSingleAlert(Connections allConnections) {

        if(allConnections.connection.get(0).getAlerts()==null)
            return null;

        ArrayList<Alert> toReturn = allConnections.connection.get(0).getAlerts().getAlertlist();
        ArrayList<Alert> toRemove = new ArrayList<>();

        String html = "";

        for (Connection c : allConnections.connection) {
            if (c.getAlerts().getAlertlist() != null)
                for (Alert aSingleAlert : toReturn) {
                    boolean toDel = true;
                    for (Alert anAlert : c.getAlerts().getAlertlist()) {
                        if (aSingleAlert.getHeader().contentEquals(anAlert.getHeader()))
                            toDel = false;
                    }
                    if (toDel)
                        toRemove.add(aSingleAlert);
                }
        }

        toReturn.remove(toRemove);

        if (toReturn.size() == 0)
            return null;

        String textAlert = "";
        html = "";
        for (Alert anAlert : toReturn) {
            textAlert += anAlert.getHeader() + "<br/>";
            html += ("<h3>" + anAlert.getHeader() + "</h3>");
            html += (anAlert.getDescription());
        }
        if (textAlert.endsWith("<br/>"))
            textAlert = textAlert.substring(0, textAlert.length() - 5);


        getView().findViewById(R.id.singlealertcard).setVisibility(View.VISIBLE);

        ((TextView) getView().findViewById(R.id.singlealert)).setText(Html.fromHtml(textAlert));

        final SpannableString s = new SpannableString(html); // msg should have url to enable clicking
        Linkify.addLinks(s, Linkify.ALL);

        String finalHtml = html;
        getView().findViewById(R.id.singlealertcard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog d = new AlertDialog.Builder(getContext())
                        .setMessage(Html.fromHtml(finalHtml)).create();
                d.show();
            }
        });


        return toReturn;
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

        recyclerView.setAdapter(new TipAdapter(list));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
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
        Log.e("CVE", "SEARCH");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int score = sp.getInt("searchGame", 0) + 1;
        sp.edit().putInt("searchGame", score).commit();

        String myStart;
        String myArrival;
        myStart = tvDeparture.getText().toString();
        myArrival = tvArrival.getText().toString();

        String langue = getString(R.string.url_lang);
        if (settings.getBoolean("prefnl", false))
            langue = "NL";

        String dA = "depart";
        if (settings.getString(getString(R.string.key_planner_da), "1")
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
        }

        url = url.replace(" ", "%20");


        url = "https://api.irail.be/connections.php?to=" + url;
        Log.e("CVE", "Search " + url);

        final String finalUrl = url;
        Ion.with(this).load(url).userAgent("WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android").as(new TypeToken<Connections>() {
        }).setCallback(new FutureCallback<Connections>() {
            @Override
            public void onCompleted(Exception e, Connections result) {
                if (e != null) {
                    //ref.push().setValue(new LogCVE("NULL","",finalUrl,""));
                    e.printStackTrace();
                }

                allConnections = result;

                if (allConnections == null) {
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
                    if (getView().findViewById(R.id.progress) != null)
                        getView().findViewById(R.id.progress).setVisibility(View.GONE);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });


    }


    private void showDateTimeDialog() {

        final DateTimePicker mDateTimeDialog = new DateTimePicker(getActivity(), this);
        final String timeS = android.provider.Settings.System.getString(
                getActivity().getContentResolver(),
                android.provider.Settings.System.TIME_12_24);
        final boolean is24h = !(timeS == null || timeS.equals("12"));
        mDateTimeDialog.setIs24HourView(is24h);
        mDateTimeDialog.show();
    }

    private void updateActionBar() {
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(
                    Utils.formatDate(mDate.getTime(), abDatePattern) + " - " + Utils.formatDate(mDate.getTime(), abTimePattern));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
