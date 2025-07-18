package com.example.clicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TransferActivity extends AppCompatActivity {

    private static final String[] exportChoices = {"Follows", "Contacts", "Catches", "Labels", "FF Spots"};
    private static final int EXPORT_FOLLOWS_INDEX = 0;
    private static final int EXPORT_CONTACTS_INDEX = 1;
    private static final int EXPORT_CATCHES_INDEX = 2;
    private static final int EXPORT_LABELS_INDEX = 3;
    private static final int EXPORT_FF_INDEX = 4;
    private static final String TAG = "TransferActivity";
    private final boolean[] checkedItems = {false, false, true, false, false};
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
                            Log.d(TAG, String.format("[%s]", line));
                            helper.addOrUpdatePoint(new Point(line));
                        }
                        Toast.makeText(TransferActivity.this, String.format("Imported %d points", lines.size()), Toast.LENGTH_SHORT).show();
                    } catch (IOException | ParseException e) {
                        Toast.makeText(TransferActivity.this, "File Import " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failure to import from TSV", e);
                    }
                }
            });
    ActivityResultLauncher<Intent> exportToTSVActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    List<Point> points = getPointsForExport();
                    int counter = 0;
                    try (OutputStreamWriter os = new OutputStreamWriter(this.getContentResolver().openOutputStream(result.getData().getData()))) {
                        os.write(Point.CSV_HEADER());
                        for (Point point : points) {
                            os.write(point + "\n");
                            counter++;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failure writing out file of points.", e);
                        Toast.makeText(TransferActivity.this, "Failure exporting points.", Toast.LENGTH_LONG).show();
                    } finally {
                        Toast.makeText(TransferActivity.this, String.format("Exported %d points.", counter), Toast.LENGTH_LONG).show();
                    }
                }
            });
    ActivityResultLauncher<Intent> exportToGPXActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    List<Point> points = getPointsForExport();
                    int counter = 0;

                    try (PrintWriter os = new PrintWriter(new OutputStreamWriter(TransferActivity.this.getContentResolver().openOutputStream(result.getData().getData())))) {
                        os.println("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
                        os.println("<gpx version=\"1.1\" creator=\"Crow Clicker\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:h=\"http://www.humminbird.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
                        String waypoint = "<wpt lat=\"%f\" lon=\"%f\"><ele>0.0</ele><time>%s</time><name>%s%s</name><desc>%s</desc><sym>%s</sym></wpt>";
                        for (Point point : points) {
                            String icon = "Diamond";
                            String suffix = "";
                            if (point.getContactType().equals(ContactType.CATCH.toString())) {
                                icon = "Fish";
                                suffix = "-" + point.getFishSize();
                            }
                            if (point.getContactType().equals(ContactType.CONTACT.toString())) {
                                icon = "Redfish";
                                suffix = "-C";
                            }
                            if (point.getContactType().equals(ContactType.FOLLOW.toString())) {
                                icon = "Flag";
                                suffix = "-F";
                            }
                            if (point.getName().trim().equals("label")) {
                                icon = "Diamond";
                                suffix = "";
                                os.println(String.format(waypoint, point.getLat(), point.getLon(), point.getTimeStamp().toInstant().toString(), point.getNotes(), suffix, point.getNotes(), icon));
                            }
                            if (point.getName().trim().equals("FF")) {
                                suffix = "";
                                icon = "Beverage";
                                os.println(String.format(waypoint, point.getLat(), point.getLon(), point.getTimeStamp().toInstant().toString(), point.getNotes(), suffix, point.getNotes(), icon));
                            } else
                                os.println(String.format(waypoint, point.getLat(), point.getLon(), point.getTimeStamp().toInstant().toString(), point.getName(), suffix, point.getNotes(), icon));
                            counter++;
                        }
                        os.println("</gpx>");
                    } catch (IOException e) {
                        Log.e(TAG, "Failure writing out GPX of waypoints.", e);
                        Toast.makeText(TransferActivity.this, "Failure exporting points as GPX.", Toast.LENGTH_LONG).show();
                    } finally {
                        Toast.makeText(TransferActivity.this, String.format("Exported %d points.", counter), Toast.LENGTH_LONG).show();
                    }
                }
            });
    private ActivityTransferBinding binding;

    @NonNull
    private List<Point> getPointsForExport() {
        PointsHelper pointsHelper = new PointsHelper(TransferActivity.this.getApplicationContext());
        List<Point> points = new LinkedList<Point>();
        String lake = PreferenceManager.getDefaultSharedPreferences(this).getString("Lake", "");
        if (checkedItems[EXPORT_CATCHES_INDEX])
            points.addAll(pointsHelper.getAllPointsOf(ContactType.CATCH, lake));
        if (checkedItems[EXPORT_CONTACTS_INDEX])
            points.addAll(pointsHelper.getAllPointsOf(ContactType.CONTACT, lake));
        if (checkedItems[EXPORT_FOLLOWS_INDEX])
            points.addAll(pointsHelper.getAllPointsOf(ContactType.FOLLOW, lake));
        if (checkedItems[EXPORT_LABELS_INDEX])
            points.addAll(pointsHelper.getAllLabels(lake));
        if (checkedItems[EXPORT_FF_INDEX])
            points.addAll(pointsHelper.getAllFFs(lake));
        return points;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    public void clearPoints(View view) {
        String lake = PreferenceManager.getDefaultSharedPreferences(this).getString("Lake", "");
        new AlertDialog.Builder(view.getContext())
                .setTitle("Warning!!")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage("Are you sure to delete all points for lake " + lake + "?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    PointsHelper helper = new PointsHelper(getApplicationContext());
                    helper.clearPoints(lake);
                    Toast.makeText(getApplicationContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    public void clearCatches(View view) {
        String lake = PreferenceManager.getDefaultSharedPreferences(this).getString("Lake", "");
        new AlertDialog.Builder(view.getContext())
                .setTitle("Warning!!")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage("Are you sure to delete all CATCHES fro lake " + lake + "?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    PointsHelper helper = new PointsHelper(getApplicationContext());
                    long total = helper.clearAllPointsOf(ContactType.CATCH, lake);
                    Toast.makeText(getApplicationContext(), String.format("Successfully deleted %d catches", total), Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    public void importPoints(View view) {
        SheetAccess sheet = new SheetAccess(getApplicationContext());
        String lake = PreferenceManager.getDefaultSharedPreferences(this).getString("Lake", "");
        sheet.syncSheet(lake);
        Toast.makeText(this, "Background sync for " + lake + " started.", Toast.LENGTH_LONG).show();
    }

    public void importFromTSV(View view) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile.putExtra(Intent.EXTRA_TITLE, "points.tsv");
        chooseFile = Intent.createChooser(chooseFile, "Select Import data from TSV");
        importFromTSVActivity.launch(chooseFile);
    }

    private AlertDialog exportSelectDialog(DialogInterface.OnClickListener positiveButton) {
        Arrays.fill(checkedItems, false);
        checkedItems[EXPORT_CATCHES_INDEX] = true;
        AlertDialog.Builder dialog = new AlertDialog.Builder(TransferActivity.this)
                .setTitle("Export Selection")
                .setMultiChoiceItems(exportChoices, checkedItems, (dialogInterface, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Export", positiveButton)
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                });
        return dialog.create();
    }

    public void exportPointsToTSV(View view) {
        exportSelectDialog((dialogInterface, i) -> {
            Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            chooseFile.setType("text/tsv");
            chooseFile.putExtra(Intent.EXTRA_TITLE, "clicker_points.tsv");
            chooseFile = Intent.createChooser(chooseFile, "Select Export data as TSV");
            exportToTSVActivity.launch(chooseFile);
        }).show();
    }

    public void exportToGPX(View view) {
        exportSelectDialog((dialogInterface, i) -> {
            Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            chooseFile.setType("application/gpx+xml");
            chooseFile.putExtra(Intent.EXTRA_TITLE, "clicker_points.gpx");
            chooseFile = Intent.createChooser(chooseFile, "Select Export data as GPX");
            exportToGPXActivity.launch(chooseFile);
        }).show();
    }

    public void downloadApk(View view) {
        String url = "https://shawanodentalclinic.com/crow-clicker.apk";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}