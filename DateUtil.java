package com.example.util;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    // Compare two java.sql.Date and return the difference in days
    public static long getDaysDifference(Date date1, Date date2) {
        LocalDate localDate1 = date1.toLocalDate();
        LocalDate localDate2 = date2.toLocalDate();
        return ChronoUnit.DAYS.between(localDate1, localDate2);
    }

    // Check if the date is before the current date
    public static boolean isBeforeCurrentDate(Date date) {
        LocalDate localDate = date.toLocalDate();
        return localDate.isBefore(LocalDate.now());
    }

    // Check if the date is after the current date
    public static boolean isAfterCurrentDate(Date date) {
        LocalDate localDate = date.toLocalDate();
        return localDate.isAfter(LocalDate.now());
    }

    public static void main(String[] args) {
        Date date1 = Date.valueOf("2023-12-01");
        Date date2 = Date.valueOf("2024-01-01");

        System.out.println("Difference in days: " + getDaysDifference(date1, date2));
        System.out.println("Is date1 before current date? " + isBeforeCurrentDate(date1));
        System.out.println("Is date2 after current date? " + isAfterCurrentDate(date2));
    }
}