package tof.cv.mpp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.DownloadLastMessageTask;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.adapter.TrainInfoAdapter;
import tof.cv.mpp.bo.Message;
import tof.cv.mpp.widget.TrainAppWidgetProvider;
import tof.cv.mpp.widget.TrainWidgetProvider;

public class InfoTrainFragment extends ListFragment {
    protected static final String TAG = "ChatFragment";
    private UtilsWeb.Vehicle currentVehicle;
    private TextView mTitleText;
    private TextView mMessageText;
    private String fromTo;
    String id;
    private long timestamp;
    private ArrayList<Message> mSaveMessageList = new ArrayList<Message>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_train, null);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTitleText = (TextView) getActivity().findViewById(R.id.title);
        mMessageText = (TextView) getActivity().findViewById(R.id.last_message);

        mMessageText.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (currentVehicle != null) {
                    Bundle bundle = new Bundle();

                    bundle.putString(DbAdapterConnection.KEY_NAME,
                            currentVehicle.getId());
                    Intent mIntent = new Intent(v.getContext(),
                            ChatActivity.class);
                    mIntent.putExtras(bundle);
                    startActivityForResult(mIntent, 0);
                }
            }
        });

        Log.i("'", "" + mMessageText);
        registerForContextMenu(getListView());
    }

    public void displayInfo(final String fileName, final String vehicle) {

        this.timestamp = System.currentTimeMillis();
        mMessageText = (TextView) getActivity().findViewById(R.id.last_message);
        if (PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                .getBoolean("preffirstM", false)) {
            new DownloadLastMessageTask(this).execute(vehicle);
            setLastMessageText(getString(R.string.txt_load_message));
        } else
            mMessageText.setText(Html.fromHtml("<small>"
                    + getText(R.string.txt_infrabel) + "</small>"));

        Runnable trainSearch = new Runnable() {
            public void run() {
                currentVehicle = UtilsWeb.getMemoryvehicle(fileName, InfoTrainFragment.this.getActivity());
                if (getActivity() != null)
                    getActivity().runOnUiThread(displayResult);
            }
        };
        Thread thread = new Thread(null, trainSearch, "MyThread");
        thread.start();
    }

    public void displayInfo(String vehicle, String fromTo, long timestamp) {
        this.id = vehicle;
        this.fromTo = fromTo;
        if (timestamp != 0)
            this.timestamp = timestamp;
        else
            this.timestamp = System.currentTimeMillis();
        mMessageText = (TextView) getActivity().findViewById(R.id.last_message);
        if (PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                .getBoolean("preffirstM", false)) {
            new DownloadLastMessageTask(this).execute(vehicle);
            setLastMessageText(getString(R.string.txt_load_message));
        } else
            mMessageText.setText(Html.fromHtml("<small>"
                    + getText(R.string.txt_infrabel) + "</small>"));

        myTrainSearchThread(vehicle, timestamp);
    }



    private void myTrainSearchThread(final String vehicle, final long timestamp) {
        Runnable trainSearch = new Runnable() {
            public void run() {
                currentVehicle = UtilsWeb.getAPIvehicle(vehicle, getActivity(),
                        timestamp);
                if (getActivity() != null)
                    getActivity().runOnUiThread(displayResult);
            }
        };
        Thread thread = new Thread(null, trainSearch, "MyThread");
        thread.start();
    }

    private Runnable displayResult = new Runnable() {
        public void run() {

            if (currentVehicle != null
                    && currentVehicle.getVehicleStops() != null) {
               TrainInfoAdapter trainInfoAdapter = new TrainInfoAdapter(
                        getActivity(), R.layout.row_info_train, currentVehicle
                        .getVehicleStops().getVehicleStop());
                setListAdapter(trainInfoAdapter);
                setTitle(Utils.formatDate(new Date(timestamp), "dd MMM HH:mm"));
            } else {
                Toast.makeText(getActivity(), R.string.txt_connection,
                        Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    };

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, "Add to Widget")
                .setIcon(R.drawable.ic_menu_save)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(Menu.NONE, 1, Menu.NONE, "Fav")
                .setIcon(R.drawable.ic_menu_star)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

       // menu.add(Menu.NONE, 2, Menu.NONE, "Map")
       //         .setIcon(R.drawable.ic_menu_mapmode)
       //         .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu.add(Menu.NONE, 4, Menu.NONE, R.string.btn_home_compensate)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu.add(Menu.NONE, 3, Menu.NONE, "Chat")
                .setIcon(R.drawable.ic_menu_start_conversation)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                widget();
                return true;
            case 1:
                if (currentVehicle != null) {
                    Utils.addAsStarred(currentVehicle.getId(), "", 2, getActivity());
                    startActivity(new Intent(getActivity(), StarredActivity.class));
                }
                return true;
            case 2:
              /*  if (currentVehicle != null) {
                    Intent i = new Intent(getActivity(), MapVehicleActivity.class);
                    i.putExtra("Name", currentVehicle.getId());
                    startActivity(i);
                }*/
                return true;
            case 3:
                if (currentVehicle != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(DbAdapterConnection.KEY_NAME,
                            currentVehicle.getId());
                    Intent mIntent = new Intent(getActivity(), ChatActivity.class);
                    mIntent.putExtras(bundle);
                    startActivityForResult(mIntent, 0);
                    return true;
                }

            case 4:
                if (currentVehicle != null) {
                    new Thread(new Runnable() {
                        public void run() {
                            saveToSd();
                        }
                    }).start();
                }

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveToSd() {

        int maxDelay = 0;
        for (UtilsWeb.VehicleStop aStop : currentVehicle.getVehicleStops().getVehicleStop()) {
            if (Integer.valueOf(aStop.getDelay()) > maxDelay)
                maxDelay = Integer.valueOf(aStop.getDelay());
        }

        FileOutputStream f;
        Context context = getActivity();
        File myFile = new File(getActivity().getDir("COMPENSATION", Context.MODE_PRIVATE), "" + System.currentTimeMillis() + ";" + (maxDelay / 60) + ";;" + id);
        Log.i("", "" + myFile.exists());
        if (myFile.exists())
            myFile.delete();

        try {
            f = new FileOutputStream(myFile);

            String langue = context.getString(R.string.url_lang);
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    "prefnl", false))
                langue = "nl";
            String dateTime = "";
            if (timestamp != 0) {
                String formattedDate = Utils.formatDate(new Date(timestamp),
                        "ddMMyy");
                String formattedTime = Utils
                        .formatDate(new Date(timestamp), "HHmm");
                dateTime = "&date=" + formattedDate + "&time=" + formattedTime;
            }

            String url = "http://api.irail.be/vehicle.php/?id=" + id
                    + "&lang=" + langue + dateTime + "&format=JSON&fast=true";

            InputStream in = UtilsWeb.retrieveStream(url, getActivity());

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
            Intent i = new Intent(getActivity(), CompensationActivity.class);
            startActivity(i);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void widget() {

        AlertDialog.Builder ad;
        ad = new AlertDialog.Builder(getActivity());
        ad.setTitle(R.string.wid_confirm);
        ad.setPositiveButton(android.R.string.ok,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        final DbAdapterConnection mDbHelper = new DbAdapterConnection(
                                getActivity());
                        mDbHelper.open();
                        mDbHelper.deleteAllWidgetStops();
                        if (fromTo == null)
                            fromTo = currentVehicle.getVehicleStops()
                                    .getVehicleStop().get(0).getStation()
                                    + " - "
                                    + currentVehicle
                                    .getVehicleStops()
                                    .getVehicleStop()
                                    .get(currentVehicle
                                            .getVehicleStops()
                                            .getVehicleStop().size() - 1).getStation();

                        mDbHelper.createWidgetStop(currentVehicle.getId()
                                .replace("BE.NMBS.", ""), "1", Utils
                                .formatDateWidget(new Date()), fromTo);

                        for (UtilsWeb.VehicleStop oneStop : currentVehicle
                                .getVehicleStops().getVehicleStop())
                            mDbHelper.createWidgetStop(oneStop.getStation(), ""
                                    + oneStop.getTime(), oneStop.getDelay(),
                                    oneStop.getStatus());

                        mDbHelper.close();

                        Intent intent = new Intent(
                             TrainAppWidgetProvider.TRAIN_WIDGET_UPDATE);
                        getActivity().sendBroadcast(intent);

                        intent = new Intent(TrainWidgetProvider.UPDATE_ACTION);
                        getActivity().sendBroadcast(intent);

                        Toast.makeText(getActivity(),
                                getString(R.string.wid_added, ""),
                                Toast.LENGTH_SHORT).show();

                    }
                });

        ad.setNegativeButton(android.R.string.no,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                    }
                });
        if (currentVehicle != null)
            ad.show();

    }

    public void setTitle(String txt) {
        mTitleText.setText(txt);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        UtilsWeb.VehicleStop clicked = (UtilsWeb.VehicleStop) getListAdapter().getItem(
                (int) info.id);

        menu.add(0, 0, 0, clicked.getStation());
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
                        .getMenuInfo();
                UtilsWeb.VehicleStop stop = (UtilsWeb.VehicleStop) getListAdapter().getItem(
                        (int) menuInfo.id);
                Intent i = new Intent(getActivity(), InfoStationActivity.class);
                i.putExtra("Name", stop.getStation());
                i.putExtra("timestamp", stop.getTime());
                startActivity(i);

                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    public void setLastMessageText(Spanned spanned) {
        mMessageText.setText(spanned);
    }

    public void setLastMessageText(String text) {
        mMessageText.setText(text);
    }

    public void addMessage(Message message) {
        this.mSaveMessageList.add(message);
    }

}
