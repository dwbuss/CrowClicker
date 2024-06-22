package com.example.clicker;

import android.location.Location;

import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunTimes;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Solunar {

    private static final int[] IMAGE_LOOKUP = {
            R.drawable.moon0,
            R.drawable.moon1,
            R.drawable.moon2,
            R.drawable.moon3,
            R.drawable.moon4,
            R.drawable.moon5,
            R.drawable.moon6,
            R.drawable.moon7,
            R.drawable.moon8,
            R.drawable.moon9,
            R.drawable.moon10,
            R.drawable.moon11,
            R.drawable.moon12,
            R.drawable.moon13,
            R.drawable.moon14,
            R.drawable.moon15,
            R.drawable.moon16,
            R.drawable.moon17,
            R.drawable.moon18,
            R.drawable.moon19,
            R.drawable.moon20,
            R.drawable.moon21,
            R.drawable.moon22,
            R.drawable.moon23,
            R.drawable.moon24,
            R.drawable.moon25,
            R.drawable.moon26,
            R.drawable.moon27,
            R.drawable.moon28,
            R.drawable.moon29,
    };
    private static final String TAG = "Solunar";
    public String longitude;
    public String latitude;
    public String offset;
    public String sunRise;
    public String sunSet;
    public String moonRise;
    public String moonSet;
    public String moonOverHead;
    public String moonUnderFoot;
    public String moonPhase;
    public int moonPhaseIcon;
    public String minor;
    public String major;
    public boolean isMajor;
    public boolean isMinor;
    public String moonDegree;
    public String moonState;

    public void populate(Location loc, Calendar cal) {
        if (loc != null) {
            populate(loc.getLongitude(), loc.getLatitude(), cal);
        }
    }

    String parseTime(ZonedDateTime time) {
        if (time != null)
            return time.format(DateTimeFormatter.ofPattern("h:mm a"));
        return "N/A";
    }

    public void populate(double lon, double lat, Calendar cal) {
        moonDegree = "";
        moonState = "";
        int offsetInMillis = cal.getTimeZone().getOffset(cal.getTimeInMillis());
        offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "+" : "-") + Integer.parseInt(offset.split(":")[0]);
        Calendar startOfDay = Calendar.getInstance(Locale.US);
        startOfDay.setTimeZone(cal.getTimeZone());
        startOfDay.setTime(cal.getTime());
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        SunTimes times = SunTimes.compute()
                .on(startOfDay)
                .fullCycle()
                .at(lat, lon)
                .execute();
        MoonTimes moon = MoonTimes.compute()
                .on(startOfDay)
                .fullCycle()
                .oneDay()
                .at(lat, lon)
                .execute();

        MoonPosition moonp;
        double prev = 0;
        boolean increasing = false;
        boolean decreasing = false;
        Date moonOverHeadDt = null;
        Date moonUnderFootDt = null;

        Date afterAddingMins = startOfDay.getTime();
        for (int i = 0; i < 1440; i++) {
            long curTimeInMs = afterAddingMins.getTime();
            afterAddingMins = new Date(curTimeInMs + 60000);
            moonp = MoonPosition.compute()
                    .at(lat, lon)
                    .on(afterAddingMins)
                    .execute();
            double alt = moonp.getAltitude();
            if (increasing && alt < prev) {
                moonOverHeadDt = afterAddingMins;
                increasing = false;
                decreasing = true;
            } else if (decreasing && alt > prev) {
                moonUnderFootDt = afterAddingMins;
                decreasing = false;
                increasing = true;
            } else if (prev != 0) {
                if (alt > prev) increasing = true;
                else decreasing = true;
            }
            prev = alt;
        }
        double phase = getPhase(cal);
        moonPhase = getMoonPhaseText(phase);
        int phase2 = ((int) Math.floor(phase)) % 30;
        moonPhaseIcon = IMAGE_LOOKUP[phase2];
        longitude = Double.toString(lon);
        latitude = Double.toString(lat);
        sunRise = parseTime(times.getRise());
        sunSet = parseTime(times.getSet());
        moonRise = parseTime(moon.getRise());
        moonSet = parseTime(moon.getSet());
        moonOverHead = parseTime(moonOverHeadDt);
        moonUnderFoot = parseTime(moonUnderFootDt);
        major = addMajor(cal, moonOverHeadDt, moonUnderFootDt, moon);
        minor = addMinor(cal, moon);
        if (!isMajor && !isMinor) {
            if (Integer.parseInt(moonDegree) > 0) {
                // convert times - Date.from(moon.getRise().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()).compareTo(cal.getTime()) < 0
                if (moonOverHeadDt != null && moonOverHeadDt.compareTo(cal.getTime()) > 0)
                    moonState = "M1";
                else
                    moonState = "M2";
                if ((moonOverHeadDt !=null) && (moon.getRise() != null) && (Date.from(moon.getRise().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()).compareTo(cal.getTime()) < 0 &&
                        Date.from(moon.getRise().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()).compareTo(moonOverHeadDt) > 0))
                    moonState = "M1";
            } else {
                if (moonUnderFootDt != null && moonUnderFootDt.compareTo(cal.getTime()) > 0)
                    moonState = "M3";
                else moonState = "M4";

                if (moonUnderFootDt != null && (moon.getSet() != null) && (Date.from(moon.getSet().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()).compareTo(cal.getTime()) < 0 &&
                        Date.from(moon.getSet().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()).compareTo(moonUnderFootDt) > 0))
                    moonState = "M3";
            }
        }
    }

    String parseTime(Date time) {
        if (time != null)
            return new SimpleDateFormat("h:mm a").format(time);
        return "N/A";
    }

    double getPhase(Calendar cal) {
        double MOON_PHASE_LENGTH = 29.530588853;//3.691323606625     1.8456618033125
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // Convert the year into the format expected by the algorithm.
        double transformedYear = year - Math.floor((12 - month) / 10);

        // Convert the month into the format expected by the algorithm.
        int transformedMonth = month + 9;
        if (transformedMonth >= 12) {
            transformedMonth = transformedMonth - 12;
        }

        // Logic to compute moon phase as a fraction between 0 and 1
        double term1 = Math.floor(365.25 * (transformedYear + 4712));
        double term2 = Math.floor(30.6 * transformedMonth + 0.5);
        double term3 = Math.floor(Math.floor((transformedYear / 100) + 49) * 0.75) - 38;

        double intermediate = term1 + term2 + day + 59;
        if (intermediate > 2299160) {
            intermediate = intermediate - term3;
        }

        double normalizedPhase = (intermediate - 2451550.1) / MOON_PHASE_LENGTH;
        normalizedPhase = normalizedPhase - Math.floor(normalizedPhase);
        if (normalizedPhase < 0) {
            normalizedPhase = normalizedPhase + 1;
        }

        // Return the result as a value between 0 and MOON_PHASE_LENGTH
        return normalizedPhase * MOON_PHASE_LENGTH;
    }

    String getMoonPhaseText(double phaseValue) {
        phaseValue = phaseValue + .5; // adjusting for location?
        //System.err.println("  " + phaseValue);
        if (phaseValue >= 27.6849270496875 || phaseValue < 1.8456618033125) {
            return "1 - New";
        } else if (phaseValue >= 1.8456618033125 && phaseValue < 5.5369854099375) {
            return "2 - Waxing Crescent";
        } else if (phaseValue >= 5.5369854099375 && phaseValue < 9.2283090165625) {
            return "3 - First Quarter";
        } else if (phaseValue >= 9.2283090165625 && phaseValue < 12.9196326231875) {
            return "4 - Waxing Gibbous";
        } else if (phaseValue >= 12.9196326231875 && phaseValue < 16.6109562298125) {
            return "5 - Full Moon";
        } else if (phaseValue >= 16.6109562298125 && phaseValue < 20.3022798364375) {
            return "6 - Waning Gibbous";
        } else if (phaseValue >= 20.3022798364375 && phaseValue < 23.9936034430625) {
            return "7 - Third Quarter";
        } else if (phaseValue >= 23.9936034430625 && phaseValue < 27.6849270496875) {
            return "8 - Waning Crescent";
        }
        return "No matching moon phase for " + phaseValue;
    }

    private String addMajor(Calendar cal, Date moonOverHead, Date moonUnderFoot, MoonTimes moon) {
        isMajor = false;
        Date curTime = cal.getTime();
        long overHead = moonOverHead == null ? 0 : moonOverHead.getTime();
        long underFoot = moonUnderFoot == null ? 0 : moonUnderFoot.getTime();
        Date underFootMajorStart = new Date(underFoot - 3600000);
        Date underFootMajorEnd = new Date(underFoot + 3600000);
        Date overHeadMajorStart = new Date(overHead - 3600000);
        Date overHeadMajorEnd = new Date(overHead + 3600000);

        moonDegree = Integer.toString((int) Math.toDegrees(SunCalc4JavaUtils.getMoonPosition(curTime, Double.parseDouble(latitude), Double.parseDouble(longitude)).get("altitude")));

        if (moonOverHead != null && moonUnderFoot != null && overHead > underFoot) {
            if (((underFootMajorStart.compareTo(curTime) < 0) && (underFootMajorEnd.compareTo(curTime) > 0))) {
                isMajor = true;
                moonState = "MU";
            }
            if (((overHeadMajorStart.compareTo(curTime) < 0) && (overHeadMajorEnd.compareTo(curTime) > 0))) {
                isMajor = true;
                moonState = "MO";
            }

            return parseTime(new Date(underFoot - 3600000)) + " - " +
                    parseTime(new Date(underFoot + 3600000)) + "    " +
                    parseTime(new Date(overHead - 3600000)) + " - " +
                    parseTime(new Date(overHead + 3600000));
        } else {
            String range = "";
            if (moonOverHead != null) {
                range = parseTime(new Date(overHead - 3600000)) + " - " +
                        parseTime(new Date(overHead + 3600000)) + "    ";
                if ((overHeadMajorStart.compareTo(curTime) < 0) && (overHeadMajorEnd.compareTo(curTime) > 0)) {
                    isMajor = true;
                    moonState = "MO";
                }
            } else
                range = "N/A    ";
            if (moonUnderFoot != null) {
                range += parseTime(new Date(underFoot - 3600000)) + " - " +
                        parseTime(new Date(underFoot + 3600000));
                if ((underFootMajorStart.compareTo(curTime) < 0) && (underFootMajorEnd.compareTo(curTime) > 0)) {
                    isMajor = true;
                    moonState = "MU";
                }
            } else
                range += "N/A";
            return range;
        }
    }

    private String addMinor(Calendar cal, MoonTimes moon) {
        isMinor = false;
        Date curTime = cal.getTime();
        long moonSet = (moon.getSet() == null) ? 0 : moon.getSet().toEpochSecond() * 1000;
        long moonRise = (moon.getRise() == null) ? 0 : moon.getRise().toEpochSecond() * 1000;
        Date riseMinorStart = new Date(moonRise - 1800000);
        Date riseMinorEnd = new Date(moonRise + 1800000);
        Date setMinorStart = new Date(moonSet - 1800000);
        Date setMinorEnd = new Date(moonSet + 1800000);
        if (moon.getSet() != null && moon.getRise() != null && moonSet < moonRise) {
            if (((riseMinorStart.compareTo(curTime) < 0) && (riseMinorEnd.compareTo(curTime) > 0))) {
                isMinor = true;
                moonState = "MR";
            }
            if (((setMinorStart.compareTo(curTime) < 0) && (setMinorEnd.compareTo(curTime) > 0))) {
                isMinor = true;
                moonState = "MS";
            }
            return parseTime(new Date(moonSet - 1800000)) + " - " +
                    parseTime(new Date(moonSet + 1800000)) + "    " +
                    parseTime(new Date(moonRise - 1800000)) + " - " +
                    parseTime(new Date(moonRise + 1800000));
        } else {
            String range = "";
            if (moon.getRise() != null) {
                range = parseTime(new Date(moonRise - 1800000)) + " - " +
                        parseTime(new Date(moonRise + 1800000)) + "    ";
                if ((riseMinorStart.compareTo(curTime) < 0) && (riseMinorEnd.compareTo(curTime) > 0)) {
                    isMinor = true;
                    moonState = "MR";
                }
            } else
                range = "N/A    ";
            if (moon.getSet() != null) {
                range += parseTime(new Date(moonSet - 1800000)) + " - " +
                        parseTime(new Date(moonSet + 1800000));
                if ((setMinorStart.compareTo(curTime) < 0) && (setMinorEnd.compareTo(curTime) > 0)) {
                    isMinor = true;
                    moonState = "MS";
                }
            } else
                range += "N/A";
            return range;
        }
    }

    public String getEventNotification(String time) {
        if (minor == null)
            return "";
        String minor1 = minor.split("    ")[0];
        String minor2 = minor.split("    ")[1];

        if (minor1.contains(time + " - "))
            return "Minor disappointment " + minor1 + " is starting - time to drink!";
        if (minor1.contains(" - " + time))
            return "Minor " + minor1 + " has ended - all clear!";
        if (minor2.contains(time + " - "))
            return "Minor disappointment " + minor2 + " is starting - time to drink!";
        if (minor2.contains(" - " + time))
            return "Minor " + minor2 + " has ended - all clear!";

        String major1 = major.split("    ")[0];
        String major2 = major.split("    ")[1];
        if (major1.contains(time + " - "))
            return "Major disappointment " + major1 + " is starting - time to drink!";
        if (major1.contains(" - " + time))
            return "Major " + major1 + " has ended - all clear!";
        if (major2.contains(time + " - "))
            return "Major disappointment " + major2 + " is starting - time to drink!";
        if (major2.contains(" - " + time))
            return "Major " + major2 + " has ended - all clear!";
        return "";
    }
}
