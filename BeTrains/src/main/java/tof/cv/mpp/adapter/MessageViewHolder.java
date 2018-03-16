package tof.cv.mpp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tof.cv.mpp.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {


    private TextView trainid;
    private TextView time;
    private TextView messagebody;
    private TextView nickname;
    public TextView donator;
    public TextView beta;
    public View itemView;
    public ImageView iv;

    public MessageViewHolder(View itemView) {
        super(itemView);
        trainid = (TextView) itemView.findViewById(R.id.trainid);
        time = (TextView) itemView.findViewById(R.id.time);
        messagebody = (TextView) itemView.findViewById(R.id.messagebody);
        nickname = (TextView) itemView.findViewById(R.id.nickname);
        iv = (ImageView) itemView.findViewById(R.id.profile_image);
        donator = (TextView) itemView.findViewById(R.id.donator);
        beta = (TextView) itemView.findViewById(R.id.beta);
        this.itemView = itemView;
    }

    public TextView getTrainid() {
        return trainid;
    }

    public TextView getTime() {
        return time;
    }

    public TextView getMessagebody() {
        return messagebody;
    }

    public TextView getNickname() {
        return nickname;
    }


}