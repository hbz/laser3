package de.laser

import de.laser.addressbook.Address
import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.annotations.UnstableFeature
import de.laser.auth.Role
import de.laser.storage.RDStore
import de.laser.workflow.WfChecklist
import grails.gorm.transactions.Transactional

@UnstableFeature
@Transactional
class AccessService {

    static final String CHECK_VIEW = 'CHECK_VIEW'                       // TODO
    static final String CHECK_EDIT = 'CHECK_EDIT'                       // TODO
    static final String CHECK_VIEW_AND_EDIT = 'CHECK_VIEW_AND_EDIT'     // TODO

    public static final String READ    = 'READ'
    public static final String WRITE   = 'WRITE'

    ContextService contextService
    UserService userService

    // NO ROLE_ADMIN/ROLE_YODA CHECKS HERE ..
    // NO ROLE_ADMIN/ROLE_YODA CHECKS HERE ..
    // NO ROLE_ADMIN/ROLE_YODA CHECKS HERE ..

    // NO INST_USER CHECKS FOR READ .. TODO
    // NO INST_USER CHECKS FOR READ .. TODO
    // NO INST_USER CHECKS FOR READ .. TODO

    boolean hasAccessToDocument(DocContext dctx, String perm) {
        // moved from ajaxHtmlController.documentPreview()$checkPermission
        // logic based on /views/templates/documents/card
        boolean check = false
        Org ctxOrg = contextService.getOrg()

        Doc doc = dctx ? dctx.owner : null
        if (!doc) {
            // .. invalid
        }
        else if (doc.contentType != Doc.CONTENT_TYPE_FILE) {
            // .. invalid
        }
        else if (doc.owner.id == ctxOrg.id) {
            if (perm == WRITE) {
                check = contextService.isInstEditor()
            }
            else {
                check = true
            }
        }
        else if (dctx.shareConf) {
            if (!dctx.targetOrg) {
                println '------------------- ERMS-6460: hasAccessToDocument() NO docContext.targetOrg' // TODO - REMOVE!!
            }

            if (dctx.shareConf == RDStore.SHARE_CONF_UPLOADER_ORG) {
                // .. if (doc.owner.id == ctxOrg.id)
            }
            else if (dctx.shareConf == RDStore.SHARE_CONF_UPLOADER_AND_TARGET) {
                // .. if (doc.owner.id == ctxOrg.id)
                if (dctx.targetOrg.id == ctxOrg.id) {
                    if (perm == READ) {
                        check = true
                    }
                }
            }
            else if (dctx.shareConf == RDStore.SHARE_CONF_ALL) {
                // .. context based restrictions must be applied // todo --> problem?
                if (perm == READ) {
                    check = true
                }
            }
        }
        else if (dctx.sharedFrom) {
            if (dctx.license) {
                dctx.license.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]) {
                        if (perm == READ) {
                            check = true
                        }
                    }
                }
            }
            else if (dctx.subscription) {
                dctx.subscription.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER]) {
                        if (perm == READ) {
                            check = true
                        }
                    }
                }
            }
        }
        // survey workaround -- todo ??
        else if ( dctx.surveyConfig ) {
            Map orgIdMap = dctx.surveyConfig.getSurveyOrgsIDs()
            if (ctxOrg.id in orgIdMap.orgsWithSubIDs || ctxOrg.id in orgIdMap.orgsWithoutSubIDs) {
                check = true // ??? READ | WRITE
            }
        }
        return check
    }

    boolean hasAccessToDocNote(DocContext dctx, String perm) {
        boolean check = false
        Org ctxOrg = contextService.getOrg()

        Doc doc = dctx ? dctx.owner : null
        if (!doc) {
            // .. invalid
        }
        else if (doc.contentType != Doc.CONTENT_TYPE_STRING) {
            // .. invalid
        }
        else if (doc.owner.id == ctxOrg.id) {
            if (perm == WRITE) {
                check = contextService.isInstEditor()
            }
            else {
                check = true
            }
        }
        else if (dctx.sharedFrom) {
            if (dctx.license) {
                dctx.license.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]) {
                        if (perm == READ) {
                            check = true
                        }
                    }
                }
            }
            else if (dctx.subscription) {
                dctx.subscription.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER]) {
                        if (perm == READ) {
                            check = true
                        }
                    }
                }
            }
        }
        //else if (dctx.link && dctx.link.owner.id == ctxOrg.id) {
            // .. ??
        //}
        else if ( dctx.surveyConfig ) {
            // .. TODO TODO TODO
        }
        return check
    }

    boolean hasAccessToTask(Task task, String perm, boolean checkCustomerType = false) {
        boolean check = false
        Org ctxOrg = contextService.getOrg()

        if (!task) {
            // .. invalid
        }
        else if (checkCustomerType && !(ctxOrg.isCustomerType_Pro() || ctxOrg.isCustomerType_Support())) {
            // .. failed
        }
        else if (task.creator && task.creator.id == contextService.getUser().id) {
            check = true
        }
        else if (task.responsibleUser && task.responsibleUser.id == contextService.getUser().id) {
//            if (perm == WRITE) {
//                check = contextService.isInstEditor()
//            }
//            else {
                check = true // ?????
//            }
        }
        else if (task.responsibleOrg && task.responsibleOrg.id == ctxOrg.id) {
            if (perm == WRITE) {
                check = contextService.isInstEditor()
            }
            else {
                check = true
            }
        }

        return check
    }

    boolean hasAccessToWorkflow(WfChecklist workflow, String perm, boolean checkCustomerType = false) {
        boolean check = false
        Org ctxOrg = contextService.getOrg()

        if (!workflow) {
            // .. invalid
        }
        else if (checkCustomerType && !(ctxOrg.isCustomerType_Pro() || ctxOrg.isCustomerType_Support())) {
            // .. failed
        }
        else if (workflow.owner.id == ctxOrg.id) {
            if (perm == WRITE) {
                check = contextService.isInstEditor()
            }
            else {
                check = true
            }
        }
        return check
    }

    boolean hasAccessToAddress(Address address, String perm) {
        boolean check = false
        Org ctxOrg = contextService.getOrg()

        if (!address) {
            // .. invalid
        }
        else if (address.tenant && address.tenant.id == ctxOrg.id) {
            if (perm == WRITE) {
                check = contextService.isInstEditor()
            }
            else {
                check = true
            }
        }
        else if (address.org && address.org.id == ctxOrg.id) {
            if (perm == READ) {
                check = true
            }
            else if (perm == WRITE && !address.tenant) { // public
                check = contextService.isInstEditor()
            }
        }
        else if (address.provider || address.vendor) {
            if (perm == READ) {
                check = true
            }
        }
        return check
    }

    boolean hasAccessToContact(Contact contact, String perm) {
        boolean check = false
        Org ctxOrg = contextService.getOrg()

        if (!contact) {
            // .. invalid
        }
        else if (contact.prs) {
            if (contact.prs.tenant && contact.prs.tenant.id == ctxOrg.id) {
                if (perm == WRITE) {
                    check = contextService.isInstEditor()
                }
                else {
                    check = true
                }
            }
            else if (contact.prs.isPublic) {
                if (perm == READ) {
                    check = true
                }
            }
        }
        return check
    }

    boolean hasAccessToPerson(Person person, String perm) {
        boolean check = false
        Org ctxOrg = contextService.getOrg()

        if (!person) {
            // .. invalid
        }
        else if (person.tenant && person.tenant.id == ctxOrg.id) {
            if (perm == WRITE) {
                check = contextService.isInstEditor()
            }
            else {
                check = true
            }
        }
        else if (person.isPublic) {
            if (perm == READ) {
                check = true
            }
        }
        return check
    }
}
