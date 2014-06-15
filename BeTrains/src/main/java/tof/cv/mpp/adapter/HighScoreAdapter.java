package tof.cv.mpp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.leaderboard.LeaderboardScore;

import java.util.List;

import tof.cv.mpp.R;

/**
 * Created by CVE on 31/01/14.
 */
public class HighScoreAdapter extends ArrayAdapter<LeaderboardScore>{

    LayoutInflater myLayoutInflater;
    ImageManager im;

    public HighScoreAdapter(Context context, int resource, List<LeaderboardScore> objects) {
        super(context, resource, objects);
        myLayoutInflater=  LayoutInflater.from(context);
        im = ImageManager.create(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = myLayoutInflater.inflate(R.layout.row_high, parent, false);
        }
        LeaderboardScore item = getItem(position);
        ImageView iv = (ImageView) row.findViewById(R.id.profilePic);
        im.loadImage(iv, item.getScoreHolder().getHiResImageUri(),R.drawable.icon);

        TextView tvScore = (TextView) row
                .findViewById(R.id.userPoints);
        tvScore.setText(item.getDisplayScore());

        TextView tvRank = (TextView) row
                .findViewById(R.id.userRank);
        tvRank.setText(item.getDisplayRank());

        TextView tvName = (TextView) row
                .findViewById(R.id.userName);
        tvName.setText(item.getScoreHolderDisplayName());

        return row;
    }
}


