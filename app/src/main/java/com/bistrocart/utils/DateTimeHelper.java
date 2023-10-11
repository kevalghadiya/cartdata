package com.bistrocart.utils;

import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeHelper {
    private static final String TAG = "DateTimeHelper";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";


    public static Calendar getCalendarObj(String strDate, String format, boolean returnDefaultIfError) {
        if (TextUtils.isEmpty(strDate)) {
            if (returnDefaultIfError) {
                return Calendar.getInstance();
            } else {
                return null;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            Date date = sdf.parse(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            if (returnDefaultIfError) {
                return Calendar.getInstance();
            } else {
                return null;
            }
        }
    }

    public static String convertFormat(String strDate, String format, String convertFormat) {
        if (TextUtils.isEmpty(strDate)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        SimpleDateFormat sdfNew = new SimpleDateFormat(convertFormat, Locale.getDefault());

        try {
            Date date = sdf.parse(strDate);
            return sdfNew.format(date);
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    public static String convertFormat(Date date, String convertFormat) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdfNew = new SimpleDateFormat(convertFormat, Locale.getDefault());
        return sdfNew.format(date);
    }

    public static String getDate(String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            Date date = new Date();
            return sdf.format(date);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    public static String getDate(Calendar calendar, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            return sdf.format(calendar.getTime());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    public static String getAge(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(strDate));

            int diff = Calendar.getInstance().get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
            return diff + "";
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }


    public static String getTimeAgo(String dateStr, String dateFormat) {
        if (TextUtils.isEmpty(dateStr))
            return "";

        String timeAgo = "";

        try {
            SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
            Date past = format.parse(dateStr);
            Date now = new Date();
            long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
            if (seconds < 60) {
                timeAgo = seconds + " seconds ago";

            } else if (minutes < 60) {
                timeAgo = minutes + " minutes ago";

            } else if (hours < 24) {
                timeAgo = hours + " hours ago";

            } else if (days < 30) {
                timeAgo = days + " days ago";
            } else {
                timeAgo = convertFormat(dateStr, dateFormat, "dd MMM yyyy");
            }
            return timeAgo;

        } catch (Exception j) {
//            AppLog.e(TAG, j.getLocalizedMessage());
            return timeAgo;
        }


    }
}