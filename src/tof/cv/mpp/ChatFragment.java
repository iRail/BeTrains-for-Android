package tof.cv.mpp;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.adapter.MessageAdapter;
import tof.cv.mpp.bo.Message;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatFragment extends ListFragment {
	/** Called when the activity is first created. */
	private TextView mTitleText;
	private Button btnSettings;
	private Button btnSend;
	private Button btnMore;
	private EditText messageTxtField;
	private final String TAG = "MessagesTrain.java";
	private int total = 5;
	private boolean posted = false;
	private ArrayList<Message> listOfMessage = new ArrayList<Message>();
	String trainId;
	SharedPreferences settings;

	private static final int MENU_FILTER = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_chat, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);

		setHasOptionsMenu(true);
		getSupportActivity().getSupportActionBar().setTitle(
				getString(R.string.txt_messages));

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mTitleText = (TextView) getActivity().findViewById(R.id.pseudo);
		// mBodyText = (TextView) findViewById(R.id.messagesblock);
		btnSettings = (Button) getActivity().findViewById(R.id.settings);
		btnSend = (Button) getActivity().findViewById(R.id.send);
		btnMore = (Button) getActivity().findViewById(R.id.more);
		messageTxtField = (EditText) getActivity().findViewById(
				R.id.yourmessage);

		setBtnSettingsListener();
		setBtnMoreListener();
		setBtnSendListener();
		Log.i("", "Created " + trainId);
		update();

	}

	private void setBtnSendListener() {
		btnSend.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (posted) {
					Toast.makeText(getActivity(),
							"Only one message per session.", Toast.LENGTH_LONG)
							.show();
				} else {
					String pseudo = PreferenceManager
							.getDefaultSharedPreferences(getActivity())
							.getString("prefPseudo", "Anonymous");
					if (pseudo.contentEquals("Anonymous"))
						Toast.makeText(
								getActivity(),
								"Click on 'change' button to choose a User Name",
								Toast.LENGTH_LONG).show();
					else if (messageTxtField.getText().toString()
							.contentEquals(""))
						Toast.makeText(getActivity(), "Please write something",
								Toast.LENGTH_LONG).show();
					else if (requestPhpSend(pseudo, messageTxtField.getText()
							.toString(), trainId)) {
						Toast.makeText(getActivity(),
								getString(android.R.string.ok),
								Toast.LENGTH_LONG).show();
						posted = true;
					}

					else
						Toast.makeText(getActivity(), "Problem",
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
				startActivity(new Intent(getActivity(),
						PreferenceActivity.class).putExtra("screen",
						PreferenceActivity.PAGE_GENERAL));
			}
		});

	}

	public void update() {
		final Runnable getMessageFromTrain = new Runnable() {
			public void run() {
				listOfMessage = requestPhpRead(trainId, 0, total, getActivity());
				TextView messagesEmpty = (TextView) getActivity().findViewById(
						android.R.id.empty);
				if (listOfMessage != null) {
					Log.i(TAG, "count= " + listOfMessage.size());
					if (listOfMessage.size() == 0)
						messagesEmpty
								.setText(getString(R.string.txt_no_message));
				} else
					messagesEmpty.setText(getString(R.string.txt_connection));

				getActivity().runOnUiThread(returnRes);
			}
		};

		Thread thread = new Thread(null, getMessageFromTrain, "ChatThread");
		thread.start();
	}

	private Runnable returnRes = new Runnable() {

		public void run() {
			MessageAdapter adapter = new MessageAdapter(getActivity(),
					R.layout.row_message, listOfMessage);
			setListAdapter(adapter);
		}

	};

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

		LinearLayout mSendLayout = (LinearLayout) getActivity().findViewById(
				R.id.send_layout);

		if (trainId == null) {
			mSendLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onListItemClick(ListView l, final View v, final int position,
			long id) {
		super.onListItemClick(l, v, position, id);
		AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
		if (trainId == null) {
			ad.setTitle(getResources().getString(
					R.string.confirm_message_train,
					listOfMessage.get(position).gettrain_id()));
			ad.setPositiveButton(android.R.string.ok,
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {

							Bundle bundle = new Bundle();
							bundle.putString(DbAdapterConnection.KEY_NAME,
									listOfMessage.get(position).gettrain_id());
							Intent mIntent = new Intent(v.getContext(),
									ChatActivity.class);
							mIntent.putExtras(bundle);
							startActivityForResult(mIntent, 0);

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
			ad.setTitle(getResources().getString(R.string.confirm_info_train,
					listOfMessage.get(position).gettrain_id()));
			ad.setPositiveButton(android.R.string.ok,
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {

							Intent i = new Intent(getActivity(),
									InfoTrainActivity.class);

							i.putExtra(DbAdapterConnection.KEY_NAME,
									listOfMessage.get(position).gettrain_id());

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

	public static ArrayList<Message> requestPhpRead(String trainId, int start,
			int span, Context context) {

		String TAG = "requestPhpRead";
		ArrayList<Message> listOfMessages = new ArrayList<Message>();

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://christophe.frandroid.com/betrains/php/messages.php");
		String txt = null;
		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("id",
					"hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a"));
			nameValuePairs.add(new BasicNameValuePair("message_count", ""
					+ span));
			nameValuePairs.add(new BasicNameValuePair("message_index", ""
					+ start));
			nameValuePairs.add(new BasicNameValuePair("mode", "read"));
			nameValuePairs.add(new BasicNameValuePair("order", "DESC"));
			if (trainId != null)
				nameValuePairs.add(new BasicNameValuePair("train_id", trainId));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);

			BasicResponseHandler myHandler = new BasicResponseHandler();

			txt = myHandler.handleResponse(response);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO: USE XML PARSER
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
					Log.w(TAG, "messages: " + params[1] + " " + params[2] + " "
							+ params[3] + " " + params[4]);
					listOfMessages.add(new Message(params[1], params[2],
							params[3], params[4]));
					i++;
				}

			}
			return listOfMessages;

		} else {
			System.out.println("function in connection maker returns null !!");
			listOfMessages.add(new Message(context
					.getString(R.string.txt_server_down), context
					.getString(R.string.txt_no_message), "", ""));
			return listOfMessages;
		}

	}

	public static boolean requestPhpSend(String pseudo, String message,
			String trainId) {
		try {
			String txt = "";
			HttpClient client = new DefaultHttpClient();

			HttpPost httppost = new HttpPost(
					"http://christophe.frandroid.com/betrains/php/messages.php");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("code",
					"hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a"));
			nameValuePairs.add(new BasicNameValuePair("train_id", trainId));
			nameValuePairs.add(new BasicNameValuePair("user_message", message));
			nameValuePairs.add(new BasicNameValuePair("user_name", pseudo));
			nameValuePairs.add(new BasicNameValuePair("mode", "write"));
			nameValuePairs.add(new BasicNameValuePair("order", "DESC"));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(httppost);

			BasicResponseHandler myHandler = new BasicResponseHandler();
			txt = myHandler.handleResponse(response);

			return txt.contains("true");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, MENU_FILTER, Menu.NONE, "Filter")
				.setIcon(R.drawable.icon)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_FILTER:
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

			alert.setTitle(R.string.txt_filter);
			alert.setMessage(R.string.txt_filter_message);

			// Set an EditText view to get user input
			final EditText input = new EditText(getActivity());
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							total = 5;

							Bundle bundle = new Bundle();
							bundle.putString(DbAdapterConnection.KEY_NAME,
									getString(R.string.txt_train) + " "
											+ input.getText().toString());
							Intent mIntent = new Intent(getActivity(),
									ChatActivity.class);
							mIntent.putExtras(bundle);
							startActivity(mIntent);

						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			alert.show();
			return true;
		case (android.R.id.home):
			// app icon in ActionBar is clicked; Go home
			Intent intent = new Intent(getActivity(), WelcomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}