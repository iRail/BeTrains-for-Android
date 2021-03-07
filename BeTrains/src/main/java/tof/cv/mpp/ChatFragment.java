package tof.cv.mpp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import tof.cv.mpp.MyPreferenceActivity.Prefs1Fragment;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.adapter.MessageViewHolder;
import tof.cv.mpp.bo.Message;
import tof.cv.mpp.view.CoolEditText;
import tof.cv.mpp.view.LetterTileProvider;

public class ChatFragment extends Fragment {
    /**
     * Called when the activity is first created.
     */
    FirebaseRecyclerAdapter mFirebaseAdapter;
    private TextView mTitleText;
    private Button btnSettings;
    private Button btnSend;
    private CoolEditText messageTxtField;
    private final String TAG = "MessagesTrain.java";
    private boolean posted = false;
    String trainId;
    DatabaseReference ref;
    Resources res;
    int tileSize;


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
        messageTxtField = getView().findViewById(
                R.id.yourmessage);

        setBtnSettingsListener();
        setBtnSendListener();
        update();

        boolean isTablet = this.getActivity().getResources().getBoolean(R.bool.tablet_layout);

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(
                    !isTablet);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_drawer_chat);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                            .getString("prefname", "Anonymous");
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
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        String pic = "";
        if (getContext() != null && !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("hidepic", false))
            pic = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("profilepic", "");

        String user_id = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("prefmail", "");
        boolean donator = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("donator", false);
        boolean beta = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("beta", false);


        if (user_id.endsWith("@cloudtestlabaccounts.com"))
            Toast.makeText(getActivity(), "Hello Bot", Toast.LENGTH_LONG).show();
        else
            ref.push().setValue(new Message(pseudo, messageTxtField.getText().toString(), formattedDate, trainId, pic, user_id, donator, beta));

        update();
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        messageTxtField.setText("");
        messageTxtField.clearFocus();

    }


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
        // Log.e("CVE", "TRAIN: " + trainId);
        final RecyclerView mMessageRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerview);

        ref = FirebaseDatabase.getInstance().getReference().child("chat").getRef();
        Query ref2 = trainId == null ? ref.limitToLast(99) : ref.orderByChild("train_id")
                .equalTo(trainId).limitToLast(99);

        res = getResources();
        tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message,
                MessageViewHolder>(
                Message.class,
                R.layout.row_message,
                MessageViewHolder.class,
                ref2) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder,
                                              final Message message, int position) {
                viewHolder.getNickname().setText(message.getUser_name());

                if (message.getUser_message() != null & message.getUser_message().contains("http")) {
                    List<String> extractedUrls = extractUrls(message.getUser_message());

                    for (String url : extractedUrls) {

                        if (url.endsWith(".gif")) {
                            message.setUserMessage(message.getUser_message().replace(url, ""));
                            Glide.with(viewHolder.image).load(url).into(viewHolder.image);
                            break;
                        }
                    }
                } else
                    viewHolder.image.setImageDrawable(null);

                viewHolder.getMessagebody().setText(message.getUser_message());

                if (message.getEntry_date().contains(":"))
                    viewHolder.getTime().setText(message.getEntry_date().substring(0, message.getEntry_date().lastIndexOf(":")));
                else
                    viewHolder.getTime().setText(message.getEntry_date());

                viewHolder.getTrainid().setText(message.getTrain_id());

                if (message.beta)
                    viewHolder.beta.setVisibility(View.VISIBLE);
                else
                    viewHolder.beta.setVisibility(View.GONE);

                if (message.donator)
                    viewHolder.donator.setVisibility(View.VISIBLE);
                else
                    viewHolder.donator.setVisibility(View.GONE);

                if (message.pic_url != null && message.pic_url.length() > 0)
                    Picasso.with(getContext()).load(message.pic_url).into(viewHolder.iv);
                else {

                    final LetterTileProvider tileProvider = new LetterTileProvider(getContext());
                    final Bitmap letterTile = tileProvider.getLetterTile(message.getUser_name(), message.getUser_name(), tileSize, tileSize);
                    viewHolder.iv.setImageBitmap(letterTile);
                }


                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                        if (trainId == null) {
                            Bundle bundle = new Bundle();
                            bundle.putString(DbAdapterConnection.KEY_NAME,
                                    message.getTrain_id());
                            Intent mIntent = new Intent(getContext(),
                                    ChatActivity.class);
                            mIntent.putExtras(bundle);
                            startActivityForResult(mIntent, 0);
                        } else {
                            Intent i = new Intent(getActivity(),
                                    InfoTrainActivity.class);

                            i.putExtra(DbAdapterConnection.KEY_NAME,
                                    message.getTrain_id());

                            startActivity(i);
                        }
                    }
                });
            }

            public List<String> extractUrls(String text) {
                List<String> containedUrls = new ArrayList<String>();
                String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
                Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
                Matcher urlMatcher = pattern.matcher(text);

                while (urlMatcher.find()) {
                    containedUrls.add(text.substring(urlMatcher.start(0),
                            urlMatcher.end(0)));
                }

                return containedUrls;
            }

            @Override
            protected void onDataChanged() {
                if (getActivity() == null)
                    return;

                int itemCount = mMessageRecyclerView.getAdapter().getItemCount();

                TextView messagesEmpty = (TextView) getActivity().findViewById(
                        R.id.emptychat);

                if (itemCount > 0) {
                    if (getActivity() instanceof InfoTrainActivity)
                        ((InfoTrainActivity) getActivity()).setChatBadge(itemCount);
                    if (messagesEmpty != null)
                        messagesEmpty.setVisibility(View.GONE);
                } else if (messagesEmpty != null) {
                    messagesEmpty.setVisibility(View.VISIBLE);
                    messagesEmpty.setText(R.string.chat_no_message);
                }
            }
        };

        AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                Log.e("CVEADAPTER", "onItemRangeChanged ");
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                Log.e("CVEADAPTER", "onItemRangeMoved ");
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                Log.e("CVEADAPTER", "onItemRangeChanged ");
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                Log.e("CVEADAPTER", "onItemRangeRemoved ");
            }

        };

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mFirebaseAdapter.registerAdapterDataObserver(mObserver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFirebaseAdapter.cleanup();
    }

    public void onResume() {
        super.onResume();
        if (trainId != null)
            mTitleText.setText(PreferenceManager.getDefaultSharedPreferences(
                    getActivity()).getString("prefname", "Anonymous")
                    + " - " + trainId);
        else
            mTitleText.setText(PreferenceManager.getDefaultSharedPreferences(
                    getActivity()).getString("prefname", "Anonymous"));

        LinearLayout mSendLayout = (LinearLayout) getView().findViewById(
                R.id.send_layout);

        if (trainId == null) {
            mSendLayout.setVisibility(View.GONE);
        }

        update();
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