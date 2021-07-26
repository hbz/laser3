package de.laser

import de.laser.workflow.*
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class WorkflowService {

    static final String OP_STATUS_DONE  = 'OP_STATUS_DONE'
    static final String OP_STATUS_ERROR = 'OP_STATUS_ERROR'

    //static Log static_logger = LogFactory.getLog(WorkflowService)

    Map<String, Object> handleWorkflow(GrailsParameterMap params) {
        log.debug('handleWorkflow() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            if (cmd[0] == 'create') {
                WfWorkflowPrototype wf = new WfWorkflowPrototype()
                internalEditWorkflow(wf, params)
            }
            else if (cmd[0] == 'edit') {
                WfWorkflowPrototype wf = WfWorkflowPrototype.get(params.id)
                internalEditWorkflow(wf, params)
            }
        }
        else if (cmd[1] == WfWorkflow.KEY) {
            if (cmd[0] == 'create') {
                WfWorkflow wf = new WfWorkflow()
                internalEditWorkflow(wf, params)
            }
            else if (cmd[0] == 'edit') {
                WfWorkflow wf = WfWorkflow.get(params.id)
                internalEditWorkflow(wf, params)
            }
        }
    }

    Map<String, Object> handleTask(GrailsParameterMap params) {
        log.debug('handleTask() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfTaskPrototype.KEY) {
            if (cmd[0] == 'create') {
                WfTaskPrototype task = new WfTaskPrototype()
                internalEditTask(task, params)
            }
            else if (cmd[0] == 'edit') {
                WfTaskPrototype task = WfTaskPrototype.get( params.id )
                internalEditTask(task, params)
            }
        }
        else if (cmd[1] == WfTask.KEY) {
            if (cmd[0] == 'create') {
                WfTask task = new WfTask()
                internalEditTask(task, params)
            }
            else if (cmd[0] == 'edit') {
                WfTask task = WfTask.get( params.id )
                internalEditTask(task, params)
            }
        }
    }

    Map<String, Object> handleCondition(GrailsParameterMap params) {
        log.debug('handleCondition() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfConditionPrototype.KEY) {
            if (cmd[0] == 'create') {
                WfConditionPrototype condition = new WfConditionPrototype()
                internalEditCondition(condition, params)
            }
            else if (cmd[0] == 'edit') {
                WfConditionPrototype condition = WfConditionPrototype.get( params.id )
                internalEditCondition(condition, params)
            }
        }
        else if (cmd[1] == WfCondition.KEY) {
            if (cmd[0] == 'create') {
                WfCondition condition = new WfCondition()
                internalEditCondition(condition, params)
            }
            else if (cmd[0] == 'edit') {
                WfCondition condition = WfCondition.get( params.id )
                internalEditCondition(condition, params)
            }
        }
    }

    Map<String, Object> deleteWorkflow(GrailsParameterMap params) {
        log.debug('deleteWorkflow() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [
            cmd: cmd[0], key: cmd[1]
        ]

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            WfWorkflowPrototype wf = WfWorkflowPrototype.get(params.id)
            result.workflow = wf

            if (! wf.inStructure()) {
                try {
                    wf.delete()
                    result.status = OP_STATUS_DONE
                }
                catch (Exception e) {
                    result.status = OP_STATUS_ERROR
                }
            }
        }
        else if (cmd[1] == WfWorkflow.KEY) {
            WfWorkflow wf = WfWorkflow.get(params.id)
            result.workflow = wf
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

    Map<String, Object> deleteCondition(GrailsParameterMap params) {
        log.debug('deleteCondition() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [
                cmd: cmd[0], key: cmd[1]
        ]

        if (cmd[1] == WfConditionPrototype.KEY) {
            WfConditionPrototype condition = WfConditionPrototype.get( params.id )
            result.condition = condition

            if (! condition.inStructure()) {
                try {
                    condition.delete()
                    result.status = OP_STATUS_DONE
                }
                catch (Exception e) {
                    result.status = OP_STATUS_ERROR
                }
            }
        }
        else if (cmd[1] == WfCondition.KEY) {
            WfCondition condition = WfCondition.get( params.id )
            result.condition = condition
            result.status = OP_STATUS_ERROR

            println '--- TODO ---'
        }
        result
    }

    Map<String, Object> internalEditWorkflow(WfWorkflowBase wf, GrailsParameterMap params) {

        log.debug( wf.toString() )
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            wf = wf as WfWorkflowPrototype

            Closure getParam     = { key -> params.get(WfWorkflowPrototype.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfWorkflowPrototype.KEY + '_' + key) }

            wf.child = WfTaskPrototype.get(getLongParam('child'))
            wf.description = getParam('description')
            wf.state = RefdataValue.get(getLongParam('state'))
            wf.title = getParam('title')
        }
        else if (cmd[1] == WfWorkflow.KEY) {
            wf = wf as WfWorkflow

            Closure getParam     = { key -> params.get(WfWorkflow.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfWorkflow.KEY + '_' + key) }

            wf.child = WfTask.get(getLongParam('child'))
            wf.description = getParam('description')
            wf.state = RefdataValue.get(getLongParam('state'))
            wf.title = getParam('title')

            wf.comment = getParam('comment')
            wf.prototype = WfWorkflowPrototype.get(getLongParam('prototype'))
            wf.status = RefdataValue.get(getLongParam('status'))
            wf.subscription = Subscription.get(getLongParam('subscription'))
        }

        Map<String, Object> result = [
            workflow: wf, cmd: cmd[0], key: cmd[1]
        ]

        if (! wf.save()) {
            result.status = OP_STATUS_ERROR
            log.debug( 'changes: ' + result.changes.toString() )
            log.debug( 'validation: ' + wf.validate() )
            log.debug( wf.getErrors().toString() )
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

            task.child = WfTaskPrototype.get(getLongParam('child'))
            task.condition = WfConditionPrototype.get(getLongParam('condition'))
            task.description = getParam('description')
            task.next = WfTaskPrototype.get(getLongParam('next'))
            task.priority = RefdataValue.get(getLongParam('priority'))
            task.title = getParam('title')
            //task.type = RefdataValue.get(getLongParam('type'))
        }
        else if (cmd[1] == WfTask.KEY) {
            task = task as WfTask

            Closure getParam     = { key -> params.get(WfTask.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfTask.KEY + '_' + key) }

            task.child = WfTask.get(getLongParam('child'))
            task.condition = WfCondition.get(getLongParam('condition'))
            task.description = getParam('description')
            task.next = WfTask.get(getLongParam('next'))
            task.priority = RefdataValue.get(getLongParam('priority'))
            task.title = getParam('title')
            //task.type = RefdataValue.get(getLongParam('type'))

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

    // TODO
    Map<String, Object> internalEditCondition(WfConditionBase condition, GrailsParameterMap params) {

        log.debug( condition.toString() )
        String[] cmd = (params.cmd as String).split(':')

        if (cmd[1] == WfConditionPrototype.KEY) {
            condition = condition as WfConditionPrototype

            Closure getParam     = { key -> params.get(WfConditionPrototype.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfConditionPrototype.KEY + '_' + key) }
            Closure getIntParam  = { key -> params.int(WfConditionPrototype.KEY + '_' + key) }

            condition.description = getParam('description')
            condition.title = getParam('title')
            condition.type = getIntParam('type') ?: 0

            condition.checkbox1 = getParam('checkbox1') ? true : false
            condition.checkbox1_isTrigger = getParam('checkbox1_isTrigger') ? true : false
            condition.checkbox2 = getParam('checkbox2') ? true : false
            condition.checkbox2_isTrigger = getParam('checkbox2_isTrigger') ? true : false
        }
        else if (cmd[1] == WfCondition.KEY) {
            condition = condition as WfCondition

            Closure getParam     = { key -> params.get(WfCondition.KEY + '_' + key).toString().trim() }
            Closure getLongParam = { key -> params.long(WfCondition.KEY + '_' + key) }
            Closure getIntParam  = { key -> params.int(WfCondition.KEY + '_' + key) }

            condition.description = getParam('description')
            condition.title = getParam('title')
            condition.type = getIntParam('type') ?: 0

            condition.checkbox1 = getParam('checkbox1') ? true : false
            condition.checkbox1_isTrigger = getParam('checkbox1_isTrigger') ? true : false
            condition.checkbox2 = getParam('checkbox2') ? true : false
            condition.checkbox2_isTrigger = getParam('checkbox2_isTrigger') ? true : false
        }

        Map<String, Object> result = [
                condition: condition, cmd: cmd[0], key: cmd[1]
        ]

        if (! condition.save()) {
            result.status = OP_STATUS_ERROR
            log.debug( 'changes: ' + result.changes.toString() )
            log.debug( 'validation: ' + condition.validate() )
            log.debug( condition.getErrors().toString() )
        }
        else {
            result.status = OP_STATUS_DONE
        }
        result
    }
}