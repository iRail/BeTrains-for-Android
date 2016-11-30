package tof.cv.mpp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.StringPart;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import tof.cv.mpp.MyPreferenceActivity.Prefs1Fragment;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.adapter.MessageViewHolder;
import tof.cv.mpp.bo.Message;

public class ChatFragment extends Fragment {
    /**
     * Called when the activity is first created.
     */
    FirebaseRecyclerAdapter mFirebaseAdapter;
    private TextView mTitleText;
    private Button btnSettings;
    private Button btnSend;
    private EditText messageTxtField;
    private final String TAG = "MessagesTrain.java";
    private boolean posted = false;
    String trainId;
    private String toTast;
    private String toEmpty;

    private static final int MENU_FILTER = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTitleText = (TextView) getView().findViewById(R.id.pseudo);
        btnSettings = (Button) getView().findViewById(R.id.settings);
        btnSend = (Button) getView().findViewById(R.id.send);
        messageTxtField = (EditText) getView().findViewById(
                R.id.yourmessage);

        setBtnSettingsListener();
        setBtnSendListener();
        update();

        boolean isTablet = this.getActivity().getResources().getBoolean(R.bool.tablet_layout);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(
                !isTablet);

        //getActivity().getActionBar().setIcon(R.drawable.ab_chat);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);

    }

    private void setBtnSendListener() {
        btnSend.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (posted) {
                    Toast.makeText(getActivity(),
                            R.string.chat_send_err_max_messages, Toast.LENGTH_LONG)
                            .show();
                } else {
                    String pseudo = PreferenceManager
                            .getDefaultSharedPreferences(getActivity())
                            .getString("prefPseudo", "Anonymous");
                    if (pseudo.contentEquals("Anonymous"))
                        Toast.makeText(
                                getActivity(),
                                R.string.chat_send_err_username,
                                Toast.LENGTH_LONG).show();
                    else if (messageTxtField.getText().toString()
                            .contentEquals(""))
                        Toast.makeText(getActivity(), R.string.chat_send_err_empty,
                                Toast.LENGTH_LONG).show();
                    else {
                        postMessage(pseudo);
                    }
                }

            }

        });

    }

    private void postMessage(final String pseudo) {

        Runnable trainSearch = new Runnable() {

            public void run() {
                requestPhpSend(pseudo,
                        messageTxtField.getText().toString(), trainId, getActivity());
            }
        };

        Thread thread = new Thread(null, trainSearch, "MyThread");
        thread.start();

    }

    private Runnable displayToast = new Runnable() {
        public void run() {
            Toast.makeText(getActivity(), toTast, Toast.LENGTH_LONG).show();
        }
    };


    private void setBtnSettingsListener() {
        btnSettings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 11)
                    startActivity(new Intent(getActivity(),
                            MyPreferenceActivity.class).putExtra(
                            PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                            Prefs1Fragment.class.getName()));
                else {
                    startActivity(new Intent(getActivity(), MyPreferenceActivity.class));
                }
            }
        });

    }

    public void update() {
        Log.e("CVE", "TRAIN: " + trainId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("chat").getRef();


        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message,
                MessageViewHolder>(
                Message.class,
                R.layout.row_message,
                MessageViewHolder.class,
                trainId == null ? ref.limitToLast(99) : ref.orderByChild("train_id")
                        .equalTo(trainId).limitToLast(99)) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder,
                                              final Message message, int position) {
                viewHolder.getNickname().setText(message.getUser_name());

                viewHolder.getMessagebody().setText(message.getUser_message());

                if (message.getEntry_date().contains(":"))
                    viewHolder.getTime().setText(message.getEntry_date().substring(0, message.getEntry_date().lastIndexOf(":")));
                else
                    viewHolder.getTime().setText(message.getEntry_date());

                viewHolder.getTrainid().setText(message.getTrain_id());

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                        if (trainId == null) {
                            ad.setTitle(getResources().getString(
                                    R.string.chat_open_train_messages,
                                    message.getTrain_id()));
                            ad.setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {

                                            Bundle bundle = new Bundle();
                                            bundle.putString(DbAdapterConnection.KEY_NAME,
                                                    message.getTrain_id());
                                            Intent mIntent = new Intent(getContext(),
                                                    ChatActivity.class);
                                            mIntent.putExtras(bundle);
                                            startActivityForResult(mIntent, 0);

                                        }
                                    });

                            ad.setNegativeButton(android.R.string.no,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {

                                        }
                                    });
                            ad.show();
                        } else {
                            ad.setTitle(getResources().getString(R.string.chat_open_train_info,
                                    message.getTrain_id()));
                            ad.setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {

                                            Intent i = new Intent(getActivity(),
                                                    InfoTrainActivity.class);

                                            i.putExtra(DbAdapterConnection.KEY_NAME,
                                                    message.getTrain_id());

                                            startActivity(i);

                                        }
                                    });

                            ad.setNegativeButton(android.R.string.no,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int arg1) {

                                        }
                                    });
                            ad.show();
                        }
                    }
                });
            }
        };

        AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                TextView messagesEmpty = (TextView) getView().findViewById(
                        android.R.id.empty);
                //Log.e("CVE", "NUMBER " + itemCount);
                if (itemCount > 0) {
                    if (getActivity() instanceof InfoTrainActivity)
                        ((InfoTrainActivity) getActivity()).setChatBadge();
                    messagesEmpty.setVisibility(View.GONE);
                } else {
                    messagesEmpty.setVisibility(View.VISIBLE);
                    messagesEmpty.setText(R.string.chat_no_message);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                //perform check and show/hide empty view
            }
        };
        mFirebaseAdapter.registerAdapterDataObserver(mObserver);


        RecyclerView mMessageRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerview);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFirebaseAdapter.cleanup();
    }

    public void onResume() {
        super.onResume();
        Log.i("BETRAINS", "train ID= " + trainId);
        if (trainId != null)
            mTitleText.setText(PreferenceManager.getDefaultSharedPreferences(
                    getActivity()).getString("prefPseudo", "Anonymous")
                    + " - " + trainId);
        else
            mTitleText.setText(PreferenceManager.getDefaultSharedPreferences(
                    getActivity()).getString("prefPseudo", "Anonymous"));

        LinearLayout mSendLayout = (LinearLayout) getView().findViewById(
                R.id.send_layout);

        if (trainId == null) {
            mSendLayout.setVisibility(View.GONE);
        }

        update();
    }

    public boolean requestPhpSend(String pseudo, String message,
                                  String trainId, FragmentActivity a) {
        try {
            String txt = "";

            List params = new ArrayList();
            params.add(new StringPart("code",
                    "hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a"));
            params.add(new StringPart("mode", "write"));
            params.add(new StringPart("train_id", trainId));
            params.add(new StringPart("user_message", message));
            params.add(new StringPart("user_name", pseudo));

            Ion.with(a).load("http://christophe.frandroid.com/betrains/php/messages.php").addMultipartParts(params).asString().setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (result.contains("true")) {

                        toTast = getString(android.R.string.ok);
                        getActivity().runOnUiThread(displayToast);
                        try {
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("chatUnlock", true).commit();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        posted = true;
                    } else
                        toTast = "Problem";
                }
            });
            return txt.contains("true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, MENU_FILTER, Menu.NONE, "Filter")
                .setIcon(R.drawable.ic_menu_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_FILTER:
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle(R.string.chat_action_filter);
                alert.setMessage(R.string.chat_filter_message);

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alert.setView(input);

                alert.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                Bundle bundle = new Bundle();
                                bundle.putString(DbAdapterConnection.KEY_NAME,
                                        input.getText().toString());
                                Intent mIntent = new Intent(getActivity(),
                                        ChatActivity.class);
                                mIntent.putExtras(bundle);
                                startActivity(mIntent);

                            }
                        });

                alert.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });

                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}