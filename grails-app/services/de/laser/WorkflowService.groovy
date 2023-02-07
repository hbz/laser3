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

    Map<String, Object> cmd(GrailsParameterMap params) {
        log.debug('cmd() ' + params)

        Map<String, Object> result = [:]

        if (params.cmd) {
            String[] cmd = (params.cmd as String).split(':')
            String wfObjKey = cmd[1]

            if (cmd[0] == 'create') { // actions
                if (wfObjKey == WfChecklist.KEY) {
                    result = internalEditChecklist(new WfChecklist(), params)
                }
                else if (wfObjKey == WfCheckpoint.KEY) {
                    result = internalEditCheckpoint(new WfCheckpoint(), params)
                }
            }
            else if (cmd[0] == 'instantiate') { // actions
                result.status = OP_STATUS_ERROR

                if (wfObjKey == WfChecklist.KEY) {
                    if (params.target && params.sourceId) {
                        GrailsParameterMap clone = params.clone() as GrailsParameterMap
                        clone.setProperty('cmd', params.cmd + ':' + params.sourceId)

                        result = instantiateCompleteChecklist(clone)
                    }
                }
            }
            else if (cmd[0] == 'edit') { // todo
                Long wfObjId = cmd[2] as Long

                if (wfObjKey == WfChecklist.KEY) {
                    result = internalEditChecklist(WfChecklist.get(wfObjId), params)
                }
                else if (wfObjKey == WfCheckpoint.KEY) {
                    result = internalEditCheckpoint(WfCheckpoint.get(wfObjId), params)
                }
            }
            else if (cmd[0] == 'delete') { // table
                if (wfObjKey == WfChecklist.KEY ) {
                    result = removeCompleteChecklist(params)
                }
                else if (wfObjKey == WfCheckpoint.KEY)  {
                    result = deleteCheckpoint(params)
                }
            }
            else if (cmd[0] in ['moveUp', 'moveDown']) { // details
                if (wfObjKey == WfCheckpoint.KEY)  {
                    result = moveCheckpoint(params)
                }
            }
            else if (cmd[0] == 'usage') { // table
                ParamsHelper ph = getNewParamsHelper( cmd[1], params )

                if (cmd[1] == WfChecklist.KEY) {
//                WfWorkflow workflow = WfWorkflow.get( cmd[2] )
//                boolean wChanged
//
//                String comment = ph.getString('comment')
//                if (comment != workflow.comment) {
//                    workflow.comment = comment
//                    wChanged = true
//                }
//                RefdataValue status = ph.getRefdataValue('status')
//                if (status != workflow.status) {
//                    workflow.status = status
//                    wChanged = true
//                }
//                User user = User.get(ph.getLong('user'))
//                if (user != workflow.user) {
//                    workflow.user = user
//                    workflow.userLastUpdated = new Date()
//                    wChanged = true
//                }
//                if (wChanged) {
//                    result.status = workflow.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
//                }
                }
                else if (cmd[1] == WfCheckpoint.KEY) {
                    WfCheckpoint cpoint = WfCheckpoint.get( cmd[2] )
                    boolean tChanged

                    boolean done = ph.getChecked('done')
                    if (done != cpoint.done) {
                        cpoint.done = done
                        tChanged = true
                        println tChanged
                    }
                    Date date = ph.getDate('date')
                    if (date != cpoint.date) {
                        cpoint.date = date
                        tChanged = true
                        println tChanged
                    }
                    String comment = ph.getString('comment')
                    if (comment != cpoint.comment) {
                        cpoint.comment = comment
                        tChanged = true
                        println tChanged
                    }
                    if (tChanged) {
                        result.status = cpoint.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
                    }
                }
            }
        }
        result
    }

    Map<String, Object> deleteCheckpoint(GrailsParameterMap params) {
        log.debug('deleteCheckpoint() ' + params)
        String[] cmd = (params.cmd as String).split(':')

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
            log.error e.getMessage()
        }
        result
    }

    Map<String, Object> moveCheckpoint(GrailsParameterMap params) {
        log.debug('moveCheckpoint() ' + params)
        String[] cmd = (params.cmd as String).split(':')

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
            log.error e.getMessage()
        }
        result
    }

    Map<String, Object> internalEditChecklist(WfChecklist clist, GrailsParameterMap params) {
        log.debug( 'internalEditChecklist() ' + clist.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = getNewParamsHelper( cmd[1], params )

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
                result.status = clist.save() ? OP_STATUS_DONE : OP_STATUS_ERROR

                // todo
                if (cmd[0] == 'create' && ph.getInt('numberOfPoints')) {
                    for (int i = 1; i <= ph.getInt('numberOfPoints'); i++) {

                        if (result.status == OP_STATUS_DONE) {
                            GrailsParameterMap tmp = params.clone() as GrailsParameterMap
                            tmp.clear()
                            tmp.WF_CHECKPOINT_title = 'Aufgabe ' + i
                            tmp.WF_CHECKPOINT_position = i
                            tmp.WF_CHECKPOINT_checklist = clist.id
                            tmp.cmd = 'create:WF_CHECKPOINT'

                            Map<String, Object> tmpResult = internalEditCheckpoint(new WfCheckpoint(), tmp)
                            result.status = tmpResult.status
                        }
                    }
                }
            }
            catch (Exception e) {
                result.status = OP_STATUS_ERROR

                log.debug( 'internalEditChecklist() -> ' + e.getMessage() )
                e.printStackTrace()
            }
            finally {
                if (result.status == OP_STATUS_ERROR) {
                    log.debug( 'TransactionStatus.setRollbackOnly(!)' )
                    ts.setRollbackOnly()
                }
            }
        }
        result
    }

    Map<String, Object> internalEditCheckpoint(WfCheckpoint cpoint, GrailsParameterMap params) {
        log.debug( 'internalEditCheckpoint() ' + cpoint.toString() )
        String[] cmd = (params.cmd as String).split(':')

        ParamsHelper ph = getNewParamsHelper( cmd[1], params )

        cpoint.title        = ph.getString('title')
        cpoint.description  = ph.getString('description')
        cpoint.comment      = ph.getString('comment')

        cpoint.date         = ph.getDate('date')
        cpoint.done         = Boolean.parseBoolean(ph.getString('done'))
        cpoint.checklist    = WfChecklist.get(ph.getLong('checklist'))
        cpoint.position     = ph.getInt('position')

        Map<String, Object> result = [ checkpoint: cpoint, cmd: cmd[0], key: cmd[1] ]

        result.status = cpoint.save() ? OP_STATUS_DONE : OP_STATUS_ERROR
        result
    }

    Map<String, Object> instantiateCompleteChecklist(GrailsParameterMap params) {
        log.debug('instantiateCompleteChecklist() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        WfChecklist.withTransaction { TransactionStatus ts ->

            try {
                result.source = WfChecklist.get( cmd[2] )

                result.checklist               = new WfChecklist()
                result.checklist.title         = result.source.title
                result.checklist.description   = result.source.description
                result.checklist.owner         = result.source.owner

                def target = genericOIDService.resolveOID(params.target)

                if (target instanceof Org)               { result.checklist.org = target }
                else if (target instanceof License)      { result.checklist.license = target }
                else if (target instanceof Subscription) { result.checklist.subscription = target }

                if (! result.checklist.validate()) {
                    log.debug( '[ ' + result.source.id + ' ].instantiate(' + target + ') : ' + result.checklist.getErrors().toString() )
                }

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
                    result.status = OP_STATUS_ERROR

                    log.debug( 'instantiateCompleteChecklist() -> ' + result.checklist.getErrors().toString() )
                    log.debug( 'TransactionStatus.setRollbackOnly()' )
                    ts.setRollbackOnly()
                }
                else {
                    result.status = OP_STATUS_DONE
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

    Map<String, Object> removeCompleteChecklist(GrailsParameterMap params) {
        log.debug('removeCompleteChecklist() ' + params)
        String[] cmd = (params.cmd as String).split(':')

        Map<String, Object> result = [ cmd: cmd[0], key: cmd[1] ]

        WfChecklist.withTransaction { TransactionStatus ts ->

            try {
                result.checklist = WfChecklist.get(cmd[2])

                WfCheckpoint.executeUpdate('delete from WfCheckpoint cp where cp.checklist = :cl', [cl: result.checklist])
                result.checklist.delete()

//                if (result.checklist) {
//                    result.status = OP_STATUS_ERROR
//
//                    log.debug( 'removeCompleteChecklist() -> ' + result.checklist.getErrors().toString() )
//                    log.debug( 'TransactionStatus.setRollbackOnly(A)' )
//                    ts.setRollbackOnly()
//                }
//                else {
                    result.status = OP_STATUS_DONE
                    ts.flush() // TODO
//                }
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
        if (userRoleName && ctxOrg.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM'] && user.hasAffiliationForForeignOrg(userRoleName, ctxOrg)) {
            return true
        }
        false
    }
}