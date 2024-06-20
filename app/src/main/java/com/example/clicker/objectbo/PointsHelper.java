package com.example.clicker.objectbo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.clicker.ContactType;
import com.example.clicker.ObjectBoxApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.QueryBuilder;
import io.objectbox.query.QueryCondition;

public class PointsHelper {
    private final Box<Point> pointBox;
    private final SharedPreferences prefs;

    public PointsHelper(Context context) {
        BoxStore boxStore = ((ObjectBoxApp) context.getApplicationContext()).getBoxStore();
        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        pointBox = boxStore.boxFor(Point.class);
    }

    public String getDailyCatch() {
        return retrieveDaily(ContactType.CATCH, null, prefs.getString("Lake", ""));
    }

    public String getDailyContact() {
        return retrieveDaily(ContactType.CONTACT, null, prefs.getString("Lake", ""));
    }

    public String getDailyFollow() {
        return retrieveDaily(ContactType.FOLLOW, null, prefs.getString("Lake", ""));
    }

    public String getDailyCatch(String angler) {
        return retrieveDaily(ContactType.CATCH, angler, prefs.getString("Lake", ""));
    }

    public String getDailyContact(String angler) {
        return retrieveDaily(ContactType.CONTACT, angler, prefs.getString("Lake", ""));
    }

    public String getDailyFollow(String angler) {
        return retrieveDaily(ContactType.FOLLOW, angler, prefs.getString("Lake", ""));
    }

    public String getTripCatch(int tripLength) {
        return retrieveTrip(tripLength, ContactType.CATCH, prefs.getString("Lake", ""));
    }

    public String getTripContact(int tripLength) {
        return retrieveTrip(tripLength, ContactType.CONTACT, prefs.getString("Lake", ""));
    }

    public String getTripFollow(int tripLength) {
        return retrieveTrip(tripLength, ContactType.FOLLOW, prefs.getString("Lake", ""));
    }

    public String getTotalCatch(String lake) {
        QueryCondition<Point> baseQuery = Point_.contactType.equal(ContactType.CATCH.toString())
                .and(Point_.lake.equal(lake))
                .and(Point_.fishSize.notEqual(""));
        return Long.toString(pointBox.query(baseQuery).build().count());
    }

    public String getTotalContact(String lake) {
        QueryCondition<Point> baseQuery = Point_.contactType.equal(ContactType.CONTACT.toString()).and(Point_.lake.equal(lake));
        return Long.toString(pointBox.query(baseQuery).build().count());
    }

    public String getTotalFollow(String lake) {
        QueryCondition<Point> baseQuery = Point_.contactType.equal(ContactType.FOLLOW.toString()).and(Point_.lake.equal(lake));
        return Long.toString(pointBox.query(baseQuery).build().count());
    }

    public Point getPointById(long id) {
        return pointBox.query().equal(Point_.id, id).build().findUnique();
    }

    public void addOrUpdatePoint(Point point) {
        pointBox.put(point);
    }

    public void clearPoints(String lake) {
        pointBox.query(Point_.lake.equal(lake)).build().remove();
    }

    public long clearAllPointsOf(ContactType type, String lake) {
        return pointBox.query(Point_.contactType.equal(type.toString()).and(Point_.sheetId.notEqual(0)).and(Point_.lake.equal(lake))).build().remove();
    }

    public void deletePoint(long id) {
        Point point = getPointById(id);
        if (point != null) {
            pointBox.remove(id);
        }
    }

    public List<Point> getAll() {
        return pointBox.query().build().find();
    }

    public ArrayList<Point> getPointsForTrip(int tripLength, String lake) {
        Calendar today = Calendar.getInstance(Locale.US);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, -tripLength);
        int flags = QueryBuilder.NULLS_LAST | QueryBuilder.DESCENDING;

        List<Point> tempPoints = pointBox.query(Point_.lake.equal(lake))
                .order(Point_.timeStamp, flags)
                .greater(Point_.timeStamp, today.getTime())
                .build().find();
        ArrayList<Point> points = new ArrayList<>();
        for (Point p : tempPoints) {
            if (!p.getName().equalsIgnoreCase("Label") && !p.getName().equalsIgnoreCase("FF"))
                points.add(p);
        }
        return points;
    }

    private String retrieveDaily(ContactType type, String angler, String lake) {
        Calendar today = Calendar.getInstance(Locale.US);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        return retrieveFor(today, type, angler, lake);
    }

    private String retrieveTrip(int tripLength, ContactType type, String lake) {
        Calendar today = Calendar.getInstance(Locale.US);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.add(Calendar.DATE, -tripLength);
        return retrieveFor(today, type, null, lake);
    }

    private String retrieveFor(Calendar date, ContactType type, String angler, String lake) {
        boolean haveAngler = (angler != null && !angler.trim().isEmpty());
        QueryCondition<Point> baseQuery = Point_.contactType.equal(type.toString())
                .and(Point_.timeStamp.greater(date.getTime()))
                .and(Point_.lake.equal(lake))
                .and(Point_.name.notEqual("FF"));
        if (haveAngler)
            baseQuery = baseQuery.and(Point_.name.equal(angler));
        return Long.toString(pointBox.query(baseQuery).build().count());
    }

    public List<Point> getAllLabels(String lake) {
        QueryCondition<Point> baseQuery = Point_.name.equal("label").and(Point_.lake.equal(lake));
        return pointBox.query(baseQuery).build().find();
    }

    public List<Point> getAllPointsOf(ContactType type, String lake) {
        QueryCondition<Point> baseQuery = Point_.contactType.equal(type.toString()).and(Point_.lake.equal(lake));
        return pointBox.query(baseQuery).build().find();
    }

    public Collection<Point> getAllFFs(String lake) {
        QueryCondition<Point> baseQuery = Point_.name.equal("FF").and(Point_.lake.equal(lake));
        return pointBox.query(baseQuery).build().find();
    }
}
