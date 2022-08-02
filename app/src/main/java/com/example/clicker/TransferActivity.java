package com.example.clicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clicker.databinding.ActivityTransferBinding;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointsHelper;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.ParseException;
import java.util.List;

public class TransferActivity extends AppCompatActivity {

    private static final String TAG = "TransferActivity";
    ActivityResultLauncher<Intent> importFromTSVActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    PointsHelper helper = new PointsHelper(TransferActivity.this);
                    try (Reader reader = new InputStreamReader(TransferActivity.this.getContentResolver().openInputStream(data.getData()))) {
                        List<String> lines = IOUtils.readLines(reader);
                        lines.remove(0);
                        for (String line : lines) {
                            Log.d(TAG, line);
                            helper.addOrUpdatePoint(new Point(line));
                        }
                        Toast.makeText(TransferActivity.this, String.format("Imported %d points", lines.size()), Toast.LENGTH_SHORT).show();
                    } catch (IOException | ParseException e) {
                        Toast.makeText(TransferActivity.this, "File Import " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failure to import from CSV", e);
                    }
                }
            });
    ActivityResultLauncher<Intent> exportToTSVActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        List<Point> points = new PointsHelper(TransferActivity.this.getApplicationContext()).getAll();
                        int counter = 0;

                        try (OutputStreamWriter os = new OutputStreamWriter(TransferActivity.this.getContentResolver().openOutputStream(result.getData().getData()))) {
                            os.write(Point.CSV_HEADER());
                            for (Point point : points) {
                                if (!point.getName().trim().equals("label")) {
                                    os.write(point + "\n");
                                    counter++;
                                }
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Failure writing out file of points.", e);
                            Toast.makeText(TransferActivity.this, "Failure exporting points.", Toast.LENGTH_LONG).show();
                        } finally {
                            Toast.makeText(TransferActivity.this, String.format("Exported %d points.", counter), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
    ActivityResultLauncher<Intent> exportToGPXActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        List<Point> points = new PointsHelper(TransferActivity.this.getApplicationContext()).getAll();
                        int counter = 0;

                        try (PrintWriter os = new PrintWriter(new OutputStreamWriter(TransferActivity.this.getContentResolver().openOutputStream(result.getData().getData())))) {
                            os.println("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
                            os.println("<gpx version=\"1.1\" creator=\"Crow Clicker\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:h=\"http://www.humminbird.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
                            String waypoint = "<wpt lat=\"%f\" lon=\"%f\"><ele>0.0</ele><time>%s</time><name>%s-%s</name><desc>%s</desc><sym>%s</sym></wpt>";
                            for (Point point : points) {
                                if (!point.getName().trim().equals("label")) {
                                    String icon = "Fish";
                                    String suffix = point.getFishSize();
                                    if (point.getContactType().equals(ContactType.CONTACT.toString())) {
                                        icon = "Redfish";
                                        suffix = "C";
                                    }
                                    if (point.getContactType().equals(ContactType.FOLLOW.toString())) {
                                        icon = "Flag";
                                        suffix = "F";
                                    }
                                    os.println(String.format(waypoint, point.getLat(), point.getLon(), point.getTimeStamp().toInstant().toString(), point.getName(), suffix, point.getNotes(), icon));
                                    counter++;
                                }
                            }
                            os.println("</gpx>");
                        } catch (IOException e) {
                            Log.e(TAG, "Failure writing out GPX of waypoints.", e);
                            Toast.makeText(TransferActivity.this, "Failure exporting points as GPX.", Toast.LENGTH_LONG).show();
                        } finally {
                            Toast.makeText(TransferActivity.this, String.format("Exported %d points.", counter), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
    private ActivityTransferBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    public void clearPoints(View view) {
        AlertDialog dialogDelete = new AlertDialog.Builder(view.getContext())
                .setTitle("Warning!!")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage("Are you sure to delete all points?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    PointsHelper helper = new PointsHelper(getApplicationContext());
                    helper.clearPoints();
                    Toast.makeText(getApplicationContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    public void importPoints(View view) {
        SheetAccess sheet = new SheetAccess(getApplicationContext());
        sheet.syncSheet();
        Toast.makeText(this, "Background sync started.", Toast.LENGTH_LONG).show();
    }

    public void importFromTSV(View view) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile.putExtra(Intent.EXTRA_TITLE, "points.tsv");
        chooseFile = Intent.createChooser(chooseFile, "Select Import TSV File");
        importFromTSVActivity.launch(chooseFile);
    }

    public void exportPointsToTSV(View view) {
        Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        chooseFile.setType("text/tsv");
        chooseFile.putExtra(Intent.EXTRA_TITLE, "points.tsv");
        chooseFile = Intent.createChooser(chooseFile, "Select Export TSV File");
        exportToTSVActivity.launch(chooseFile);
    }

    public void exportToGPX(View view) {
        Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        chooseFile.setType("application/gpx+xml");
        chooseFile.putExtra(Intent.EXTRA_TITLE, "clicker_points.gpx");
        chooseFile = Intent.createChooser(chooseFile, "Select Export GPX File");
        exportToGPXActivity.launch(chooseFile);
    }
}