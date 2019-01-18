package de.laser.helper

import java.text.SimpleDateFormat
import java.sql.Date
import java.sql.Timestamp

class SqlDateUtils {

    // ist getestet
    static boolean isToday(date) {
        SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(date).compareTo(simpleDateFormat.format(new Date(System.currentTimeMillis())))) == 0
    }
    static boolean isYesterday(date) {
        def yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        yesterday = new Date(yesterday.getTimeInMillis())
        SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(date).compareTo(simpleDateFormat.format(yesterday))) == 0
    }
    static boolean isYesterdayOrToday(date){
        isYesterday(date) || isToday(date)
    }
    // ist getestet
    static boolean isBeforeToday(date) {
        SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(date).compareTo(simpleDateFormat.format(new Date(System.currentTimeMillis())))) < 0
    }
    //TODO testen!
    static boolean isAfterToday(date) {
        SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(date).compareTo(simpleDateFormat.format(new Date(System.currentTimeMillis())))) > 0
    }
    //TODO testen!
    static boolean isDateBetween(dateToTest, Date fromDate, Date toDate) {
        SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyyMMdd")
        (simpleDateFormat.format(dateToTest).compareTo(simpleDateFormat.format(fromDate))) >= 0 &&
        (simpleDateFormat.format(toDate).compareTo(simpleDateFormat.format(dateToTest))) >= 0
    }
    //ist getestet
    static boolean isDateBetweenTodayAndReminderPeriod(dateToTest, int reminderPeriod) {
        Date today = new Date(System.currentTimeMillis())
        Date infoDate = getDateInNrOfDays(reminderPeriod)
        isDateBetween(dateToTest, today, infoDate)
    }
    // ist getestet
    static Date getDateInNrOfDays(int nrOfDays) {
        Calendar cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_WEEK, nrOfDays)
        new Date(cal.getTime().getTime())
    }
}
