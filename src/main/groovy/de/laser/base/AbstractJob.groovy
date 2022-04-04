package de.laser.base

abstract class AbstractJob {
    private Date job_start
    private Date job_end

    static List<String> configFlags = []

    //Todo REDUCE VISIBILITY after changing code to use method setJobStart/End instead of modifying the jobIsRunning attribute
    protected boolean jobIsRunning = false

    abstract boolean isAvailable()

    protected boolean isRunning() {
        jobIsRunning
    }

    protected setJobStart(){
        job_start = new Date()
    }

    protected setJobEnd(){
        job_end = new Date()
    }

    protected long getJobDurationInMillis(){
        job_end.getTime() - job_start.getTime()
    }
}
