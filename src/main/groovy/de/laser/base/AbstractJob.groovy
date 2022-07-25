package de.laser.base

import de.laser.system.SystemEvent
import groovy.util.logging.Slf4j

/**
 * Abstract class for scheduled cronjobs.
 */
@Slf4j
abstract class AbstractJob {

    static List<List> configurationProperties = []

    boolean jobIsRunning = false

    abstract boolean isAvailable()

    boolean isRunning() {
        jobIsRunning
    }

    protected boolean start(String startEventToken = null, boolean suppressLog = false) {
        if (! isAvailable()) {
            return false
        }
        else {
            jobIsRunning = true

            if (!suppressLog) {
                log.info ' -> ' + this.class.simpleName + ' started'
            }
            if (startEventToken) {
                SystemEvent.createEvent( startEventToken )
            }
        }
        true
    }

    protected void stop(String stopEventToken = null, boolean suppressLog = false) {
        if (!suppressLog) {
            log.info ' -> ' + this.class.simpleName + ' finished'
        }
        if (stopEventToken) {
            SystemEvent.createEvent( stopEventToken )
        }

        jobIsRunning = false
    }
}
