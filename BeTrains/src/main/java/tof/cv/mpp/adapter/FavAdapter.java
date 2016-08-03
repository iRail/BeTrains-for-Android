package tof.cv.mpp.adapter;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.DbAdapterConnection;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FavAdapter extends CursorAdapter {

	public FavAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		View rowView = view;
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		TextView nameTv = (TextView) rowView.findViewById(R.id.firstLine);
		TextView typeTv = (TextView) rowView.findViewById(R.id.secondLine);
		int nameColumn = cursor
				.getColumnIndex(DbAdapterConnection.KEY_FAV_NAME);
		int nameTwoColumn = cursor
		.getColumnIndex(DbAdapterConnection.KEY_FAV_NAMETWO);
		int typeColumn = cursor
				.getColumnIndex(DbAdapterConnection.KEY_FAV_TYPE);

		int type = cursor.getInt(typeColumn);
		switch (type) {
		case 1:
			typeTv.setText(context.getString(R.string.station));
			imageView.setImageResource(R.drawable.ic_fav_station);
			nameTv.setText(cursor.getString(nameColumn));
			break;
		case 2:
			typeTv.setText(context.getString(R.string.train));
			imageView.setImageResource(R.drawable.ic_fav_train);
			nameTv.setText(cursor.getString(nameColumn));
			break;
		case 3:
			typeTv.setText(context.getString(R.string.trip));
			imageView.setImageResource(R.drawable.ic_fav_map);
			nameTv.setText(cursor.getString(nameColumn)+" - "+cursor.getString(nameTwoColumn));
			break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.row_favorite, parent, false);
		bindView(v, context, cursor);
		return v;
	}
}
