package de.laser

import de.laser.ctrl.SubscriptionControllerService
import de.laser.properties.SubscriptionProperty
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
 
import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.i18n.LocaleContextHolder

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class SubscriptionController {

    def springSecurityService
    def contextService
    def addressbookService
    def genericOIDService
    def exportService
    def renewals_reversemap = ['subject': 'subject', 'provider': 'provid', 'pkgname': 'tokname']
    def accessService
    def docstoreService
    SubscriptionControllerService subscriptionControllerService
    def linksGenerationService
    def subscriptionService
    def escapeService
    def deletionService
    def auditService
    def surveyService
    FormService formService
    AccessPointService accessPointService
    CopyElementsService copyElementsService

    //-------------------------------------- general or ungroupable section -------------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrl = 2)
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def show() {
        Map<String,Object> ctrlResult = subscriptionControllerService.show(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else ctrlResult.result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", ctrl = 1)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER") })
    def tasks() {
        Map<String,Object> ctrlResult = subscriptionControllerService.tasks(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else {
            if (params.deleteId) {
                Task dTask = Task.get(params.deleteId)
                if (dTask && dTask.creator.id == ctrlResult.result.user.id) {
                    try {
                        flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label'), dTask.title])
                        dTask.delete()
                        if(params.returnToShow)
                            redirect action: 'show', id: params.id
                    }
                    catch (Exception e) {
                        log.error(e)
                        flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'task.label'), params.deleteId])
                    }
                }
            }
            ctrlResult.result
        }
    }

    /*@DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })*/
    @Secured(['ROLE_ADMIN'])
    def compare() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(accessService.CHECK_VIEW)

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def unlinkLicense() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if(!result) {
            response.sendError(401)
        }
        else {
            subscriptionService.setOrgLicRole(result.subscription,License.get(params.license),true)
            redirect(url: request.getHeader('referer'))
        }
    }

    //--------------------------------------------- new subscription creation -----------------------------------------------------------

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR")
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

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR")
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

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def delete() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def notes() {
        Map<String,Object> ctrlResult = subscriptionControllerService.notes(this)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else ctrlResult.result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def documents() {
        Map<String,Object> ctrlResult = subscriptionControllerService.documents(this)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else ctrlResult.result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        Map<String,Object> result = [user:springSecurityService.getCurrentUser(),institution:contextService.org]
        result.ownobj = Subscription.get(params.instanceId)
        result.owntp = 'subscription'
        if(params.id) {
            result.docctx = DocContext.get(params.id)
            result.doc = result.docctx.owner
        }

        render template: "/templates/documents/modal", model: result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def deleteDocuments() {
        docstoreService.unifiedDeleteDocuments(params)
        redirect controller: 'subscription', action: params.redirectAction, id: params.instanceId
    }

    //--------------------------------- consortia members section ----------------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def members() {
        Map<String,Object> ctrlResult = subscriptionControllerService.members(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else {
            SimpleDateFormat sdf = DateUtil.SDF_ymd
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

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
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

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
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

    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
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

    //-------------------------------- survey section --------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def surveys() {
        Map<String,Object> ctrlResult = subscriptionControllerService.surveys(this)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else ctrlResult.result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER")
    })
    def surveysConsortia() {
        Map<String,Object> ctrlResult = subscriptionControllerService.surveysConsortia(this)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
        }
        else ctrlResult.result
    }

    //------------------------------------- packages section -------------------------------------------

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
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

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
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
            String filename = "${escapeService.escapeString(ctrlResult.result.subscription.dropdownNamingConvention())}_${DateUtil.SDF_NoTimeNoPoint.format(new Date())}"
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def addEntitlements() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addEntitlements(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
            }
        }
        else {
            String filename = "${escapeService.escapeString(ctrlResult.result.subscription.dropdownNamingConvention())}_${DateUtil.SDF_NoTimeNoPoint.format(new Date())}"
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

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
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

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def processAddIssueEntitlementsSurvey() {
        Map<String, Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableIssueEntitlementsSurvey(result.institution, result.surveyConfig)
        if (result.subscriptionInstance) {
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
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

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def addCoverage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addCoverage(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_OK)
            redirect action: 'index', id: ctrlResult.result.subId, params: params
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def removeCoverage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeCoverage(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_OK)
            redirect action: 'index', id: ctrlResult.result.subId, params: params
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def manageEntitlementGroup() {
        Map<String, Object> result = setResultGenericsAndCheckAccess(accessService.CHECK_VIEW_AND_EDIT)
        result.titleGroups = result.subscription.ieGroups
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
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

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def processCreateEntitlementGroup() {
        Map<String, Object> ctrlResult = subscriptionControllerService.processCreateEntitlementGroup(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'manageEntitlementGroup', id: params.id
    }

    @Secured(['ROLE_ADMIN'])
    Map renewEntitlements() {
        params.id = params.targetObjectId
        params.sourceObjectId = genericOIDService.resolveOID(params.targetObjectId)?.instanceOf?.id
        Map result = copyElementsService.loadDataFor_PackagesEntitlements()
        //result.comparisonMap = comparisonService.buildTIPPComparisonMap(result.sourceIEs+result.targetIEs)
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def showEntitlementsRenewWithSurvey() {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def renewEntitlementsWithSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.org
        result.user = contextService.user
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeAsInteger()
        result.offset = params.offset  ? Integer.parseInt(params.offset) : 0
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
        }else if(params.exportXLS) {
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

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def linkLicenseMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf && result.subscriptionInstance._getCalculatedType() != CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentLicenses = Links.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType',[subscription:result.parentSub,linkType:RDStore.LINKTYPE_LICENSE])

        result.validLicenses = []

        if(result.parentLicenses) {
            def childLicenses = License.where {
                instanceOf in result.parentLicenses
            }

            childLicenses?.each {
                result.validLicenses << it
            }
        }

        def validSubChilds = Subscription.findAllByInstanceOf(result.parentSub)
        //Sortieren
        result.validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }

        def oldID =  params.id
        params.id = result.parentSub.id

        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

        params.id = oldID

        result
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processLinkLicenseMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
            return
        }
        if(formService.validateToken(params)) {
            result.parentSub = result.subscriptionInstance.instanceOf && result.subscriptionInstance._getCalculatedType() != CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

            /*RefdataValue licenseeRoleType = OR_LICENSEE_CONS
            if(result.subscriptionInstance._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE)
                licenseeRoleType = OR_LICENSEE_COLL

            result.parentLicense = result.parentSub.owner*/

            Set<Subscription> validSubChilds = Subscription.findAllByInstanceOf(result.parentSub)

            List selectedMembers = params.list("selectedMembers")

            List<GString> changeAccepted = []
            validSubChilds.each { Subscription subChild ->
                if (selectedMembers.contains(subChild.id.toString())) { //toString needed for type check
                    License newLicense = License.get(params.license_All)
                    if(subscriptionService.setOrgLicRole(subChild,newLicense,params.processOption == 'unlinkLicense'))
                        changeAccepted << "${subChild.name} (${message(code:'subscription.linkInstance.label')} ${subChild.getSubscriber().sortname})"
                }
            }
            if (changeAccepted) {
                flash.message = message(code: 'subscription.linkLicenseMembers.changeAcceptedAll', args: [changeAccepted.join(', ')])
            }
        }

        redirect(action: 'linkLicenseMembers', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processUnLinkLicenseMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }
        if(formService.validateToken(params)) {
            result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

            //result.parentLicense = result.parentSub.owner TODO we need to iterate over ALL linked licenses!

            List selectedMembers = params.list("selectedMembers")

            Set<Subscription> validSubChilds = Subscription.findAllByInstanceOf(result.parentSub)

            List<GString> removeLic = []
            validSubChilds.each { Subscription subChild ->
                if(subChild.id in selectedMembers || params.unlinkAll == 'true') {
                    Links.findAllByDestinationSubscriptionAndLinkType(subChild,RDStore.LINKTYPE_LICENSE).each { Links li ->
                        if (subscriptionService.setOrgLicRole(subChild,li.sourceLicense,true)) {
                            removeLic << "${subChild.name} (${message(code:'subscription.linkInstance.label')} ${subChild.getSubscriber().sortname})"
                        }
                    }
                }

            }
            if (removeLic) {
                flash.message = message(code: 'subscription.linkLicenseMembers.removeAcceptedAll', args: [removeLic.join(', ')])
            }
        }

        redirect(action: 'linkLicenseMembers', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def linkPackagesMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentPackages = result.parentSub.packages.sort { it.pkg.name }

        result.validPackages = result.parentPackages

        def validSubChilds = Subscription.findAllByInstanceOf(result.parentSub)
        //Sortieren
        result.validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }

        def oldID = params.id
        if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_COLLECTIVE])
            params.id = result.parentSub.id

        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

        params.id = oldID

        result
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processLinkPackagesMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        if(formService.validateToken(params)) {
            result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

            //List changeAccepted = []
            //List changeAcceptedwithIE = []
            //List changeFailed = []

            List selectedMembers = params.list("selectedMembers")

            if(selectedMembers && params.package_All){
                Package pkg_to_link = SubscriptionPackage.get(params.package_All).pkg
                selectedMembers.each { id ->
                    Subscription subChild = Subscription.get(Long.parseLong(id))
                    if (params.processOption == 'linkwithIE' || params.processOption == 'linkwithoutIE') {
                        if (!(pkg_to_link in subChild.packages.pkg)) {

                            if (params.processOption == 'linkwithIE') {

                                pkg_to_link.addToSubscriptionCurrentStock(subChild, result.parentSub)
                                //changeAcceptedwithIE << "${subChild?.name} (${message(code: 'subscription.linkInstance.label')} ${subChild?.orgRelations.find { it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_COLLECTIVE] }.org.sortname})"

                            } else {
                                pkg_to_link.addToSubscription(subChild, false)
                                //changeAccepted << "${subChild?.name} (${message(code: 'subscription.linkInstance.label')} ${subChild?.orgRelations.find { it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_COLLECTIVE] }.org.sortname})"

                            }
                        } /*else {
                            //changeFailed << "${subChild?.name} (${message(code: 'subscription.linkInstance.label')} ${subChild?.orgRelations.find { it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_COLLECTIVE] }.org.sortname})"
                        }

                        if (changeAccepted) {
                            //flash.message = message(code: 'subscription.linkPackagesMembers.changeAcceptedAll', args: [pkg_to_link.name, changeAccepted.join(", ")])
                        }
                        if (changeAcceptedwithIE) {
                            ///flash.message = message(code: 'subscription.linkPackagesMembers.changeAcceptedIEAll', args: [pkg_to_link.name, changeAcceptedwithIE.join(", ")])
                        }

                        if (!changeAccepted && !changeAcceptedwithIE){
                            //flash.error = message(code: 'subscription.linkPackagesMembers.noChanges')
                        }*/

                    }

                    if (params.processOption == 'unlinkwithIE' || params.processOption == 'unlinkwithoutIE') {
                        if (pkg_to_link in subChild.packages.pkg) {

                            if (params.processOption == 'unlinkwithIE') {
                                pkg_to_link.unlinkFromSubscription(subChild, true)
                                /*if() {
                                    //changeAcceptedwithIE << "${subChild?.name} (${message(code: 'subscription.linkInstance.label')} ${subChild?.orgRelations.find { it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_COLLECTIVE] }.org.sortname})"
                                }*/
                            } else {
                                pkg_to_link.unlinkFromSubscription(subChild, false)
                                /*if() {
                                    //changeAccepted << "${subChild?.name} (${message(code: 'subscription.linkInstance.label')} ${subChild?.orgRelations.find { it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_COLLECTIVE] }.org.sortname})"
                                }*/
                            }
                        } /*else {
                            //changeFailed << "${subChild?.name} (${message(code: 'subscription.linkInstance.label')} ${subChild?.orgRelations.find { it.roleType in [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_COLLECTIVE] }.org.sortname})"
                        }

                        if (changeAccepted) {
                            //flash.message = message(code: 'subscription.linkPackagesMembers.changeAcceptedUnlinkAll', args: [pkg_to_link.name, changeAccepted.join(", ")])
                        }
                        if (changeAcceptedwithIE) {
                            //flash.message = message(code: 'subscription.linkPackagesMembers.changeAcceptedUnlinkWithIEAll', args: [pkg_to_link.name, changeAcceptedwithIE.join(", ")])
                        }

                        if (!changeAccepted && !changeAcceptedwithIE){
                            //flash.error = message(code: 'subscription.linkPackagesMembers.noChanges')
                        }*/

                    }
                }

            }else {
                if(!selectedMembers) {
                    flash.error = message(code: 'subscription.linkPackagesMembers.noSelectedMember')
                }

                if(!params.package_All) {
                    flash.error = message(code: 'subscription.linkPackagesMembers.noSelectedPackage')
                }
            }
        }

        redirect(action: 'linkPackagesMembers', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processUnLinkPackagesMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }
        flash.error = ""

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentPackages = result.parentSub.packages.sort { it.pkg.name }

        Set<Subscription> validSubChilds = Subscription.findAllByInstanceOf(result.parentSub)

        validSubChilds.each { Subscription subChild ->

            subChild.packages.pkg.each { pkg ->

                    if(!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg.subscription = :sub and ci.subPkg.pkg = :pkg',[pkg:pkg,sub:subChild])) {
                        String query = "from IssueEntitlement ie, Package pkg where ie.subscription =:sub and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
                        Map<String,Object> queryParams = [sub: subChild, pkg_id: pkg.id]


                        if (subChild.isEditableBy(result.user)) {
                            result.editable = true
                            if (params.withIE) {
                                if(pkg.unlinkFromSubscription(subChild, true)){
                                    flash.message = message(code: 'subscription.linkPackagesMembers.unlinkInfo.withIE.successful')
                                }else {
                                    flash.error = message(code: 'subscription.linkPackagesMembers.unlinkInfo.withIE.fail')
                                }
                            } else {
                                if(pkg.unlinkFromSubscription(subChild, false)){
                                    flash.message = message(code: 'subscription.linkPackagesMembers.unlinkInfo.onlyPackage.successful')
                                }else {
                                    flash.error = message(code: 'subscription.linkPackagesMembers.unlinkInfo.onlyPackage.fail')
                                }
                            }
                        }
                    } else {
                        flash.error += "Für das Paket ${pkg.name} von ${subChild.getSubscriber().name} waren noch Kosten anhängig. Das Paket wurde daher nicht entknüpft."
                        }
                }
            }
        redirect(action: 'linkPackagesMembers', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def propertiesMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.filterPropDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null

        params.remove('filterPropDef')


        result.parentSub = result.subscriptionInstance

        Set<Subscription> validSubChildren = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.sub.instanceOf = :parent and oo.roleType = :roleType order by oo.org.sortname asc",[parent:result.parentSub,roleType:RDStore.OR_SUBSCRIBER_CONS])
        if(validSubChildren) {
            result.validSubChilds = validSubChildren
            Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select distinct(sp.type) from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :ctx and sp.instanceOf = null",[subscriptionSet:validSubChildren,ctx:result.institution])
            propList.addAll(result.parentSub.propertySet.type)
            result.propList = propList

            def oldID = params.id
            params.id = result.parentSub.id

            result.filteredSubChilds = validSubChildren

            params.id = oldID

            List<Subscription> childSubs = result.parentSub.getNonDeletedDerivedSubscriptions()
            if(childSubs) {
                String localizedName
                switch(LocaleContextHolder.getLocale()) {
                    case Locale.GERMANY:
                    case Locale.GERMAN: localizedName = "name_de"
                        break
                    default: localizedName = "name_en"
                        break
                }
                String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet:childSubs, context:result.institution] )

                result.memberProperties = memberProperties
            }
        }

        result
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def subscriptionPropertiesMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        params.tab = params.tab ?: 'generalProperties'

        result.parentSub = result.subscriptionInstance.instanceOf && result.subscription._getCalculatedType() != CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        def validSubChilds = Subscription.findAllByInstanceOf(result.parentSub)
        //Sortieren
        result.validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }

        def oldID = params.id
        params.id = result.parentSub.id

        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

        if(params.tab == 'providerAgency') {

            result.modalPrsLinkRole = RefdataValue.getByValueAndCategory('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)
            result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(contextService.getOrg())
            result.visibleOrgRelations = []
            result.parentSub.orgRelations?.each { or ->
                if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_COLLECTIVE.id])) {
                    result.visibleOrgRelations << or
                }
            }
            result.visibleOrgRelations.sort { it?.org?.sortname }
        }

        params.id = oldID

        result
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processPropertiesMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        if(formService.validateToken(params)) {
            result.filterPropDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null

            result.parentSub = result.subscriptionInstance.instanceOf && result.subscription._getCalculatedType() != CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE ? result.subscriptionInstance.instanceOf : result.subscriptionInstance


            if (params.filterPropDef && params.filterPropValue) {

                def filterPropDef = params.filterPropDef
                def propDef = genericOIDService.resolveOID(filterPropDef.replace(" ", ""))


                def newProperties = 0
                def changeProperties = 0

                if (propDef) {

                    List selectedMembers = params.list("selectedMembers")

                    if(selectedMembers){
                        selectedMembers.each { subId ->
                            Subscription subChild = Subscription.get(subId)
                            if (propDef?.tenant != null) {
                                //private Property
                                Subscription owner = subChild

                                def existingProps = owner.propertySet.findAll {
                                    it.owner.id == owner.id &&  it.type.id == propDef.id && it.tenant.id == result.institution && !it.isPublic
                                }
                                existingProps.removeAll { it.type.name != propDef.name } // dubious fix

                                if (existingProps.size() == 0 || propDef.multipleOccurrence) {
                                    AbstractPropertyWithCalculatedLastUpdated newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, owner, propDef, result.institution)
                                    if (newProp.hasErrors()) {
                                        log.error(newProp.errors.toString())
                                    } else {
                                        log.debug("New private property created: " + newProp.type.name)

                                        newProperties++
                                        subscriptionService.updateProperty(newProp, params.filterPropValue)
                                    }
                                }

                                if (existingProps.size() == 1){
                                    def privateProp = SubscriptionProperty.get(existingProps[0].id)
                                    changeProperties++
                                    subscriptionService.updateProperty(privateProp, params.filterPropValue)

                                }

                            } else {
                                //custom Property
                                def owner = subChild

                                def existingProp = owner.propertySet.find {
                                    it.type.id == propDef.id && it.owner.id == owner.id
                                }

                                if (existingProp == null || propDef.multipleOccurrence) {
                                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, owner, propDef, result.institution)
                                    if (newProp.hasErrors()) {
                                        log.error(newProp.errors.toString())
                                    } else {
                                        log.debug("New custom property created: " + newProp.type.name)
                                        newProperties++
                                        subscriptionService.updateProperty(newProp, params.filterPropValue)
                                    }
                                }

                                if (existingProp){
                                    SubscriptionProperty customProp = SubscriptionProperty.get(existingProp.id)
                                    changeProperties++
                                    subscriptionService.updateProperty(customProp, params.filterPropValue)
                                }
                            }

                        }
                        flash.message = message(code: 'subscription.propertiesMembers.successful', args: [newProperties, changeProperties])
                    }else{
                        flash.error = message(code: 'subscription.propertiesMembers.successful', args: [newProperties, changeProperties])
                    }

                }

            }
        }

        def filterPropDef = params.filterPropDef
        def id = params.id
        redirect(action: 'propertiesMembers', id: id, params: [filterPropDef: filterPropDef])
    }

    @DebugAnnotation(perm = "ORG_INST_COLLECTIVE,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST_COLLECTIVE,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processSubscriptionPropertiesMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        List selectedMembers = params.list("selectedMembers")

        if(selectedMembers){
            def change = []
            def noChange = []
            selectedMembers.each { subID ->

                        Subscription subChild = Subscription.get(subID)

                        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
                        def startDate = params.valid_from ? sdf.parse(params.valid_from) : null
                        def endDate = params.valid_to ? sdf.parse(params.valid_to) : null


                        if(startDate && !auditService.getAuditConfig(subChild?.instanceOf, 'startDate'))
                        {
                            subChild?.startDate = startDate
                            change << message(code: 'default.startDate.label')
                        }

                        if(startDate && auditService.getAuditConfig(subChild?.instanceOf, 'startDate'))
                        {
                            noChange << message(code: 'default.startDate.label')
                        }

                        if(endDate && !auditService.getAuditConfig(subChild?.instanceOf, 'endDate'))
                        {
                            subChild?.endDate = endDate
                            change << message(code: 'default.endDate.label')
                        }

                        if(endDate && auditService.getAuditConfig(subChild?.instanceOf, 'endDate'))
                        {
                            noChange << message(code: 'default.endDate.label')
                        }


                        if(params.status && !auditService.getAuditConfig(subChild?.instanceOf, 'status'))
                        {
                            subChild?.status = RefdataValue.get(params.status) ?: subChild?.status
                            change << message(code: 'subscription.status.label')
                        }
                        if(params.status && auditService.getAuditConfig(subChild?.instanceOf, 'status'))
                        {
                            noChange << message(code: 'subscription.status.label')
                        }

                        if(params.kind && !auditService.getAuditConfig(subChild?.instanceOf, 'kind'))
                        {
                            subChild?.kind = RefdataValue.get(params.kind) ?: subChild?.kind
                            change << message(code: 'subscription.kind.label')
                        }
                        if(params.kind && auditService.getAuditConfig(subChild?.instanceOf, 'kind'))
                        {
                            noChange << message(code: 'subscription.kind.label')
                        }

                        if(params.form && !auditService.getAuditConfig(subChild?.instanceOf, 'form'))
                        {
                            subChild?.form = RefdataValue.get(params.form) ?: subChild?.form
                            change << message(code: 'subscription.form.label')
                        }
                        if(params.form && auditService.getAuditConfig(subChild?.instanceOf, 'form'))
                        {
                            noChange << message(code: 'subscription.form.label')
                        }

                        if(params.resource && !auditService.getAuditConfig(subChild?.instanceOf, 'resource'))
                        {
                            subChild?.resource = RefdataValue.get(params.resource) ?: subChild?.resource
                            change << message(code: 'subscription.resource.label')
                        }
                        if(params.resource && auditService.getAuditConfig(subChild?.instanceOf, 'resource'))
                        {
                            noChange << message(code: 'subscription.resource.label')
                        }

                        if(params.isPublicForApi && !auditService.getAuditConfig(subChild?.instanceOf, 'isPublicForApi'))
                        {
                            subChild?.isPublicForApi = RefdataValue.get(params.isPublicForApi) == RDStore.YN_YES
                            change << message(code: 'subscription.isPublicForApi.label')
                        }
                        if(params.isPublicForApi && auditService.getAuditConfig(subChild?.instanceOf, 'isPublicForApi'))
                        {
                            noChange << message(code: 'subscription.isPublicForApi.label')
                        }

                        if(params.hasPerpetualAccess && !auditService.getAuditConfig(subChild?.instanceOf, 'hasPerpetualAccess'))
                        {
                            subChild?.hasPerpetualAccess = RefdataValue.get(params.hasPerpetualAccess) == RDStore.YN_YES
                            change << message(code: 'subscription.hasPerpetualAccess.label')
                        }
                        if(params.hasPerpetuaLAccess && auditService.getAuditConfig(subChild?.instanceOf, 'hasPerpetualAccess'))
                        {
                            noChange << message(code: 'subscription.hasPerpetualAccess.label')
                        }

                        if (subChild?.isDirty()) {
                            subChild?.save(flush: true)
                        }
                    }

                    if(change){
                        flash.message = message(code: 'subscription.subscriptionPropertiesMembers.changes', args: [change?.unique { a, b -> a <=> b }.join(', ').toString()])
                    }

                    if(noChange){
                        flash.error = message(code: 'subscription.subscriptionPropertiesMembers.noChanges', args: [noChange?.unique { a, b -> a <=> b }.join(', ').toString()])
                    }
        }else {
            flash.error = message(code: 'subscription.subscriptionPropertiesMembers.noSelectedMember')
        }

        redirect(action: 'subscriptionPropertiesMembers', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def processDeletePropertiesMembers() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.filterPropDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null

        result.parentSub = result.subscriptionInstance.instanceOf && result.subscription._getCalculatedType() != CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        List<Subscription> validSubChilds = Subscription.findAllByInstanceOf(result.parentSub)

        if (params.filterPropDef) {

            String filterPropDef = params.filterPropDef
            PropertyDefinition propDef = (PropertyDefinition) genericOIDService.resolveOID(filterPropDef.replace(" ", ""))

            int deletedProperties = 0

            if (propDef) {
                validSubChilds.each { Subscription subChild ->

                    if (propDef.tenant != null) {
                        //private Property

                        List<SubscriptionProperty> existingProps = subChild.propertySet.findAll {
                            it.owner.id == subChild.id && it.type.id == propDef.id && it.tenant.id == result.institution.id
                        }
                        existingProps.removeAll { it.type.name != propDef.name } // dubious fix


                        if (existingProps.size() == 1 ){
                            SubscriptionProperty privateProp = SubscriptionProperty.get(existingProps[0].id)

                            try {
                                privateProp.delete(flush:true)
                                deletedProperties++
                            } catch (Exception e)
                            {
                                log.error( e.toString() )
                            }

                        }

                    } else {
                        //custom Property

                        def existingProp = subChild.propertySet.find {
                            it.type.id == propDef.id && it.owner.id == subChild.id && it.tenant.id == result.institution.id
                        }


                        if (existingProp && !(existingProp.hasProperty('instanceOf') && existingProp.instanceOf && AuditConfig.getConfig(existingProp.instanceOf))){
                            SubscriptionProperty customProp = SubscriptionProperty.get(existingProp.id)

                            try {
                                customProp.delete(flush:true)
                                deletedProperties++
                            } catch (Exception e){
                                log.error( e.toString() )
                            }

                        }
                    }

                }
                flash.message = message(code: 'subscription.propertiesMembers.deletedProperties', args: [deletedProperties])
            }

        }

        def filterPropDef = params.filterPropDef
        def id = params.id
        redirect(action: 'propertiesMembers', id: id, params: [filterPropDef: filterPropDef])
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def removeEntitlement() {
        log.debug("removeEntitlement....");
        IssueEntitlement ie = IssueEntitlement.get(params.ieid)
        def deleted_ie = RDStore.TIPP_STATUS_DELETED
        ie.status = deleted_ie;

        redirect action: 'index', id: params.sub
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def removeEntitlementGroup() {
        log.debug("removeEntitlementGroup....");
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.get(params.titleGroup)

        if(issueEntitlementGroup) {
            IssueEntitlementGroupItem.findAllByIeGroup(issueEntitlementGroup).each {
                it.delete(flush: true)
            }

            IssueEntitlementGroup.executeUpdate("delete from IssueEntitlementGroup ieg where ieg.id in (:issueEntitlementGroup)", [issueEntitlementGroup: issueEntitlementGroup.id])
        }

        redirect action: 'manageEntitlementGroup', id: params.sub
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def processRemoveEntitlements() {
        log.debug("processRemoveEntitlements....");

        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        if (result.subscriptionInstance && params.singleTitle) {
            if(subscriptionService.deleteEntitlement(result.subscriptionInstance,params.singleTitle))
                log.debug("Deleted tipp ${params.singleTitle} from sub ${result.subscriptionInstance.id}")
        } else {
            log.error("Unable to locate subscription instance");
        }

        redirect action: 'renewEntitlements', model: [targetObjectId: result.subscriptionInstance?.id, packageId: params.packageId]

    }
    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def processRemoveIssueEntitlementsSurvey() {
        log.debug("processRemoveIssueEntitlementsSurvey....");

        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.subscriptionInstance = Subscription.get(params.id)
        result.subscription = Subscription.get(params.id)
        result.institution = result.subscription?.subscriber
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableIssueEntitlementsSurvey(result.institution, result.surveyConfig)
        if (!result.editable) {
            response.sendError(401); return
        }

        if (result.subscriptionInstance && params.singleTitle) {
            if(subscriptionService.deleteEntitlementbyID(result.subscriptionInstance,params.singleTitle))
                log.debug("Deleted ie ${params.singleTitle} from sub ${result.subscriptionInstance.id}")
        } else {
            log.error("Unable to locate subscription instance");
        }

        redirect action: 'renewEntitlementsWithSurvey', params: [targetObjectId: result.subscriptionInstance?.id, surveyConfigID: result.surveyConfig?.id]
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def processRenewEntitlements() {
        log.debug("processRenewEntitlements ...")
        params.id = Long.parseLong(params.id)
        params.packageId = Long.parseLong(params.packageId)
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        List tippsToAdd = params."tippsToAdd".split(",")
        List tippsToDelete = params."tippsToDelete".split(",")

        def ie_accept_status = RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION

        if(result.subscriptionInstance) {
            tippsToAdd.each { tipp ->
                try {
                    if(subscriptionService.addEntitlement(result.subscriptionInstance,tipp,null,false, ie_accept_status))
                        log.debug("Added tipp ${tipp} to sub ${result.subscriptionInstance.id}")
                }
                catch (EntitlementCreationException e) {
                    flash.error = e.getMessage()
                }
            }
            tippsToDelete.each { tipp ->
                if(subscriptionService.deleteEntitlement(result.subscriptionInstance,tipp))
                    log.debug("Deleted tipp ${tipp} from sub ${result.subscriptionInstance.id}")
            }
            if(params.process == "finalise") {
                SubscriptionPackage sp = SubscriptionPackage.findBySubscriptionAndPkg(result.subscriptionInstance,Package.get(params.packageId))
                sp.finishDate = new Date()
                if(!sp.save(flush:true)) {
                    flash.error = sp.errors
                }
                else flash.message = message(code:'subscription.details.renewEntitlements.submitSuccess',args:[sp.pkg.name])
            }
        }
        else {
            log.error("Unable to locate subscription instance")
        }
        redirect action: 'index', id: params.id
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def processRenewEntitlementsWithSurvey() {
        log.debug("processRenewEntitlementsWithSurvey ...")
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.subscriptionInstance = Subscription.get(params.id)
        result.subscription = Subscription.get(params.id)
        result.institution = result.subscription?.subscriber
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableIssueEntitlementsSurvey(result.institution, result.surveyConfig)
        if (!result.editable) {
            response.sendError(401); return
        }

        List iesToAdd = params."iesToAdd".split(",")


        def ie_accept_status = RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION

        Integer countIEsToAdd = 0

        if(result.subscriptionInstance) {
            iesToAdd.each { ieID ->
                IssueEntitlement ie = IssueEntitlement.findById(ieID)
                def tipp = ie.tipp


                if(tipp) {
                    try {
                        if (subscriptionService.addEntitlement(result.subscriptionInstance, tipp.gokbId, ie, (ie.priceItem != null), ie_accept_status)) {
                            log.debug("Added tipp ${tipp.gokbId} to sub ${result.subscriptionInstance.id}")
                            countIEsToAdd++
                        }
                    }
                    catch (EntitlementCreationException e) {
                        log.debug("Error: Added tipp ${tipp} to sub ${result.subscriptionInstance.id}: " + e.getMessage())
                        flash.error = message(code: 'renewEntitlementsWithSurvey.noSelectedTipps')
                    }
                }
            }

            if(countIEsToAdd > 0){
                flash.message = message(code:'renewEntitlementsWithSurvey.tippsToAdd', args: [countIEsToAdd])
            }

        }
        else {
            log.error("Unable to locate subscription instance")
        }
        redirect action: 'renewEntitlementsWithSurvey', id: params.id, params: [targetObjectId: params.id, surveyConfigID: result.surveyConfig?.id]
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def addEmptyPriceItem() {
        if(params.ieid) {
            IssueEntitlement ie = IssueEntitlement.get(params.ieid)
            if(ie && !ie.priceItem) {
                PriceItem pi = new PriceItem(issueEntitlement: ie)
                pi.setGlobalUID()
                if(!pi.save(flush: true)) {
                    log.error(pi.errors.toString())
                    flash.error = message(code:'subscription.details.addEmptyPriceItem.priceItemNotSaved')
                }
            }
            else {
                flash.error = message(code:'subscription.details.addEmptyPriceItem.issueEntitlementNotFound')
            }
        }
        else {
            flash.error = message(code:'subscription.details.addEmptyPriceItem.noIssueEntitlement')
        }
        redirect action: 'index', id: params.id
    }

    def buildRenewalsQuery(params) {
        log.debug("BuildQuery...");

        StringWriter sw = new StringWriter()
        sw.write("rectype:'Package'")

        renewals_reversemap.each { mapping ->

            // log.debug("testing ${mapping.key}");

            if (params[mapping.key] != null) {
                if (params[mapping.key].class == java.util.ArrayList) {
                    params[mapping.key].each { p ->
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${p}\"")
                    }
                } else {
                    // Only add the param if it's length is > 0 or we end up with really ugly URLs
                    // II : Changed to only do this if the value is NOT an *
                    if (params[mapping.key].length() > 0 && !(params[mapping.key].equalsIgnoreCase('*'))) {
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${params[mapping.key]}\"")
                    }
                }
            }
        }


        def result = sw.toString();
        result;
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def setupPendingChangeConfiguration() {
        Map<String, Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if(!result) {
            response.sendError(403)
            return
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
                PendingChangeConfiguration pcc = PendingChangeConfiguration.construct(configMap)
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def history() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.contextOrg = contextService.getOrg()
        result.max = params.max ?: result.user.getDefaultPageSize()
        result.offset = params.offset ?: 0;

        Map<String, Object> qry_params = [cname: result.subscription.class.name, poid: "${result.subscription.id}"]

        result.historyLines = AuditLogEvent.executeQuery(
                "select e from AuditLogEvent as e where className = :cname and persistedObjectId = :poid order by id desc",
                qry_params, [max: result.max, offset: result.offset]
        )
        result.historyLinesTotal = AuditLogEvent.executeQuery(
                "select e.id from AuditLogEvent as e where className = :cname and persistedObjectId = :poid", qry_params
        ).size()

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def changes() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        result.contextOrg = contextService.getOrg()
        result.max = params.max ?: result.user.getDefaultPageSize()
        result.offset = params.offset ?: 0;

        String baseQuery = "select pc from PendingChange as pc where pc.subscription = :sub and pc.status.value in (:stats)"
        def baseParams = [sub: result.subscription, stats: ['Accepted', 'Rejected']]

        result.todoHistoryLines = PendingChange.executeQuery(
                baseQuery + " order by pc.ts desc",
                baseParams,
                [max: result.max, offset: result.offset]
        )

        result.todoHistoryLinesTotal = PendingChange.executeQuery(
                baseQuery,
                baseParams
        )[0] ?: 0

        result
    }

    /* TODO Cost per use tab, still needed?
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_USER") })
    def costPerUse() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
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

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processRenewSubscription() {

        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
        if (!result.editable) {
            response.sendError(401); return
        }

        ArrayList<Links> previousSubscriptions = Links.findAllByDestinationSubscriptionAndLinkType(result.subscription, RDStore.LINKTYPE_FOLLOWS)
        if (previousSubscriptions.size() > 0) {
            flash.error = message(code: 'subscription.renewSubExist')
        } else {
            def sub_startDate = params.subscription.start_date ? DateUtil.parseDateGeneric(params.subscription.start_date) : null
            def sub_endDate = params.subscription.end_date ? DateUtil.parseDateGeneric(params.subscription.end_date) : null
            def sub_status = params.subStatus ?: RDStore.SUBSCRIPTION_NO_STATUS
            def sub_isMultiYear = params.subscription.isMultiYear
            def new_subname = params.subscription.name
            def manualCancellationDate = null

            use(TimeCategory) {
                manualCancellationDate =  result.subscription.manualCancellationDate ? (result.subscription.manualCancellationDate + 1.year) : null
            }

            Subscription newSub = new Subscription(
                    name: new_subname,
                    startDate: sub_startDate,
                    endDate: sub_endDate,
                    manualCancellationDate: manualCancellationDate,
                    identifier: UUID.randomUUID().toString(),
                    isSlaved: result.subscription.isSlaved,
                    type: result.subscription.type,
                    kind: result.subscription.kind,
                    resource: result.subscription.resource,
                    form: result.subscription.form,
                    isPublicForApi: result.subscription.isPublicForApi,
                    hasPerpetualAccess: result.subscription.hasPerpetualAccess,
                    status: sub_status,
                    isMultiYear: sub_isMultiYear ?: false,
                    administrative: result.subscription.administrative,
            )

            if (!newSub.save(flush:true)) {
                log.error("Problem saving subscription ${newSub.errors}");
                return newSub
            } else {
                log.debug("Save ok")
                if(accessService.checkPerm("ORG_CONSORTIUM")) {
                    if (params.list('auditList')) {
                        //copy audit
                        params.list('auditList').each { auditField ->
                            //All ReferenceFields were copied!
                            //'name', 'startDate', 'endDate', 'manualCancellationDate', 'status', 'type', 'form', 'resource'
                            AuditConfig.addConfig(newSub, auditField)
                        }
                    }
                }
                //Copy References
                //OrgRole
                result.subscription.orgRelations?.each { or ->

                    if ((or.org.id == contextService.getOrg().id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS,  RDStore.OR_SUBSCRIBER_CONS_HIDDEN])) {
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, or.properties)
                        newOrgRole.sub = newSub
                        newOrgRole.save()
                    }
                }
                //link to previous subscription
                Links prevLink = Links.construct([source: newSub, destination: result.subscription, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.org])
                if (!prevLink.save(flush:true)) {
                    log.error("Problem linking to previous subscription: ${prevLink.errors}")
                }
                result.newSub = newSub

                if (params.targetObjectId == "null") params.remove("targetObjectId")
                result.isRenewSub = true

                redirect controller: 'subscription',
                            action: 'copyElementsIntoSubscription',
                            params: [sourceObjectId: genericOIDService.getOID(result.subscription), targetObjectId: genericOIDService.getOID(newSub), isRenewSub: true]
            }
        }
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def renewSubscription() {
        Map<String,Object> result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
        result.institution = contextService.org
        if (!result.editable) {
            response.sendError(401); return
        }

        Subscription subscription = Subscription.get(params.baseSubscription ?: params.id)
        SimpleDateFormat sdf = new SimpleDateFormat('dd.MM.yyyy')
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
                                 sub_status   : RDStore.SUBSCRIPTION_INTENDED.id.toString()
                                ]

        result.subscription = subscription
        result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copyElementsIntoSubscription() {
        Map<String, Object> result = [:]
        result.user = contextService.user
        result.contextOrg = contextService.getOrg()
        flash.error = ""
        flash.message = ""
        if (params.sourceObjectId == "null") params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if (params.targetObjectId == "null") params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        result.showConsortiaFunctions = showConsortiaFunctions(result.contextOrg, result.sourceObject)
        result.consortialView = result.showConsortiaFunctions

        result.editable = result.sourceObject?.isEditableBy(result.user)

        if (!result.editable) {
            response.sendError(401); return
        }

        if (params.isRenewSub) {result.isRenewSub = params.isRenewSub}
        if (params.fromSurvey && accessService.checkPerm("ORG_CONSORTIUM")) {result.fromSurvey = params.fromSurvey}

        result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL && result.targetObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL) ?: false

        if (params.copyObject) {result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL)}

        result.allObjects_readRights = subscriptionService.getMySubscriptions_readRights([status: RDStore.SUBSCRIPTION_CURRENT.id])
        result.allObjects_writeRights = subscriptionService.getMySubscriptions_writeRights([status: RDStore.SUBSCRIPTION_CURRENT.id])

        List<String> subTypSubscriberVisible = [CalculatedType.TYPE_CONSORTIAL,
                                                CalculatedType.TYPE_ADMINISTRATIVE]
        result.isSubscriberVisible =
                result.sourceObject &&
                result.targetObject &&
                subTypSubscriberVisible.contains(result.sourceObject._getCalculatedType()) &&
                subTypSubscriberVisible.contains(result.targetObject._getCalculatedType())

        if (! result.isSubscriberVisible) {
            //flash.message += message(code: 'subscription.info.subscriberNotAvailable')
        }

        switch (params.workFlowPart) {
            case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                if (params.isRenewSub){
                    params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                    result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                } else {
                    result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                }
                break
            case CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS:
                result << copyElementsService.copyObjectElements_PackagesEntitlements(params)
                if (params.isRenewSub){
                    params.workFlowPart = CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
                    result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                } else {
                    result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                }
                break
            case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                if (params.isRenewSub){
                    if (!params.fromSurvey && result.isSubscriberVisible){
                        params.workFlowPart = CopyElementsService.WORKFLOW_SUBSCRIBER
                        result << copyElementsService.loadDataFor_Subscriber(params)
                    } else {
                        params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                        result << copyElementsService.loadDataFor_Properties(params)
                    }
                } else {
                    result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                }
                break
            case CopyElementsService.WORKFLOW_SUBSCRIBER:
                result << copyElementsService.copyObjectElements_Subscriber(params)
                if (params.isRenewSub) {
                    params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                    result << copyElementsService.loadDataFor_Properties(params)
                } else {
                    result << copyElementsService.loadDataFor_Subscriber(params)
                }
                break
            case CopyElementsService.WORKFLOW_PROPERTIES:
                result << copyElementsService.copyObjectElements_Properties(params)
                if (params.isRenewSub && result.targetObject){
                    flash.error = ""
                    flash.message = ""
                    def surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(result.sourceObject, true)

                    if(surveyConfig && result.fromSurvey) {
                        redirect controller: 'survey', action: 'renewalWithSurvey', params: [id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id]
                    }else {
                        redirect controller: 'subscription', action: 'show', params: [id: result.targetObject.id]
                    }
                } else {
                    result << copyElementsService.loadDataFor_Properties(params)
                }
                break
            case CopyElementsService.WORKFLOW_END:
                result << copyElementsService.copyObjectElements_Properties(params)
                if (result.targetObject){
                    flash.error = ""
                    flash.message = ""

                    def surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(result.sourceObject, true)

                    if(surveyConfig && result.fromSurvey) {
                        redirect controller: 'survey', action: 'renewalWithSurvey', params: [id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id]
                    }else {
                        redirect controller: 'subscription', action: 'show', params: [id: result.targetObject.id]
                    }
                }
                break
            default:
                result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                break
        }

        if (params.targetObjectId) {
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }
        result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS
        result.workFlowPartNext = params.workFlowPartNext ?: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS

        if (params.isRenewSub) {result.isRenewSub = params.isRenewSub}
        result
    }

    @DebugAnnotation(perm = "ORG_INST", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copyMyElements() {
        Map<String, Object> result = [:]
        result.user = contextService.user
        result.contextOrg = contextService.getOrg()
        flash.error = ""
        flash.message = ""
        if (params.sourceObjectId == "null") params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if (params.targetObjectId == "null") params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        //isVisibleBy benötigt hier um zu schauen, ob das Objekt überhaupt angesehen darf
        result.editable = result.sourceObject?.isVisibleBy(result.user)

        if (!result.editable) {
            response.sendError(401); return
        }

        result.allObjects_readRights = subscriptionService.getMySubscriptionsWithMyElements_readRights([status: RDStore.SUBSCRIPTION_CURRENT.id])
        result.allObjects_writeRights = subscriptionService.getMySubscriptionsWithMyElements_writeRights([status: RDStore.SUBSCRIPTION_CURRENT.id])

        switch (params.workFlowPart) {
            case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)

                break;
            case CopyElementsService.WORKFLOW_PROPERTIES:
                result << copyElementsService.copyObjectElements_Properties(params)
                result << copyElementsService.loadDataFor_Properties(params)

                break;
            case CopyElementsService.WORKFLOW_END:
                result << copyElementsService.copyObjectElements_Properties(params)
                if (result.targetObject){
                    flash.error = ""
                    flash.message = ""
                    redirect controller: 'subscription', action: 'show', params: [id: result.targetObject.id]
                }
                break;
            default:
                result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                break;
        }

        if (params.targetObjectId) {
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }
        result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
        result.workFlowPartNext = params.workFlowPartNext ?: CopyElementsService.WORKFLOW_PROPERTIES
        result
    }


    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copySubscription() {
        Map<String, Object> result = [:]
        result.user = contextService.user
        result.contextOrg = contextService.getOrg()
        flash.error = ""
        flash.message = ""
        if (params.sourceObjectId == "null") params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if (params.targetObjectId == "null") params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        result.showConsortiaFunctions = showConsortiaFunctions(result.contextOrg, result.sourceObject)
        result.consortialView = result.showConsortiaFunctions

        result.editable = result.sourceObject?.isEditableBy(result.user)

        if (!result.editable) {
            response.sendError(401); return
        }

        result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL)
        result.copyObject = true

        if (params.name && !result.targetObject) {
            String sub_name = params.name ?: "Kopie von ${result.sourceObject.name}"

            Object targetObject = new Subscription(
                    name: sub_name,
                    status: RDStore.SUBSCRIPTION_NO_STATUS,
                    identifier: java.util.UUID.randomUUID().toString(),
                    type: result.sourceObject.type,
                    isSlaved: result.sourceObject.isSlaved,
                    administrative: result.sourceObject.administrative
            )
            //Copy InstanceOf
            if (params.targetObject?.copylinktoSubscription) {
                targetObject.instanceOf = result.sourceObject.instanceOf ?: null
            }


            if (!targetObject.save()) {
                log.error("Problem saving subscription ${targetObject.errors}");
            }else {
                result.targetObject = targetObject
                params.targetObjectId = genericOIDService.getOID(targetObject)

                //Copy References
                result.sourceObject.orgRelations.each { OrgRole or ->
                    if ((or.org.id == result.contextOrg.id) || (or.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id])) {
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, or.properties)
                        newOrgRole.sub = result.targetObject
                        newOrgRole.save()
                    }
                }

            }
        }


        switch (params.workFlowPart) {
            case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                if(result.targetObject) {
                    params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                }
                result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                break
            case CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS:
                result << copyElementsService.copyObjectElements_PackagesEntitlements(params)
                params.workFlowPart = CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
                result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                break
            case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                result << copyElementsService.loadDataFor_Properties(params)
                break
            case CopyElementsService.WORKFLOW_END:
                result << copyElementsService.copyObjectElements_Properties(params)
                if (result.targetObject){
                        redirect controller: 'subscription', action: 'show', params: [id: result.targetObject.id]
                }
                break
            default:
                result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                break
        }

        result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS

        result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSubscriptions() {
        boolean withErrors = false
        Org contextOrg = contextService.org
        SimpleDateFormat databaseDateFormatParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
        flash.error = ""
        def candidates = JSON.parse(params.candidates)
        candidates.eachWithIndex{ entry, int s ->
            if(params["take${s}"]) {
                //create object itself
                Subscription sub = new Subscription(name: entry.name,
                        status: genericOIDService.resolveOID(entry.status),
                        kind: genericOIDService.resolveOID(entry.kind),
                        form: genericOIDService.resolveOID(entry.form),
                        resource: genericOIDService.resolveOID(entry.resource),
                        type: genericOIDService.resolveOID(entry.type),
                        identifier: UUID.randomUUID())
                sub.startDate = entry.startDate ? databaseDateFormatParser.parse(entry.startDate) : null
                sub.endDate = entry.endDate ? databaseDateFormatParser.parse(entry.endDate) : null
                sub.manualCancellationDate = entry.manualCancellationDate ? databaseDateFormatParser.parse(entry.manualCancellationDate) : null
                /* TODO [ticket=2276]
                if(sub.type == SUBSCRIPTION_TYPE_ADMINISTRATIVE)
                    sub.administrative = true*/
                sub.instanceOf = entry.instanceOf ? genericOIDService.resolveOID(entry.instanceOf) : null
                Org member = entry.member ? genericOIDService.resolveOID(entry.member) : null
                Org provider = entry.provider ? genericOIDService.resolveOID(entry.provider) : null
                Org agency = entry.agency ? genericOIDService.resolveOID(entry.agency) : null
                if(sub.instanceOf && member)
                    sub.isSlaved = RDStore.YN_YES
                if(sub.save(flush:true)) {
                    //create the org role associations
                    RefdataValue parentRoleType, memberRoleType
                    if(accessService.checkPerm("ORG_CONSORTIUM")) {
                        parentRoleType = RDStore.OR_SUBSCRIPTION_CONSORTIA
                        memberRoleType = RDStore.OR_SUBSCRIBER_CONS
                    }
                    else
                        parentRoleType = RDStore.OR_SUBSCRIBER
                    entry.licenses.each { String licenseOID ->
                        License license = (License) genericOIDService.resolveOID(licenseOID)
                        subscriptionService.setOrgLicRole(sub,license,false)
                    }
                    OrgRole parentRole = new OrgRole(roleType: parentRoleType, sub: sub, org: contextOrg)
                    if(!parentRole.save(flush:true)) {
                        withErrors = true
                        flash.error += parentRole.errors
                    }
                    if(memberRoleType && member) {
                        OrgRole memberRole = new OrgRole(roleType: memberRoleType, sub: sub, org: member)
                        if(!memberRole.save(flush:true)) {
                            withErrors = true
                            flash.error += memberRole.errors
                        }
                    }
                    if(provider) {
                        OrgRole providerRole = new OrgRole(roleType: RDStore.OR_PROVIDER, sub: sub, org: provider)
                        if(!providerRole.save(flush:true)) {
                            withErrors = true
                            flash.error += providerRole.errors
                        }
                    }
                    if(agency) {
                        OrgRole agencyRole = new OrgRole(roleType: RDStore.OR_AGENCY, sub: sub, org: agency)
                        if(!agencyRole.save(flush:true)) {
                            withErrors = true
                            flash.error += agencyRole.errors
                        }
                    }
                    //process subscription properties
                    entry.properties.each { k, v ->
                        if(v.propValue?.trim()) {
                            log.debug("${k}:${v.propValue}")
                            PropertyDefinition propDef = (PropertyDefinition) genericOIDService.resolveOID(k)
                            List<String> valueList
                            if(propDef.multipleOccurrence) {
                                valueList = v?.propValue?.split(',')
                            }
                            else valueList = [v.propValue]
                            //in most cases, valueList is a list with one entry
                            valueList.each { value ->
                                try {
                                    subscriptionService.createProperty(propDef,sub,contextOrg,value.trim(),v.propNote)
                                }
                                catch (Exception e) {
                                    withErrors = true
                                    flash.error += e.getMessage()
                                }
                            }
                        }
                    }
                    if(entry.notes) {
                        Doc docContent = new Doc(contentType: Doc.CONTENT_TYPE_STRING, content: entry.notes, title: message(code:'myinst.subscriptionImport.notes.title',args:[sdf.format(new Date())]), type: RefdataValue.getByValueAndCategory('Note', RDConstants.DOCUMENT_TYPE), owner: contextOrg, user: contextService.user)
                        if(docContent.save(flush:true)) {
                            DocContext dc = new DocContext(subscription: sub, owner: docContent, doctype: RDStore.DOC_TYPE_NOTE)
                            dc.save(flush:true)
                        }
                    }
                }
                else {
                    withErrors = true
                    flash.error += sub.errors
                }
            }
        }
        if(!withErrors)
            redirect controller: 'myInstitution', action: 'currentSubscriptions'
        else redirect(url: request.getHeader("referer"))
    }

    //--------------------------------------------- admin section -------------------------------------------------

    @Secured(['ROLE_ADMIN'])
    def pendingChanges() {
        Map<String,Object> ctrlResult = subscriptionControllerService.pendingChanges(this)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result)
                response.sendError(401)
        }
        else
            ctrlResult.result
    }

    Map<String,Object> setResultGenericsAndCheckAccess(checkOption) {
        Map<String, Object> result = [:]
        result.user = contextService.user
        result.subscriptionInstance = Subscription.get(params.id)
        if(!params.id && params.subscription)
            result.subscriptionInstance = Subscription.get(params.subscription)
        result.subscription = result.subscriptionInstance //TODO temp, remove the duplicate
        result.contextOrg = contextService.getOrg()
        result.institution = result.subscription ? result.subscription.subscriber : result.contextOrg //TODO temp, remove the duplicate
        if(result.subscription) {
            result.licenses = Links.findAllByDestinationSubscriptionAndLinkType(result.subscription, RDStore.LINKTYPE_LICENSE).collect { Links li -> li.sourceLicense }

            LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(result.subscription)
            result.navPrevSubscription = links.prevLink
            result.navNextSubscription = links.nextLink

            result.showConsortiaFunctions = showConsortiaFunctions(result.contextOrg, result.subscription)

            if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
                if (!result.subscription.isVisibleBy(result.user)) {
                    log.debug("--- NOT VISIBLE ---")
                    return null
                }
            }
            result.editable = result.subscription.isEditableBy(result.user)

            if(params.orgBasicMemberView){
                result.editable = false
            }

            if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
                if (!result.editable) {
                    log.debug("--- NOT EDITABLE ---")
                    return null
                }
            }
        }
        else {
            if(checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
                result.editable = accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_EDITOR")
            }
        }
        result.consortialView = result.showConsortiaFunctions ?: result.contextOrg.getCustomerType() == "ORG_CONSORTIUM"

        Map args = [:]
        if(result.consortialView) {
            args.superOrgType = [message(code:'consortium.superOrgType')]
            args.memberTypeSingle = [message(code:'consortium.subscriber')]
            args.memberType = [message(code:'consortium.subscriber')]
            args.memberTypeGenitive = [message(code:'consortium.subscriber')]
        }
        result.args = args

        result
    }

    static boolean showConsortiaFunctions(Org contextOrg, Subscription subscription) {
        return ((subscription.getConsortia()?.id == contextOrg.id) && subscription._getCalculatedType() in
                [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE])
    }

}
