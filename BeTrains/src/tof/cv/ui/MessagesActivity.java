package tof.cv.ui;

import greendroid.app.GDListActivity;
import greendroid.widget.GDActionBar;
import greendroid.widget.GDActionBarItem;
import greendroid.widget.NormalActionBarItem;

import java.util.ArrayList;

import tof.cv.adapters.MessageAdapter;
import tof.cv.bo.Message;
import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MessagesActivity extends GDListActivity {

	/** Called when the activity is first created. */
	private TextView mTitleText;
	private Button btnSettings;
	private Button btnSend;
	private Button btnMore;
	private EditText messageTxtField;
	private Context context;
	private final String TAG = "MessagesTrain.java";
	private int total = 5;
	private boolean posted = false;
	private ArrayList<Message> listOfMessage = new ArrayList<Message>();
	private String trainId;
	private GDActionBar mActionBar;
	private LinearLayout mSendLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences settings = PreferenceManager

		.getDefaultSharedPreferences(getBaseContext());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(this);

		setContext(this);

		mActionBar = getGDActionBar();
		mActionBar.setTitle(getString(R.string.txt_messages));

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			trainId = extras.getString("ID");
		}

		mTitleText = (TextView) findViewById(R.id.pseudo);
		// mBodyText = (TextView) findViewById(R.id.messagesblock);
		btnSettings = (Button) findViewById(R.id.settings);
		btnSend = (Button) findViewById(R.id.send);
		btnMore = (Button) findViewById(R.id.more);
		messageTxtField = (EditText) findViewById(R.id.yourmessage);
		mSendLayout = (LinearLayout) findViewById(R.id.send_layout);

		if (trainId == null) {
			mSendLayout.setVisibility(View.GONE);
	        addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(R.drawable.ic_title_filter),R.id.action_bar_filter);
			//addActionBarItem(R.drawable.ic_title_filter);

		}
		View home = (View) findViewById(R.id.gd_action_bar_home_item);
		home.setOnClickListener((new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MessagesActivity.this,WelcomeActivity.class));
			}
		}));
		update();

		setBtnSettingsListener();
		setBtnMoreListener();
		setBtnSendListener();

	}

	@Override
	public int createLayout() {
		return R.layout.activity_messages;
	}

	private void updateListView() {

		MessageAdapter adapter = new MessageAdapter(this, R.layout.row_message,
				this.listOfMessage);
		setListAdapter(adapter);
	}

	private void setBtnSendListener() {
		btnSend.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (posted) {
					Toast.makeText(getApplicationContext(),
							"Only one message per session.", Toast.LENGTH_LONG)
							.show();
				} else {
					String pseudo = PreferenceManager
							.getDefaultSharedPreferences(getBaseContext())
							.getString("prefPseudo", "Anonymous");
					if (pseudo.contentEquals("Anonymous"))
						Toast
								.makeText(
										getApplicationContext(),
										"Click on 'change' button to choose a User Name",
										Toast.LENGTH_LONG).show();
					else if (messageTxtField.getText().toString()
							.contentEquals(""))
						Toast.makeText(getApplicationContext(),
								"Please write something", Toast.LENGTH_LONG)
								.show();
					else if (ConnectionMaker.requestPhpSend(pseudo,
							messageTxtField.getText().toString(), trainId)) {
						Toast.makeText(getApplicationContext(),
								getString(android.R.string.ok),
								Toast.LENGTH_LONG).show();
						posted = true;
					}

					else
						Toast.makeText(getApplicationContext(), "Problem",
								Toast.LENGTH_LONG).show();

					update();
				}

			}

		});

	}

	private void setBtnMoreListener() {
		btnMore.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				total += 5;
				update();
			}
		});

	}

	private void setBtnSettingsListener() {
		btnSettings.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MessagesActivity.this,
						SettingsActivity.class));
			}
		});

	}

	public void update() {
		final Runnable getMessageFromTrain = new Runnable() {

			public void run() {

				listOfMessage = ConnectionMaker.requestPhpRead(trainId, 0,
						total,context);
				runOnUiThread(returnRes);
			}
		};

		Thread thread = new Thread(null, getMessageFromTrain,
				"MagentoBackground");
		thread.start();
	}

	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void setNoTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void onResume() {
		super.onResume();
Log.i("BETRAINS","train ID= "+trainId);
		if (trainId != null)
			mTitleText.setText(PreferenceManager.getDefaultSharedPreferences(
					getBaseContext()).getString("prefPseudo", "Anonymous")
					+ " - " + trainId);
		else
			mTitleText.setText(PreferenceManager.getDefaultSharedPreferences(
					getBaseContext()).getString("prefPseudo", "Anonymous"));

	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	private Runnable returnRes = new Runnable() {

		public void run() {
			int i = 0;
			TextView messagesEmpty = (TextView) findViewById(android.R.id.empty);
			if (listOfMessage != null) {
				Log.i(TAG, "count= " + listOfMessage.size());
				updateListView();
				if (listOfMessage.size() == 0)
					messagesEmpty.setText(getString(R.string.txt_no_message));
			} else
				messagesEmpty.setText(getString(R.string.txt_connection));

			updateListView();
			// mBodyText.setText(Html.fromHtml(allmessages));

		}

	};

	public boolean onHandleActionBarItemClick(GDActionBarItem item, int position) {

		switch (position) {
		case 0:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle(R.string.txt_filter);
			alert.setMessage(R.string.txt_filter_message);

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							total = 5;

							Bundle bundle = new Bundle();
							bundle.putString("ID", getString(
											R.string.txt_train)+" "+input.getText().toString());
							Intent mIntent = new Intent(MessagesActivity.this,
									MessagesActivity.class);
							mIntent.putExtras(bundle);
							startActivity(mIntent);


						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Canceled.
						}
					});

			alert.show();
			return true;

		default:
			return super.onHandleActionBarItemClick(item,position);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position,
			long id) {
		super.onListItemClick(l, v, position, id);
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		if (trainId == null) {
			ad.setTitle(getResources().getString(
					R.string.confirm_message_train,
					listOfMessage.get(position).gettrain_id()));
			ad.setPositiveButton(android.R.string.ok,
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {

							Bundle bundle = new Bundle();
							bundle.putString("ID", listOfMessage.get(position)
									.gettrain_id());
							Intent mIntent = new Intent(MessagesActivity.this,
									MessagesActivity.class);
							mIntent.putExtras(bundle);
							startActivity(mIntent);
						}
					});

			ad.setNegativeButton(android.R.string.no,
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {

						}
					});
			ad.show();
		}

		else {
			ad.setTitle(getResources().getString(
					R.string.confirm_info_train,
					listOfMessage.get(position).gettrain_id()));
			ad.setPositiveButton(android.R.string.ok,
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {

							Intent i = new Intent(context, InfoTrainActivity.class);

							i.putExtra(ConnectionDbAdapter.KEY_TRAINS, listOfMessage.get(position)
									.gettrain_id());

							startActivity(i);

						}
					});

			ad.setNegativeButton(android.R.string.no,
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {

						}
					});
			ad.show();
		}
	}
}