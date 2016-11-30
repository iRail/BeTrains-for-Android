package tof.cv.mpp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tof.cv.mpp.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {


    private TextView trainid;
    private TextView time;
    private TextView messagebody;
    private TextView nickname;
    public View itemView;

    public MessageViewHolder(View itemView) {
        super(itemView);
        trainid = (TextView) itemView.findViewById(R.id.trainid);
        time = (TextView) itemView.findViewById(R.id.time);
        messagebody = (TextView) itemView.findViewById(R.id.messagebody);
        nickname = (TextView) itemView.findViewById(R.id.nickname);
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