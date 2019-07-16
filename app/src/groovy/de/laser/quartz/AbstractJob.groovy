package de.laser.quartz

abstract class AbstractJob {

    static configFlags = []

    protected boolean jobIsRunning = false

    abstract boolean isAvailable()

    abstract boolean isRunning()
}
