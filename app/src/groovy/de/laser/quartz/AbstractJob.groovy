package de.laser.quartz

abstract class AbstractJob {

    static triggers = {}

    protected String configFlags = [:]

    def getInfo() {
        def result = []

        if (triggers.simple) {
            result.type = 'simple'
            println triggers.simple
        }
        else if (triggers.cron) {
            result.type = 'cron'
            println triggers.cron

        }
        else if (triggers.custom) {
            result.type = 'custom'
            println triggers.custom
        }
    }
}
