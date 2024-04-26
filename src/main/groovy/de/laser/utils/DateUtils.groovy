package de.laser.utils

import de.laser.storage.BeanStore
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Helper class for centralised date parsing and formatting
 */
@Slf4j
class DateUtils {

    public static final String DATE_FORMAT_NOTIME           = 'default.date.format.notime'          // yyyy-MM-dd --- dd.MM.yyyy
    public static final String DATE_FORMAT_NOTIME_SHORT     = 'default.date.format.notimeShort'     // yy-MM-dd --- dd.MM.yy
    public static final String DATE_FORMAT_NOZ              = 'default.date.format.noZ'             // yyyy-MM-dd HH:mm:ss --- dd.MM.yyyy HH:mm:ss

    // -- localized (current locale or DateUtils.getLocaleXY()) pattern/output

    /**
     * Returns the localised date format for the given token and locale constant
     * @param token the date format token
     * @param locale the {@link Locale} constant containing the language
     * @return a {@link SimpleDateFormat} using the given format
     */
    static SimpleDateFormat getLocalizedSDF_byTokenAndLocale(String token, Locale locale) {
        try {
            MessageSource messageSource = BeanStore.getMessageSource()
            String format = messageSource.getMessage(token, null, locale)

            return new SimpleDateFormat(format)
        } catch (Exception e) {
            log.warn "No localized date format/pattern found for ( ${token}, ${locale} )"
            log.warn e.getMessage()
        }
        return null
    }

    /**
     * Returns a localised MMM-yyyy formatter
     * @param locale the {@link Locale} constant containing the language
     * @return a {@link SimpleDateFormat} using the month-year format
     */
    static SimpleDateFormat getLocalizedSDF_MMMyyyy(Locale locale = null){
        return new SimpleDateFormat('MMM-yyyy', locale ?: LocaleUtils.getCurrentLocale())
    }

    /**
     * Returns a localised date format without time
     * @param locale the {@link Locale} constant containing the language
     * @return a {@link SimpleDateFormat} using the date format
     */
    static SimpleDateFormat getLocalizedSDF_noTime(Locale locale = null){
        getLocalizedSDF_byTokenAndLocale(DATE_FORMAT_NOTIME, locale ?: LocaleUtils.getCurrentLocale())
    }

    /**
     * Returns a localised shortened date format without time
     * @param locale the {@link Locale} constant containing the language
     * @return a {@link SimpleDateFormat} using the shortened date format
     */
    static SimpleDateFormat getLocalizedSDF_noTimeShort(Locale locale = null){
        getLocalizedSDF_byTokenAndLocale(DATE_FORMAT_NOTIME_SHORT, locale ?: LocaleUtils.getCurrentLocale())
    }

    /**
     * Returns a localised full date format without "Z" at the end
     * @param locale the {@link Locale} constant containing the language
     * @return a {@link SimpleDateFormat} using the full date format
     */
    static SimpleDateFormat getLocalizedSDF_noZ(Locale locale = null){
        getLocalizedSDF_byTokenAndLocale(DATE_FORMAT_NOZ, locale ?: LocaleUtils.getCurrentLocale())
    }

    // -- fixed pattern/output

    /**
     * Returns a date formatter in pattern dd.MM.yyy
     * @return a {@link SimpleDateFormat} using the dd.MM.yyy format
     */
    static SimpleDateFormat getSDF_ddMMyyy(){
        return new SimpleDateFormat('dd.MM.yyy')
    }

    /**
     * Returns a date formatter in pattern dd.MM.yyyy (i.e. German date-month format)
     * @return a {@link SimpleDateFormat} using the dd.MM.yyyy format
     */
    static SimpleDateFormat getSDF_ddMMyyyy(){
        return new SimpleDateFormat('dd.MM.yyyy')
    }

    /**
     * Returns a date formatter in pattern MM (two-digit month format)
     * @return a {@link SimpleDateFormat} using the two-digit month format
     */
    static SimpleDateFormat getSDF_MM(){
        return new SimpleDateFormat('MM')
    }

    /**
     * Returns a date formatter in pattern yy (two-digit year format)
     * @return a {@link SimpleDateFormat} using the two-digit year format
     */
    static SimpleDateFormat getSDF_yy(){
        return new SimpleDateFormat('yy')
    }

    /**
     * Returns a date formatter in pattern yyyy (four-digit year format)
     * @return a {@link SimpleDateFormat} using the four-digit year format
     */
    static SimpleDateFormat getSDF_yyyy(){
        return new SimpleDateFormat('yyyy')
    }

    /**
     * Returns a date formatter in pattern yyyy-MM (year-moth)
     * @return a {@link SimpleDateFormat} using the four-digit year-month format
     */
    static SimpleDateFormat getSDF_yyyyMM(){
        return new SimpleDateFormat('yyyy-MM')
    }

    /**
     * Returns a date formatter in pattern yyyy-MM-dd (year-moth-day)
     * @return a {@link SimpleDateFormat} using the four-digit year-month-day format
     */
    static SimpleDateFormat getSDF_yyyyMMdd(){
        return new SimpleDateFormat('yyyy-MM-dd')
    }

    /**
     * Returns a date formatter in pattern yyyy-MM-dd hh:mm:SS.S
     * @return a {@link SimpleDateFormat} using the full date-time format incl. milliseconds
     */
    static SimpleDateFormat getSDF_yyyyMMdd_hhmmSSS(){
        return new SimpleDateFormat('yyyy-MM-dd hh:mm:SS.S')
    }

    /**
     * Returns a date formatter in pattern yyyy-MM-dd hh:mm:SS
     * @return a {@link SimpleDateFormat} using the full date-time format
     */
    static SimpleDateFormat getSDF_yyyyMMdd_HHmmss(){
        return new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
    }

    /**
     * Returns a date formatter in pattern yyyy-MM-dd hh:mm:ss.S
     * @return a {@link SimpleDateFormat} using the full date-time format incl. milliseconds
     */
    static SimpleDateFormat getSDF_yyyyMMdd_HHmmssS(){
        return new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.S')
    }

    /**
     * Returns a date formatter in pattern yyyy-MM-ddThh:mm:ss without Z
     * @return a {@link SimpleDateFormat} using the XML date-time format without Z
     */
    static SimpleDateFormat getSDF_yyyyMMddTHHmmss(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }

    /**
     * Returns a date formatter in pattern yyyy-MM-ddThh:mm:ssZ
     * @return a {@link SimpleDateFormat} using the XML date-time format with Z
     */
    static SimpleDateFormat getSDF_yyyyMMddTHHmmssZ(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    }

    // --

    /**
     * Returns a filename-usable date-time formatter
     * @return a {@link SimpleDateFormat} using format yyyyMMdd-HHmm suitable for filenames
     */
    static SimpleDateFormat getSDF_forFilename(){
        return new SimpleDateFormat('yyyyMMdd-HHmm')
    }

    /**
     * Returns a year-month-date format without dots
     * @return a {@link SimpleDateFormat} using format yyyyMMdd
     */
    static SimpleDateFormat getSDF_noTimeNoPoint(){
        return new SimpleDateFormat('yyyyMMdd')
    }

    /**
     * Returns a time formatter without date
     * @return a {@link SimpleDateFormat} using format HH:mm:ss
     */
    static SimpleDateFormat getSDF_onlyTime(){
        return new SimpleDateFormat('HH:mm:ss')
    }

    // -- other stuff ..

    /**
     * Extracts the year from the given date and returns it as integer
     * @param date the {@link Date} from which the year should be extracted
     * @return the year or null if no (valid) date has been submitted
     */
    static int getYearAsInteger(Date date) {
        date ? getSDF_yyyy().format(date).toInteger() : null
    }

    /**
     * Parses the given string; an attempt is made for various formats (in this order):
     * <ul>
     *     <li>yyyy-MM-ddTHH:mm:ssZ</li>
     *     <li>yyyy-MM-ddTHH:mm:ss.S</li>
     *     <li>yyyy-MM-dd</li>
     *     <li>yyyy/MM/dd</li>
     *     <li>dd.MM.yy</li>
     *     <li>dd.MM.yyyy</li>
     *     <li>MM.yy</li>
     *     <li>MM.yyyy</li>
     *     <li>dd/MM/yy</li>
     *     <li>dd/MM/yyyy</li>
     *     <li>yyyy/MM</li>
     * </ul>
     * @param value the date string to parse
     * @return the parsed {@link Date} object or null if no formatter matched
     */
    static Date parseDateGeneric(String value) {
        Date parsed_date = null

        List<SimpleDateFormat> supportedFormats = [
                getSDF_yyyyMMddTHHmmssZ(),
                getSDF_yyyyMMdd_HHmmssS(),
                getSDF_yyyyMMdd(),
                new SimpleDateFormat('yyyy/MM/dd'),
                new SimpleDateFormat('dd.MM.yy'),
                getSDF_ddMMyyyy(),
                new SimpleDateFormat('MM.yy'),
                new SimpleDateFormat('MM.yyyy'),
                new SimpleDateFormat('dd/MM/yy'),//Parsing was wrong, needs to be under supervision
                new SimpleDateFormat('dd/MM/yyyy'),
                new SimpleDateFormat('yyyy/MM'),
                new SimpleDateFormat('yyyy-MM'),
                getSDF_yyyy()
        ]

        if (value && (value.trim().length() > 0)) {
            for (Iterator<SimpleDateFormat> i = supportedFormats.iterator(); (i.hasNext() && (parsed_date == null));) {
                SimpleDateFormat next = i.next()
                try {
                    parsed_date = next.parse(value)
                }
                catch (Exception e) {
                    //log.debug("Parser for ${next.toPattern()} could not parse date ${value}. Trying next one ...")
                }
            }
        }
        parsed_date
    }

    /**
     * Checks if the given string is a valid date format
     * @param value the string to check
     * @return true if one of the validator matches, false otherwise
     */
    static boolean isDate(String value) {
        //'yyyy-MM-dd'
        if (value.length() == 10 && value ==~ /\d{4}\-\d{2}\-\d{2}/) {
            return true
        }
        //'yyyy/MM/dd'
        if (value.length() == 10 && value ==~ /\d{4}\/\d{2}\/\d{2}/) {
            return true
        }
        //'dd.MM.yyyy'
        if (value.length() == 10 && value ==~ /\d{2}\.\d{2}\.\d{4}/) {
            return true
        }
        //'MM.yyyy'
        if (value.length() == 7 && value ==~ /\d{2}\.\d{4}/) {
            return true
        }
        //'dd/MM/yyyy'
        if (value.length() == 10 && value ==~ /\d{2}\/\d{2}\/\d{4}/) {
            return true
        }
        //'yyyy/MM'
        if (value.length() == 7 && value ==~ /\d{4}\/\d{2}/) {
            return true
        }
        return false
    }

    /**
     * Checks if the given date object is the current day
     * @param date the {@link Date} to verify
     * @return true if the given date is the current day, false otherwise
     */
    static boolean isDateToday(Date date) {
        if (date) {
            return getSDF_noTimeNoPoint().format(date) == getSDF_noTimeNoPoint().format(new Date())
        }
        false
    }

    // --

    /**
     * Converts the given {@link Date} object into a {@link LocalDate} equivalent
     * @param date the {@link Date} object to convert
     * @return the {@link LocalDate} equivalent
     */
    static LocalDate dateToLocalDate(Date date) {
        if (!date) {
            //log.debug 'DateUtils.dateToLocalDate( NULL )'
            return null
        }
        LocalDate.ofInstant( date.toInstant(), ZoneId.systemDefault())
    }

    /**
     * Converts the given {@link Date} object into a {@link LocalDateTime} equivalent
     * @param date the {@link Date} object to convert
     * @return the {@link LocalDateTime} equivalent
     */
    static LocalDateTime dateToLocalDateTime(Date date) {
        if (!date) {
            //log.debug 'DateUtils.dateToLocalDateTime( NULL )'
            return null
        }
        LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault())
    }

    /**
     * Converts the given {@link java.util.Date} object into a {@link java.sql.Date} equivalent
     * @param date the {@link java.util.Date} object to convert
     * @return the {@link java.sql.Date} equivalent
     */
    static java.sql.Date localDateToSqlDate(LocalDate localDate) {
        if (!localDate) {
            //log.debug 'DateUtils.localDateToSqlDate( NULL )'
            return null
        }
        java.sql.Date.valueOf(localDate) //java.sql.Date extends java.util.Date
    }
}
