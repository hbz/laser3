package de.laser

import de.laser.annotations.DebugAnnotation
import de.laser.auth.User
import de.laser.ctrl.SubscriptionControllerService

import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class SubscriptionController {

    def contextService
    def genericOIDService
    def exportService
    def accessService
    def docstoreService
    SubscriptionControllerService subscriptionControllerService
    def subscriptionService
    def escapeService
    def deletionService
    def surveyService
    AccessPointService accessPointService
    CopyElementsService copyElementsService

    //-------------------------------------- general or ungroupable section -------------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def show() {
        Map<String,Object> ctrlResult = subscriptionControllerService.show(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else ctrlResult.result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", ctrlService = 2)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER") })
    def tasks() {
        Map<String,Object> ctrlResult = subscriptionControllerService.tasks(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result)
                response.sendError(401)
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            if(params.returnToShow)
                redirect action: 'show', id: params.id
            else
                ctrlResult.result
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def history() {
        Map<String,Object> ctrlResult = subscriptionControllerService.history(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else ctrlResult.result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def changes() {
        Map<String,Object> ctrlResult = subscriptionControllerService.changes(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else ctrlResult.result
    }

    /*@DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })*/
    @Secured(['ROLE_ADMIN'])
    def compare() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def unlinkLicense() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            response.sendError(401)
        }
        else {
            subscriptionService.setOrgLicRole(result.subscription,License.get(params.license),true)
            redirect(url: request.getHeader('referer'))
        }
    }

    //--------------------------------------------- new subscription creation -----------------------------------------------------------

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = {ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")})
    def emptySubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.emptySubscription(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = message(code: ctrlResult.messageToken)
            redirect action: 'currentSubscriptions'
        }
        else
            ctrlResult.result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR") })
    def processEmptySubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processEmptySubscription(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.errorMessage
            redirect controller: 'myInstitution', action: 'currentSubscriptions' //temp
        } else {
            redirect action: 'show', id: ctrlResult.result.newSub.id
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if(result.subscription.instanceOf)
            result.parentId = result.subscription.instanceOf.id
        else if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE, CalculatedType.TYPE_COLLECTIVE, CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE])
            result.parentId = result.subscription.id

        if (params.process  && result.editable) {
            result.licenses.each { License l ->
                subscriptionService.setOrgLicRole(result.subscription,l,true)
            }
            result.delResult = deletionService.deleteSubscription(result.subscription, false)
        }
        else {
            result.delResult = deletionService.deleteSubscription(result.subscription, DeletionService.DRY_RUN)
        }

        result
    }

    //--------------------------------------------- document section ----------------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def notes() {
        Map<String,Object> ctrlResult = subscriptionControllerService.notes(this, params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else ctrlResult.result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def documents() {
        Map<String,Object> ctrlResult = subscriptionControllerService.documents(this, params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else ctrlResult.result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        Map<String,Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        result.ownobj = Subscription.get(params.instanceId)
        result.owntp = 'subscription'
        if(params.id) {
            result.docctx = DocContext.get(params.id)
            result.doc = result.docctx.owner
        }

        render template: "/templates/documents/modal", model: result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def deleteDocuments() {
        docstoreService.unifiedDeleteDocuments(params)
        redirect controller: 'subscription', action: params.redirectAction, id: params.instanceId
    }

    //--------------------------------- consortia members section ----------------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def members() {
        Map<String,Object> ctrlResult = subscriptionControllerService.members(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else {
            SimpleDateFormat sdf = DateUtils.SDF_ymd
            String datetoday = sdf.format(new Date(System.currentTimeMillis()))
            String filename = escapeService.escapeString(ctrlResult.result.subscription.name) + "_" + message(code:'subscriptionDetails.members.members') + "_" + datetoday
            if(params.exportXLS || params.exportShibboleths || params.exportEZProxys || params.exportProxys || params.exportIPs) {
                SXSSFWorkbook wb
                if(params.exportXLS) {
                    wb = (SXSSFWorkbook) exportService.exportOrg(ctrlResult.result.orgs, filename, true, 'xlsx')
                }
                else if (params.exportIPs) {
                    filename = "${datetoday}_" + escapeService.escapeString(message(code: 'subscriptionDetails.members.exportIPs.fileName'))
                    wb = (SXSSFWorkbook) accessPointService.exportIPsOfOrgs(ctrlResult.result.filteredSubChilds.orgs.flatten())
                }else if (params.exportProxys) {
                    filename = "${datetoday}_" + escapeService.escapeString(message(code: 'subscriptionDetails.members.exportProxys.fileName'))
                    wb = (SXSSFWorkbook) accessPointService.exportProxysOfOrgs(ctrlResult.result.filteredSubChilds.orgs.flatten())
                }else if (params.exportEZProxys) {
                    filename = "${datetoday}_" + escapeService.escapeString(message(code: 'subscriptionDetails.members.exportEZProxys.fileName'))
                    wb = (SXSSFWorkbook) accessPointService.exportEZProxysOfOrgs(ctrlResult.result.filteredSubChilds.orgs.flatten())
                }else if (params.exportShibboleths) {
                    filename = "${datetoday}_" + escapeService.escapeString(message(code: 'subscriptionDetails.members.exportShibboleths.fileName'))
                    wb = (SXSSFWorkbook) accessPointService.exportShibbolethsOfOrgs(ctrlResult.result.filteredSubChilds.orgs.flatten())
                }
                if(wb) {
                    response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                }
            }
            else {
                withFormat {
                    html {
                        ctrlResult.result
                    }
                    csv {
                        response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                        response.contentType = "text/csv"
                        ServletOutputStream out = response.outputStream
                        out.withWriter { writer ->
                            writer.write((String) exportService.exportOrg(ctrlResult.result.orgs, filename, true, "csv"))
                        }
                        out.close()
                    }
                }
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def addMembers() {
        log.debug("addMembers ..")
        Map<String,Object> ctrlResult = subscriptionControllerService.addMembers(this,params)

        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else {
            ctrlResult.result
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAddMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processAddMembers(this,params)
        if (ctrlResult.error == SubscriptionControllerService.STATUS_ERROR) {
            if(ctrlResult.result)
                redirect controller: 'subscription', action: 'show', params: [id: ctrlResult.result.subscription.id]
            else
                response.sendError(401)
        }
        else {
            redirect controller: 'subscription', action: 'members', params: [id: ctrlResult.result.subscription.id]
        }
    }

    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def linkNextPrevMemberSub() {
        Map<String,Object> ctrlResult = subscriptionControllerService.linkNextPrevMemberSub(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
            else redirect(url: request.getHeader('referer'))
        }
        else {
            redirect(action: 'show', id: ctrlResult.redirect)
        }
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def linkLicenseMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.linkLicenseMembers(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            ctrlResult.result
        }
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processLinkLicenseMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processLinkLicenseMembers(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            flash.message = message(code: 'subscription.linkLicenseMembers.changeAcceptedAll', args: [ctrlResult.result.message])
            redirect(action: 'linkLicenseMembers', id: params.id)
        }
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processUnLinkLicenseMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processUnLinkLicenseMembers(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            flash.message = message(code: 'subscription.linkLicenseMembers.removeAcceptedAll', args: [ctrlResult.result.message])
        }
        redirect(action: 'linkLicenseMembers', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def linkPackagesMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.linkPackagesMembers(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else
            ctrlResult.result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processLinkPackagesMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processLinkPackagesMembers(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
            }
            else {
                flash.error = ctrlResult.result.error
                redirect(action: 'linkPackagesMembers', id: params.id)
            }
        }
        else {
            redirect(action: 'linkPackagesMembers', id: params.id)
        }
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processUnLinkPackagesMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processUnLinkPackagesMembers(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            flash.message = ctrlResult.result.message.join("<br/>")
            flash.error = ctrlResult.result.error.join("<br/>")
        }
        redirect(action: 'linkPackagesMembers', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def propertiesMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.propertiesMembers(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            ctrlResult.result
        }
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processPropertiesMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processPropertiesMembers(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
            else {
                flash.error = ctrlResult.result.error
            }
        }
        else {
            flash.message = ctrlResult.result.message
        }
        redirect(action: 'propertiesMembers', id: params.id, params: [filterPropDef: params.filterPropDef])
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processDeletePropertiesMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processDeletePropertiesMembers(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            flash.message = ctrlResult.result.message
        }
        redirect(action: 'propertiesMembers', id: params.id, params: [filterPropDef: params.filterPropDef])
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def subscriptionPropertiesMembers() {
        params.tab = params.tab ?: 'generalProperties'
        Map<String,Object> ctrlResult = subscriptionControllerService.subscriptionPropertiesMembers(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            ctrlResult.result
        }
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processSubscriptionPropertiesMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processSubscriptionPropertiesMembers(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            if(ctrlResult.result.change){
                flash.message = message(code: 'subscription.subscriptionPropertiesMembers.changes', args: [ctrlResult.result.change.join(', ').toString()])
            }
            if(ctrlResult.result.noChange){
                flash.error = message(code: 'subscription.subscriptionPropertiesMembers.noChanges', args: [ctrlResult.result.noChange.join(', ').toString()])
            }
        }
        redirect(action: 'subscriptionPropertiesMembers', id: params.id)
    }

    //-------------------------------- survey section --------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def surveys() {
        Map<String,Object> ctrlResult = subscriptionControllerService.surveys(this)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else ctrlResult.result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_USER", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER")
    })
    def surveysConsortia() {
        Map<String,Object> ctrlResult = subscriptionControllerService.surveysConsortia(this, params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else ctrlResult.result
    }

    //------------------------------------- packages section -------------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def linkPackage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.linkPackage(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            if(params.addUUID) {
                switch(params.addType) {
                    case "With": flash.message = message(code:'subscription.details.link.processingWithEntitlements')
                        redirect action: 'index', params: [id: params.id, gokbId: params.addUUID]
                        break
                    case "Without": flash.message = message(code:'subscription.details.link.processingWithoutEntitlements')
                        redirect action: 'addEntitlements', params: [id: params.id, packageLinkPreselect: params.addUUID, preselectedName: ctrlResult.result.packageName]
                        break
                }
            }
            else {
                flash.message = ctrlResult.result.message
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def unlinkPackage() {
        Map<String, Object> ctrlResult = subscriptionControllerService.unlinkPackage(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
            }
            else {
                flash.error = ctrlResult.result.error
                redirect(action:'show', id: params.subscription)
            }
        }
        else {
            if (params.confirmed) {
                flash.message = ctrlResult.result.message
                redirect(action:'show', id: params.subscription)
            }
            else {
                render(template: "unlinkPackageModal", model: [pkg: ctrlResult.result.package, subscription: ctrlResult.result.subscription, conflicts_list: ctrlResult.result.conflicts_list])
            }
        }
    }

    //-------------------------------- issue entitlements holding --------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def index() {
        Map<String,Object> ctrlResult = subscriptionControllerService.index(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result)
                response.sendError(401)
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            String filename = "${escapeService.escapeString(ctrlResult.result.subscription.dropdownNamingConvention())}_${DateUtils.SDF_NoTimeNoPoint.format(new Date())}"
            if (params.exportKBart) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
                response.contentType = "text/tsv"
                ServletOutputStream out = response.outputStream
                Map<String, List> tableData = exportService.generateTitleExportKBART(ctrlResult.result.entitlements)
                out.withWriter { writer ->
                    writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
                }
                out.flush()
                out.close()
            }
            else if(params.exportXLSX) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.xlsx")
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                Map<String,List> export = exportService.generateTitleExportXLS(ctrlResult.result.entitlements)
                Map sheetData = [:]
                sheetData[message(code:'menu.my.titles')] = [titleRow:export.titles,columnData:export.rows]
                SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
                workbook.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                workbook.dispose()
            }
            else {
                withFormat {
                    html {
                        flash.message = ctrlResult.result.message
                        ctrlResult.result
                    }
                    csv {
                        response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                        response.contentType = "text/csv"
                        ServletOutputStream out = response.outputStream
                        Map<String,List> tableData = exportService.generateTitleExportCSV(ctrlResult.result.entitlements)
                        out.withWriter { writer ->
                            writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.rows,';'))
                        }
                        out.close()
                    }
                }
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def addEntitlements() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addEntitlements(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            String filename = "${escapeService.escapeString(ctrlResult.result.subscription.dropdownNamingConvention())}_${DateUtils.SDF_NoTimeNoPoint.format(new Date())}"
            if(params.exportKBart) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
                response.contentType = "text/tsv"
                ServletOutputStream out = response.outputStream
                Map<String,List> tableData = exportService.generateTitleExportKBART(ctrlResult.result.tipps)
                out.withWriter { writer ->
                    writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.columnData,'\t'))
                }
                out.flush()
                out.close()
            }
            else if(params.exportXLSX) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.xlsx")
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                Map<String,List> export = exportService.generateTitleExportXLS(ctrlResult.result.tipps)
                Map sheetData = [:]
                sheetData[message(code:'menu.my.titles')] = [titleRow:export.titles,columnData:export.rows]
                SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
                workbook.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                workbook.dispose()
            }
            withFormat {
                html {
                    flash.message = ctrlResult.result.message
                    flash.error = ctrlResult.result.error
                    ctrlResult.result
                }
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    Map<String,List> tableData = exportService.generateTitleExportCSV(ctrlResult.result.tipps)
                    out.withWriter { writer ->
                        writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.rows,';'))
                    }
                    out.flush()
                    out.close()
                }
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removeEntitlement() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeEntitlement(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR)
            flash.error = message(code:'default.delete.error.general.message')
        else {
            Object[] args = [message(code:'issueEntitlement.label'),params.ieid]
            flash.message = message(code: 'default.deleted.message',args: args)
        }
        redirect action: 'index', id: params.sub
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removeEntitlementWithIEGroups() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeEntitlementWithIEGroups(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR)
            flash.error = message(code:'default.delete.error.general.message')
        else {
            Object[] args = [message(code:'issueEntitlement.label'),params.ieid]
            flash.message = message(code: 'default.deleted.message',args: args)
        }
        redirect action: 'index', id: params.sub
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAddEntitlements() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processAddEntitlements(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            flash.error = ctrlResult.result.error
            flash.message = ctrlResult.result.message
        }
        redirect action: 'addEntitlements', id: ctrlResult.result.subscription.id
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processRemoveEntitlements() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if (!result) {
            response.sendError(401)
        }
        if(subscriptionService.deleteEntitlement(result.subscription,params.singleTitle))
            log.debug("Deleted tipp ${params.singleTitle} from sub ${result.subscription.id}")
        redirect action: 'renewEntitlements', model: [targetObjectId: result.subscription.id, packageId: params.packageId]
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAddIssueEntitlementsSurvey() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableIssueEntitlementsSurvey(result.institution, result.surveyConfig)
        if (result.subscription) {
            if(params.singleTitle) {
                IssueEntitlement ie = IssueEntitlement.get(params.singleTitle)
                TitleInstancePackagePlatform tipp = ie.tipp
                try {
                    if(subscriptionService.addEntitlement(result.subscription, tipp.gokbId, ie, (ie.priceItem != null) , RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION)) {
                        flash.message = message(code: 'subscription.details.addEntitlements.titleAddToSub', args: [tipp.title.title])
                    }
                }
                catch(EntitlementCreationException e) {
                    flash.error = e.getMessage()
                }
            }
        } else {
            log.error("Unable to locate subscription instance")
        }
        redirect action: 'renewEntitlementsWithSurvey', params: [targetObjectId: result.subscription.id, surveyConfigID: result.surveyConfig.id]
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processRemoveIssueEntitlementsSurvey() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableIssueEntitlementsSurvey(result.institution, result.surveyConfig)
        if(subscriptionService.deleteEntitlementbyID(result.subscription,params.singleTitle))
            log.debug("Deleted ie ${params.singleTitle} from sub ${result.subscription.id}")
        redirect action: 'renewEntitlementsWithSurvey', params: [targetObjectId: result.subscription.id, surveyConfigID: result.surveyConfig?.id]
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def subscriptionBatchUpdate() {
        Map<String,Object> ctrlResult = subscriptionControllerService.subscriptionBatchUpdate(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result)
                response.sendError(401)
            else {
                flash.error = message(code:'default.save.error.general.message')
                redirect action: 'index', params: [id: ctrlResult.result.subscription.id, sort: params.sort, order: params.order, offset: params.offset, max: params.max]
            }
        }
        else {
            redirect action: 'index', params: [id: ctrlResult.result.subscription.id, sort: params.sort, order: params.order, offset: params.offset, max: params.max]
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def addEmptyPriceItem() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addEmptyPriceItem(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'index', id: params.id
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def addCoverage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addCoverage(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_OK)
            redirect action: 'index', id: ctrlResult.result.subId, params: params
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removeCoverage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeCoverage(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_OK)
            redirect action: 'index', id: ctrlResult.result.subId, params: params
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def manageEntitlementGroup() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        result.titleGroups = result.subscription.ieGroups
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def editEntitlementGroupItem() {
        Map<String,Object> ctrlResult = subscriptionControllerService.editEntitlementGroupItem(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            redirect action: 'index', id: params.id
        }
        else {
            if(params.cmd == 'edit')
                render template: 'editEntitlementGroupItem', model: ctrlResult.result
            else redirect action: 'index', id: params.id
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processCreateEntitlementGroup() {
        Map<String, Object> ctrlResult = subscriptionControllerService.processCreateEntitlementGroup(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'manageEntitlementGroup', id: params.id
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removeEntitlementGroup() {
        Map<String, Object> ctrlResult = subscriptionControllerService.removeEntitlementGroup(params)
        Object[] args = [message(code:'issueEntitlementGroup.label'),params.titleGroup]
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR)
            flash.error = message(code:'default.not.found.message',args)
        else flash.message = message(code:'default.deleted.message',args)
        redirect action: 'manageEntitlementGroup', id: params.sub
    }

    @Secured(['ROLE_ADMIN'])
    Map renewEntitlements() {
        params.id = params.targetObjectId
        params.sourceObjectId = genericOIDService.resolveOID(params.targetObjectId)?.instanceOf?.id
        //Map result = copyElementsService.loadDataFor_PackagesEntitlements()
        //result.comparisonMap = comparisonService.buildTIPPComparisonMap(result.sourceIEs+result.targetIEs)
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processRenewEntitlements() {
        Map<String, Object> ctrlResult = subscriptionControllerService.processRenewEntitlements(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
            else {
                flash.error = ctrlResult.result.error
            }
        }
        else {
            flash.message = ctrlResult.result.message
        }
        redirect action: 'index', id: params.id
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def showEntitlementsRenewWithSurvey() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        result.surveyConfig = SurveyConfig.get(params.id)
        result.surveyInfo = result.surveyConfig.surveyInfo
        result.subscription =  result.surveyConfig.subscription
        result.ies = subscriptionService.getIssueEntitlementsNotFixed(result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(result.contextOrg))
        result.filename = "renewEntitlements_${escapeService.escapeString(result.subscription.dropdownNamingConvention())}"
        if (params.exportKBart) {
            response.setHeader("Content-disposition", "attachment; filename=${result.filename}.tsv")
            response.contentType = "text/tsv"
            ServletOutputStream out = response.outputStream
            Map<String, List> tableData = exportService.generateTitleExportKBART(result.ies)
            out.withWriter { Writer writer ->
                writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
            }
            out.flush()
            out.close()
        }
        else if(params.exportXLS) {
            response.setHeader("Content-disposition", "attachment; filename=${result.filename}.xlsx")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            Map<String,List> export = exportService.generateTitleExportXLS(result.ies)
            Map sheetData = [:]
            sheetData[g.message(code:'subscription.details.renewEntitlements.label')] = [titleRow:export.titles,columnData:export.rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
        }
        else {
            withFormat {
                html {
                    result
                }
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def renewEntitlementsWithSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        Subscription newSub = params.targetObjectId ? Subscription.get(params.targetObjectId) : Subscription.get(params.id)
        Subscription baseSub = result.surveyConfig.subscription ?: newSub.instanceOf
        params.id = newSub.id
        params.sourceObjectId = baseSub.id
        params.tab = params.tab ?: 'allIEs'
        List<IssueEntitlement> sourceIEs
        if(params.tab == 'allIEs') {
            sourceIEs = subscriptionService.getIssueEntitlementsWithFilter(baseSub, params+[max:5000,offset:0])
        }
        if(params.tab == 'selectedIEs') {
            sourceIEs = subscriptionService.getIssueEntitlementsWithFilter(newSub, params+[ieAcceptStatusNotFixed: true])
        }
        List<IssueEntitlement> targetIEs = subscriptionService.getIssueEntitlementsWithFilter(newSub, [max: 5000, offset: 0])
        List<IssueEntitlement> allIEs = subscriptionService.getIssueEntitlementsFixed(baseSub)
        result.subjects = subscriptionService.getSubjects(allIEs.collect {it.tipp.title.id})
        result.seriesNames = subscriptionService.getSeriesNames(allIEs.collect {it.tipp.title.id})
        result.countSelectedIEs = subscriptionService.getIssueEntitlementsNotFixed(newSub).size()
        result.countAllIEs = allIEs.size()
        result.countAllSourceIEs = sourceIEs.size()
        result.num_ies_rows = sourceIEs.size()//subscriptionService.getIssueEntitlementsFixed(baseSub).size()
        result.sourceIEs = sourceIEs.drop(result.offset).take(result.max)
        result.targetIEs = targetIEs
        result.newSub = newSub
        result.subscription = baseSub
        result.subscriber = result.newSub.getSubscriber()
        result.editable = surveyService.isEditableIssueEntitlementsSurvey(result.institution, result.surveyConfig)
        String filename = escapeService.escapeString(message(code:'renewEntitlementsWithSurvey.selectableTitles')+'_'+result.newSub.dropdownNamingConvention())
        if (params.exportKBart) {
            response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
            response.contentType = "text/tsv"
            ServletOutputStream out = response.outputStream
            Map<String, List> tableData = exportService.generateTitleExportKBART(sourceIEs)
            out.withWriter { Writer writer ->
                writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
            }
            out.flush()
            out.close()
        }
        else if(params.exportXLS) {
            response.setHeader("Content-disposition", "attachment; filename=${filename}.xlsx")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            Map<String,List> export = exportService.generateTitleExportXLS(sourceIEs)
            Map sheetData = [:]
            sheetData[g.message(code:'renewEntitlementsWithSurvey.selectableTitles')] = [titleRow:export.titles,columnData:export.rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
        }
        else {
            result
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processRenewEntitlementsWithSurvey() {
        Map<String, Object> ctrlResult = subscriptionControllerService.processRenewEntitlementsWithSurvey(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
            else flash.error = ctrlResult.result.error
        }
        redirect action: 'renewEntitlementsWithSurvey', id: params.id, params: [targetObjectId: params.id, surveyConfigID: ctrlResult.result.surveyConfig?.id]
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def setupPendingChangeConfiguration() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(!result) {
            response.sendError(403)
        }
        log.debug("Received params: ${params}")
        SubscriptionPackage subscriptionPackage = SubscriptionPackage.get(params.subscriptionPackage)
        PendingChangeConfiguration.SETTING_KEYS.each { String settingKey ->
            Map<String,Object> configMap = [subscriptionPackage:subscriptionPackage,settingKey:settingKey,withNotification:false]
            boolean auditable = false
            //Set because we have up to three keys in params with the settingKey
            Set<String> keySettings = params.keySet().findAll { k -> k.contains(settingKey) }
            keySettings.each { key ->
                List<String> settingData = key.split('!§!')
                switch(settingData[1]) {
                    case 'setting': configMap.settingValue = RefdataValue.get(params[key])
                        break
                    case 'notification': configMap.withNotification = params[key] != null
                        break
                    case 'auditable': auditable = params[key] != null
                        break
                }
            }
            try {
                PendingChangeConfiguration.construct(configMap)
                boolean hasConfig = AuditConfig.getConfig(subscriptionPackage.subscription,settingKey) != null
                if(auditable && !hasConfig) {
                    AuditConfig.addConfig(subscriptionPackage.subscription,settingKey)
                }
                else if(!auditable && hasConfig) {
                    AuditConfig.removeConfig(subscriptionPackage.subscription,settingKey)
                }
            }
            catch (CreationException e) {
                flash.error = e.message
            }
        }
        redirect(action:'show', params:[id:params.id])
    }

    /* TODO Cost per use tab, still needed?
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def costPerUse() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        // Can we remove this block?
        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
            result.institutional_usage_identifier = OrgSetting.get(result.institution, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)
        }

        // Get a unique list of invoices
        // select inv, sum(cost) from costItem as ci where ci.sub = x
        log.debug("Get all invoices for sub ${result.subscription}");
        result.costItems = []
        CostItem.executeQuery(INVOICES_FOR_SUB_HQL, [sub: result.subscription]).each {

            log.debug(it);

            def cost_row = [invoice: it[0], total: it[2]]

            cost_row.total_cost_for_sub = it[2];

            if (it && (it[3]?.startDate) && (it[3]?.endDate)) {

                log.debug("Total costs for sub : ${cost_row.total_cost_for_sub} period will be ${it[3]?.startDate} to ${it[3]?.endDate}");

                def usage_str = Fact.executeQuery(TOTAL_USAGE_FOR_SUB_IN_PERIOD, [
                        start   : it[3].startDate,
                        end     : it[3].endDate,
                        sub     : result.subscription,
                        factType: 'STATS:JR1'])[0]

                if (usage_str && usage_str.trim().length() > 0) {
                    cost_row.total_usage_for_sub = Double.parseDouble(usage_str);
                    if (cost_row.total_usage_for_sub > 0) {
                        cost_row.overall_cost_per_use = cost_row.total_cost_for_sub / cost_row.total_usage_for_sub;
                    } else {
                        cost_row.overall_cost_per_use = 0;
                    }
                } else {
                    cost_row.total_usage_for_sub = Double.parseDouble('0');
                    cost_row.overall_cost_per_use = cost_row.total_usage_for_sub
                }

                // Work out what cost items appear under this subscription in the period given
                cost_row.usage = Fact.executeQuery(USAGE_FOR_SUB_IN_PERIOD, [start: it[3].startDate, end: it[3].endDate, sub: result.subscription, jr1a: 'STATS:JR1'])
                cost_row.billingCurrency = it[3].billingCurrency.value.take(3)
                result.costItems.add(cost_row);
            } else {
                log.error("Invoice ${it} had no start or end date");
            }
        }

        result
    }*/

    //--------------------------------------------- renewal section ---------------------------------------------

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def renewSubscription() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Subscription subscription = Subscription.get(params.baseSubscription ?: params.id)
        result.subscription = subscription
        SimpleDateFormat sdf = DateUtils.SDF_dmy
        Date newStartDate
        Date newEndDate
        use(TimeCategory) {
            newStartDate = subscription.endDate ? (subscription.endDate + 1.day) : null
            newEndDate = subscription.endDate ? (subscription.endDate + 1.year) : null
        }
        result.isRenewSub = true
        result.permissionInfo = [sub_startDate: newStartDate ? sdf.format(newStartDate) : null,
                                 sub_endDate  : newEndDate ? sdf.format(newEndDate) : null,
                                 sub_name     : subscription.name,
                                 sub_id       : subscription.id,
                                 sub_status   : RDStore.SUBSCRIPTION_INTENDED.id.toString()]
        result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processRenewSubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processRenewSubscription(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
            else {
                flash.error = ctrlResult.result.error
                if(ctrlResult.result.newSub)
                    ctrlResult.result.newSub
            }
        }
        else {
            redirect controller: 'subscription',
                    action: 'copyElementsIntoSubscription',
                    params: [sourceObjectId: genericOIDService.getOID(ctrlResult.result.subscription), targetObjectId: genericOIDService.getOID(ctrlResult.result.newSub), isRenewSub: true]
        }
    }

    //------------------------------------------------ copy section ---------------------------------------------

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copySubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.copySubscription(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
            }
            else flash.error = ctrlResult.result.error
        }
        else {
            switch (params.workFlowPart) {
                case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                    if(ctrlResult.result.targetObject) {
                        params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                    }
                    ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                    break
                case CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS:
                    ctrlResult.result << copyElementsService.copyObjectElements_PackagesEntitlements(params)
                    params.workFlowPart = CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
                    ctrlResult.result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    break
                case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                    params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                    ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                    break
                case CopyElementsService.WORKFLOW_END:
                    ctrlResult.result << copyElementsService.copyObjectElements_Properties(params)
                    if (ctrlResult.result.targetObject){
                        redirect controller: 'subscription', action: 'show', params: [id: ctrlResult.result.targetObject.id]
                    }
                    break
                default:
                    ctrlResult.result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                    break
            }
            ctrlResult.result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS
            ctrlResult.result
        }
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copyElementsIntoSubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.copyElementsIntoSubscription(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
            }
            else flash.error = ctrlResult.result.error
        }
        else {
            switch (params.workFlowPart) {
                case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                    if (params.isRenewSub){
                        params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                        ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS:
                    ctrlResult.result << copyElementsService.copyObjectElements_PackagesEntitlements(params)
                    if (params.isRenewSub){
                        params.workFlowPart = CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
                        ctrlResult.result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                    if (params.isRenewSub){
                        if (!params.fromSurvey && result.isSubscriberVisible){
                            params.workFlowPart = CopyElementsService.WORKFLOW_SUBSCRIBER
                            ctrlResult.result << copyElementsService.loadDataFor_Subscriber(params)
                        } else {
                            params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                            ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                        }
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_SUBSCRIBER:
                    ctrlResult.result << copyElementsService.copyObjectElements_Subscriber(params)
                    if (params.isRenewSub) {
                        params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                        ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_Subscriber(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_PROPERTIES:
                    ctrlResult.result << copyElementsService.copyObjectElements_Properties(params)
                    if(!(params.isRenewSub && ctrlResult.result.targetObject)) {
                        ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_END:
                    ctrlResult.result << copyElementsService.copyObjectElements_Properties(params)
                    break
                default:
                    ctrlResult.result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                    break
            }
            ctrlResult.result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS
            ctrlResult.result.workFlowPartNext = params.workFlowPartNext ?: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
            if (params.isRenewSub) {
                ctrlResult.result.isRenewSub = params.isRenewSub
            }
            if((params.workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES && params.isRenewSub && ctrlResult.result.targetObject) ||
               (params.workFlowPart == CopyElementsService.WORKFLOW_END && ctrlResult.result.targetObject)) {
                SurveyConfig surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(ctrlResult.result.sourceObject, true)
                if (surveyConfig && ctrlResult.result.fromSurvey) {
                    redirect controller: 'survey', action: 'renewalWithSurvey', params: [id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id]
                }
                else {
                    redirect controller: 'subscription', action: 'show', params: [id: ctrlResult.result.targetObject.id]
                }
            }
            else ctrlResult.result
        }
    }

    @DebugAnnotation(perm = "ORG_INST", affil = "INST_EDITOR", specRole = "ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copyMyElements() {
        Map<String, Object> result = subscriptionControllerService.setCopyResultGenerics(params)
        if (!result) {
            response.sendError(401)
        }
        else {
            result.allObjects_readRights = subscriptionService.getMySubscriptionsWithMyElements_readRights([status: RDStore.SUBSCRIPTION_CURRENT.id])
            result.allObjects_writeRights = subscriptionService.getMySubscriptionsWithMyElements_writeRights([status: RDStore.SUBSCRIPTION_CURRENT.id])
            switch (params.workFlowPart) {
                case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                    result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                    result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    break
                case CopyElementsService.WORKFLOW_PROPERTIES:
                    result << copyElementsService.copyObjectElements_Properties(params)
                    result << copyElementsService.loadDataFor_Properties(params)
                    break
                case CopyElementsService.WORKFLOW_END:
                    result << copyElementsService.copyObjectElements_Properties(params)
                    if (result.targetObject){
                        flash.error = ""
                        flash.message = ""
                        redirect controller: 'subscription', action: 'show', params: [id: result.targetObject.id]
                    }
                    break
                default:
                    result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    break
            }
            if (params.targetObjectId) {
                result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
            }
            result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
            result.workFlowPartNext = params.workFlowPartNext ?: CopyElementsService.WORKFLOW_PROPERTIES
            result
        }
    }

    //----------------------------------------- subscription import section -----------------------------------------

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSubscriptions() {
        def candidates = JSON.parse(params.candidates)
        List errors = subscriptionService.addSubscriptions(candidates,params)
        if(errors.size() > 0) {
            flash.errors = errors.join("<br/>")
            redirect(url: request.getHeader("referer"))
        }
        else redirect controller: 'myInstitution', action: 'currentSubscriptions'
    }

    //--------------------------------------------- admin section -------------------------------------------------

    @DebugAnnotation(ctrlService = 2)
    @Secured(['ROLE_ADMIN'])
    def pendingChanges() {
        Map<String,Object> ctrlResult = subscriptionControllerService.pendingChanges(this, params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result)
                response.sendError(401)
        }
        else
            ctrlResult.result
    }

    //--------------------------------------------- helper section -------------------------------------------------

}
