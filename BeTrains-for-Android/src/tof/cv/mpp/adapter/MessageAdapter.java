package tof.cv.mpp.adapter;

import java.util.ArrayList;

import tof.cv.mpp.R;
import tof.cv.mpp.bo.Message;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MessageAdapter extends AbstractAdapter<Message>{

	public MessageAdapter(Context context, int textViewResourceId,
			ArrayList<Message> items) {
		super(context, textViewResourceId, items);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) super.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_message, null);
		}
		Message o = items.get(position);
		if (o != null) {
			TextView t1 = (TextView) v.findViewById(R.id.trainid);
			TextView t2 = (TextView) v.findViewById(R.id.time);
			TextView t3 = (TextView) v.findViewById(R.id.messagebody);
			TextView t4 = (TextView) v.findViewById(R.id.nickname);

			t4.setText(o.getauteur());
			if(o.gettime().contains(":"))
				t2.setText(o.gettime().substring(0,o.gettime().lastIndexOf(":")));
			t3.setText(o.getbody());
			t1.setText(o.gettrain_id());

		}
		return v;
	}

}
