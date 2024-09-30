package de.laser

import de.laser.annotations.UnstableFeature
import de.laser.storage.RDStore

@UnstableFeature
class TmpRefactoringService {

    ContextService contextService
    UserService userService

    boolean hasAccessToDoc(Doc doc, DocContext docCtx) {

        // moved from ajaxHtmlController.documentPreview()$checkPermission
        // logic based on /views/templates/documents/card

        boolean check = false
        Org ctxOrg = contextService.getOrg()

        if (docCtx.owner.id != doc.id) {
            println 'hasAccessToDoc() - docCtx.owner.id != doc.id'
        }
        else if ( doc.owner.id == ctxOrg.id ) {
            check = true
        }
        else if ( docCtx.shareConf ) {
            if ( docCtx.shareConf == RDStore.SHARE_CONF_UPLOADER_ORG ) {
                check = (doc.owner.id == ctxOrg.id)
            }
            if ( docCtx.shareConf == RDStore.SHARE_CONF_UPLOADER_AND_TARGET ) {
                check = (doc.owner.id == ctxOrg.id) || (docCtx.targetOrg.id == ctxOrg.id)
            }
            if ( docCtx.shareConf == RDStore.SHARE_CONF_ALL ) {
                // context based restrictions must be applied // todo: problem
                check = true
            }
        }
        else if ( docCtx.sharedFrom ) {
            if (docCtx.license) {
                docCtx.license.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]) {
                        check = true
                    }
                }
            }
            else if (docCtx.subscription) {
                docCtx.subscription.orgRelations.each {
                    if (it.org.id == ctxOrg.id && it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER]) {
                        check = true
                    }
                }
            }
        }
        // survey workaround
        else if ( docCtx.surveyConfig ) {
            Map orgIdMap = docCtx.surveyConfig.getSurveyOrgsIDs()
            if (ctxOrg.id in orgIdMap.orgsWithSubIDs || ctxOrg.id in orgIdMap.orgsWithoutSubIDs) { // TODO ???
                check = true
            }
        }
        return check
    }

    boolean hasAccessToDocNote() {
        return true
    }

    boolean hasAccessToTask() {
        return true
    }

}
