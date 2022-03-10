package de.laser

import de.laser.helper.ConfigUtils
import de.laser.helper.DateUtils
import de.laser.workflow.*
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.util.WebUtils
import org.springframework.transaction.TransactionStatus

/**
 * This service contains methods for workflow handling
 */
@Transactional
class WorkflowService {

    def contextService
    def genericOIDService

    static final String OP_STATUS_DONE  = 'OP_STATUS_DONE'
    static final String OP_STATUS_ERROR = 'OP_STATUS_ERROR'

    //static Log static_logger = LogFactory.getLog(WorkflowService)

    /**
     * Subclass to parse parameters
     */
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
        DocContext getDocContext(String key) {
            Long id = getLong(key)
            DocContext.findById(id)
        }
        RefdataValue getRefdataValue(String key) {
            Long id = getLong(key)
            RefdataValue.findById(id)
        }
    }

    /**
     * Gets a parameter helper instance
     * @param cmpKey the key for the parameter
     * @param params the request parameter map
     * @return a new helper instance for the given key
     */
    ParamsHelper getNewParamsHelper(String cmpKey, GrailsParameterMap params) {
        new ParamsHelper(cmpKey, params)
    }

    /**
     * Menu switch to handle calls.
     * Supported are create, edit, delete and instantiate
     * @param params the request parameter map
     * @return the call result map, depending on the call
     */
    Map<String, Object> cmd(GrailsParameterMap params) {
        log.debug('cmd() ' + params)
        Map<String, Object> result = [:]

        if (params.cmd) {
            String[] cmd = (params.cmd as String).split(':')
            String wfObjKey = cmd[1]

            if (cmd[0] == 'create') {
                if (wfObjKey == WfWorkflowPrototype.KEY) {
                    WfWorkflowPrototype wf = new WfWorkflowPrototype()
                    result = internalEditWorkflow(wf, params)
                }
                else if (wfObjKey == WfTaskPrototype.KEY) {
                    WfTaskPrototype task = new WfTaskPrototype()
                    result = internalEditTask(task, params)
                }
                else if (wfObjKey == WfConditionPrototype.KEY) {
                    WfConditionPrototype condition = new WfConditionPrototype()
                    result = internalEditCondition(condition, params)
                }
                else if (wfObjKey == WfWorkflow.KEY) {
                    WfWorkflow wf = new WfWorkflow()
                    result = internalEditWorkflow(wf, params)
                }
                else if (wfObjKey == WfTask.KEY) {
                    WfTask task = new WfTask()
                    result = internalEditTask(task, params)
                }
                else if (wfObjKey == WfCondition.KEY) {
                    WfCondition condition = new WfCondition()
                    result = internalEditCondition(condition, params)
                }
            }
            else if (cmd[0] == 'edit') {
                Long wfObjId = cmd[2] as Long

                if (wfObjKey == WfWorkflowPrototype.KEY) {
                    WfWorkflowPrototype wf = WfWorkflowPrototype.get( wfObjId )
                    result = internalEditWorkflow(wf, params)
                }
                else if (wfObjKey == WfTaskPrototype.KEY) {
                    WfTaskPrototype task = WfTaskPrototype.get( wfObjId )
                    result = internalEditTask(task, params)
                }
                else if (wfObjKey == WfConditionPrototype.KEY) {
                    WfConditionPrototype condition = WfConditionPrototype.get( wfObjId )
                    result = internalEditCondition(condition, params)
                }
                else if (wfObjKey == WfWorkflow.KEY) {
                    WfWorkflow wf = WfWorkflow.get( wfObjId )
                    result = internalEditWorkflow(wf, params)
                }
                else if (wfObjKey == WfTask.KEY) {
                    WfTask task = WfTask.get( wfObjId )
                    result = internalEditTask(task, params)
                }
                else if (wfObjKey == WfCondition.KEY) {
                    WfCondition condition = WfCondition.get( wfObjId )
                    result = internalEditCondition(condition, params)
                }
            }
            else if (cmd[0] == 'delete') {
                if (wfObjKey in [ WfWorkflowPrototype.KEY, WfWorkflow.KEY ]) {
                    result = deleteWorkflow(params)
                }
                else if (wfObjKey in [ WfTaskPrototype.KEY, WfTask.KEY ]) {
                    result = deleteTask(params)
                }
                else if (wfObjKey in [ WfConditionPrototype.KEY, WfCondition.KEY ]) {
                    result = deleteCondition(params)
                }
            }
            else if (cmd[0] == 'instantiate') {
                if (wfObjKey == WfWorkflowPrototype.KEY) {
                    result = instantiateCompleteWorkflow(params)
                }
            }
        }
        result
    }

    /**
     * Call to delete the given workflow
     * @param params the request parameter map
     * @return a result map with the execution status
     */
    Map<String, Object> deleteWorkflow(GrailsParameterMap params) {
        log.debug('deleteWorkflow() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            WfWorkflowPrototype wf = WfWorkflowPrototype.get( cmd[2] )
            result.workflow = wf

            if (! wf?.inUse()) {
                try {
                    wf.delete()
                    result.status = OP_STATUS_DONE
                    result.workflow = null // gap
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

    /**
     * Call to delete the given task
     * @param params the request parameter map
     * @return a result map with the execution status
     */
    Map<String, Object> deleteTask(GrailsParameterMap params) {
        log.debug('deleteTask() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfTaskPrototype.KEY) {
            WfTaskPrototype task = WfTaskPrototype.get( cmd[2] )
            result.task = task

            if (! task?.inUse()) {
                try {
                    task.delete()
                    result.task = null // gap
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

    /**
     * Call to delete the given workflow condition
     * @param params the request parameter map
     * @return a result map with the execution status
     */
    Map<String, Object> deleteCondition(GrailsParameterMap params) {
        log.debug('deleteCondition() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[1] == WfConditionPrototype.KEY) {
            WfConditionPrototype condition = WfConditionPrototype.get( cmd[2] )
            result.condition = condition

            if (! condition?.inUse()) {
                try {
                    condition.delete()
                    result.condition = null // gap
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

    /**
     * Edits the given workflow (or prototype) with the specified params
     * @param wf the workflow or its prototype to edit
     * @param params the input parameter map
     * @return a result map with the execution status
     */
    Map<String, Object> internalEditWorkflow(WfWorkflowBase wf, GrailsParameterMap params) {

        log.debug( wf.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = new ParamsHelper( cmd[1], params )

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            wf = wf as WfWorkflowPrototype

            wf.title        = ph.getString('title')
            wf.description  = ph.getString('description')
            wf.task         = WfTaskPrototype.get(ph.getLong('task'))
            wf.state        = RefdataValue.get(ph.getLong('state'))
        }
        else if (cmd[1] == WfWorkflow.KEY) {
            wf = wf as WfWorkflow

            wf.title        = ph.getString('title')
            wf.description  = ph.getString('description')
            wf.comment      = ph.getString('comment')
            wf.status       = RefdataValue.get(ph.getLong('status'))

            // wf.task         = WfTask.get(ph.getLong('task'))
            // wf.prototype    = WfWorkflowPrototype.get(ph.getLong('prototype'))
            // wf.subscription = Subscription.get(ph.getLong('subscription'))
        }

        Map<String, Object> result = [ workflow: wf, cmd: cmd[0], key: cmd[1] ]

        result.status = wf.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
        result
    }

    /**
     * Edits the given task (or prototype) with the specified params
     * @param task the task or its prototype to edit
     * @param params the input parameter map
     * @return a result map with the execution status
     */
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

    /**
     * TODO - method under construction!
     * Edits the given condition (or prototype) with the specified params
     * @param condition the condition or its prototype to edit
     * @param params the input parameter map
     * @return a result map with the execution status
     */
    Map<String, Object> internalEditCondition(WfConditionBase condition, GrailsParameterMap params) {

        log.debug( condition.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = new ParamsHelper( cmd[1], params )

        Closure resetValuesAndMeta = { WfConditionBase wfc ->

            wfc.checkbox1 = false
            wfc.checkbox2 = false
            wfc.checkbox1_isTrigger = false
            wfc.checkbox2_isTrigger = false

            wfc.date1 = null
            wfc.date2 = null
            wfc.file1 = null

            wfc.checkbox1_title = null
            wfc.checkbox2_title = null
            wfc.date1_title = null
            wfc.date2_title = null
            wfc.file1_title = null
        }

        Closure setValuesAndMeta = { WfConditionBase wfc ->

            wfc.checkbox1_title = ph.getString('checkbox1_title')
            wfc.checkbox2_title = ph.getString('checkbox2_title')
            wfc.date1_title     = ph.getString('date1_title')
            wfc.date2_title     = ph.getString('date2_title')
            wfc.file1_title     = ph.getString('file1_title')

            wfc.checkbox1_isTrigger = ph.getString('checkbox1_isTrigger') == 'on'
            wfc.checkbox2_isTrigger = ph.getString('checkbox2_isTrigger') == 'on'
        }

        if (cmd[1] == WfConditionPrototype.KEY) {
            condition = condition as WfConditionPrototype

            if (ph.getInt('type') && ph.getInt('type') != condition.type) {
                resetValuesAndMeta(condition)
            }
            else {
                setValuesAndMeta(condition)
            }

            // if created
            //if (condition.checkbox1 == null) { condition.checkbox1 = false }
            //if (condition.checkbox2 == null) { condition.checkbox2 = false }

            condition.title         = ph.getString('title')
            condition.description   = ph.getString('description')
            condition.type          = ph.getInt('type') ?: 0
        }
        else if (cmd[1] == WfCondition.KEY) {
            condition = condition as WfCondition

            if (ph.getInt('type') && ph.getInt('type') != condition.type) {
                resetValuesAndMeta(condition)
            }
            else {
                setValuesAndMeta(condition)

                // values
                condition.checkbox1 = ph.getString('checkbox1') == 'on'
                condition.checkbox2 = ph.getString('checkbox2') == 'on'
                condition.date1     = ph.getDate('date1')
                condition.date2     = ph.getDate('date2')
                condition.file1     = ph.getDocContext('file1')
            }

            condition.title         = ph.getString('title')
            condition.description   = ph.getString('description')
            condition.type          = ph.getInt('type') ?: 0
        }

        Map<String, Object> result = [ condition: condition, cmd: cmd[0], key: cmd[1] ]

        result.status = condition.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
        result
    }

    /**
     * Instantiates the given workflow to the given subscription
     * @param params the request parameter map
     * @return a result map with the execution status
     */
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

    /**
     * Removes the workflow instance from its target
     * @param params the request parameter map
     * @return a result map with the execution status
     */
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
                        ts.flush() // TODO
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

    /**
     * Generic method from the dashboard to proceed with the workflow
     * @param params the request parameter map
     * @return a result containing the execution result
     */
    Map<String, Object> usage(GrailsParameterMap params) {
        log.debug('usage() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[0] == 'usage') {  // TODO return msg

            ParamsHelper ph = getNewParamsHelper( cmd[1], params )

            if (cmd[1] == WfWorkflow.KEY) {
                WfWorkflow workflow = WfWorkflow.get( cmd[2] )
                boolean wChanged

                String comment = ph.getString('comment')
                if (comment != workflow.comment) {
                    workflow.comment = comment
                    wChanged = true
                }
                RefdataValue status = ph.getRefdataValue('status')
                if (status != workflow.status) {
                    workflow.status = status
                    wChanged = true
                }
                if (wChanged) {
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
                            cChanged = true
                        }
                    }
                    if (cFields.contains('checkbox2')) {
                        String checkbox2 = ph.getString('checkbox2')
                        if ((checkbox2 == 'on') != condition.checkbox2) {
                            condition.checkbox2 = (checkbox2 == 'on')
                            cChanged = true
                        }
                    }
                    if (cFields.contains('date1')) {
                        Date date1 = ph.getDate('date1')
                        if (date1 != condition.date1) {
                            condition.date1 = date1
                            cChanged = true
                        }
                    }
                    if (cFields.contains('date2')) {
                        Date date2 = ph.getDate('date2')
                        if (date2 != condition.date2) {
                            condition.date2 = date2
                            cChanged = true
                        }
                    }

                    if (params.get('wfUploadFile-placeholder')) {

                        def file = WebUtils.retrieveGrailsWebRequest().getCurrentRequest().getFile("wfUploadFile")
                        if (file) {
                            Doc.withTransaction { TransactionStatus ts ->
                                try {
                                    Doc doc = new Doc(
                                        contentType:    Doc.CONTENT_TYPE_FILE,
                                        filename:       file.originalFilename,
                                        mimeType:       file.contentType,
                                        title:          params.wfUploadTitle ?: file.originalFilename,
                                        type:           RefdataValue.get(params.wfUploadDoctype),
                                        creator:        contextService.getUser(),
                                        owner:          contextService.getOrg()
                                    )
                                    doc.save()

                                    String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'
                                    String fName = doc.uuid

                                    File folder = new File("${fPath}")
                                    if (!folder.exists()) {
                                        folder.mkdirs()
                                    }
                                    File newFile = new File("${fPath}/${fName}")
                                    file.transferTo(newFile)

                                    Subscription sub = genericOIDService.resolveOID(params.wfUploadOwner) as Subscription

                                    DocContext docctx = new DocContext(
                                        subscription: sub,
                                        owner: doc,
                                        doctype: RefdataValue.get(params.wfUploadDoctype)
                                    )
                                    docctx.save()

                                    condition.file1 = docctx
                                    cChanged = true
                                }
                                catch (Exception e) {
                                    cChanged = false
                                    result.status = OP_STATUS_ERROR

                                    e.printStackTrace()
                                    ts.setRollbackOnly()
                                }
                            }
                        }
                    }
                    else if (cFields.contains('file1')) {
                        DocContext file1 = ph.getDocContext('file1')
                        if (file1 != condition.file1) {
                            condition.file1 = file1
                            cChanged = true
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
        else if (cmd[0] == 'instantiate') {
            result.status = OP_STATUS_ERROR

            if (params.subId && params.workflowId) {
                GrailsParameterMap clone = params.clone()
                clone.setProperty( 'cmd', params.cmd + ':' + params.workflowId )

                result = instantiateCompleteWorkflow( clone )
            }
        }

        result
    }
}