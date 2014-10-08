package tof.cv.mpp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.adapter.FavAdapter;

public class StarredFragment extends ListFragment {
    protected static final String TAG = "StarredFragment";
    private static DbAdapterConnection mDbHelper;
    private Cursor mCursor;
    private static final int REMOVE_ID = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_starred, null);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mDbHelper = new DbAdapterConnection(getActivity());

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
        getActivity().getActionBar().setIcon(R.drawable.ab_starred);
        getActivity().getActionBar().setSubtitle(null);
    }

    public void onResume() {
        super.onResume();
        populateList();
    }

    public void populateList() {
        mDbHelper.open();
        mCursor = mDbHelper.fetchAllFav();
        FavAdapter fAdapter = new FavAdapter(getActivity(), mCursor);
        setListAdapter(fAdapter);
        mDbHelper.close();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        mCursor.moveToPosition(position);
        String item = mCursor.getString(mCursor
                .getColumnIndex(DbAdapterConnection.KEY_FAV_NAME));
        String itemTwo = mCursor.getString(mCursor
                .getColumnIndex(DbAdapterConnection.KEY_FAV_NAMETWO));
        int type = mCursor.getInt(mCursor
                .getColumnIndex(DbAdapterConnection.KEY_FAV_TYPE));
        Intent i;
        switch (type) {
            case 1:
                i = new Intent(getActivity(), InfoStationActivity.class);
                i.putExtra("Name", item);
                i.putExtra("ID", itemTwo);
                startActivity(i);
                break;
            case 2:
                i = new Intent(getActivity(), InfoTrainActivity.class);
                i.putExtra("Name", item);
                startActivity(i);
                break;
            case 3:
                i = new Intent(getActivity(), WelcomeActivity.class);
                i.putExtra("Departure", item);
                i.putExtra("Arrival", itemTwo);
                startActivity(i);
                getActivity().finish();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, REMOVE_ID, 0, R.string.txt_remove);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case REMOVE_ID:

                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
                        .getMenuInfo();
                mDbHelper.open();
                mDbHelper.deleteFav(menuInfo.id);
                mDbHelper.close();
                populateList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }
}
