package tof.cv.mpp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import tof.cv.mpp.MyPreferenceActivity.Prefs1Fragment;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.adapter.MessageAdapter;
import tof.cv.mpp.bo.Message;

public class ChatFragment extends ListFragment {
	/** Called when the activity is first created. */
	private TextView mTitleText;
	private Button btnSettings;
	private Button btnSend;
	private Button btnMore;
	private EditText messageTxtField;
	private final String TAG = "MessagesTrain.java";
	private int total = 15;
	private boolean posted = false;
	private ArrayList<Message> listOfMessage = new ArrayList<Message>();
	private ProgressDialog progressDialog;
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
		// mBodyText = (TextView) findViewById(R.id.messagesblock);
		btnSettings = (Button) getView().findViewById(R.id.settings);
		btnSend = (Button) getView().findViewById(R.id.send);
		btnMore = (Button) getView().findViewById(R.id.more);
		messageTxtField = (EditText) getView().findViewById(
				R.id.yourmessage);

		setBtnSettingsListener();
		setBtnMoreListener();
		setBtnSendListener();
		Log.i("", "Created " + trainId);
		update();

        boolean isTablet=this.getActivity().getResources().getBoolean(R.bool.tablet_layout);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(
                !isTablet);

        //getActivity().getActionBar().setIcon(R.drawable.ab_chat);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(null);

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

				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						progressDialog = ProgressDialog.show(getActivity(), "",
								getString(R.string.txt_patient), true);
					}
				});

				if (requestPhpSend(pseudo,
						messageTxtField.getText().toString(), trainId)) {

					toTast = getString(android.R.string.ok);
					getActivity().runOnUiThread(displayToast);
                    try {
                      PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("chatUnlock",true).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    posted = true;
				}

				else
					toTast = "Problem";
				getActivity().runOnUiThread(displayToast);
				getActivity().runOnUiThread(dismissPd);
			}
		};

		Thread thread = new Thread(null, trainSearch, "MyThread");
		thread.start();

	}

	private Runnable dismissPd = new Runnable() {
		public void run() {
			progressDialog.dismiss();
		}
	};
	private Runnable displayToast = new Runnable() {
		public void run() {
			Toast.makeText(getActivity(), toTast, Toast.LENGTH_LONG).show();
		}
	};

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
				if (Build.VERSION.SDK_INT>=11)
					startActivity(new Intent(getActivity(),
							MyPreferenceActivity.class).putExtra(
							PreferenceActivity.EXTRA_SHOW_FRAGMENT,
							Prefs1Fragment.class.getName()));
				else{
					startActivity(new Intent(getActivity(), MyPreferenceActivity.class));
				}
			}
		});

	}

	public void update() {
		final Runnable getMessageFromTrain = new Runnable() {
			public void run() {
				try {
					listOfMessage = UtilsWeb.requestPhpRead(trainId, 0, total,
							getActivity());
					if (listOfMessage != null) {
						Log.i(TAG, "count= " + listOfMessage.size());
						if (listOfMessage.size() == 0) {
							if (getActivity() != null)
								getActivity().runOnUiThread(updateEmpty);
						} else {
							if (getActivity() != null)
								getActivity().runOnUiThread(returnRes);
						}
						toEmpty = getString(R.string.txt_no_message);
					} else {
						toEmpty = getString(R.string.txt_connection);
						getActivity().runOnUiThread(returnRes);
					}
				}catch (Exception e){
					e.printStackTrace();
				}
				

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
	private Runnable updateEmpty = new Runnable() {

		public void run() {
			TextView messagesEmpty = (TextView) getView().findViewById(
					android.R.id.empty);
			messagesEmpty.setText(toEmpty);
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

		LinearLayout mSendLayout = (LinearLayout) getView().findViewById(
				R.id.send_layout);

		if (trainId == null) {
			mSendLayout.setVisibility(View.GONE);
		}

        update();
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
					new DialogInterface.OnClickListener() {
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
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {

						}
					});
			ad.show();
		}

		else {
			ad.setTitle(getResources().getString(R.string.confirm_info_train,
					listOfMessage.get(position).gettrain_id()));
			ad.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {

							Intent i = new Intent(getActivity(),
									InfoTrainActivity.class);

							i.putExtra(DbAdapterConnection.KEY_NAME,
									listOfMessage.get(position).gettrain_id());

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

	public static boolean requestPhpSend(String pseudo, String message,
			String trainId) {
		try {
			String txt = "";

			// On cree le client
			HttpClient client = new HttpClient();

			HttpClientParams clientParams = new HttpClientParams();
			clientParams.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,
					"UTF-8");
			client.setParams(clientParams);
			/*
			 * HttpClient client = new DefaultHttpClient();
			 * 
			 * client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET
			 * , "UTF-8");
			 * 
			 * HttpPost httppost = new HttpPost(
			 * "http://christophe.frcom/betrains/php/messages.php");
			 * List<NameValuePair> nameValuePairs = new
			 * ArrayList<NameValuePair>(2); nameValuePairs.add(new
			 * BasicNameValuePair("code",
			 * "hZkzZDzsiF5354LP42SdsuzbgNBXZa78123475621857a"));
			 * nameValuePairs.add(new BasicNameValuePair("train_id", trainId));
			 * nameValuePairs.add(new BasicNameValuePair("user_message",
			 * message)); nameValuePairs.add(new BasicNameValuePair("user_name",
			 * pseudo)); nameValuePairs.add(new BasicNameValuePair("mode",
			 * "write")); nameValuePairs.add(new BasicNameValuePair("order",
			 * "DESC"));
			 * 
			 * httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			 * 
			 * HttpResponse response = client.execute(httppost);
			 * 
			 * BasicResponseHandler myHandler = new BasicResponseHandler(); txt
			 * = myHandler.handleResponse(response);
			 */
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
					} catch (Exception e) { /* on fait rien */
					}
				}
			}

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
									input.getText().toString());
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}