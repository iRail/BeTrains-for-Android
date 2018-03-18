package tof.cv.mpp.adapter;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.view.LetterTileProvider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
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
		int  tileSize = context.getResources().getDimensionPixelSize(R.dimen.letter_tile_size);;
		LetterTileProvider tileProvider = new LetterTileProvider(context);

		switch (type) {
		case 1:
			typeTv.setText(context.getString(R.string.station));
			imageView.setImageBitmap(tileProvider.getLetterTile(cursor.getString(nameColumn),cursor.getString(nameColumn), tileSize, tileSize));
			nameTv.setText(cursor.getString(nameColumn));
			break;
		case 2:
			typeTv.setText(context.getString(R.string.train));
			String numbers= cursor.getString(nameColumn).replaceAll("\\D+","");
			imageView.setImageBitmap(tileProvider.getLetterTile(numbers, cursor.getString(nameColumn), tileSize, tileSize));
			nameTv.setText(cursor.getString(nameColumn));
			break;
		case 3:
			typeTv.setText(context.getString(R.string.trip));
			imageView.setImageBitmap(tileProvider.getLetterTile(cursor.getString(nameColumn),cursor.getString(nameColumn), tileSize, tileSize));
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
