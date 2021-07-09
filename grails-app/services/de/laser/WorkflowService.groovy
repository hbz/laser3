package de.laser

import de.laser.workflow.*
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class WorkflowService {

    //static Log static_logger = LogFactory.getLog(WorkflowService)

    void createSequence(GrailsParameterMap params) {
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

    void createTask(GrailsParameterMap params) {
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

    void editSequence(GrailsParameterMap params) {
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

    void editTask(GrailsParameterMap params) {
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

    WfSequenceBase internalEditSequence(WfSequenceBase seq, GrailsParameterMap params) {

        log.debug( seq.toString() )
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfSequencePrototype.KEY) {
            seq = seq as WfSequencePrototype

            Closure getParam     = { key -> params.get(WfSequencePrototype.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfSequencePrototype.KEY + '_' + key) }

            seq.description = getParam('description')
            seq.head = WfTaskPrototype.get(getLongParam('head'))
            seq.title = getParam('title')
            seq.type = RefdataValue.get(getLongParam('type'))
        }
        else if (cmd[1] == WfSequence.KEY) {
            seq = seq as WfSequence

            Closure getParam     = { key -> params.get(WfSequence.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfSequence.KEY + '_' + key) }

            seq.description = getParam('description')
            seq.head = WfTask.get(getLongParam('head'))
            seq.title = getParam('title')
            seq.type = RefdataValue.get(getLongParam('type'))

            seq.comment = getParam('comment')
            seq.prototype = WfSequencePrototype.get(getLongParam('prototype'))
            seq.status = RefdataValue.get(getLongParam('status'))
            seq.subscription = Subscription.get(getLongParam('subscription'))
        }

        log.debug( 'changed: ' + seq.dirtyPropertyNames.toString() +  ' validation: ' + seq.validate() )
        if (seq.getErrors()) {
            log.debug( seq.getErrors().toString() )
        }

        seq.save()
    }

    WfTaskBase internalEditTask(WfTaskBase task, GrailsParameterMap params) {

        log.debug( task.toString() )
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfTaskPrototype.KEY) {
            task = task as WfTaskPrototype

            Closure getParam     = { key -> params.get(WfTaskPrototype.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfTaskPrototype.KEY + '_' + key) }

            task.description = getParam('description')
            task.next = WfTaskPrototype.get(getLongParam('next'))
            task.head = WfTaskPrototype.get(getLongParam('head'))
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
            task.head = WfTask.get(getLongParam('head'))
            task.priority = RefdataValue.get(getLongParam('priority'))
            task.title = getParam('title')
            task.type = RefdataValue.get(getLongParam('type'))

            task.comment = getParam('comment')
            task.prototype = WfTaskPrototype.get(getLongParam('prototype'))
            task.status = RefdataValue.get(getLongParam('status'))
        }

        log.debug( 'changed: ' + task.dirtyPropertyNames.toString() +  ' validation: ' + task.validate() )
        if (task.getErrors()) {
            log.debug( task.getErrors().toString() )
        }

        task.save()
    }
}