package de.laser

import de.laser.annotations.UnstableFeature
import de.laser.storage.RDStore
import de.laser.workflow.WfChecklist
import grails.gorm.transactions.Transactional

@UnstableFeature
@Transactional
class AccessService {

    static final String CHECK_VIEW = 'CHECK_VIEW'                       // TODO
    static final String CHECK_EDIT = 'CHECK_EDIT'                       // TODO
    static final String CHECK_VIEW_AND_EDIT = 'CHECK_VIEW_AND_EDIT'     // TODO

    ContextService contextService
    UserService userService

    boolean hasAccessToDocument(DocContext dctx) {
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
            check = true
        }
        else if (dctx.shareConf) {
            if (dctx.shareConf == RDStore.SHARE_CONF_UPLOADER_ORG) {
                // .. if (doc.owner.id == ctxOrg.id)
            }
            else if (dctx.shareConf == RDStore.SHARE_CONF_UPLOADER_AND_TARGET) {
                // .. if (doc.owner.id == ctxOrg.id)
                if (dctx.targetOrg.id == ctxOrg.id) {
                    check = true
                }
            }
            else if ( dctx.shareConf == RDStore.SHARE_CONF_ALL ) {
                // .. context based restrictions must be applied // todo --> problem?
                check = true
            }
        }
        else if (dctx.sharedFrom) {
            if (dctx.license) {
                dctx.license.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]) {
                        check = true
                    }
                }
            }
            else if (dctx.subscription) {
                dctx.subscription.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER]) {
                        check = true
                    }
                }
            }
        }
        // survey workaround -- todo ??
        else if ( dctx.surveyConfig ) {
            Map orgIdMap = dctx.surveyConfig.getSurveyOrgsIDs()
            if (ctxOrg.id in orgIdMap.orgsWithSubIDs || ctxOrg.id in orgIdMap.orgsWithoutSubIDs) {
                check = true
            }
        }

        return check
    }

    boolean hasAccessToDocNote(DocContext dctx) {
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
            check = true
        }
        else if (dctx.sharedFrom) {
            if (dctx.license) {
                dctx.license.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]) {
                        check = true
                    }
                }
            }
            else if (dctx.subscription) {
                dctx.subscription.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER]) {
                        check = true
                    }
                }
            }
        }
        else if ( dctx.surveyConfig ) {
            // .. TODO TODO TODO
        }

//        license:        License,
//        subscription:   Subscription,
//        link:           Links,            <<<--- TODO ?
//        org:            Org,
//        surveyConfig:   SurveyConfig,
//        provider:       Provider,
//        vendor:         Vendor

        return check
    }

    boolean hasAccessToTask(Task task) {
        boolean check = false

        if (!task) {
            // .. invalid
        }
        else if (task.creator && task.creator.id == contextService.getUser().id) {
            check = true
        }
        else if (task.responsibleOrg && task.responsibleOrg.id == contextService.getOrg().id) {
            check = true
        }
        else if (task.responsibleUser && task.responsibleUser.id != contextService.getUser().id) {
            check = true
        }

//        License         license
//        Org             org
//        Provider        provider
//        Vendor          vendor
//        Subscription    subscription
//        SurveyConfig    surveyConfig
//        TitleInstancePackagePlatform tipp

        return check
    }

    boolean hasAccessToWorkflow(WfChecklist workflow) {
        boolean check = false

        if (!workflow) {
            // .. invalid
        }
        else if (workflow.owner.id == contextService.getOrg().id) {
            check = true
        }
//        Subscription subscription
//        License license
//        Org org
//        Provider provider
//        Vendor vendor

        return check
    }

}
