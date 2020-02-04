package de.laser.helper

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.text.SimpleDateFormat

class DateUtil {

    static Log static_logger = LogFactory.getLog(DateUtil)

    public static final String DATE_FORMAT_NOTIME_NOPOINT   = 'default.date.format.notimenopoint'
    public static final String DATE_FORMAT_NOTIME           = 'default.date.format.notime'
    public static final String DATE_FORMAT_NOZ              = 'default.date.format.noZ'
    public static final String DATE_FORMAT_ONLYTIME         = 'default.date.format.onlytime'


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

    static Date toDate_NoTime(String value) {
        (Date) getSDF_NoTime()?.parseObject(value)
    }
}
