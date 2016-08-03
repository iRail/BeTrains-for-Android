package tof.cv.mpp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import tof.cv.mpp.adapter.CompensationAdapter;


public class CompensationFragment extends ListFragment {
    protected static final String TAG = "CompensationFragment";
    CompensationAdapter a;
    private TextView mTitleText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compensation, null);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, "Web")
                .setIcon(R.drawable.ic_menu_goto)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (0):
                String url = getString(R.string.compensation_url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
        // getActivity().getActionBar().setIcon(R.drawable.ab_sncb);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);

        boolean isTablet = this.getActivity().getResources().getBoolean(R.bool.tablet_layout);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(
                !isTablet);

        mTitleText = (TextView) getView().findViewById(R.id.title);

    }

    public void onResume() {
        super.onResume();
        updateList();
    }

    public void updateList() {

        String[] f = getActivity().getDir("COMPENSATION", Context.MODE_PRIVATE).list();
        Arrays.sort(f);

        ArrayList<String> arraylist = new ArrayList<String>(Arrays.asList(f));
        a = new CompensationAdapter(getActivity(),
                android.R.layout.simple_list_item_1, arraylist);
        setListAdapter(a);

        if (f.length == 0) {
            WebView mywebview = (WebView) getView().findViewById(android.R.id.empty);
            String data = this.getActivity().getString(R.string.compensation_html);
            mywebview.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "UTF-8", null);
            mTitleText.setVisibility(View.GONE);
        } else {
            mTitleText.setVisibility(View.VISIBLE);
            try {
                long elapsed = System.currentTimeMillis() - Long.valueOf(f[0].split(";")[0]);
                mTitleText.setText(getResources().getQuantityString(R.plurals.compensation_days, (int) (elapsed / DateUtils.DAY_IN_MILLIS), (int) (elapsed / DateUtils.DAY_IN_MILLIS)));
                //mTitleText.setText("Retards depuis le: "+Utils.formatDate(new Date(Long.valueOf(f[0].split(";")[0])-4*DateUtils.DAY_IN_MILLIS),"d MMMMM"));

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] items = {getString(R.string.compensation_show_info), getString(R.string.compensation_edit_delay), getString(R.string.compensation_edit_text), getString(R.string.remove)};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {

                    switch (which) {
                        case 0:
                            String[] all=getListAdapter().getItem(position).toString().split(";");
                            Intent i = new Intent(getActivity(), InfoTrainActivity.class);
                            String id = getListAdapter().getItem(position).toString().split(";")[3];
                            i.putExtra("FileName", getListAdapter().getItem(position).toString());
                            i.putExtra("Name",all[all.length-1] );
                            startActivity(i);
                            break;
                        case 1:

                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            final EditText input = new EditText(getActivity());
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                            alert.setView(input);
                            input.setText(getListAdapter().getItem(position).toString().split(";")[1]);
                            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    File directory = getActivity().getDir("COMPENSATION", Context.MODE_PRIVATE);
                                    File from = new File(directory, getListAdapter().getItem(position).toString());
                                    String newDate = getListAdapter().getItem(position).toString().split(";")[0];
                                    String newDelay = input.getText().toString().replaceAll(";", ",");
                                    String newText = getListAdapter().getItem(position).toString().split(";")[2];
                                    String newId = getListAdapter().getItem(position).toString().split(";")[3];
                                    File to = new File(directory, newDate + ";" + newDelay + ";" + newText + ";" + newId);
                                    from.renameTo(to);

                                    updateList();
                                }
                            });

                            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });

                            alert.show();

                            break;
                        case 2:

                            AlertDialog.Builder alert2 = new AlertDialog.Builder(getActivity());
                            final EditText input2 = new EditText(getActivity());
                            alert2.setView(input2);
                            input2.setText(getListAdapter().getItem(position).toString().split(";")[2]);
                            alert2.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    File directory = getActivity().getDir("COMPENSATION", Context.MODE_PRIVATE);
                                    File from = new File(directory, getListAdapter().getItem(position).toString());

                                    String newDate = getListAdapter().getItem(position).toString().split(";")[0];
                                    String newDelay = getListAdapter().getItem(position).toString().split(";")[1];
                                    String newText = input2.getText().toString().replaceAll(";", ",");
                                    String newId = getListAdapter().getItem(position).toString().split(";")[3];

                                    File to = new File(directory, newDate + ";" + newDelay + ";" + newText + ";" + newId);
                                    from.renameTo(to);

                                    updateList();
                                }
                            });

                            alert2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });

                            alert2.show();

                            break;
                        case 3:
                            File directory = getActivity().getDir("COMPENSATION", Context.MODE_PRIVATE);
                            new File(directory, getListAdapter().getItem(position).toString())
                                    .delete();
                            updateList();
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        builder.create().show();
    }

}
