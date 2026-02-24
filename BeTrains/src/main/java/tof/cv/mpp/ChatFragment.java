package tof.cv.mpp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.adapter.MessageViewHolder;
import tof.cv.mpp.bo.Message;
import tof.cv.mpp.view.LetterTileProvider;

public class ChatFragment extends Fragment {

    FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private TextView mTitleText;
    private FloatingActionButton btnSend;
    private EditText messageTxtField;
    String trainId;
    DatabaseReference ref;
    Resources res;
    int tileSize;
    RecyclerView mMessageRecyclerView;

    private static final int MENU_FILTER = 0;
    private static final int MENU_PROFILE = 1;

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
        mTitleText = getView().findViewById(R.id.pseudo);
        btnSend = getView().findViewById(R.id.send);
        messageTxtField = getView().findViewById(R.id.yourmessage);

        setBtnSendListener();
        update();

        boolean isTablet = this.getActivity().getResources().getBoolean(R.bool.tablet_layout);

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(
                    !isTablet);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBtnSendListener() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (messageTxtField.getText().toString().isEmpty())
                    Toast.makeText(getActivity(), R.string.chat_send_err_empty, Toast.LENGTH_LONG).show();
                else {
                    postMessage(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("prefname",
                            "Anonymous"));
                }
            }
        });
    }

    private void postMessage(final String pseudo) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(Calendar.getInstance().getTime());
        String pic = "";
        if (getContext() != null
                && !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("hidepic", false))
            pic = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("profilepic", "");

        String user_id = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("prefmail", "");
        boolean donator = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("donator", false);
        boolean beta = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("beta", false);

        ref.push().setValue(new Message(pseudo, messageTxtField.getText().toString(), formattedDate, trainId, pic,
                user_id, donator, beta));

        update();
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        messageTxtField.setText("");
        messageTxtField.clearFocus();
    }

    public void update() {
        if (trainId == null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.activity_label_chat);
        else
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("" + trainId);
        mMessageRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerview);

        ref = FirebaseDatabase.getInstance().getReference().child("chat").getRef();
        Query ref2 = trainId == null ? ref.limitToLast(99)
                : ref.orderByChild("train_id").equalTo(trainId).limitToLast(99);

        res = getResources();
        tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

        FirebaseRecyclerOptions<Message> options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(ref2, Message.class)
                .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_message, parent, false);
                return new MessageViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder viewHolder,
                    int position, @NonNull final Message message) {
                viewHolder.getNickname().setText(message.getUser_name());

                if (message.getUser_message() != null && message.getUser_message().contains("http")) {
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
                    viewHolder.getTime()
                            .setText(message.getEntry_date().substring(0, message.getEntry_date().lastIndexOf(":")));
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
                    Picasso.get().load(message.pic_url).into(viewHolder.iv);
                else {
                    final LetterTileProvider tileProvider = new LetterTileProvider(getContext());
                    final Bitmap letterTile = tileProvider.getLetterTile(message.getUser_name(), message.getUser_name(),
                            tileSize, tileSize);
                    viewHolder.iv.setImageBitmap(letterTile);
                }

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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

            @Override
            public void onDataChanged() {
                if (getActivity() == null || getView() == null)
                    return;

                int itemCount = getItemCount();

                TextView messagesEmpty = (TextView) getActivity().findViewById(
                        R.id.emptychat);

                if (itemCount > 0) {
                    if (getActivity() instanceof InfoTrainActivity)
                        ((InfoTrainActivity) getActivity()).setChatBadge(itemCount);
                    if (messagesEmpty != null) {
                        messagesEmpty.setVisibility(View.GONE);
                        getView().findViewById(R.id.recyclerview).setVisibility(View.VISIBLE);
                        getView().findViewById(R.id.send_layout).setVisibility(View.VISIBLE);
                    }
                } else if (messagesEmpty != null) {
                    messagesEmpty.setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.recyclerview).setVisibility(View.GONE);
                    getView().findViewById(R.id.send_layout).setVisibility(View.GONE);
                    messagesEmpty.setText(R.string.chat_no_message);
                }
            }
        };

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mFirebaseAdapter != null)
            mFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFirebaseAdapter != null)
            mFirebaseAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();

        mTitleText.setText(PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getString("prefname", "Anonymous"));

        View mSendLayout = getView().findViewById(
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

        menu.add(Menu.NONE, MENU_PROFILE, Menu.NONE, "Profile")
                .setIcon(R.drawable.ic_profile)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_FILTER) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.chat_action_filter);
            alert.setMessage(R.string.chat_filter_message);

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
        } else if (item.getItemId() == MENU_PROFILE) {
            startActivity(new Intent(getActivity(), ProfileSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static List<String> extractUrls(String text) {
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
}
