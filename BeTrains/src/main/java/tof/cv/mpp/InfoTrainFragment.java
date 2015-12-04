package tof.cv.mpp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.ShareCompat;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.TrainInfoAdapter;
import tof.cv.mpp.bo.Message;
import tof.cv.mpp.bo.Vehicle;
import tof.cv.mpp.widget.TrainAppWidgetProvider;
import tof.cv.mpp.widget.TrainWidgetProvider;

public class InfoTrainFragment extends ListFragment {
    protected static final String TAG = "ChatFragment";
    private Vehicle currentVehicle;
    private TextView mTitleText;
    private TextView mMessageText;
    private String fromTo;
    String id;
    private long timestamp;


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
        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        this.timestamp = System.currentTimeMillis();
        mMessageText = (TextView) getActivity().findViewById(R.id.last_message);
        if (PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                .getBoolean("preffirstM", true)) {
            displayLastMessage(vehicle);
        }

        Runnable trainSearch = new Runnable() {
            public void run() {
                currentVehicle = Utils.getMemoryvehicle(fileName, InfoTrainFragment.this.getActivity());
                if (getActivity() != null)
                    getActivity().runOnUiThread(displayResult);
            }
        };
        Thread thread = new Thread(null, trainSearch, "MyThread");
        thread.start();
    }

    public void displayInfo(String vehicle, String fromTo, long timestamp) {
        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        this.id = vehicle;
        this.fromTo = fromTo;
        if (timestamp != 0)
            this.timestamp = timestamp;
        else
            this.timestamp = System.currentTimeMillis();
        mMessageText = (TextView) getActivity().findViewById(R.id.last_message);
        if (PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                .getBoolean("preffirstM", true)) {
            displayLastMessage(vehicle);

        }

        myTrainSearch(vehicle);
    }

    private void displayLastMessage(String vehicle) {
        mMessageText.setVisibility(View.VISIBLE);
        setLastMessageText(getString(R.string.txt_load_message));
        Ion.with(this).load("http://christophe.frandroid.com/betrains/php/messages.php")
                .setBodyParameter("id", "hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a")
                .setBodyParameter("message_count", "" + 1)
                .setBodyParameter("message_index", "" + 0)
                .setBodyParameter("mode", "read")
                .setBodyParameter("order", "DESC")
                .setBodyParameter("train_id", vehicle)
                .asString(Charset.forName("ISO-8859-1")).setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String txt) {
                // TODO: USE XML PARSER
                ArrayList<Message> messageList = new ArrayList<>();
                if (txt != null && !txt.equals("")) {
                    String[] messages = txt.split("<message>");

                    int i = 1;
                    if (messages.length > 1) {
                        while (i < messages.length) {
                            String[] params = messages[i].split("CDATA");
                            for (int j = 1; j < params.length; j++) {
                                params[j] = params[j].substring(1,
                                        params[j].indexOf("]"));

                            }
                            Log.e(TAG, "messages: " + params[1] + " " + params[2] + " "
                                    + params[3] + " " + params[4]);
                            messageList.add(new Message(params[1], params[2],
                                    params[3], params[4]));
                            i++;
                        }

                    }

                }
                if (messageList != null && messageList.size() > 0) {
                    Log.i(TAG, "count= " + messageList.size());
                    Message result = messageList.get(0);
                    setLastMessageText(Html.fromHtml(result.getauteur()
                            + ": " + result.getbody() + "<br />" + "<small>"
                            + result.gettime() + "</small>"));
                } else
                    mMessageText.setVisibility(View.GONE);
            }
        });
    }


    private void myTrainSearch(final String vehicle) {

        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        String dateTime = "";
        if (timestamp != 0) {
            String formattedDate = tof.cv.mpp.Utils.Utils.formatDate(new Date(timestamp),
                    "ddMMyy");
            String formattedTime = tof.cv.mpp.Utils.Utils
                    .formatDate(new Date(timestamp), "HHmm");
            dateTime = "&date=" + formattedDate + "&time=" + formattedTime;
        }

        final String url = "http://api.irail.be/vehicle.php/?id=" + vehicle
                + "&lang=" + getString(R.string.url_lang) + dateTime + "&format=JSON";//&fast=true";
        Log.e("CVE", url);
        Ion.with(this).load(url).as(new TypeToken<Vehicle>() {
        }).withResponse().setCallback(new FutureCallback<Response<Vehicle>>() {
            @Override
            public void onCompleted(Exception e, Response<Vehicle> result) {
                currentVehicle = result.getResult();
                getView().findViewById(R.id.progress).setVisibility(View.GONE);
                getView().findViewById(android.R.id.empty).setVisibility(View.GONE);
                if (currentVehicle != null
                        && currentVehicle.getVehicleStops() != null) {
                    TrainInfoAdapter trainInfoAdapter = new TrainInfoAdapter(
                            getActivity(), R.layout.row_info_train, currentVehicle
                            .getVehicleStops().getVehicleStop());
                    setListAdapter(trainInfoAdapter);
                    //timestamp=currentVehicle.getTimestamp();
                    setTitle(Utils.formatDate(new Date(timestamp), "dd MMM HH:mm"));
                } else {
                    if (e != null) {
                        Toast.makeText(getActivity(), e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    } else {
                        if (result.getHeaders().code() == 502) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.irailissue);
                            builder.setMessage(R.string.irailissueDetail);
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getActivity().finish();
                                }
                            });
                            builder.setNegativeButton(R.string.report, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(getActivity());
                                    builder.setType("message/rfc822");
                                    builder.addEmailTo("iRail@list.iRail.be");
                                    builder.setSubject("Issue with iRail API");
                                    builder.setText("Hello, I am currently using the Android application BeTrains, and I get an error while using the iRail API.\n\n" +
                                            "I get this message: 'Could not get data. Please report this problem to iRail@list.iRail.be' while trying to query :\n" + url + "\n\n" +
                                            "I hope you can fix that soon.\nHave a nice day.");
                                    builder.setChooserTitle("Send Email");
                                    builder.startChooser();

                                    //getActivity().finish();
                                }
                            });
                            builder.create().show();
                        }

                    }


                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            getActivity().finish();
        }
    }

    private Runnable displayResult = new Runnable() {
        public void run() {
            getView().findViewById(R.id.progress).setVisibility(View.GONE);
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
    public void onListItemClick(ListView parent, View view, int position, long id) {
        Vehicle.VehicleStop stop = (Vehicle.VehicleStop) getListAdapter().getItem(position);
        Intent i = new Intent(getActivity(), InfoStationActivity.class);
        i.putExtra("Name", stop.getStation());
        i.putExtra("ID", stop.getStationInfo().getId());
        i.putExtra("timestamp", stop.getTime());
        startActivity(i);

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
        for (Vehicle.VehicleStop aStop : currentVehicle.getVehicleStops().getVehicleStop()) {
            if (Integer.valueOf(aStop.getDelay()) > maxDelay)
                maxDelay = Integer.valueOf(aStop.getDelay());
        }

        final FileOutputStream f;
        Context context = getActivity();
        final File myFile = new File(getActivity().getDir("COMPENSATION", Context.MODE_PRIVATE), "" + System.currentTimeMillis() + ";" + (maxDelay / 60) + ";;" + id);
        Log.i("", "" + myFile.exists());
        if (myFile.exists())
            myFile.delete();

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

        final String url = "http://api.irail.be/vehicle.php/?id=" + id
                + "&lang=" + langue + dateTime + "&format=JSON&fast=true";

        Ion.with(this).load(url).asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                try {
                    FileOutputStream f = new FileOutputStream(myFile);
                    f.write(new Gson().toJson(currentVehicle).getBytes());
                    f.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Intent i = new Intent(getActivity(), CompensationActivity.class);
                startActivity(i);
            }
        });

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

                        for (Vehicle.VehicleStop oneStop : currentVehicle
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

        Vehicle.VehicleStop clicked = (Vehicle.VehicleStop) getListAdapter().getItem(
                (int) info.id);

        menu.add(0, 0, 0, clicked.getStation());
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
                        .getMenuInfo();
                Vehicle.VehicleStop stop = (Vehicle.VehicleStop) getListAdapter().getItem(
                        (int) menuInfo.id);
                Intent i = new Intent(getActivity(), InfoStationActivity.class);
                i.putExtra("Name", stop.getStation());
                i.putExtra("ID", stop.getStationInfo().getId());
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
        mMessageText.setVisibility(text.length() > 1 ? View.VISIBLE : View.GONE);

    }
}
