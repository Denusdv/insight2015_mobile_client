package com.ibm.bluemixpush;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by DenisV on 8/3/15.
 */
public class PushItemAdapter  extends ArrayAdapter<PushItem> {
    Context context;
    int layoutResourceId;
    List<PushItem> list = null;

    public PushItemAdapter(Context context, int layoutResourceId, List<PushItem> data ) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.list = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PushHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new PushHolder();
            holder.title = (TextView)row.findViewById(R.id.title);
            holder.message = (TextView)row.findViewById(R.id.message);
            holder.time = (TextView)row.findViewById(R.id.time);

            row.setTag(holder);
        }
        else
        {
            holder = (PushHolder)row.getTag();
        }

        PushItem item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.message.setText(item.getMessage());
        holder.time.setText(item.getTime());

        return row;
    }

    static class PushHolder
    {
        TextView title;
        TextView message;
        TextView time;
    }
}
