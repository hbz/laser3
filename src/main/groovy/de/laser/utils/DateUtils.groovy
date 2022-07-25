package de.laser.utils

import de.laser.storage.BeanStore
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Slf4j
class DateUtils {

    public static final String DATE_FORMAT_NOTIME_NOPOINT   = 'default.date.format.notimenopoint'
    public static final String DATE_FORMAT_NOTIME           = 'default.date.format.notime'
    public static final String DATE_FORMAT_NOZ              = 'default.date.format.noZ'
    public static final String DATE_FORMAT_ONLYTIME         = 'default.date.format.onlytime'

    // -- localized

    static SimpleDateFormat getLocalizedSDF_byToken(String token) {
        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleContextHolder.getLocale()
        String format = messageSource.getMessage(token, null, locale)

        if (format) {
            return new SimpleDateFormat(format)
        }
        else {
            log.warn("No date format found for ( ${token}, ${locale} )")
        }
        return null
    }

    static SimpleDateFormat getLocalizedSDF_noTime(){
        getLocalizedSDF_byToken(DATE_FORMAT_NOTIME)
    }

    static SimpleDateFormat getLocalizedSDF_noTimeNoPoint(){
        getLocalizedSDF_byToken(DATE_FORMAT_NOTIME_NOPOINT)
    }

    static SimpleDateFormat getLocalizedSDF_noZ(){
        getLocalizedSDF_byToken(DATE_FORMAT_NOZ)
    }

    static SimpleDateFormat getLocalizedSDF_onlyTime(){
        getLocalizedSDF_byToken(DATE_FORMAT_ONLYTIME)
    }

    // -- fixed: no localization

    static SimpleDateFormat getSDF_ddMMyyy(){
        return new SimpleDateFormat('dd.MM.yyy')
    }

    static SimpleDateFormat getSDF_ddMMyyyy(){
        return new SimpleDateFormat('dd.MM.yyyy')
    }

    static SimpleDateFormat getSDF_MMMyyyy(){
        return new SimpleDateFormat('MMM-yyyy')
    }

    static SimpleDateFormat getSDF_yy(){
        return new SimpleDateFormat('yy')
    }

    static SimpleDateFormat getSDF_yyyy(){
        return new SimpleDateFormat('yyyy')
    }

    static SimpleDateFormat getSDF_YYYY(){
        return new SimpleDateFormat('YYYY')
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

    // --

    static int getYearAsInteger(Date date) {
        date ? getSDF_yyyy().format(date).toInteger() : null
    }

    // --

    static Date parseDateGeneric(String value) {
        Date parsed_date = null

        List<SimpleDateFormat> supportedFormats = [
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"),
                new SimpleDateFormat('yyyy-MM-dd'),
                new SimpleDateFormat('yyyy/MM/dd'),
                new SimpleDateFormat('dd.MM.yy'),
                new SimpleDateFormat('dd.MM.yyyy'),
                new SimpleDateFormat('MM.yy'),
                new SimpleDateFormat('MM.yyyy'),
                new SimpleDateFormat('dd/MM/yy'),//Parsing was wrong, needs to be under supervision
                new SimpleDateFormat('dd/MM/yyyy'),
                new SimpleDateFormat('yyyy/MM'),
                new SimpleDateFormat('yyyy')
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
