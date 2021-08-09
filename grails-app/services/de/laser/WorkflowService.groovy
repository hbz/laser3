package de.laser

import de.laser.helper.DateUtils
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

        boolean containsKey(String key) {
            params.containsKey(cmpKey + key)
        }

        String getString(String key) {
            params.get(cmpKey + key) ? params.get(cmpKey + key).toString().trim() : null
        }
        Long getLong(String key) {
            params.long(cmpKey + key)
        }
        Integer getInt(String key) {
            params.int(cmpKey + key)
        }
        Date getDate(String key) {
            params.get(cmpKey + key) ? DateUtils.parseDateGeneric(params.get(cmpKey + key)) : null

        }
        RefdataValue getRefdataValue(String key) {
            Long id = getLong(key)
            RefdataValue.findById(id)
        }
    }

    ParamsHelper getNewParamsHelper(String cmpKey, GrailsParameterMap params) {
        new ParamsHelper(cmpKey, params)
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
                WfWorkflowPrototype wf = WfWorkflowPrototype.get( cmd[2] )
                internalEditWorkflow(wf, params)
            }
        }
        else if (cmd[1] == WfWorkflow.KEY) {
            if (cmd[0] == 'create') {
                WfWorkflow wf = new WfWorkflow()
                internalEditWorkflow(wf, params)
            }
            else if (cmd[0] == 'edit') {
                WfWorkflow wf = WfWorkflow.get( cmd[2] )
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
                WfTaskPrototype task = WfTaskPrototype.get( cmd[2] )
                internalEditTask(task, params)
            }
        }
        else if (cmd[1] == WfTask.KEY) {
            if (cmd[0] == 'create') {
                WfTask task = new WfTask()
                internalEditTask(task, params)
            }
            else if (cmd[0] == 'edit') {
                WfTask task = WfTask.get( cmd[2] )
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
                WfConditionPrototype condition = WfConditionPrototype.get( cmd[2] )
                internalEditCondition(condition, params)
            }
        }
        else if (cmd[1] == WfCondition.KEY) {
            if (cmd[0] == 'create') {
                WfCondition condition = new WfCondition()
                internalEditCondition(condition, params)
            }
            else if (cmd[0] == 'edit') {
                WfCondition condition = WfCondition.get( cmd[2] )
                internalEditCondition(condition, params)
            }
        }
    }

    Map<String, Object> deleteWorkflow(GrailsParameterMap params) {
        log.debug('deleteWorkflow() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            WfWorkflowPrototype wf = WfWorkflowPrototype.get( cmd[2] )
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
            WfTaskPrototype task = WfTaskPrototype.get( cmd[2] )
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
            WfTask task = WfTask.get( cmd[2] )
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
            WfConditionPrototype condition = WfConditionPrototype.get( cmd[2] )
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
            WfCondition condition = WfCondition.get( cmd[2] )
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

            wf.title        = ph.getString('title')
            wf.description  = ph.getString('description')
            wf.child        = WfTaskPrototype.get(ph.getLong('child')) // TODO - xyz
            wf.state        = RefdataValue.get(ph.getLong('state'))
        }
        else if (cmd[1] == WfWorkflow.KEY) {
            wf = wf as WfWorkflow

            wf.title        = ph.getString('title')
            wf.description  = ph.getString('description')
            wf.comment      = ph.getString('comment')
            wf.status       = RefdataValue.get(ph.getLong('status'))

            // wf.child        = WfTask.get(ph.getLong('child'))  // TODO - xyz
            // wf.prototype    = WfWorkflowPrototype.get(ph.getLong('prototype'))
            // wf.subscription = Subscription.get(ph.getLong('subscription'))
        }

        Map<String, Object> result = [ workflow: wf, cmd: cmd[0], key: cmd[1] ]

        result.status = wf.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
        result
    }

    Map<String, Object> internalEditTask(WfTaskBase task, GrailsParameterMap params) {

        log.debug( task.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = new ParamsHelper( cmd[1], params )

        if (cmd[1] == WfTaskPrototype.KEY) {
            task = task as WfTaskPrototype

            task.title          = ph.getString('title')
            task.description    = ph.getString('description')
            task.priority       = RefdataValue.get(ph.getLong('priority'))
            task.condition      = WfConditionPrototype.get(ph.getLong('condition'))
            task.child          = WfTaskPrototype.get(ph.getLong('child'))
            task.next           = WfTaskPrototype.get(ph.getLong('next'))

        }
        else if (cmd[1] == WfTask.KEY) {
            task = task as WfTask

            task.title          = ph.getString('title')
            task.description    = ph.getString('description')
            task.priority       = RefdataValue.get(ph.getLong('priority'))
            task.comment        = ph.getString('comment')
            task.status         = RefdataValue.get(ph.getLong('status'))

            // task.condition      = WfCondition.get(ph.getLong('condition'))
            // task.child          = WfTask.get(ph.getLong('child'))
            // task.next           = WfTask.get(ph.getLong('next'))
            // task.prototype      = WfTaskPrototype.get(ph.getLong('prototype'))
        }

        Map<String, Object> result = [ task: task, cmd: cmd[0], key: cmd[1] ]

        result.status = task.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
        result
    }

    // TODO
    Map<String, Object> internalEditCondition(WfConditionBase condition, GrailsParameterMap params) {

        log.debug( condition.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = new ParamsHelper( cmd[1], params )

        if (cmd[1] == WfConditionPrototype.KEY) {
            condition = condition as WfConditionPrototype

            condition.title         = ph.getString('title')
            condition.description   = ph.getString('description')
            condition.type          = ph.getInt('type') ?: 0

            // values

            condition.checkbox1 = ph.getString('checkbox1') == 'on'
            condition.checkbox2 = ph.getString('checkbox2') == 'on'

            condition.date1 = ph.getDate('date1')
            condition.date2 = ph.getDate('date2')

            // meta

            condition.checkbox1_title = ph.getString('checkbox1_title')
            condition.checkbox2_title = ph.getString('checkbox2_title')

            condition.date1_title = ph.getString('date1_title')
            condition.date2_title = ph.getString('date2_title')

            condition.checkbox1_isTrigger = ph.getString('checkbox1_isTrigger') == 'on'
            condition.checkbox2_isTrigger = ph.getString('checkbox2_isTrigger') == 'on'
        }
        else if (cmd[1] == WfCondition.KEY) {
            condition = condition as WfCondition

            condition.title         = ph.getString('title')
            condition.description   = ph.getString('description')
            condition.type          = ph.getInt('type') ?: 0

            // values

            condition.checkbox1 = ph.getString('checkbox1') == 'on'
            condition.checkbox2 = ph.getString('checkbox2') == 'on'

            condition.date1 = ph.getDate('date1')
            condition.date2 = ph.getDate('date2')

            // meta

            condition.checkbox1_title = ph.getString('checkbox1_title')
            condition.checkbox2_title = ph.getString('checkbox2_title')

            condition.date1_title = ph.getString('date1_title')
            condition.date2_title = ph.getString('date2_title')

            condition.checkbox1_isTrigger = ph.getString('checkbox1_isTrigger') == 'on'
            condition.checkbox2_isTrigger = ph.getString('checkbox2_isTrigger') == 'on'

        }

        Map<String, Object> result = [ condition: condition, cmd: cmd[0], key: cmd[1] ]

        result.status = condition.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
        result
    }

    Map<String, Object> instantiateCompleteWorkflow(GrailsParameterMap params) {
        log.debug('instantiateCompleteWorkflow() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfWorkflowPrototype.KEY) {

            WfWorkflowPrototype.withTransaction { TransactionStatus ts ->

                try {
                    result.prototype    = WfWorkflowPrototype.get( cmd[2] )
                    result.workflow     = result.prototype.instantiate( params.long('subId') ) // TODO

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
                    result.workflow = WfWorkflow.get( cmd[2] ).remove()

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

    Map<String, Object> handleUsage(GrailsParameterMap params) {
        log.debug('handleUsage() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[0] == 'usage') {  // TODO return msg

            ParamsHelper ph = getNewParamsHelper( cmd[1], params )

            if (cmd[1] == WfWorkflow.KEY) {
                WfWorkflow workflow = WfWorkflow.get( cmd[2] )

                RefdataValue status = ph.getRefdataValue('status')
                if (status != workflow.status) {
                    workflow.status = status
                    result.status = workflow.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
                }
            }
            if (cmd[1] == WfTask.KEY) {
                WfTask task = WfTask.get( cmd[2] )
                boolean tChanged

                RefdataValue status = ph.getRefdataValue('status')
                if (status != task.status) {
                    task.status = status
                    tChanged = true
                }
                String comment = ph.getString('comment')
                if (comment != task.comment) {
                    task.comment = comment
                    tChanged = true
                }
                if (tChanged) {
                    result.status = task.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
                }

                if (task.condition) {
                    ph = getNewParamsHelper( WfCondition.KEY, params )

                    WfCondition condition = task.condition
                    List<String> cFields = condition.getFields()
                    boolean cChanged

                    if (cFields.contains('checkbox1')) {
                        String checkbox1 = ph.getString('checkbox1')
                        if ((checkbox1 == 'on') != condition.checkbox1) {
                            condition.checkbox1 = (checkbox1 == 'on')
                        }
                    }
                    if (cFields.contains('checkbox2')) {
                        String checkbox2 = ph.getString('checkbox2')
                        if ((checkbox2 == 'on') != condition.checkbox2) {
                            condition.checkbox2 = (checkbox2 == 'on')
                        }
                    }
                    if (cFields.contains('date1')) {
                        Date date1 = ph.getDate('date1')
                        if (date1 != condition.date1) {
                            condition.date1 = date1
                        }
                    }
                    if (cFields.contains('date2')) {
                        Date date2 = ph.getDate('date2')
                        if (date2 != condition.date2) {
                            condition.date2 = date2
                        }
                    }
                    if (cChanged) {
                        result.status = condition.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
                    }
                }
            }
        }
        else if (cmd[0] == 'delete') {
            result.putAll( removeCompleteWorkflow( params ) )
        }

        result
    }
}