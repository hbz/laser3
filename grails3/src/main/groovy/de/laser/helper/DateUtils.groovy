package de.laser.helper

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.text.SimpleDateFormat

class DateUtils {

    static Log static_logger = LogFactory.getLog(DateUtils)

    public static final String DATE_FORMAT_NOTIME_NOPOINT   = 'default.date.format.notimenopoint'
    public static final String DATE_FORMAT_NOTIME           = 'default.date.format.notime'
    public static final String DATE_FORMAT_NOZ              = 'default.date.format.noZ'
    public static final String DATE_FORMAT_ONLYTIME         = 'default.date.format.onlytime'

    /** This is a Utility-Class. There are only static methods. It is not necessary to create an instance */
    private DateUtils(){}

    static SimpleDateFormat getSimpleDateFormatByToken(String token) {
        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        String format = messageSource.getMessage(token, null, locale)

        if (format) {
            return new SimpleDateFormat(format)
        }
        else {
            static_logger.debug("WARNING: no date format found for ( ${token}, ${locale} )")

        }
        return null
    }

    static SimpleDateFormat getSDF_NoTimeNoPoint(){
        getSimpleDateFormatByToken(DATE_FORMAT_NOTIME_NOPOINT)
    }

    static SimpleDateFormat getSDF_NoTime(){
        getSimpleDateFormatByToken(DATE_FORMAT_NOTIME)
    }

    static SimpleDateFormat getSDF_NoZ(){
        getSimpleDateFormatByToken(DATE_FORMAT_NOZ)
    }

    static SimpleDateFormat getSDF_OnlyTime(){
        getSimpleDateFormatByToken(DATE_FORMAT_ONLYTIME)
    }

    static SimpleDateFormat getSDF_ymd(){
        return new SimpleDateFormat('yyyy-MM-dd')
    }

    static SimpleDateFormat getSDF_dmy(){
        return new SimpleDateFormat('dd.MM.yyyy')
    }

    static SimpleDateFormat getSDF_forFilename(){
        return new SimpleDateFormat('yyyyMMdd-HHmm')
    }

    static SimpleDateFormat getSDF_yearMonth(){
        return new SimpleDateFormat('yyyy-MM')
    }

    static SimpleDateFormat getSDF_year(){
        return new SimpleDateFormat('yyyy')
    }

    static Date toDate_NoTime(String value) {
        (Date) getSDF_NoTime()?.parseObject(value)
    }

    // --

    static int getYearAsInteger(Date date) {
        date ? new SimpleDateFormat('yyyy').format(date).toInteger() : null
    }

    // --

    static Date parseDateGeneric(String value) {
        Date parsed_date

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
                    //static_logger.debug("Parser for ${next.toPattern()} could not parse date ${value}. Trying next one ...")
                }
            }
        }
        parsed_date
    }

    static boolean  isDate(String value) {
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
}
