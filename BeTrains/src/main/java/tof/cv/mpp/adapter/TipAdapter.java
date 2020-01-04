package tof.cv.mpp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tof.cv.mpp.R;

public class TipAdapter  extends RecyclerView.Adapter<TipAdapter.MyViewHolder> {
    List<HashMap<String, String>> list;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tip;
        public TextView text;
        public MyViewHolder(View v) {
            super(v);
            tip = v.findViewById(R.id.tiptitle);
            text = v.findViewById(R.id.tiptext);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TipAdapter(     List<HashMap<String, String>> list ) {
        this.list = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TipAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_tip, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Map<String,String> map = list.get(position);
        holder.tip.setText(map.get("tip"));
        holder.text.setText(map.get("title"));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }
}