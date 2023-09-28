package com.badasstechie.identity.util;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class Time {
    public static String timeAgo(Instant thenInstant) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime then = ZonedDateTime.ofInstant(thenInstant, now.getZone());

        if (then.getYear() < now.getYear()) {
            // if time is older than this year, return years ago
            int years = Math.abs(now.getYear() - then.getYear());
            return years + " year" + (years > 1 ? "s" : "") + " ago";
        }
        else if (then.getMonthValue() < now.getMonthValue()) {
            // if time is older than this month, return months ago
            int months = Math.abs(now.getMonthValue() - then.getMonthValue());
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        }
        else if (getWeekNumber(then) < getWeekNumber(now)) {
            // if time is older than this week, return weeks ago
            int weeks = Math.abs(getWeekNumber(now) - getWeekNumber(then));
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        }
        else if (then.getDayOfYear() < now.getDayOfYear()) {
            // if time is older than today, return days ago
            int days = Math.abs(now.getDayOfYear() - then.getDayOfYear());
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }
        else if (then.getHour() < now.getHour()) {
            // if time is older than this hour, return hours ago
            int hours = Math.abs(now.getHour() - then.getHour());
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }
        else if (then.getMinute() < now.getMinute()) {
            // if time is older than this minute, return minutes ago
            int minutes = Math.abs(now.getMinute() - then.getMinute());
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }
        else {
            // if time is older than this second, return seconds ago
            int seconds = Math.abs(now.getSecond() - then.getSecond());
            return seconds + " second" + (seconds > 1 ? "s" : "") + " ago";
        }
    }

    public static String formatTime(Instant thenInstant) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime then = ZonedDateTime.ofInstant(thenInstant, now.getZone());

        if (then.getYear() < now.getYear()) {
            // if time is older than this year, return month, day, and year
            String month = then.getMonth().toString();
            return month.charAt(0) + month.substring(1, 3).toLowerCase(Locale.ROOT) + " " + then.getDayOfMonth() + ", " + then.getYear();
        }
        else if (getWeekNumber(then) < getWeekNumber(now)) {
            // if time is older than this week, return month and day
            String month = then.getMonth().toString();
            return month.charAt(0) + month.substring(1, 3).toLowerCase(Locale.ROOT) + " " + then.getDayOfMonth();
        }
        else if (then.getDayOfYear() < now.getDayOfYear()) {
            // if time is older than today, return day of week
            String dayOfWeek = then.getDayOfWeek().toString();
            return dayOfWeek.charAt(0) + dayOfWeek.substring(1, 3).toLowerCase(Locale.ROOT);
        }
        else {
            // if time is today, return time
            return String.format("%02d:%02d", then.getHour(), then.getMinute());
        }
    }

    private static int getWeekNumber(ZonedDateTime date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfWeekBasedYear());
    }
}
