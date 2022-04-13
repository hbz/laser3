package de.laser.helper

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class MigrationHelper {

    static final String DE_U_CO_PHONEBK_X_ICU   = "de-u-co-phonebk-x-icu"
    static final String EN_US_U_VA_POSIX_X_ICU  = "en-US-u-va-posix-x-icu"

    static LocalDate dateToLocalDate(Date date) {
        if (!date) {
            //println 'MigrationHelper.dateToLocalDate( NULL )'
            return null
        }
        LocalDate.ofInstant( date.toInstant(), ZoneId.systemDefault())
    }

    static LocalDateTime dateToLocalDateTime(Date date) {
        if (!date) {
            //println 'MigrationHelper.dateToLocalDateTime( NULL )'
            return null
        }
        LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault())
    }

    static Date localDateToSqlDate(LocalDate localDate) {
        if (!localDate) {
            //println 'MigrationHelper.localDateToSqlDate( NULL )'
            return null
        }
        java.sql.Date.valueOf(localDate) //java.sql.Date extends java.util.Date
    }
}
