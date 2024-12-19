package de.laser


import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import de.laser.workflow.*
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.TransactionStatus

/**
 * This service handles workflow-related requests such as creating, updating entire lists
 * and creating, altering or deleting checkpoints on them
 * @see WfChecklist
 * @see WfCheckpoint
 */
@Transactional
class WorkflowService {

    AccessService accessService
    ContextService contextService
    GenericOIDService genericOIDService

    public static final String OP_STATUS_DONE       = 'OP_STATUS_DONE'
    public static final String OP_STATUS_ERROR      = 'OP_STATUS_ERROR'
    public static final String OP_STATUS_FORBIDDEN  = 'OP_STATUS_FORBIDDEN'

    /**
     * Constructs a new ParamsHelper instance with the given key and parameter map
     * @param cmpKey the key to extract
     * @param params the request parameter map
     * @return the new {@link ParamsHelper} instance
     */
    private ParamsHelper _getNewParamsHelper(String cmpKey, GrailsParameterMap params) {
        new ParamsHelper(cmpKey, params)
    }

    /**
     * Reloads the workflow list. If a command has been submitted, the list is updated before being returned
     * @param result the result map containing generics
     * @param params the request parameter map
     * @return the updated result set
     */
    def executeCmdAndUpdateResult(Map<String, Object> result, GrailsParameterMap params) {

        if (params.cmd) {
            log.debug('executeCmdAndUpdateResult() ' + params)

            String[] cmd = (params.cmd as String).split(':')

            if (cmd[1] in [WfChecklist.KEY, WfCheckpoint.KEY] ) {
                result.putAll( executeCmd(params) )
            }
        }
        if (params.info) {
            result.info = params.info // @ flyout
        }
        result.checklist = []

//        if (true) {
            if (result.license) {
                result.checklists = sortByLastUpdated(WfChecklist.findAllByLicenseAndOwner(result.license as License, contextService.getOrg()))
            }
            else if (result.orgInstance) {
                result.checklists = sortByLastUpdated(WfChecklist.findAllByOrgAndOwner(result.orgInstance as Org, contextService.getOrg()))
            }
            else if (result.provider) {
                result.checklists = sortByLastUpdated( WfChecklist.findAllByProviderAndOwner(result.provider as Provider, contextService.getOrg()))
            }
            else if (result.subscription) {
                result.checklists = sortByLastUpdated(WfChecklist.findAllBySubscriptionAndOwner(result.subscription as Subscription, contextService.getOrg()))
            }
            else if (result.vendor) {
                result.checklists = sortByLastUpdated(WfChecklist.findAllByVendorAndOwner(result.vendor as Vendor, contextService.getOrg()))
            }
//        }

        result.checklistCount = result.checklists.size()
    }

    /**
     * Executes the given command on the workflow list and returns the updated list
     * @param params the request parameter map
     * @return a {@link Map} containing the current workflow
     */
    Map<String, Object> executeCmd(GrailsParameterMap params) {
        log.debug('executeCmd() ' + params)

        Map<String, Object> result = [:]

        if (params.cmd) {
            result.status = OP_STATUS_FORBIDDEN

            if (hasWRITE()) {
                String[] cmd = (params.cmd as String).split(':')

                if (cmd[1] == WfChecklist.KEY) {

                    if (cmd[0] == 'create') { // actions > modal
                        result = _createChecklist(params) // TODO check perms
                    }
                    else if (cmd[0] == 'instantiate') { // actions > modal
                        result.status = OP_STATUS_ERROR

                        if (params.target && params.sourceId) {
                            GrailsParameterMap clone = params.clone() as GrailsParameterMap
                            clone.setProperty('cmd', params.cmd + ':' + params.sourceId)

                            result = instantiateChecklist(clone)
                        }
                    }
                    else if (cmd[0] == 'delete') { // table
                        if (accessService.hasAccessToWorkflow(WfChecklist.get(cmd[2]), AccessService.WRITE)) {
                            result = _deleteChecklist(cmd)
                        }
                    }
                }
                else if (cmd[1] == WfCheckpoint.KEY) {

                    if (cmd[0] == 'create') { // flyout
                        result = _createCheckpoint(params) // TODO check perms
                    }
                    else if (cmd[0] == 'delete') {  // flyout
                        if (accessService.hasAccessToWorkflow(WfCheckpoint.get(cmd[2]).checklist, AccessService.WRITE)) {
                            result = _deleteCheckpoint(cmd)
                        }
                    }
                    else if (cmd[0] in ['moveUp', 'moveDown']) { // flyout
                        if (accessService.hasAccessToWorkflow(WfCheckpoint.get(cmd[2]).checklist, AccessService.WRITE)) {
                            result = _moveCheckpoint(cmd)
                        }
                    }
                    else if (cmd[0] == 'modal') { // table
                        if (accessService.hasAccessToWorkflow(WfCheckpoint.get(cmd[2]).checklist, AccessService.WRITE)) {
                            ParamsHelper ph = _getNewParamsHelper( cmd[1], params )

                            WfCheckpoint cpoint = WfCheckpoint.get( cmd[2] )
                            boolean tChanged = false

                            boolean done = ph.getChecked('done')
                            if (done != cpoint.done) {
                                cpoint.done = done
                                tChanged = true
                            }
                            Date date = ph.getDate('date')
                            if (date != cpoint.date) {
                                cpoint.date = date
                                tChanged = true
                            }
                            String comment = ph.getString('comment')
                            if (comment != cpoint.comment) {
                                cpoint.comment = comment
                                tChanged = true
                            }
                            if (tChanged) {
                                result.status = cpoint.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
                            }
                        }
                    }
                }
            }
        }
        result
    }

    /**
     * Deletes the given checkpoint
     * @param cmd the command as array [command, key, workflow checkpoint]
     * @return the deletion result with the new empty position
     */
    private Map<String, Object> _deleteCheckpoint(String[] cmd) {
        log.debug('_deleteCheckpoint() ' + cmd)

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]
        WfCheckpoint cpoint = WfCheckpoint.get( cmd[2] )
        result.checkpoint = cpoint

        try {
            cpoint.delete()
            result.checkpoint = null // gap
            result.status = OP_STATUS_DONE
        }
        catch (Exception e) {
            result.status = OP_STATUS_ERROR
            log.error(e.getMessage())
        }
        result
    }

    /**
     * Moves the given checkpoint up or down by one
     * @param cmd the command in structure [command (moveUp or moveDown), key, workflow point]
     * @return the movement result status: {@link #OP_STATUS_DONE} if successful, {@link #OP_STATUS_ERROR} on failure
     */
    private Map<String, Object> _moveCheckpoint(String[] cmd) {
        log.debug('_moveCheckpoint() ' + cmd)

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]
        WfCheckpoint cpoint = WfCheckpoint.get( cmd[2] )
        result.checkpoint = cpoint

        try {
            Set<WfCheckpoint> sequence = cpoint.checklist.getSequence()
            int idx = sequence.findIndexOf {it.id == cpoint.id}
            int pos = cpoint.position
            WfCheckpoint cpoint2

            if (result.cmd == 'moveUp')        { cpoint2 = sequence.getAt(idx-1) }
            else if (result.cmd == 'moveDown') { cpoint2 = sequence.getAt(idx+1) }

            if (cpoint2) {
                cpoint.position = cpoint2.position
                cpoint.save()
                cpoint2.position = pos
                cpoint2.save()

                result.status = OP_STATUS_DONE
            }
        }
        catch (Exception e) {
            result.status = OP_STATUS_ERROR
            log.error(e.getMessage())
        }
        result
    }

    /**
     * Creates a new checklist with the given parameters
     * @param params the request parameter map, containing the data for the new workflow checklist
     * @return a {@link Map} containing the creation result
     */
    private Map<String, Object> _createChecklist(GrailsParameterMap params) {
        log.debug( '_createChecklist() ' + params )

        String[] cmd = (params.cmd as String).split(':')
        ParamsHelper ph = _getNewParamsHelper( cmd[1], params )

        WfChecklist clist   = new WfChecklist()
        clist.title         = ph.getString('title')
        clist.description   = ph.getString('description')
        clist.comment       = ph.getString('comment')

        def target = ph.getTarget()

        clist.license       = target instanceof License ? target : null
        clist.org           = target instanceof Org ? target : null
        clist.provider      = target instanceof Provider ? target : null
        clist.subscription  = target instanceof Subscription ? target : null
        clist.vendor        = target instanceof Vendor ? target : null

        clist.owner         = contextService.getOrg()
        clist.template      = Boolean.parseBoolean(ph.getString('template'))

        Map<String, Object> result = [ checklist: clist, cmd: cmd[0], key: cmd[1] ]

        WfChecklist.withTransaction { TransactionStatus ts ->

            try {
                result.status = OP_STATUS_ERROR

                if (! clist.validate()) {
                    log.debug( 'createChecklist() : ' + clist.getErrors().toString() )
                }
                else {
                    boolean saved = clist.save()

                    // todo
                    if (cmd[0] == 'create' && ph.getInt('numberOfPoints')) {
                        for (int i = 1; i <= ph.getInt('numberOfPoints'); i++) {

                            if (saved) {
                                GrailsParameterMap tmp = params.clone() as GrailsParameterMap
                                tmp.clear()
                                tmp.WF_CHECKPOINT_title = 'Aufgabe ' + i
                                tmp.WF_CHECKPOINT_position = i
                                tmp.WF_CHECKPOINT_checklist = clist.id
                                tmp.cmd = 'create:WF_CHECKPOINT'

                                Map<String, Object> tmpResult = _createCheckpoint(tmp)
                                saved = saved && (tmpResult.status == OP_STATUS_DONE)
                            }
                        }
                    }
                    result.status = saved ? OP_STATUS_DONE : OP_STATUS_ERROR
                }
            }
            catch (Exception e) {
                result.status = OP_STATUS_ERROR

                log.debug( 'internalEditChecklist() -> ' + e.getMessage() )
                e.printStackTrace()
            }
            finally {
                if (result.status == OP_STATUS_ERROR && clist.validate()) {
                    log.debug( 'TransactionStatus.setRollbackOnly(!)' )
                    ts.setRollbackOnly()
                }
            }
        }
        result
    }

    /**
     * Creates a new checkpoint for the given list with the given parameters
     * @param params the request parameter map
     * @return a {@link Map} containing the creation result
     */
    private Map<String, Object> _createCheckpoint(GrailsParameterMap params) {
        log.debug( '_createCheckpoint() ' + params )

        String[] cmd = (params.cmd as String).split(':')
        ParamsHelper ph = _getNewParamsHelper( cmd[1], params )

        WfCheckpoint cpoint = new WfCheckpoint()
        cpoint.title        = ph.getString('title')
        cpoint.description  = ph.getString('description')
        cpoint.comment      = ph.getString('comment')

        cpoint.date         = ph.getDate('date')
        cpoint.done         = Boolean.parseBoolean(ph.getString('done'))
        cpoint.checklist    = WfChecklist.get(ph.getLong('checklist'))
        cpoint.position     = ph.getInt('position') ?: cpoint.checklist.getNextPosition()

        Map<String, Object> result = [ checkpoint: cpoint, cmd: cmd[0], key: cmd[1] ]

        result.status = cpoint.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
        result
    }

    /**
     * Creates a new checklist, prefilling values from another checklist
     * @param params the request parameter map
     * @return a {@link Map} containing the creation result
     */
    Map<String, Object> instantiateChecklist(GrailsParameterMap params) {
        log.debug('instantiateChecklist() ' + params)

        String[] cmd = (params.cmd as String).split(':')
        ParamsHelper ph = _getNewParamsHelper( cmd[1], params )

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        WfChecklist.withTransaction { TransactionStatus ts ->

            try {
                result.source = WfChecklist.get( cmd[2] )

                result.checklist               = new WfChecklist()
                result.checklist.title         = ph.getString('title') ?: result.source.title
                result.checklist.description   = ph.getString('description') ?: result.source.description
                result.checklist.owner         = result.source.owner

                def target = genericOIDService.resolveOID(params.target)

                if (target instanceof License)              { result.checklist.license = target }
                else if (target instanceof Org)             { result.checklist.org = target }
                else if (target instanceof Provider)        { result.checklist.provider = target }
                else if (target instanceof Subscription)    { result.checklist.subscription = target }
                else if (target instanceof Vendor)          { result.checklist.vendor = target }

                result.status = OP_STATUS_ERROR

                if (! result.checklist.validate()) {
                    log.debug( '[ ' + result.source.id + ' ].instantiate(' + target + ') : ' + result.checklist.getErrors().toString() )
                }
                else {
                    boolean saved = result.checklist.save()

                    result.source.getSequence().each { cpoint ->
                        WfCheckpoint checkpoint = new WfCheckpoint()
                        checkpoint.title        = cpoint.title
                        checkpoint.description  = cpoint.description
                        checkpoint.checklist    = result.checklist as WfChecklist
                        checkpoint.position     = cpoint.position

                        if (! checkpoint.validate()) {
                            log.debug( '[ ' + checkpoint.id + ' ].instantiate() : ' + checkpoint.getErrors().toString() )
                        }
                        saved = saved && checkpoint.save()
                    }

                    if (! saved) {
                        log.debug( 'instantiateCompleteChecklist() -> ' + result.checklist.getErrors().toString() )
                        log.debug( 'TransactionStatus.setRollbackOnly()' )
                        ts.setRollbackOnly()
                    }
                    else {
                        result.status = OP_STATUS_DONE
                    }
                }
            }
            catch (Exception e) {
                result.status = OP_STATUS_ERROR

                log.debug( 'instantiateCompleteChecklist() -> ' + e.getMessage() )
                e.printStackTrace()

                log.debug( 'TransactionStatus.setRollbackOnly()' )
                ts.setRollbackOnly()
            }
        }

        result
    }

    /**
     * Deletes the given checklist
     * @param cmd the command to be executed, in structure [command, key, list]
     * @return a {@link Map} confirming the deletion for [command, key]
     */
    private Map<String, Object> _deleteChecklist(String[] cmd) {
        log.debug('_deleteChecklist() ' + cmd)

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        WfChecklist.withTransaction { TransactionStatus ts ->

            try {
                result.checklist = WfChecklist.get(cmd[2])

                WfCheckpoint.executeUpdate('delete from WfCheckpoint cp where cp.checklist = :cl', [cl: result.checklist])
                result.checklist.delete()

                result.status = OP_STATUS_DONE
                ts.flush() // TODO
            }
            catch (Exception e) {
                result.status = OP_STATUS_ERROR

                log.debug( 'removeCompleteChecklist() -> ' + e.getMessage() )
                e.printStackTrace()

                log.debug( 'TransactionStatus.setRollbackOnly(B)' )
                ts.setRollbackOnly()
            }
        }

        result
    }

    /**
     * Sorts the given checklists by their date of last update and returns the updated list
     * @param wfList the {@link List} of {@link WfChecklist} to sort
     * @return the sorted list
     */
    List<WfChecklist> sortByLastUpdated(List<WfChecklist> wfList) {
//        println wfList.collect{ it.id + '_' + it.getInfo().lastUpdated }.take(5)
        wfList.sort{ a,b -> b.getInfo().lastUpdated <=> a.getInfo().lastUpdated }
    }

    /**
     * Retrieves the current workflows for the given object (one of {@link Subscription}, {@link License}, {@link Org}) and institution
     * @param obj the object of which workflows should be returned
     * @param owner the institution ({@link Org}) who defined the workflows to be returned
     * @return a {@link List} of {@link WfChecklist} workflows attached to the given object
     */
    List<WfChecklist> getWorkflows(def obj, Org owner) {
        List result = []

        if (hasREAD()) {
            if (obj instanceof License) {
                result = WfChecklist.executeQuery('select wf from WfChecklist wf where wf.license = :lic and wf.owner = :ctxOrg', [lic: obj, ctxOrg: owner])
            }
            else if (obj instanceof Org) {
                result = WfChecklist.executeQuery('select wf from WfChecklist wf where wf.org = :org and wf.owner = :ctxOrg', [org: obj, ctxOrg: owner])
            }
            else if (obj instanceof Provider) {
                result = WfChecklist.executeQuery('select wf from WfChecklist wf where wf.provider = :provider and wf.owner = :ctxOrg', [provider: obj, ctxOrg: owner])
            }
            else if (obj instanceof Subscription) {
                result = WfChecklist.executeQuery('select wf from WfChecklist wf where wf.subscription = :sub and wf.owner = :ctxOrg', [sub: obj, ctxOrg: owner])
            }
            else if (obj instanceof Vendor) {
                result = WfChecklist.executeQuery('select wf from WfChecklist wf where wf.vendor = :vendor and wf.owner = :ctxOrg', [vendor: obj, ctxOrg: owner])
            }
        }
        result
    }

    /**
     * Retrieves the count of the workflows for the given object (one of {@link Subscription}, {@link License}, {@link Org}) and institution
     * @param obj the object of which workflows should be returned
     * @param owner the institution ({@link Org}) who defined the workflows to be returned
     * @return the number of {@link WfChecklist} workflows attached to the given object
     */
    int getWorkflowCount(def obj, Org owner) {
        int result = 0

        if (hasREAD()) {
            if (obj instanceof License) {
                result = WfChecklist.executeQuery('select count(*) from WfChecklist wf where wf.license = :lic and wf.owner = :ctxOrg', [lic: obj, ctxOrg: owner])[0]
            }
            else if (obj instanceof Org) {
                result = WfChecklist.executeQuery('select count(*) from WfChecklist wf where wf.org = :org and wf.owner = :ctxOrg', [org: obj, ctxOrg: owner])[0]
            }
            else if (obj instanceof Provider) {
                result = WfChecklist.executeQuery('select count(*) from WfChecklist wf where wf.provider = :provider and wf.owner = :ctxOrg', [provider: obj, ctxOrg: owner])[0]
            }
            else if (obj instanceof Subscription) {
                result = WfChecklist.executeQuery('select count(*) from WfChecklist wf where wf.subscription = :sub and wf.owner = :ctxOrg', [sub: obj, ctxOrg: owner])[0]
            }
            else if (obj instanceof Vendor) {
                result = WfChecklist.executeQuery('select count(*) from WfChecklist wf where wf.vendor = :vendor and wf.owner = :ctxOrg', [vendor: obj, ctxOrg: owner])[0]
            }
        }
        result
    }

    /**
     * Checks if the current user has *potential* reading rights
     * @return true if the context user belongs to a PRO customer, false otherwise
     * @see CustomerTypeService#PERMS_PRO
     * @see ContextService#isInstUser()
     */
    boolean hasREAD() {
        contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    }

    /**
     * Checks if the current user has *potential* editing rights
     * @return true if the context user is at least {@link de.laser.auth.Role#INST_EDITOR} at a PRO customer, false otherwise
     * @see CustomerTypeService#PERMS_PRO
     * @see ContextService#isInstEditor()
     */
    boolean hasWRITE() {
        contextService.isInstEditor(CustomerTypeService.PERMS_PRO)
    }
}