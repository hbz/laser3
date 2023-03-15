package de.laser

import de.laser.auth.User
import de.laser.workflow.*
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.TransactionStatus

@Transactional
class WorkflowService {

    ContextService contextService
    GenericOIDService genericOIDService

    public static final String OP_STATUS_DONE  = 'OP_STATUS_DONE'
    public static final String OP_STATUS_ERROR = 'OP_STATUS_ERROR'

    ParamsHelper getNewParamsHelper(String cmpKey, GrailsParameterMap params) {
        new ParamsHelper(cmpKey, params)
    }

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

        if (result.contextOrg) {
            if (result.orgInstance) {
                result.checklists = sortByLastUpdated(WfChecklist.findAllByOrgAndOwner(result.orgInstance as Org, result.contextOrg as Org))
            }
            else if (result.license) {
                result.checklists = sortByLastUpdated( WfChecklist.findAllByLicenseAndOwner(result.license as License, result.contextOrg as Org))
            }
            else if (result.subscription) {
                result.checklists = sortByLastUpdated(WfChecklist.findAllBySubscriptionAndOwner(result.subscription as Subscription, result.contextOrg as Org))
            }
        }

        result.checklistCount = result.checklists.size()
    }

    Map<String, Object> executeCmd(GrailsParameterMap params) {
        log.debug('executeCmd() ' + params)

        Map<String, Object> result = [:]

        if (params.cmd) {
            String[] cmd = (params.cmd as String).split(':')

            if (cmd[1] == WfChecklist.KEY) {

                if (cmd[0] == 'create') { // actions > modal
                    result = createChecklist(params)
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
                    result = deleteChecklist( cmd )
                }
            }
            else if (cmd[1] == WfCheckpoint.KEY) {

                if (cmd[0] == 'create') { // flyout
                    result = createCheckpoint(params)
                }
                else if (cmd[0] == 'delete') {  // flyout
                    result = deleteCheckpoint( cmd )
                }
                else if (cmd[0] in ['moveUp', 'moveDown']) { // flyout
                    result = moveCheckpoint( cmd )
                }
                else if (cmd[0] == 'modal') { // table
                    ParamsHelper ph = getNewParamsHelper( cmd[1], params )

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
        result
    }

    Map<String, Object> deleteCheckpoint(String[] cmd) {
        log.debug('deleteCheckpoint() ' + cmd)

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

    Map<String, Object> moveCheckpoint(String[] cmd) {
        log.debug('moveCheckpoint() ' + cmd)

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

    Map<String, Object> createChecklist(GrailsParameterMap params) {
        log.debug( 'createChecklist() ' + params )

        String[] cmd = (params.cmd as String).split(':')
        ParamsHelper ph = getNewParamsHelper( cmd[1], params )

        WfChecklist clist   = new WfChecklist()
        clist.title         = ph.getString('title')
        clist.description   = ph.getString('description')
        clist.comment       = ph.getString('comment')

        def target = ph.getTarget()

        clist.subscription  = target instanceof Subscription ? target : null
        clist.license       = target instanceof License ? target : null
        clist.org           = target instanceof Org ? target : null

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

                                Map<String, Object> tmpResult = createCheckpoint(tmp)
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

    Map<String, Object> createCheckpoint(GrailsParameterMap params) {
        log.debug( 'createCheckpoint() ' + params )

        String[] cmd = (params.cmd as String).split(':')
        ParamsHelper ph = getNewParamsHelper( cmd[1], params )

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

    Map<String, Object> instantiateChecklist(GrailsParameterMap params) {
        log.debug('instantiateChecklist() ' + params)

        String[] cmd = (params.cmd as String).split(':')
        ParamsHelper ph = getNewParamsHelper( cmd[1], params )

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        WfChecklist.withTransaction { TransactionStatus ts ->

            try {
                result.source = WfChecklist.get( cmd[2] )

                result.checklist               = new WfChecklist()
                result.checklist.title         = ph.getString('title') ?: result.source.title
                result.checklist.description   = ph.getString('description') ?: result.source.description
                result.checklist.owner         = result.source.owner

                def target = genericOIDService.resolveOID(params.target)

                if (target instanceof Org)               { result.checklist.org = target }
                else if (target instanceof License)      { result.checklist.license = target }
                else if (target instanceof Subscription) { result.checklist.subscription = target }

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

    Map<String, Object> deleteChecklist(String[] cmd) {
        log.debug('deleteChecklist() ' + cmd)

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

    List<WfChecklist> sortByLastUpdated(List<WfChecklist> wfList) {
//        println wfList.collect{ it.id + '_' + it.getInfo().lastUpdated }.take(5)
        wfList.sort{ a,b -> b.getInfo().lastUpdated <=> a.getInfo().lastUpdated }
    }

    List<WfChecklist> getWorkflows(def obj, Org owner) {
        if (obj instanceof Subscription) {
            WfChecklist.executeQuery('select wf from WfChecklist wf where wf.subscription = :sub and wf.owner = :ctxOrg', [sub: obj, ctxOrg: owner])
        }
        else if (obj instanceof License) {
            WfChecklist.executeQuery('select wf from WfChecklist wf where wf.license = :lic and wf.owner = :ctxOrg', [lic: obj, ctxOrg: owner])
        }
        else if (obj instanceof Org) {
            WfChecklist.executeQuery('select wf from WfChecklist wf where wf.org = :org and wf.owner = :ctxOrg', [org: obj, ctxOrg: owner])
        }
        else {
            return []
        }
    }

    int getWorkflowCount(def obj, Org owner) {
        if (obj instanceof Subscription) {
            WfChecklist.executeQuery('select count(wf) from WfChecklist wf where wf.subscription = :sub and wf.owner = :ctxOrg', [sub: obj, ctxOrg: owner])[0]
        }
        else if (obj instanceof License) {
            WfChecklist.executeQuery('select count(wf) from WfChecklist wf where wf.license = :lic and wf.owner = :ctxOrg', [lic: obj, ctxOrg: owner])[0]
        }
        else if (obj instanceof Org) {
            WfChecklist.executeQuery('select count(wf) from WfChecklist wf where wf.org = :org and wf.owner = :ctxOrg', [org: obj, ctxOrg: owner])[0]
        }
        else {
            return 0
        }
    }

    boolean hasUserPerm_read() {
        _innerPermissionCheck('INST_USER')
    }
    boolean hasUserPerm_edit() {
        _innerPermissionCheck('INST_EDITOR')
    }

    private boolean _innerPermissionCheck(String userRoleName) {
        User user = contextService.getUser()
        if (user.isAdmin() || user.isYoda()) {
            return true
        }
        Org ctxOrg = contextService.getOrg()
        if (userRoleName && ctxOrg.isCustomerType_Pro() && user.hasAffiliationForForeignOrg(userRoleName, ctxOrg)) {
            return true
        }
        false
    }
}