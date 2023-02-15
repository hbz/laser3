package de.laser

import de.laser.auth.User
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.utils.AppUtils
import de.laser.workflow.*
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.util.WebUtils
import org.springframework.transaction.TransactionStatus

/**
 * This service contains methods for workflow handling
 */
@Transactional
class WorkflowOldService {

    ContextService contextService
    GenericOIDService genericOIDService

    public static final String OP_STATUS_DONE  = 'OP_STATUS_DONE'
    public static final String OP_STATUS_ERROR = 'OP_STATUS_ERROR'

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
    Map<String, Object> cmd(GrailsParameterMap params) { // create, edit, delete
        log.debug('cmd() ' + params)
        Map<String, Object> result = [:]

        if (params.cmd) {
            String[] cmd = (params.cmd as String).split(':')
            String wfObjKey = cmd[1]

            /* if (cmd[0] == 'instantiate') {
                if (wfObjKey == WfWorkflowPrototype.KEY) {
                    result = instantiateCompleteWorkflow(params)
                }
            }
            else */ if (cmd[0] == 'create') {
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
                    log.error e.getMessage()
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
                    log.error e.getMessage()
                }
            }
        }
        else if (cmd[1] == WfTask.KEY) {
            WfTask task = WfTask.get( cmd[2] )
            result.task = task
            result.status = OP_STATUS_ERROR

            log.info '--- NOT IMPLEMENTED ---'
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
                    log.error e.getMessage()
                }
            }
        }
        else if (cmd[1] == WfCondition.KEY) {
            WfCondition condition = WfCondition.get( cmd[2] )
            result.condition = condition
            result.status = OP_STATUS_ERROR

            log.info '--- NOT IMPLEMENTED ---'
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
        log.debug( 'internalEditWorkflow() ' + wf.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = getNewParamsHelper( cmd[1], params )

        if (cmd[1] == WfWorkflowPrototype.KEY) {
            wf = wf as WfWorkflowPrototype

            wf.title        = ph.getString('title')
            wf.description  = ph.getString('description')
            wf.task         = WfTaskPrototype.get(ph.getLong('task'))
            wf.state        = RefdataValue.get(ph.getLong('state'))
            wf.variant      = ph.getString('variant')
            wf.targetType   = RefdataValue.get(ph.getLong('targetType'))
            wf.targetRole   = RefdataValue.get(ph.getLong('targetRole'))
        }
        else if (cmd[1] == WfWorkflow.KEY) {
            wf = wf as WfWorkflow

            wf.title        = ph.getString('title')
            wf.description  = ph.getString('description')
            wf.comment      = ph.getString('comment')
            wf.status       = RefdataValue.get(ph.getLong('status'))
            wf.user         = User.get(ph.getLong('user'))

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
        log.debug( 'internalEditTask() ' + task.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = getNewParamsHelper( cmd[1], params )

        if (cmd[1] == WfTaskPrototype.KEY) {
            task = task as WfTaskPrototype

            task.title          = ph.getString('title')
            task.description    = ph.getString('description')
            task.priority       = RefdataValue.get(ph.getLong('priority'))
            task.condition      = WfConditionPrototype.get(ph.getLong('condition'))

            // TODO - check for circular references

            WfTaskPrototype tNext = WfTaskPrototype.get(ph.getLong('next'))
            boolean circularReference= false
            while (tNext) {
                if (tNext.id == task.id) {
                    circularReference = true; break
                }
                tNext = tNext.next
            }
            if (!circularReference) {
                task.next = WfTaskPrototype.get(ph.getLong('next'))
            }
            else {
                return [ task: task, cmd: cmd[0], key: cmd[1], status: OP_STATUS_ERROR ]
            }
        }
        else if (cmd[1] == WfTask.KEY) {
            task = task as WfTask

            task.title          = ph.getString('title')
            task.description    = ph.getString('description')
            task.priority       = RefdataValue.get(ph.getLong('priority'))
            task.comment        = ph.getString('comment')
            task.status         = RefdataValue.get(ph.getLong('status'))

            // task.condition      = WfCondition.get(ph.getLong('condition'))
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
        log.debug( 'internalEditCondition() ' + condition.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = getNewParamsHelper( cmd[1], params )

        Closure resetValuesAndMeta = { WfConditionBase wfc ->

            for(int i=1; i<=4; i++) {
                wfc['checkbox' + i] = false
                wfc['checkbox' + i + '_isTrigger'] = false
                wfc['checkbox' + i + '_title'] = null

                wfc['date' + i] = null
                wfc['date' + i + '_title'] = null

                if (i<=2) {
                    wfc['file' + i] = null
                    wfc['file' + i + '_title'] = null
                }
            }
        }

        Closure setValuesAndMeta = { WfConditionBase wfc ->

            for(int i=1; i<=4; i++) {
                wfc['checkbox' + i + '_title']      = ph.getString('checkbox' + i + '_title')
                wfc['checkbox' + i + '_isTrigger']  = ph.getString('checkbox' + i + '_isTrigger') == 'on'
                wfc['date' + i + '_title']          = ph.getString('date' + i + '_title')

                if (i<=2) {
                    wfc['file' + i + '_title']      = ph.getString('file' + i + '_title')
                }
            }
        }

        if (cmd[1] == WfConditionPrototype.KEY) {
            condition = condition as WfConditionPrototype

            if (ph.getString('type') && ph.getString('type') != condition.type) {
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
            condition.type          = ph.getString('type') ?: '0_0_0'
        }
        else if (cmd[1] == WfCondition.KEY) {
            condition = condition as WfCondition

            if (ph.getString('type') && ph.getString('type') != condition.type) {
                resetValuesAndMeta(condition)
            }
            else {
                setValuesAndMeta(condition)

                // values
                for(int i=1; i<=4; i++) {
                    condition['checkbox' + i]   = ph.getString('checkbox' + i) == 'on'
                    condition['date' + i]       = ph.getDate('date' + i)

                    if (i<=2) {
                        condition['file' + i]   = ph.getDocContext('file' + i)
                    }
                }
            }

            condition.title         = ph.getString('title')
            condition.description   = ph.getString('description')
            condition.type          = ph.getString('type') ?: '0_0_0'
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
                    result.workflow     = result.prototype.instantiate( genericOIDService.resolveOID(params.target)  )

                    if (params.workflowName) {
                        result.workflow.title = params.workflowName
                    }
                    if (params.workflowUser && params.workflowUser != 'all') {
                        result.workflow.user = User.get(params.workflowUser)
                    }

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
                        log.debug( 'TransactionStatus.setRollbackOnly(A)' )
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

                    log.debug( 'TransactionStatus.setRollbackOnly(B)' )
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
    Map<String, Object> usage(GrailsParameterMap params) { // instantiate, usage, delete
        log.debug('usage() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        if (cmd[0] == 'instantiate') {
            result.status = OP_STATUS_ERROR

            if (params.target && params.sourceId) {
                GrailsParameterMap clone = params.clone() as GrailsParameterMap
                clone.setProperty('cmd', params.cmd + ':' + params.sourceId)

                result = instantiateCompleteWorkflow( clone )
            }
        }
        else if (cmd[0] == 'usage') {  // TODO return msg

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
                User user = User.get(ph.getLong('user'))
                if (user != workflow.user) {
                    workflow.user = user
                    workflow.userLastUpdated = new Date()
                    wChanged = true
                }
                if (wChanged) {
                    result.status = workflow.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
                }
            }
            else if (cmd[1] == WfTask.KEY) {
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

                    for(int i=1; i<=4; i++) {
                        if (cFields.contains('checkbox' + i)) {
                            String value = ph.getString('checkbox' + i)
                            if ((value == 'on') != condition['checkbox' + i]) {
                                condition['checkbox' + i] = (value == 'on')
                                cChanged = true
                            }
                        }
                        if (cFields.contains('date' + i)) {
                            Date value = ph.getDate('date' + i)
                            if (value != condition['date' + i]) {
                                condition['date' + i] = value
                                cChanged = true
                            }
                        }
                    }

                    for(int i=1; i<=4; i++) {
                        String fileId = 'file' + i

                        if (params.get('wfUploadFile_placeholder_' + fileId)) {

                            def file = WebUtils.retrieveGrailsWebRequest().getCurrentRequest().getFile('wfUploadFile_file_' + fileId)
                            if (file) {
                                Doc.withTransaction { TransactionStatus ts ->
                                    try {

                                        String uploadTitle         = params.get('wfUploadTitle_' + fileId) ?: file.originalFilename
                                        String uploadOwner         = params.get('wfUploadOwner_' + fileId)
                                        RefdataValue uploadDoctype = RefdataValue.get(params.get('wfUploadDoctype_' + fileId) as Serializable)
                                        RefdataValue uploadCnf     = params.get('wfUploadConfidentiality_' + fileId) ? RefdataValue.get(params.get('wfUploadConfidentiality_' + fileId) as Serializable) : null

                                        Doc doc = new Doc(
                                                contentType: Doc.CONTENT_TYPE_FILE,
                                                filename: file.originalFilename,
                                                mimeType: file.contentType,
                                                title: uploadTitle,
                                                type: uploadDoctype,
                                                confidentiality: uploadCnf,
                                                creator: contextService.getUser(),
                                                owner: contextService.getOrg(),
                                                server: AppUtils.getCurrentServer()
                                        )
                                        doc.save()

                                        String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                                        String fName = doc.uuid

                                        File folder = new File("${fPath}")
                                        if (!folder.exists()) {
                                            folder.mkdirs()
                                        }
                                        File newFile = new File("${fPath}/${fName}")
                                        file.transferTo(newFile)

                                        // TODO

                                        DocContext docctx = new DocContext(
                                                owner: doc
                                        )
                                        Object owner = genericOIDService.resolveOID( uploadOwner )

                                        if (owner instanceof Org) {
                                            docctx.org = owner

                                            if (params.get('wfUploadShareConf_' + fileId)) {
                                                docctx.shareConf = RefdataValue.get(params.get('wfUploadShareConf_' + fileId) as Serializable)
                                            }
                                        }
                                        else if (owner instanceof License) {
                                            docctx.license = owner
                                        }
                                        else if (owner instanceof Subscription) {
                                            docctx.subscription = owner
                                        }
                                        else {
                                            throw new Exception('Invalid owner for workflow document upload.')
                                        }
                                        docctx.save()

                                        condition['file' + i] = docctx
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

                        else {
                            if (cFields.contains('file' + i)) {
                                DocContext file = ph.getDocContext('file' + i)
                                if (file != condition['file' + i]) {
                                    condition['file' + i] = file
                                    cChanged = true
                                }
                            }
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

    List<WfWorkflow> sortByLastUpdated(List<WfWorkflow> wfList) {
        wfList.sort{ a,b -> b.getInfo().lastUpdated <=> a.getInfo().lastUpdated }
    }

    int getWorkflowCount(def obj, Org owner) {
        if (obj instanceof Subscription) {
            WfWorkflow.executeQuery('select count(wf) from WfWorkflow wf where wf.subscription = :sub and wf.owner = :ctxOrg', [sub: obj, ctxOrg: owner])[0]
        }
        else if (obj instanceof License) {
            WfWorkflow.executeQuery('select count(wf) from WfWorkflow wf where wf.license = :lic and wf.owner = :ctxOrg', [lic: obj, ctxOrg: owner])[0]
        }
        else if (obj instanceof Org) {
            WfWorkflow.executeQuery('select count(wf) from WfWorkflow wf where wf.org = :org and wf.owner = :ctxOrg', [org: obj, ctxOrg: owner])[0]
        }
        else {
            return 0
        }
    }

    boolean hasUserPerm_read() {
        _innerPermissionCheck('INST_USER')
    }
    boolean hasUserPerm_init() {
        _innerPermissionCheck('INST_EDITOR')
    }
    boolean hasUserPerm_edit() {
        _innerPermissionCheck('INST_EDITOR')
    }
    boolean hasUserPerm_wrench() {
        _innerPermissionCheck('INST_EDITOR')
    }

    private boolean _innerPermissionCheck(String userRoleName) {
        User user = contextService.getUser()
        if (user.isAdmin() || user.isYoda()) {
            return true
        }
        Org ctxOrg = contextService.getOrg()
        if (userRoleName && ctxOrg.getCustomerType() in ['ORG_CONSORTIUM'] && user.hasAffiliationForForeignOrg(userRoleName, ctxOrg)) {
            return true
        }
        false
    }
}