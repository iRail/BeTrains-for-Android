package tof.cv.mpp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import androidx.preference.PreferenceManager;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import tof.cv.mpp.MyPreferenceActivity.Prefs2Fragment;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.ConnectionAdapter;
import tof.cv.mpp.adapter.TipAdapter;
import tof.cv.mpp.bo.Alert;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.databinding.FragmentPlannerBinding;
import tof.cv.mpp.view.DateTimePicker;
import tof.cv.mpp.viewmodel.PlannerViewModel;

public class PlannerFragment extends Fragment implements ConnectionAdapter.CompositionRequestCallback {

    private FragmentPlannerBinding binding;
    private PlannerViewModel viewModel;

    // boolean isDebug = false;
    private static final int MENU_DT = 0;
    private static final int MENU_FAV = 1;
    private static final int MENU_PREF = 2;
    private static final int MENU_FAV_ADD = 3;

    public Calendar mDate;

    public static String datePattern = "EEE dd MMM HH:mm";
    public static String abDatePattern = "EEE dd MMM";
    public static String abTimePattern = "HH:mm";

    private static final String PREF_START = "pStart";
    private static final String PREF_STOP = "pStop";
    private static final String PREF_SEARCH_GAME = "searchGame";
    private static final String PREF_NL = "prefnl";
    private static final String PREF_CACHED = "cached";
    private static final String DEFAULT_START = "Mons";
    private static final String DEFAULT_STOP = "Tournai";

    private static SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private static final int ACTIVITY_DISPLAY = 0;
    private static final int ACTIVITY_STOP = 1;

    ActivityResultLauncher<Intent> arrivalActivityLauncher;
    ActivityResultLauncher<Intent> departureActivityResultLauncher;

    private ConnectionAdapter connAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentPlannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        setAllBtnListener();
        if (connAdapter != null) {
            connAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PlannerViewModel.class);

        arrivalActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        assert result.getData() != null;
                        String gare = result.getData().getStringExtra("GARE");
                        assert gare != null;
                        if (!gare.contentEquals("")) {
                            binding.tvStop.setText(gare);
                            editor.putString(PREF_STOP, gare);
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
                            binding.tvStart.setText(gare);
                            editor.putString(PREF_START, gare);
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

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.add(Menu.NONE, MENU_FAV, Menu.NONE, R.string.action_goto_favorites)
                        .setIcon(R.drawable.ic_menu_star)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                menu.add(Menu.NONE, MENU_FAV_ADD, Menu.NONE, R.string.action_add_to_favorites)
                        .setIcon(R.drawable.ic_menu_star_add)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case (MENU_DT):
                        showDateTimeDialog();
                        return true;
                    case (MENU_FAV_ADD):
                        Utils.addAsStarred(binding.tvStart.getText().toString(), binding.tvStop
                                .getText().toString(), 3, getActivity());
                        startActivity(new Intent(getActivity(), StarredActivity.class));
                        return true;
                    case (MENU_FAV):
                        startActivity(new Intent(getActivity(), StarredActivity.class));
                        return true;
                    case (MENU_PREF):
                        startActivity(new Intent(getActivity(),
                                MyPreferenceActivity.class).putExtra(
                                        PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                                        Prefs2Fragment.class.getName()));
                        return true;
                    default:
                        return false;
                }
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerview.setLayoutManager(layoutManager);
        binding.recyclerview.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        String pStart = settings.getString(PREF_START, DEFAULT_START);
        String pStop = settings.getString(PREF_STOP, DEFAULT_STOP);

        if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null)
            try {
                pStart = getActivity().getIntent().getExtras().getString("Departure", pStart);
                pStop = getActivity().getIntent().getExtras().getString("Arrival", pStop);
            } catch (Exception e) {
                e.printStackTrace();
            }

        fillStations(pStart, pStop);

        setupViewModelObservers();

        try {
            Connections cached = Utils.getCachedConnections(
                    PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(PREF_CACHED, ""));
            if (cached != null) {
                setupAdapter(cached);
            } else {
                fillWithTips();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateActionBar();

        if (getActivity().getIntent().hasExtra("Departure") && getActivity().getIntent().hasExtra("Arrival"))
            doSearch();
    }

    private void setupViewModelObservers() {
        viewModel.getConnections().observe(getViewLifecycleOwner(), connections -> {
            if (connections != null) {
                setupAdapter(connections);
                PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit()
                        .putString(PREF_CACHED, new Gson().toJson(connections)).commit();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding.progress != null) {
                binding.progress.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && getContext() != null) {
                Log.e("CVE", error);
                showErrorDialog(error);
            }
        });

        viewModel.getCompositions().observe(getViewLifecycleOwner(), compositions -> {
            if (connAdapter != null) {
                connAdapter.updateCompositions(compositions);
            }
        });
    }

    public void doSearch() {
        performSearch();
    }

    public void fillStations(String departure, String arrival) {
        if (departure != null && arrival != null) {
            binding.tvStart.setText(departure);
            binding.tvStop.setText(arrival);
        }
    }

    private void setAllBtnListener() {
        binding.mybuttonInvert.setOnClickListener(v -> fillStations(binding.tvStop.getText().toString(),
                binding.tvStart.getText().toString()));

        binding.tvStart.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(),
                    StationPickerActivity.class);
            departureActivityResultLauncher.launch(i);

        });

        binding.tvStop.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(),
                    StationPickerActivity.class);

            arrivalActivityLauncher.launch(i);
        });

        FloatingActionButton fab = getActivity().findViewById(
                R.id.fab);
        fab.setOnClickListener(v -> doSearch());

        binding.appbarPrev.setOnClickListener(v -> {
            mDate.add(Calendar.HOUR, -1);
            updateActionBar();
            performSearch();
        });

        binding.appbarNext.setOnClickListener(v -> {
            mDate.add(Calendar.HOUR, 1);
            updateActionBar();
            performSearch();
        });

        binding.appbarTime.setOnClickListener(v -> {
            showDateTimeDialog();
        });

    }

    private void setupAdapter(Connections allConnections) {
        if (allConnections != null && allConnections.connection != null) {
            ArrayList<Alert> singleAlert = checkSingleAlert(allConnections);
            connAdapter = new ConnectionAdapter(allConnections.connection, getActivity(), singleAlert, this);
            binding.recyclerview.setAdapter(connAdapter);
        }
    }

    private ArrayList<Alert> checkSingleAlert(Connections allConnections) {
        if (allConnections.connection.isEmpty() || allConnections.connection.get(0).getAlerts() == null)
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

        binding.singlealertcard.setVisibility(View.VISIBLE);

        binding.singlealert.setText(Html.fromHtml(textAlert));

        final SpannableString s = new SpannableString(html);
        Linkify.addLinks(s, Linkify.ALL);

        String finalHtml = html;
        binding.singlealertcard.setChecked(true);
        binding.singlealertcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog d = new MaterialAlertDialogBuilder(getContext())
                        .setMessage(Html.fromHtml(finalHtml))
                        .setPositiveButton(R.string.ok, null).create();
                d.show();
            }
        });

        return toReturn;
    }

    public void fillWithTips() {
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
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

        binding.recyclerview.setAdapter(new TipAdapter(list));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case ACTIVITY_DISPLAY:
            case ACTIVITY_STOP:
                performSearch();
                break;

            default:
                break;
        }

    }

    public void onPause() {
        super.onPause();
        String start = binding.tvStart.getText().toString();
        String stop = binding.tvStop.getText().toString();
        if (!start.contentEquals("") && !start.contentEquals("")) {
            editor.putString(PREF_START, start);
            editor.putString(PREF_STOP, stop);
            editor.commit();
        }

    }

    private void performSearch() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int score = sp.getInt(PREF_SEARCH_GAME, 0) + 1;
        sp.edit().putInt(PREF_SEARCH_GAME, score).commit();

        String myStart = binding.tvStart.getText().toString();
        String myArrival = binding.tvStop.getText().toString();

        String lang = getString(R.string.url_lang);
        if (settings.getBoolean(PREF_NL, false))
            lang = "NL";

        String timeSel = "depart";
        if (settings.getString(getString(R.string.key_planner_da), "1")
                .contentEquals("2"))
            timeSel = "arrive";

        String url = buildSearchUrl(myStart, myArrival, lang, timeSel);
        Log.e("CVE", "Search " + url);

        viewModel.search(url);
    }

    private String buildSearchUrl(String start, String arrival, String lang, String timeSel) {
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

        return android.net.Uri.parse("https://api.irail.be/v1/connections/")
                .buildUpon()
                .appendQueryParameter("to", arrival)
                .appendQueryParameter("from", start)
                .appendQueryParameter("date", day + month + year)
                .appendQueryParameter("time", hour + minutes)
                .appendQueryParameter("timeSel", timeSel)
                .appendQueryParameter("lang", lang)
                .appendQueryParameter("typeOfTransport", "train")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("fast", "true")
                .appendQueryParameter("alerts", "true")
                .build()
                .toString();
    }

    private void showDateTimeDialog() {

        DateTimePicker.show(requireContext(), this);
    }

    private void updateActionBar() {
        try {
            String date = Utils.formatDate(mDate.getTime(), abDatePattern) + " - "
                    + Utils.formatDate(mDate.getTime(), abTimePattern);
            String capitalized = date.substring(0, 1).toUpperCase() + date.substring(1);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(capitalized);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompositionNeeded(String vehicleId) {
        if (viewModel != null) {
            viewModel.loadComposition(vehicleId);
        }
    }

    private void showErrorDialog(String error) {
        String title = "Error";
        StringBuilder html = new StringBuilder();

        try {
            // Strip Java exception prefix
            String msg = error;
            if (msg.contains("IOException:")) {
                msg = msg.substring(msg.indexOf("IOException:") + "IOException:".length()).trim();
            }

            // Extract HTTP code
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("HTTP\\s*(\\d+)").matcher(msg);
            if (m.find()) {
                title = "HTTP " + m.group(1);
                msg = msg.substring(m.end()).trim();
                if (msg.startsWith(":"))
                    msg = msg.substring(1).trim();
            }

            // Check for HTML first (CSS braces would confuse JSON detection)
            if (msg.contains("<!DOCTYPE") || msg.contains("<html")) {
                // Inject dark mode CSS
                String darkCss = "<style>@media(prefers-color-scheme:dark){body{color:#fff!important;background:transparent!important}*{color:inherit!important}}</style>";
                String styledMsg = msg.replaceFirst("(<head[^>]*>)", "$1" + darkCss);
                if (styledMsg.equals(msg))
                    styledMsg = darkCss + msg;

                android.webkit.WebView webView = new android.webkit.WebView(requireContext());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    webView.getSettings().setForceDark(android.webkit.WebSettings.FORCE_DARK_AUTO);
                }
                webView.loadDataWithBaseURL(null, styledMsg, "text/html", "UTF-8", null);
                webView.setBackgroundColor(0x00000000);

                // Wrap in a layout with a header label
                android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
                layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                layout.setPadding(48, 24, 48, 0);

                android.widget.TextView header = new android.widget.TextView(requireContext());
                header.setText("Response from iRail.be (" + title + "):");
                header.setTextSize(12);
                header.setAlpha(0.7f);
                header.setPadding(0, 0, 0, 16);
                layout.addView(header);

                int heightPx = (int) (300 * getResources().getDisplayMetrics().density);
                webView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, heightPx));
                layout.addView(webView);

                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setIcon(R.drawable.ic_alert)
                        .setTitle("iRail.be")
                        .setView(layout)
                        .setPositiveButton("OK", null)
                        .show();
                return;
            } else {
                // Try JSON
                int jsonStart = msg.indexOf('{');
                if (jsonStart >= 0) {
                    String jsonStr = msg.substring(jsonStart);
                    org.json.JSONObject json = new org.json.JSONObject(jsonStr);
                    java.util.Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (key.equals("stackTrace") || key.equals("at"))
                            continue;
                        String label = key.substring(0, 1).toUpperCase()
                                + key.substring(1).replaceAll("([A-Z])", " $1").trim();
                        html.append("<b>").append(label).append("</b><br/>")
                                .append(json.getString(key)).append("<br/><br/>");
                    }
                } else if (!msg.isEmpty()) {
                    html.append(msg);
                } else {
                    html.append("An unexpected error occurred.");
                }
            }
        } catch (Exception e) {
            html.append(error);
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(Html.fromHtml(html.toString().trim(), Html.FROM_HTML_MODE_COMPACT))
                .setPositiveButton("OK", null)
                .show();
    }
}
