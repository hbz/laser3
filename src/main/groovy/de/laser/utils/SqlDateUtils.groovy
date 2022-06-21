package de.laser.utils

import groovy.transform.CompileStatic

import java.sql.Date
import java.text.SimpleDateFormat

/**
 * A helper class to perform date comparisons
 */
@CompileStatic
class SqlDateUtils {

    /**
     * Is the given date the current day?
     * @param date the {@link Date} to check
     * @return is the difference of days equal to 0 (= do we have the same day)?
     */
    static boolean isToday(date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(date).compareTo(simpleDateFormat.format(new Date(System.currentTimeMillis())))) == 0
    }

    /**
     * Is the given date one day before the current day?
     * @param date the {@link Date} to check
     * @return is the difference of days equal to 0 (= do we have the same day, i.e. today - 1)?
     */
    static boolean isYesterday(date) {
        def yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        yesterday = new Date(yesterday.getTimeInMillis())
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(date).compareTo(simpleDateFormat.format(yesterday))) == 0
    }

    /**
     * Checks whether the given date is today's or yesterday's date
     * @param date the {@link Date} to check
     * @return is the given day the current day or precedes it by one day?
     */
    static boolean isYesterdayOrToday(date){
        isYesterday(date) || isToday(date)
    }

    /**
     * Checks whether the given date is before today
     * @param date the {@link Date} to check
     * @return is the given date in the past?
     */
    static boolean isBeforeToday(date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(date).compareTo(simpleDateFormat.format(new Date(System.currentTimeMillis())))) < 0
    }

    /**
     * Checks whether the given date is after today
     * @param date the {@link Date} to check
     * @return is the given date in the future?
     */
    static boolean isAfterToday(date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(date).compareTo(simpleDateFormat.format(new Date(System.currentTimeMillis())))) > 0
    }

    /**
     * Checks whether the given date is in the given span of time
     * @param dateToTest the {@link Date} to check
     * @param fromDate the start {@link Date} of the time span to check
     * @param toDate the end {@link Date} of the time span to check
     * @return is the given date in the time span to check?
     */
    static boolean isDateBetween(dateToTest, Date fromDate, Date toDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(dateToTest).compareTo(simpleDateFormat.format(fromDate))) >= 0 &&
        (simpleDateFormat.format(toDate).compareTo(simpleDateFormat.format(dateToTest))) >= 0
    }

    /**
     * Check whether the given date between the current day and the reminder period
     * @param dateToTest the {@link Date} to check
     * @param reminderPeriod the number of days until the reminder period
     * @return is the given date between today and the reminder period?
     */
    static boolean isDateBetweenTodayAndReminderPeriod(dateToTest, int reminderPeriod) {
        Date today = new Date(System.currentTimeMillis())
        Date infoDate = getDateInNrOfDays(reminderPeriod)
        isDateBetween(dateToTest, today, infoDate)
    }

    /**
     * Calculates the date object from the given date interval
     * @param nrOfDays the number of days to add to today
     * @return the date which will be in nrOfDays
     */
    static Date getDateInNrOfDays(int nrOfDays) {
        Calendar cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_WEEK, nrOfDays) //Calendar.DAY_OF_WEEK does the job; is synonym of Calendar.DAY / Calendar.DATE
        new Date(cal.getTime().getTime())
    }
}
