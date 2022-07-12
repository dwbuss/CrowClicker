package com.example.clicker.report;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clicker.ContactType;
import com.example.clicker.MainActivity;
import com.example.clicker.PointActivity;
import com.example.clicker.R;
import com.example.clicker.objectbo.Point;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointHolder> {
    private static final String TAG = "PointAdapter";
    private final ArrayList<Point> points;
    private final ActivityResultLauncher<Intent> editPointActivity;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PointAdapter(ArrayList<Point> points, ActivityResultLauncher<Intent> editPointActivity) {
        this.points = points;
        this.editPointActivity = editPointActivity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PointAdapter.PointHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item,
                                                                  parent,
                                                                  false);
        return new PointHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PointHolder holder, int position) {

        Point point = points.get(position);

        holder.txtName.setText(point.getName());
        String timeStamp = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(point.getTimeStamp());
        holder.txtType.setText(timeStamp);

        if (point.getContactType().equalsIgnoreCase(ContactType.CATCH.toString()))
            holder.txtLenOrType.setText(point.getFishSize());
        else
            holder.txtLenOrType.setText(point.getContactType());

        holder.map.setOnClickListener(v -> {
            Intent gotoMap = new Intent(v.getContext(), MainActivity.class);
            gotoMap.putExtra("gotoPoint", point);
            v.getContext().startActivity(gotoMap);
        });

        holder.edit.setOnClickListener(v -> {
            Intent editPoint = new Intent(v.getContext(), PointActivity.class);
            editPoint.putExtra("point", point);
            editPointActivity.launch(editPoint);
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return points.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    protected static class PointHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtLenOrType;
        ImageButton map;
        ImageButton edit;

        public PointHolder(View v) {
            super(v);
            txtName = v.findViewById(R.id.name);
            txtType = v.findViewById(R.id.type);
            txtLenOrType = v.findViewById(R.id.version_number);
            map = v.findViewById(R.id.mapBtn);
            edit = v.findViewById(R.id.editBtn);
        }
    }
}