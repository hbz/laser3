package de.laser

import de.laser.annotations.UnstableFeature
import de.laser.storage.RDStore

@UnstableFeature
class TmpRefactoringService {

    ContextService contextService
    UserService userService

    boolean hasAccessToDoc(DocContext dctx) {
        // moved from ajaxHtmlController.documentPreview()$checkPermission
        // logic based on /views/templates/documents/card
        boolean check = false

        Doc doc = dctx.owner
        Org ctxOrg = contextService.getOrg()

        if (!doc || doc.contentType != Doc.CONTENT_TYPE_FILE) {
            return false
        }

        if ( doc.owner.id == ctxOrg.id ) {
            check = true
        }
        else if ( dctx.shareConf ) {
            if ( dctx.shareConf == RDStore.SHARE_CONF_UPLOADER_ORG ) {
                check = (doc.owner.id == ctxOrg.id)
            }
            if ( dctx.shareConf == RDStore.SHARE_CONF_UPLOADER_AND_TARGET ) {
                check = (doc.owner.id == ctxOrg.id) || (dctx.targetOrg.id == ctxOrg.id)
            }
            if ( dctx.shareConf == RDStore.SHARE_CONF_ALL ) {
                // context based restrictions must be applied // todo: problem
                check = true
            }
        }
        else if ( dctx.sharedFrom ) {
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
        // survey workaround
        else if ( dctx.surveyConfig ) {
            Map orgIdMap = dctx.surveyConfig.getSurveyOrgsIDs()
            if (ctxOrg.id in orgIdMap.orgsWithSubIDs || ctxOrg.id in orgIdMap.orgsWithoutSubIDs) { // TODO ???
                check = true
            }
        }
        return check
    }

    boolean hasAccessToDocNote(DocContext dctx) {
        Doc doc = dctx.owner

        if (!doc || doc.contentType != Doc.CONTENT_TYPE_STRING) {
            return false
        }
//        license:        License,
//        subscription:   Subscription,
//        link:           Links,
//        org:            Org,
//        surveyConfig:   SurveyConfig,
//        provider:       Provider,
//        vendor:         Vendor

        return true
    }

    boolean hasAccessToTask(Task task) {
//        License         license
//        Org             org
//        Provider        provider
//        Vendor          vendor
//        Subscription    subscription
//        SurveyConfig    surveyConfig
//        TitleInstancePackagePlatform tipp

        return true
    }

}
