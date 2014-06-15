package tof.cv.mpp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.achievement.Achievement;

import java.util.List;

import tof.cv.mpp.R;

/**
 * Created by CVE on 31/01/14.
 */
public class AchievementAdapter extends ArrayAdapter<Achievement> {

    LayoutInflater myLayoutInflater;
    ImageManager im;

    public AchievementAdapter(Context context, int resource, List<Achievement> objects) {
        super(context, resource, objects);
        myLayoutInflater = LayoutInflater.from(context);
        im = ImageManager.create(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = myLayoutInflater.inflate(R.layout.row_achieve, parent, false);
        }
        Achievement item = getItem(position);
        ImageView iv = (ImageView) row.findViewById(R.id.achPic);
        im.loadImage(iv, (item.getState() == Achievement.STATE_UNLOCKED) ? item.getUnlockedImageUri() : item.getRevealedImageUri(), R.drawable.icon);

        TextView tvScore = (TextView) row
                .findViewById(R.id.achDesc);
        tvScore.setText(item.getDescription());

        String title = item.getName();
        try {//Sometimes IlledalStateException.. Why???
            if(item.getTotalSteps()>=0)
                title+=" ("+(item.getFormattedCurrentSteps()==null?0:item.getFormattedCurrentSteps())+" / "+item.getFormattedTotalSteps()+")";
        } catch (Exception e) {

        }

        TextView tvName = (TextView) row
                .findViewById(R.id.achName);
        tvName.setText(title);

        return row;
    }
}


