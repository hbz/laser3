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

    /**
     * Indicates whether the job is currently running
     * @return the lock state
     */
    boolean isRunning() {
        jobIsRunning
    }

    /**
     * Checks if the job can be started and records the start if so
     * @param startEventToken the token which identifies the given job in the {@link SystemEvent} records
     * @param suppressLog should the log output be suppressed?
     * @return true if the job could be started successfully, false otherwise
     */
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

    /**
     * Records the end of the job running and releases the lock
     * @param stopEventToken the token which identifies the given job in the {@link SystemEvent} records
     * @param suppressLog should the log output be suppressed?
     */
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
