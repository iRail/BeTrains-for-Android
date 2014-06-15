package tof.cv.mpp.Utils;

import java.util.ArrayList;

import tof.cv.mpp.InfoTrainFragment;
import tof.cv.mpp.R;
import tof.cv.mpp.bo.Message;
import android.os.AsyncTask;
import android.text.Html;

public class DownloadLastMessageTask extends
		AsyncTask<String, Integer, Message> {

	private InfoTrainFragment infoTrainAct;

	public DownloadLastMessageTask(InfoTrainFragment infoTrainAct) {
		System.out.println("downloadlastmessage constructor called");
		this.infoTrainAct = infoTrainAct;
	}

	protected Message doInBackground(String... params) {
		System.out.println("do in background called , params[0] : " + params[0]);
		return getLastMessageFromServer(params[0]);
	}

	protected void onPostExecute(Message result) {
		System.out.println("onpost execute");
		if (result == null)
			infoTrainAct.setLastMessageText(infoTrainAct
					.getString(R.string.txt_no_message));
		else
			infoTrainAct.setLastMessageText(Html.fromHtml(result.getauteur()
					+ ": " + result.getbody() + "<br />" + "<small>"
					+ result.gettime() + "</small>"));
	}

	public Message getLastMessageFromServer(String train) {
		System.out.println("getLastmessage from server");
		
		ArrayList<Message> messageList = UtilsWeb.requestPhpRead(train,
				0, 1,infoTrainAct.getActivity());
		Message ServerMessage=null;
		System.out.println("Debug = " + messageList);
		if(messageList != null)
			if (messageList.size() != 0)
			ServerMessage = UtilsWeb.requestPhpRead(train, 0, 1,infoTrainAct.getActivity()).get(0);
		infoTrainAct.addMessage(ServerMessage);
		
		return ServerMessage;
	}

}
