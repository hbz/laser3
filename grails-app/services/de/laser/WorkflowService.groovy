package de.laser

import de.laser.workflow.*
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class WorkflowService {

    static final String OP_STATUS_DONE  = 'OP_STATUS_DONE'
    static final String OP_STATUS_ERROR = 'OP_STATUS_ERROR'

    //static Log static_logger = LogFactory.getLog(WorkflowService)

    Map<String, Object> createSequence(GrailsParameterMap params) {
        log.debug('createSequence() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfSequencePrototype.KEY) {

            WfSequencePrototype seq = new WfSequencePrototype()
            internalEditSequence(seq, params)
        }
        else if (cmd[1] == WfSequence.KEY) {

            WfSequence seq = new WfSequence()
            internalEditSequence(seq, params)
        }
    }

    Map<String, Object> createTask(GrailsParameterMap params) {
        log.debug('createTask() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfTaskPrototype.KEY) {

            WfTaskPrototype task = new WfTaskPrototype()
            internalEditTask(task, params)
        }
        else if (cmd[1] == WfTask.KEY) {

            WfTask task = new WfTask()
            internalEditTask(task, params)
        }
    }

    Map<String, Object> editSequence(GrailsParameterMap params) {
        log.debug('editSequence() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfSequencePrototype.KEY) {

            WfSequencePrototype seq = WfSequencePrototype.get(params.id)
            internalEditSequence(seq, params)
        }
        else if (cmd[1] == WfSequence.KEY) {

            WfSequence seq = WfSequence.get(params.id)
            internalEditSequence(seq, params)
        }
    }

    Map<String, Object> editTask(GrailsParameterMap params) {
        log.debug('editTask() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfTaskPrototype.KEY) {

            WfTaskPrototype task = WfTaskPrototype.get( params.id )
            internalEditTask(task, params)
        }
        else if (cmd[1] == WfTask.KEY) {

            WfTask task = WfTask.get( params.id )
            internalEditTask(task, params)
        }
    }

    Map<String, Object> deleteSequence(GrailsParameterMap params) {
        log.debug('deleteSequence() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [
            cmd: cmd[0], key: cmd[1]
        ]

        if (cmd[1] == WfSequencePrototype.KEY) {
            WfSequencePrototype seq = WfSequencePrototype.get(params.id)
            result.sequence = seq

            if (! seq.inStructure()) {
                try {
                    seq.delete()
                    result.status = OP_STATUS_DONE
                }
                catch (Exception e) {
                    result.status = OP_STATUS_ERROR
                }
            }
        }
        else if (cmd[1] == WfSequence.KEY) {
            WfSequence seq = WfSequence.get(params.id)
            result.sequence = seq
            result.status = OP_STATUS_ERROR

            println '--- TODO ---'
        }
        result
    }

    Map<String, Object> deleteTask(GrailsParameterMap params) {
        log.debug('deleteTask() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [
            cmd: cmd[0], key: cmd[1]
        ]

        if (cmd[1] == WfTaskPrototype.KEY) {
            WfTaskPrototype task = WfTaskPrototype.get( params.id )
            result.task = task

            if (! task.inStructure()) {
                try {
                    task.delete()
                    result.status = OP_STATUS_DONE
                }
                catch (Exception e) {
                    result.status = OP_STATUS_ERROR
                }
            }
        }
        else if (cmd[1] == WfTask.KEY) {
            WfTask task = WfTask.get( params.id )
            result.task = task
            result.status = OP_STATUS_ERROR

            println '--- TODO ---'
        }
        result
    }

    Map<String, Object> internalEditSequence(WfSequenceBase seq, GrailsParameterMap params) {

        log.debug( seq.toString() )
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfSequencePrototype.KEY) {
            seq = seq as WfSequencePrototype

            Closure getParam     = { key -> params.get(WfSequencePrototype.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfSequencePrototype.KEY + '_' + key) }

            seq.description = getParam('description')
            seq.child = WfTaskPrototype.get(getLongParam('child'))
            seq.title = getParam('title')
            seq.type = RefdataValue.get(getLongParam('type'))
        }
        else if (cmd[1] == WfSequence.KEY) {
            seq = seq as WfSequence

            Closure getParam     = { key -> params.get(WfSequence.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfSequence.KEY + '_' + key) }

            seq.description = getParam('description')
            seq.child = WfTask.get(getLongParam('child'))
            seq.title = getParam('title')
            seq.type = RefdataValue.get(getLongParam('type'))

            seq.comment = getParam('comment')
            seq.prototype = WfSequencePrototype.get(getLongParam('prototype'))
            seq.status = RefdataValue.get(getLongParam('status'))
            seq.subscription = Subscription.get(getLongParam('subscription'))
        }

        Map<String, Object> result = [
            sequence: seq, cmd: cmd[0], key: cmd[1]
        ]

        if (! seq.save()) {
            result.status = OP_STATUS_ERROR
            log.debug( 'changes: ' + result.changes.toString() )
            log.debug( 'validation: ' + seq.validate() )
            log.debug( seq.getErrors().toString() )
        }
        else {
            result.status = OP_STATUS_DONE
        }
        result
    }

    Map<String, Object> internalEditTask(WfTaskBase task, GrailsParameterMap params) {

        log.debug( task.toString() )
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfTaskPrototype.KEY) {
            task = task as WfTaskPrototype

            Closure getParam     = { key -> params.get(WfTaskPrototype.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfTaskPrototype.KEY + '_' + key) }

            task.description = getParam('description')
            task.next = WfTaskPrototype.get(getLongParam('next'))
            task.child = WfTaskPrototype.get(getLongParam('child'))
            task.priority = RefdataValue.get(getLongParam('priority'))
            task.title = getParam('title')
            task.type = RefdataValue.get(getLongParam('type'))
        }
        else if (cmd[1] == WfTask.KEY) {
            task = task as WfTask

            Closure getParam     = { key -> params.get(WfTask.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfTask.KEY + '_' + key) }

            task.description = getParam('description')
            task.next = WfTask.get(getLongParam('next'))
            task.child = WfTask.get(getLongParam('child'))
            task.priority = RefdataValue.get(getLongParam('priority'))
            task.title = getParam('title')
            task.type = RefdataValue.get(getLongParam('type'))

            task.comment = getParam('comment')
            task.prototype = WfTaskPrototype.get(getLongParam('prototype'))
            task.status = RefdataValue.get(getLongParam('status'))
        }

        Map<String, Object> result = [
            task: task, cmd: cmd[0], key: cmd[1]
        ]

        if (! task.save()) {
            result.status = OP_STATUS_ERROR
            log.debug( 'changes: ' + result.changes.toString() )
            log.debug( 'validation: ' + task.validate() )
            log.debug( task.getErrors().toString() )
        }
        else {
            result.status = OP_STATUS_DONE
        }
        result
    }
}