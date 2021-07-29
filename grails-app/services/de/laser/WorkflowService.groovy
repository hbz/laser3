package de.laser

import de.laser.workflow.*
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.TransactionStatus

@Transactional
class WorkflowService {

    def contextService

    static final String OP_STATUS_DONE  = 'OP_STATUS_DONE'
    static final String OP_STATUS_ERROR = 'OP_STATUS_ERROR'

    //static Log static_logger = LogFactory.getLog(WorkflowService)

    class ParamsHelper {

        ParamsHelper(String cmpKey, GrailsParameterMap params) {
            this.cmpKey = cmpKey + '_'
            this.params = params
        }

        String cmpKey
        GrailsParameterMap params

        String getString(String key) {
            params.get(cmpKey + key) ? params.get(cmpKey + key).toString().trim() : null
        }
        Long getLong(String key) {
            params.long(cmpKey + key)
        }
        Integer getInt(String key) {
            params.int(cmpKey + key)
        }
    }

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

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            WfWorkflowPrototype wf = WfWorkflowPrototype.get(params.id)
            result.workflow = wf

            if (! wf.inUse()) {
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
            result.putAll( removeCompleteWorkflow(params) )
        }
        result
    }

    Map<String, Object> deleteTask(GrailsParameterMap params) {
        log.debug('deleteTask() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfTaskPrototype.KEY) {
            WfTaskPrototype task = WfTaskPrototype.get( params.id )
            result.task = task

            if (! task.inUse()) {
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

            println '--- NOT IMPLEMENTED ---'
        }
        result
    }

    Map<String, Object> deleteCondition(GrailsParameterMap params) {
        log.debug('deleteCondition() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfConditionPrototype.KEY) {
            WfConditionPrototype condition = WfConditionPrototype.get( params.id )
            result.condition = condition

            if (! condition.inUse()) {
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

            println '--- NOT IMPLEMENTED ---'
        }
        result
    }

    Map<String, Object> internalEditWorkflow(WfWorkflowBase wf, GrailsParameterMap params) {

        log.debug( wf.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = new ParamsHelper( cmd[1], params )

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            wf = wf as WfWorkflowPrototype

            wf.child        = WfTaskPrototype.get(ph.getLong('child'))
            wf.description  = ph.getString('description')
            wf.state        = RefdataValue.get(ph.getLong('state'))
            wf.title        = ph.getString('title')
        }
        else if (cmd[1] == WfWorkflow.KEY) {
            wf = wf as WfWorkflow

            wf.child        = WfTask.get(ph.getLong('child'))
            wf.description  = ph.getString('description')
            wf.state        = RefdataValue.get(ph.getLong('state'))
            wf.title        = ph.getString('title')

            wf.comment      = ph.getString('comment')
            wf.prototype    = WfWorkflowPrototype.get(ph.getLong('prototype'))
            wf.status       = RefdataValue.get(ph.getLong('status'))
            wf.subscription = Subscription.get(ph.getLong('subscription'))
        }

        Map<String, Object> result = [ workflow: wf, cmd: cmd[0], key: cmd[1] ]

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

        ParamsHelper ph = new ParamsHelper( cmd[1], params )

        if (cmd[1] == WfTaskPrototype.KEY) {
            task = task as WfTaskPrototype

            task.child          = WfTaskPrototype.get(ph.getLong('child'))
            task.condition      = WfConditionPrototype.get(ph.getLong('condition'))
            task.description    = ph.getString('description')
            task.next           = WfTaskPrototype.get(ph.getLong('next'))
            task.priority       = RefdataValue.get(ph.getLong('priority'))
            task.title          = ph.getString('title')
            //task.type = RefdataValue.get(ph.getLongParam('type'))
        }
        else if (cmd[1] == WfTask.KEY) {
            task = task as WfTask

            task.child          = WfTask.get(ph.getLong('child'))
            task.condition      = WfCondition.get(ph.getLong('condition'))
            task.description    = ph.getString('description')
            task.next           = WfTask.get(ph.getLong('next'))
            task.priority       = RefdataValue.get(ph.getLong('priority'))
            task.title          = ph.getString('title')
            //task.type = RefdataValue.get(ph.getLongParam('type'))

            task.comment    = ph.getString('comment')
            task.prototype  = WfTaskPrototype.get(ph.getLong('prototype'))
            task.status     = RefdataValue.get(ph.getLong('status'))
        }

        Map<String, Object> result = [ task: task, cmd: cmd[0], key: cmd[1] ]

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

        ParamsHelper ph = new ParamsHelper( cmd[1], params )

        if (cmd[1] == WfConditionPrototype.KEY) {
            condition = condition as WfConditionPrototype

            condition.description   = ph.getString('description')
            condition.title         = ph.getString('title')
            condition.type          = ph.getInt('type') ?: 0

            condition.checkbox1 = ph.getString('checkbox1') == 'on'
            condition.checkbox2 = ph.getString('checkbox2') == 'on'

            condition.checkbox1_title = ph.getString('checkbox1_title')
            condition.checkbox2_title = ph.getString('checkbox2_title')

            condition.date1_title = ph.getString('date1_title')
            condition.date2_title = ph.getString('date2_title')

            condition.checkbox1_isTrigger = ph.getString('checkbox1_isTrigger') == 'on'
            condition.checkbox2_isTrigger = ph.getString('checkbox2_isTrigger') == 'on'
        }
        else if (cmd[1] == WfCondition.KEY) {
            condition = condition as WfCondition

            condition.description   = ph.getString('description')
            condition.title         = ph.getString('title')
            condition.type          = ph.getInt('type') ?: 0

            condition.checkbox1 = ph.getString('checkbox1') == 'on'
            condition.checkbox2 = ph.getString('checkbox2') == 'on'

            condition.checkbox1_title = ph.getString('checkbox1_title')
            condition.checkbox2_title = ph.getString('checkbox2_title')

            condition.date1_title = ph.getString('date1_title')
            condition.date2_title = ph.getString('date2_title')

            condition.checkbox1_isTrigger = ph.getString('checkbox1_isTrigger') == 'on'
            condition.checkbox2_isTrigger = ph.getString('checkbox2_isTrigger') == 'on'

        }

        Map<String, Object> result = [ condition: condition, cmd: cmd[0], key: cmd[1] ]

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

    Map<String, Object> instantiateCompleteWorkflow(GrailsParameterMap params) {
        log.debug('instantiateCompleteWorkflow() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfWorkflowPrototype.KEY) {

            WfWorkflowPrototype.withTransaction { TransactionStatus ts ->

                try {
                    result.prototype    = WfWorkflowPrototype.get(params.id)
                    result.workflow     = result.prototype.instantiate()

                    if (! result.workflow.save()) {
                        result.status = OP_STATUS_ERROR

                        log.debug( 'instantiateCompleteWorkflow() -> ' + result.workflow.getErrors().toString() )

                        log.debug( 'TransactionStatus.setRollbackOnly()' )
                        ts.setRollbackOnly()
                    }
                    else {
                        result.status = OP_STATUS_DONE
                    }
                }
                catch (Exception e) {
                    result.status = OP_STATUS_ERROR

                    log.debug( 'instantiateCompleteWorkflow() -> ' + e.getMessage() )
                    e.printStackTrace()

                    log.debug( 'TransactionStatus.setRollbackOnly()' )
                    ts.setRollbackOnly()
                }
            }
        }

        result
    }

    Map<String, Object> removeCompleteWorkflow(GrailsParameterMap params) {
        log.debug('removeCompleteWorkflow() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfWorkflow.KEY) {

            WfWorkflow.withTransaction { TransactionStatus ts ->

                try {
                    result.workflow = WfWorkflow.get(params.id).remove()

                    if (result.workflow) {
                        result.status = OP_STATUS_ERROR

                        log.debug( 'removeCompleteWorkflow() -> ' + result.workflow.getErrors().toString() )

                        log.debug( 'TransactionStatus.setRollbackOnly()' )
                        ts.setRollbackOnly()
                    }
                    else {
                        result.status = OP_STATUS_DONE
                    }
                }
                catch (Exception e) {
                    result.status = OP_STATUS_ERROR

                    log.debug( 'removeCompleteWorkflow() -> ' + e.getMessage() )
                    e.printStackTrace()

                    log.debug( 'TransactionStatus.setRollbackOnly()' )
                    ts.setRollbackOnly()
                }
            }
        }

        result
    }

}