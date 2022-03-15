import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset
import java.text.SimpleDateFormat

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// --[ filter

class ShrinkFilter extends Filter<ILoggingEvent> {

    @Override
    FilterReply decide(ILoggingEvent event) {
        if (event.getMessage().contains("HHH020003")) {
            // WARN
            // Could not find a specific ehcache configuration for cache named [..]; using defaults
            return FilterReply.DENY
        }
        else if (event.getMessage().contains("HHH020008")) {
            // WARN
            // Cache[..] Key[..] A soft-locked cache entry was expired by the underlying cache. If this happens regularly you should consider increasing the cache timeouts and/or capacity limits
            return FilterReply.DENY
        }
        else if (event.getMessage().contains("HHH90000022")) {
            // WARN
            // Hibernate's legacy org.hibernate.Criteria API is deprecated; use the JPA javax.persistence.criteria.CriteriaQuery instead
            return FilterReply.DENY
        }
        else if (event.getMessage().contains("HHH90001005")) {
            // INFO
            // Cache[..] Key[..] A soft-locked cache entry was expired by the underlying cache. If this happens regularly you should consider increasing the cache timeouts and/or capacity limits
            return FilterReply.DENY
        }
        else if (event.getMessage().contains("Cached entry expired :")) {
            return FilterReply.DENY
        }
        else if (event.getMessage().contains("Replaced rule for ")) {
            return FilterReply.DENY
        }
        return FilterReply.ACCEPT
    }
}

// --[ appender

appender('STDOUT', ConsoleAppender) {
    // TODO -- REMOVE --
    // TODO -- REMOVE --
    filter(ShrinkFilter)
    // TODO -- REMOVE --
    // TODO -- REMOVE --
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern =
            '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' +
                    '%clr(%5p) ' + // Log level
                    '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                    '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                    '%m%n%wex'
    }
}

String targetDir = "${BuildSettings.BASE_DIR}/logs"
String date = "${(new SimpleDateFormat("yyyy-MM-dd")).format(new Date())}"

if (Environment.isDevelopmentMode() && targetDir != null) {

    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/errors.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} | %level %logger{32} [%file:%line] - %msg%n"
        }
    }
    appender("LOGFILE", FileAppender) {
        file = "${targetDir}/${date}.log"
        append = true
        filter(ShrinkFilter)
        encoder(PatternLayoutEncoder) {
            pattern = "%d{HH:mm:ss.SSS} | %level [%file:%line] - %nopex%msg%n"
        }
    }

    logger("StackTrace",    ERROR, ['FULL_STACKTRACE'], false)

    logger("de.laser",      DEBUG, ['LOGFILE'], false)
    logger("com.k_int",     DEBUG, ['LOGFILE'], false)

    root(INFO, ['LOGFILE'])
}

logger("de.laser",      DEBUG, ['STDOUT'], false)
logger("com.k_int",     DEBUG, ['STDOUT'], false)

root(INFO, ['STDOUT'])


