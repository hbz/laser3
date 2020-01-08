package com.k_int.kbplus.batch

import de.laser.domain.ActivityProfiler
import de.laser.quartz.AbstractJob
import net.sf.ehcache.CacheManager

class HeartbeatJob extends AbstractJob {

    def grailsApplication
    def cacheService
    def yodaService

    static triggers = {
    // Delay 20 seconds, run every 10 mins.
    // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
    // Example - every 10 mins 0 0/10 * * * ? 
    // Every 10 mins
    cron name:'heartbeatTrigger', startDelay:10000, cronExpression: "0 0/5 * * * ?"
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
    }

    static List<String> configFlags = ['quartzHeartbeat']

    boolean isAvailable() {
        !jobIsRunning // no service needed
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true

        log.debug("Heartbeat Job")
        grailsApplication.config.quartzHeartbeat = new Date()
        ActivityProfiler.update()

        deleteUnusedUserCaches()

        jobIsRunning = false
    }

    // HOTFIX: ERMS-2023
    // TODO [ticket=2029] refactoring caches
    def deleteUnusedUserCaches() {
        CacheManager ehcacheManager = (CacheManager) cacheService.getCacheManager(cacheService.EHCACHE)
        String[] userCaches = ehcacheManager.getCacheNames().findAll { it -> it.startsWith('USER:') }

        String[] activeUsers = yodaService.getActiveUsers( (1000 * 60 * 180) ).collect{ 'USER:' + it.id } // 180 minutes
        String[] retiredUserCaches = userCaches.collect{ (it in activeUsers) ? null : it }.findAll{ it }

        retiredUserCaches.each { it ->
            ehcacheManager.removeCache(it)
        }

        if (retiredUserCaches) {
            log.debug("user caches: " + userCaches.collect{ it })
            log.debug("retired (after 180 minutes): " + retiredUserCaches.collect{ it })
        }
    }
}
