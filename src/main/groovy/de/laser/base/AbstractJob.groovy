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

    protected boolean simpleStart(boolean suppressLog = false) {
        if (isAvailable()) {
            if (!suppressLog) {
                log.info ' -> ' + this.class.simpleName + ' started'
            }
            jobIsRunning = true
            return true
        }
        return false
    }
    protected void simpleStop(boolean suppressLog = false) {
        if (!suppressLog) {
            log.info ' -> ' + this.class.simpleName + ' finished'
        }
        jobIsRunning = false
    }

    /**
        Flags jobIsRunning if available and returns systemEvent or null if not
     */
    protected SystemEvent start(String startToken) {
        if (isAvailable()) {
            log.info ' -> ' + this.class.simpleName + ' started: ' + startToken
            jobIsRunning = true
            return SystemEvent.createEvent(startToken)
        }
       return null
    }
    /**
        Flags jobIsRunning and changes systemEvent.token: A_B_START -> A_B_COMPLETE
     */
    protected void stopAndComplete(SystemEvent systemEvent, Map<String, Object> payload = [:]) {
        if (!systemEvent) {
            log.warn ' -> ' + this.class.simpleName + ' finished - but no SystemEvent given'
        }
        else {
            String stopToken = systemEvent.token.replace('_START', '_COMPLETE')
            log.info ' -> ' + this.class.simpleName + ' finished: ' + systemEvent.token + ' > ' + stopToken

            systemEvent.changeTo(stopToken, payload)
        }
        jobIsRunning = false
    }
}
