package tof.cv.bo;

import java.net.URL;

import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import tof.cv.ui.InfoTrainActivity;
import android.os.AsyncTask;
import android.view.View;

public class DownloadTrainInfoTask extends AsyncTask<URL, Integer, Long> {
	
	
	private String currentTrain;
	private String lang;
	private InfoTrainActivity infoTrainAct;
	private int currentPos;
	
	public DownloadTrainInfoTask(InfoTrainActivity infoTrainAct, int currentPos){
		this.infoTrainAct = infoTrainAct;
		this.currentPos=currentPos;
		lang = infoTrainAct.getLang();
		currentTrain = infoTrainAct.getCurrentTrain();
	}
	
	protected Long doInBackground(URL... params) {
		getTrainInfo();
		return null;
	}

	protected void onPostExecute(Long result) {
		infoTrainAct.getListView().setVisibility(View.VISIBLE);
		//infoTrainAct.setTitle(currentTrain);
		infoTrainAct.fillTrainStops(currentPos);
		infoTrainAct.setEmptyText(infoTrainAct.getString(R.string.txt_no_result));
	}
	
	public void getTrainInfo() {

		
		infoTrainAct.addStops(ConnectionMaker.getTrainLiveboard(infoTrainAct.getTrainNumber(currentTrain), infoTrainAct,
						false),currentPos);

			/*}

		}*/

	}
	
}
