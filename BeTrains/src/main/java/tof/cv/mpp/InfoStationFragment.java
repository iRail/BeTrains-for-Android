package tof.cv.mpp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Date;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.adapter.StationInfoAdapter;
import tof.cv.mpp.view.NotifyingScrollView;


public class InfoStationFragment extends ListFragment {
    protected static final String TAG = "InfoStationFragment";
    private UtilsWeb.Station currentStation;
    private TextView mTitleText;
    private long timestamp;
    private String stationString;
    private String id;
    private Target t = new Target() {
        public void onBitmapLoaded(Bitmap bitmapPic, Picasso.LoadedFrom from) {
            try {
                ((ImageView) getView().findViewById(R.id.image_header)).setImageBitmap(bitmapPic);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void onBitmapFailed(Drawable errorDrawable) {
            try {//Strange bugs in console To investigate
                getView().findViewById(R.id.Button_pic).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.image_header).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((InfoStationActivity) getActivity()).pic(null);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }

    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_station, null);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTitleText = (TextView) getView().findViewById(R.id.title);
        //registerForContextMenu(getListView());

        final ImageButton prevButton = (ImageButton) getActivity()
                .findViewById(R.id.Button_prev);
        final ImageButton nextButton = (ImageButton) getActivity()
                .findViewById(R.id.Button_next);
        nextButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

               // pd = ProgressDialog.show(getActivity(), "",
               //         getString(R.string.txt_patient), true);

                timestamp += (60 * 60 * 1000);
                searchThread();
            }
        });

        prevButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

               // pd = ProgressDialog.show(getActivity(), "",
               //         getString(R.string.txt_patient), true);

                timestamp -= (60 * 60 * 1000);
                searchThread();
            }
        });

        setHasOptionsMenu(true);

    }


    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        UtilsWeb.StationDeparture stop = (UtilsWeb.StationDeparture) getListAdapter()
                .getItem(position);
        Intent i = new Intent(getActivity(), InfoTrainActivity.class);
        i.putExtra("Name", stop.getVehicle());
        i.putExtra("timestamp", stop.getTime());
        startActivity(i);

    }


    public void displayInfo(String station, long timestamp, String id) {
        if (timestamp != 0)
            this.timestamp = timestamp;
        else
            this.timestamp = System.currentTimeMillis();

        this.stationString = station;

        this.id = id;

        searchThread();
    }

    private void searchThread() {
        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        Runnable search = new Runnable() {
            public void run() {
                currentStation = UtilsWeb.getAPIstation(id, stationString,
                        timestamp, getActivity());
                if (getActivity() != null)
                    getActivity().runOnUiThread(displayResult);
            }
        };
        Thread thread = new Thread(null, search, "stationSearch");
        thread.start();
    }

    private Runnable displayResult = new Runnable() {
        public void run() {
            getView().findViewById(R.id.progress).setVisibility(View.GONE);
           // if (pd != null)
           //     pd.dismiss();
            if (currentStation != null)
                if (currentStation.getStationDepartures() != null) {

                    if (id != null)
                        //Picasso.with(InfoStationFragment.this.getActivity()).load("http://wazabe.byethost8.com/" + currentStation.getStationStationinfo().getId().replace("BE.NMBS.", "") + ".jpg").error(R.drawable.gare).placeholder(R.drawable.gare).into(t);
                        Picasso.with(InfoStationFragment.this.getActivity()).load("http://res.cloudinary.com/dywgd02hq/image/upload/" + currentStation.getStationStationinfo().getId().replace("BE.NMBS.", "") + ".jpg").error(R.drawable.gare).placeholder(R.drawable.gare).into(t);

                    stationString = currentStation.getStation();

                    StationInfoAdapter StationInfoAdapter = new StationInfoAdapter(
                            getActivity(), R.layout.row_info_station,
                            currentStation.getStationDepartures()
                                    .getStationDeparture()
                    );
                    setListAdapter(StationInfoAdapter);
                    setTitle(Utils.formatDate(new Date(timestamp),
                            "dd MMM HH:mm"));
                } else {

                    Toast.makeText(getActivity(), R.string.txt_no_result,
                            Toast.LENGTH_LONG).show();
                    setTitle(Utils.formatDate(new Date(timestamp),
                            "dd MMM HH:mm"));

                }
            else {

                Toast.makeText(getActivity(), R.string.txt_connection,
                        Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    };

    public void setTitle(String txt) {
        getActivity().setTitle(stationString);
        mTitleText.setText(txt);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, "Fav")
                .setIcon(R.drawable.ic_menu_star)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        //menu.add(Menu.NONE, 1, Menu.NONE, "Map")
        //		.setIcon(android.R.drawable.ic_menu_mapmode)
        //		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                if (currentStation != null) {
                    Utils.addAsStarred(currentStation.getStation(), currentStation.getStationStationinfo().getId(), 1,
                            getActivity());
                    startActivity(new Intent(getActivity(), StarredActivity.class));
                }
                return true;
            case 1:
            /*if (currentStation != null) {
                Intent i = new Intent(getActivity(), MapStationActivity.class);
				i.putExtra("Name", currentStation.getStation());
				i.putExtra("lat", currentStation.getStationStationinfo()
						.getLocationY());
				i.putExtra("lon", currentStation.getStationStationinfo()
						.getLocationX());
				startActivity(i);
			}*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        UtilsWeb.StationDeparture clicked = (UtilsWeb.StationDeparture) getListAdapter().getItem(
                (int) info.id);

        menu.add(0, 0, 0, clicked.getVehicle());
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                UtilsWeb.StationDeparture stop = (UtilsWeb.StationDeparture) getListAdapter()
                        .getItem((int) menuInfo.id);
                Intent i = new Intent(getActivity(), InfoTrainActivity.class);
                i.putExtra("Name", stop.getVehicle());
                i.putExtra("timestamp", stop.getTime());
                startActivity(i);
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

}
