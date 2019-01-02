package de.laser.helper

import grails.util.Holders

class DebugUtil {

    def cacheService = Holders.grailsApplication.mainContext.getBean('cacheService')
    def benchCache   = cacheService.getTTL300Cache('DebugUtil:benchCache - ')

    def startBench(String key) {
        benchCache.put(key, new Date())
    }

    def stopBench(String key) {
        def diff

        def date = (Date) benchCache.get(key)
        if (date) {
            diff = (new Date().getTime()) - date.getTime()
        }
        return diff
    }
}
