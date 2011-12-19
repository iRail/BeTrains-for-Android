package tof.cv.mpp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import tof.cv.mpp.Utils.ConnectionDbAdapter;
import tof.cv.mpp.Utils.ConnectionMaker;
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
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
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
	private Context context;
	private final String TAG = "MessagesTrain.java";
	private int total = 5;
	private boolean posted = false;
	private ArrayList<Message> listOfMessage = new ArrayList<Message>();
	private String trainId;
	private LinearLayout mSendLayout;
	SharedPreferences settings;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(getActivity());
		return inflater.inflate(R.layout.fragment_chat, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);
		
		setHasOptionsMenu(true);
		
		context=getActivity();


		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(getActivity());

		getSupportActivity().getSupportActionBar().setTitle(
				getString(R.string.txt_messages));

		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			trainId = extras.getString("ID");
		}

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
		mSendLayout = (LinearLayout) getActivity().findViewById(
				R.id.send_layout);

		if (trainId == null) {
			mSendLayout.setVisibility(View.GONE);
			// TODO addActionBarItem(R.drawable.ic_title_filter);

		}

		update();

		setBtnSettingsListener();
		setBtnMoreListener();
		setBtnSendListener();

	}

	private void updateListView() {

		MessageAdapter adapter = new MessageAdapter(getActivity(),
				R.layout.row_message, this.listOfMessage);
		setListAdapter(adapter);
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
					else if (ConnectionMaker.requestPhpSend(pseudo,
							messageTxtField.getText().toString(), trainId)) {
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
				startActivity(new Intent(getActivity(), SettingsActivity.class));
			}
		});

	}

	public void update() {
		final Runnable getMessageFromTrain = new Runnable() {

			public void run() {

				listOfMessage = requestPhpRead(trainId, 0, total, context);
				getActivity().runOnUiThread(returnRes);
			}
		};

		Thread thread = new Thread(null, getMessageFromTrain,
				"MagentoBackground");
		thread.start();
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

	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	private Runnable returnRes = new Runnable() {

		public void run() {
			TextView messagesEmpty = (TextView) getActivity().findViewById(
					android.R.id.empty);
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

	// TODO
	/*
	 * public boolean onHandleActionBarItemClick(GDActionBarItem item, int
	 * position) {
	 * 
	 * switch (position) { case 0: AlertDialog.Builder alert = new
	 * AlertDialog.Builder(this);
	 * 
	 * alert.setTitle(R.string.txt_filter);
	 * alert.setMessage(R.string.txt_filter_message);
	 * 
	 * // Set an EditText view to get user input final EditText input = new
	 * EditText(this); input.setInputType(InputType.TYPE_CLASS_NUMBER);
	 * alert.setView(input);
	 * 
	 * alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	 * public void onClick(DialogInterface dialog, int whichButton) { total = 5;
	 * 
	 * Bundle bundle = new Bundle(); bundle.putString("ID", getString(
	 * R.string.txt_train)+" "+input.getText().toString()); Intent mIntent = new
	 * Intent(MessagesActivity.this, MessagesActivity.class);
	 * mIntent.putExtras(bundle); startActivity(mIntent);
	 * 
	 * 
	 * } });
	 * 
	 * alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	 * public void onClick(DialogInterface dialog, int whichButton) { //
	 * Canceled. } });
	 * 
	 * alert.show(); return true;
	 * 
	 * default: return super.onHandleActionBarItemClick(item,position); } }
	 */
	@Override
	public void onListItemClick(ListView l, final View v, final int position, long id) {
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
							bundle.putString("ID", listOfMessage.get(position)
									.gettrain_id());
							Intent mIntent = new
							Intent(v.getContext(),
									ChatActivity.class);
							mIntent.putExtras(bundle);
							startActivityForResult(mIntent,0);
							
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

							Intent i = new Intent(context,
									InfoTrainActivity.class);

							i.putExtra(ConnectionDbAdapter.KEY_TRAINS,
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
				nameValuePairs
						.add(new BasicNameValuePair("stringdata", trainId));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			BasicResponseHandler myHandler = new BasicResponseHandler();

			try {
				txt = myHandler.handleResponse(response);
			} catch (HttpResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (txt!=null && !txt.equals("")) {
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
/*
	public static boolean requestPhpSend(String pseudo, String message,
			String trainId) {

		ArrayList<Message> maliste = new ArrayList<Message>();
		// On cree le client
		HttpClient client = new HttpClient();

		HttpClientParams clientParams = new HttpClientParams();
		clientParams.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
				"UTF-8");
		client.setParams(clientParams);

		// Le HTTPMethod qui sera un Post en lui indiquant l'URL du traitement
		// du formulaire
		PostMethod methode = new PostMethod(
				"http://christophe.frandroid.com/betrains/php/messages.php");
		// On ajoute les parametres du formulaire
		methode.addParameter("code",
				"hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a"); // (champs,
		// valeur)
		methode.addParameter("mode", "write");
		methode.addParameter("train_id", trainId);
		methode.addParameter("user_message", message);
		methode.addParameter("user_name", pseudo);

		// Le buffer qui nous servira a recuperer le code de la page
		BufferedReader br = null;
		String txt = null;
		try {
			// http://hc.apache.org/httpclient-3.x/apidocs/org/apache/commons/httpclient/HttpStatus.html
			client.executeMethod(methode);
			// Pour la gestion des erreurs ou un debuggage, on recupere le
			// nombre renvoye.
			// System.out.println("La reponse de executeMethod est : " +
			// retour);
			br = new BufferedReader(new InputStreamReader(
					methode.getResponseBodyAsStream()));
			String readLine;

			// Tant que la ligne en cours n'est pas vide
			while (((readLine = br.readLine()) != null)) {
				txt += readLine;
			}
		} catch (Exception e) {
			System.err.println(e); // erreur possible de executeMethod
			e.printStackTrace();
		} finally {
			// On ferme la connexion
			methode.releaseConnection();
			if (br != null) {
				try {
					br.close(); // on ferme le buffer
				} catch (Exception e) { 
				}
			}
		}

		return txt.contains("true");
	}*/
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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