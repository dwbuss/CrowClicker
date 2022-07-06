package com.example.clicker.report;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.clicker.MainActivity;
import com.example.clicker.R;
import com.example.clicker.objectbo.Point;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Point> implements View.OnClickListener {

    private ArrayList<Point> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
        Button info;
    }

    public CustomAdapter(ArrayList<Point> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        Point point = (Point) object;

        switch (v.getId()) {
            case R.id.editBtn:
                String timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(point.getTimeStamp());
                Snackbar.make(v, "Ready for Edit " + timeStamp, Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
            case R.id.mapBtn:
                String timeStamp2 = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(point.getTimeStamp());
                Snackbar.make(v, "Ready for Edit " + timeStamp2, Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Point point = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.txtType = (TextView) convertView.findViewById(R.id.type);
            viewHolder.txtVersion = (TextView) convertView.findViewById(R.id.version_number);
            viewHolder.info = (Button) convertView.findViewById(R.id.editBtn);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;
        viewHolder.txtName.setText(point.getName());
        String timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(point.getTimeStamp());
        viewHolder.txtType.setText(timeStamp);
        if (point.getContactType().equalsIgnoreCase("CATCH"))
            viewHolder.txtVersion.setText(point.getFishSize());
        else
            viewHolder.txtVersion.setText(point.getContactType());
        viewHolder.info.setOnClickListener(this);
        viewHolder.info.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }
}