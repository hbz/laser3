package de.laser.utils

import de.laser.storage.BeanStore
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Slf4j
class DateUtils {

    public static final String DATE_FORMAT_NOTIME           = 'default.date.format.notime'          // yyyy-MM-dd --- dd.MM.yyyy
    public static final String DATE_FORMAT_NOTIME_SHORT     = 'default.date.format.notimeShort'     // yy-MM-dd --- dd.MM.yy
    public static final String DATE_FORMAT_NOZ              = 'default.date.format.noZ'             // yyyy-MM-dd HH:mm:ss --- dd.MM.yyyy HH:mm:ss

    // -- localized (current locale or DateUtils.getLocaleXY()) pattern/output

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

    static SimpleDateFormat getLocalizedSDF_MMMyyyy(Locale locale = null){
        return new SimpleDateFormat('MMM-yyyy', locale ?: LocaleUtils.getCurrentLocale())
    }

    static SimpleDateFormat getLocalizedSDF_noTime(Locale locale = null){
        getLocalizedSDF_byTokenAndLocale(DATE_FORMAT_NOTIME, locale ?: LocaleUtils.getCurrentLocale())
    }

    static SimpleDateFormat getLocalizedSDF_noTimeShort(Locale locale = null){
        getLocalizedSDF_byTokenAndLocale(DATE_FORMAT_NOTIME_SHORT, locale ?: LocaleUtils.getCurrentLocale())
    }

    static SimpleDateFormat getLocalizedSDF_noZ(Locale locale = null){
        getLocalizedSDF_byTokenAndLocale(DATE_FORMAT_NOZ, locale ?: LocaleUtils.getCurrentLocale())
    }

    // -- fixed pattern/output

    static SimpleDateFormat getSDF_ddMMyyy(){
        return new SimpleDateFormat('dd.MM.yyy')
    }

    static SimpleDateFormat getSDF_ddMMyyyy(){
        return new SimpleDateFormat('dd.MM.yyyy')
    }

    static SimpleDateFormat getSDF_yy(){
        return new SimpleDateFormat('yy')
    }

    static SimpleDateFormat getSDF_yyyy(){
        return new SimpleDateFormat('yyyy')
    }

    static SimpleDateFormat getSDF_yyyyMM(){
        return new SimpleDateFormat('yyyy-MM')
    }

    static SimpleDateFormat getSDF_yyyyMMdd(){
        return new SimpleDateFormat('yyyy-MM-dd')
    }

    static SimpleDateFormat getSDF_yyyyMMdd_hhmmSSS(){
        return new SimpleDateFormat('yyyy-MM-dd hh:mm:SS.S')
    }

    static SimpleDateFormat getSDF_yyyyMMdd_HHmmss(){
        return new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
    }

    static SimpleDateFormat getSDF_yyyyMMdd_HHmmssS(){
        return new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.S')
    }

    static SimpleDateFormat getSDF_yyyyMMddTHHmmss(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }

    static SimpleDateFormat getSDF_yyyyMMddTHHmmssZ(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    }

    // --

    static SimpleDateFormat getSDF_forFilename(){
        return new SimpleDateFormat('yyyyMMdd-HHmm')
    }

    static SimpleDateFormat getSDF_noTimeNoPoint(){
        return new SimpleDateFormat('yyyyMMdd')
    }

    static SimpleDateFormat getSDF_onlyTime(){
        return new SimpleDateFormat('HH:mm:ss')
    }

    // -- other stuff ..

    static int getYearAsInteger(Date date) {
        date ? getSDF_yyyy().format(date).toInteger() : null
    }

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

    static boolean isDateToday(Date date) {
        if (date) {
            return getSDF_noTimeNoPoint().format(date) == getSDF_noTimeNoPoint().format(new Date())
        }
        false
    }

    // --

    static LocalDate dateToLocalDate(Date date) {
        if (!date) {
            //log.debug 'DateUtils.dateToLocalDate( NULL )'
            return null
        }
        LocalDate.ofInstant( date.toInstant(), ZoneId.systemDefault())
    }

    static LocalDateTime dateToLocalDateTime(Date date) {
        if (!date) {
            //log.debug 'DateUtils.dateToLocalDateTime( NULL )'
            return null
        }
        LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault())
    }

    static Date localDateToSqlDate(LocalDate localDate) {
        if (!localDate) {
            //log.debug 'DateUtils.localDateToSqlDate( NULL )'
            return null
        }
        java.sql.Date.valueOf(localDate) //java.sql.Date extends java.util.Date
    }
}
