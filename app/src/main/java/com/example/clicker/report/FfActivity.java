package com.example.clicker.report;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clicker.R;
import com.example.clicker.databinding.ActivityFfBinding;

import java.util.List;

public class FfActivity extends AppCompatActivity {
    private static final String TAG = "FfActivity";
    private TableLayout mTableLayout;
    private ActivityFfBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<List<Object>> ffData = (List<List<Object>>) getIntent().getSerializableExtra("ffstandings");

        mTableLayout = (TableLayout) findViewById(R.id.tableInvoices);
        mTableLayout.setStretchAllColumns(true);

        loadData(ffData);
    }

    private void loadData(List<List<Object>> data) {
        int leftRowMargin = 0;
        int topRowMargin = 0;
        int rightRowMargin = 0;
        int bottomRowMargin = 0;
        int textSize = 30;
        if (data.isEmpty())
            return;

        mTableLayout.removeAllViews();
        int id = 0;
        final TableRow tr = new TableRow(this);
        tr.setId(id);
        TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                                                         TableLayout.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
        tr.setPadding(0, 0, 0, 0);
        tr.setLayoutParams(trParams);

        List<Object> row = data.get(0);
        for (int i = 0; i < row.size(); i++) {
            final TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                                         TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.LEFT);
            tv.setPadding(5, 15, 0, 15);
            tv.setBackgroundColor(Color.parseColor("#f8f8f8"));
            tv.setText(String.valueOf(row.get(i)));
            //tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tr.addView(tv);
        }
        mTableLayout.addView(tr, trParams);


        for (int i = 1; i < data.size(); i++) {
            final TableRow newRow = new TableRow(this);
            newRow.setId(id + i);
            newRow.setPadding(0, 0, 0, 0);
            newRow.setLayoutParams(trParams);
            List<Object> dataRow = data.get(i);
            for (int ii = 0; ii < dataRow.size(); ii++) {
                final TextView tv = new TextView(this);
                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                                             TableRow.LayoutParams.WRAP_CONTENT));
                tv.setGravity(Gravity.LEFT);
                tv.setPadding(5, 15, 0, 15);
                String value = "";
                if (dataRow.get(ii) != null) {
                    value = String.valueOf(dataRow.get(ii));
                }
                tv.setText(value);
                //      tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                newRow.addView(tv);
            }
            mTableLayout.addView(newRow, trParams);
        }
    }
}

