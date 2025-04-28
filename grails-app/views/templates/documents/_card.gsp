<%@ page import="de.laser.wekb.Provider; de.laser.wekb.Vendor; de.laser.ui.Btn; de.laser.ui.Icon; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.*; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>
<%
    List<DocContext> baseItems = []
    List<DocContext> sharedItems = []

    String documentMessage
    switch(ownobj.class.name) {
        case Org.class.name: documentMessage = "default.documents.label"
            editable = userService.hasFormalAffiliation(contextService.getOrg(), 'INST_EDITOR')
            break
        default: documentMessage = "license.documents"
            break
    }

    boolean editable2 = contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    Set<DocContext> documentSet = ownobj.documents

    //Those are the rights settings the DMS needs to cope with. See the following documentation which is currently a requirement specification, too, and serves as base for ERMS-2393
    if(ownobj instanceof Org && ownobj.id == contextService.getOrg().id) {
        //get all documents which has been attached to this org
        documentSet.addAll(docstoreService.getTargettedDocuments(ownobj))
    }

    documentSet.sort{it.owner?.title}.each{ it ->
        boolean visible = false //is the document visible?
        boolean inOwnerOrg = it.owner.owner?.id == contextService.getOrg().id //are we owners of the document?
        boolean inTargetOrg = false //are we in the org to which a document is attached?

        if(it.targetOrg) {
            inTargetOrg = contextService.getOrg().id == it.targetOrg.id
        }
        else if(it.subscription) {
            inTargetOrg = contextService.getOrg().id == it.subscription.getSubscriberRespConsortia().id
        }
        else if(it.license) {
            inTargetOrg = contextService.getOrg().id in it.license.getAllLicensee().id
        }

        switch(it.shareConf) {
            case RDStore.SHARE_CONF_UPLOADER_ORG: if(inOwnerOrg) visible = true //visible only for thes users of org which uploaded the document
                break
            case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: if(inOwnerOrg || inTargetOrg) visible = true //the owner org and the target org may see the document i.e. the document has been shared with the target org
                break
            case RDStore.SHARE_CONF_ALL: visible = true //definition says that everyone with "access" to target org
                break
            default:
                if(it.org) {
                    //fallback: documents are visible if share configuration is missing or obsolete
                    visible = inOwnerOrg
                }
                else if(inOwnerOrg || it.sharedFrom)
                    //other owner objects than orgs - in particular licenses and subscriptions: visibility is set if the owner org visits the owner object or sharing is activated
                    visible = true
                break
        }
//        if (ownobj instanceof Org && it.shareConf == RDStore.SHARE_CONF_UPLOADER_AND_TARGET && inTargetOrg && inOwnerOrg) {
//            baseItems << it // workaround: ERMS-6460 / ERMS-6467
//        }
//        else
        if ((it.sharedFrom || (ownobj instanceof Org && inTargetOrg)) && visible) {
            //a shared item; assign it to the shared docs section
            sharedItems << it
        }
        else if(visible) {
            //an item directly attached to the owner object
            baseItems << it
        }
    }
%>
<g:if test="${(contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support() || contextService.getOrg().isCustomerType_Inst_Pro())}">
    <ui:card message="${documentMessage}" class="documents ${css_class}" href="#modalCreateDocument" editable="${editable || editable2}">
        <g:each in="${baseItems}" var="docctx">
            <g:if test="${docctx.isDocAFile() && (docctx.status?.value!='Deleted')}">
                <div class="ui small feed content">
                    <div class="ui grid summary">
                        <div class="eight wide column la-column-right-lessPadding">
                            <ui:documentShareConfigIcon docctx="${docctx}"/>
                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}" />
                            <g:if test="${supportedMimeType}">
                                <a href="#documentPreview" data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code:'template.documents.missing')}</a>
                            </g:if>
                            <g:else>
                                ${docctx.owner.title ?: docctx.owner.filename ?: message(code:'template.documents.missing')}
                            </g:else>
                            <g:if test="${docctx.getDocType()}">
                                (${docctx.getDocType().getI10n("value")})
                            </g:if>
                        </div>
                        <div class="right aligned eight wide column la-column-left-lessPadding">

                        <g:if test="${! (editable || editable2)}">
                            <%-- 1 --%>
                            <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}" class="${Btn.MODERN.SIMPLE}" target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>

                            <g:if test="${docctx.isShared}">
                                %{-- TODO: ERMS-6253 - show shared icon --}%
                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.on')}">
                                    <span class="${Btn.MODERN.SIMPLE} disabled">
                                        <i class="${Icon.SIG.SHARED_OBJECT_ON} green"></i>
                                    </span>
                                </span>
                            </g:if>
                        </g:if>
                        <g:else>
                            <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id}">
                                <%-- 1 --%>
                                <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}" class="${Btn.MODERN.SIMPLE}" target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>

                                <%-- 2 --%>
                                <laser:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="${Btn.MODERN.SIMPLE}"
                                        data-ui="modal"
                                        data-href="#modalEditDocument_${docctx.id}"
                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                    <i class="${Icon.CMD.EDIT}"></i>
                                </button>
                            </g:if>
                            <g:elseif test="${docctx.shareConf in [RDStore.SHARE_CONF_ALL, RDStore.SHARE_CONF_UPLOADER_AND_TARGET]}">
                                <%-- 1 --%>
                                <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}" class="${Btn.MODERN.SIMPLE}" target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                            </g:elseif>

                            <%-- 3 --%>
                            <g:if test="${!(ownobj instanceof Org) && !(ownobj instanceof Provider) && !(ownobj instanceof Vendor) && ownobj?.showUIShareButton() && userService.hasFormalAffiliation(docctx.owner.owner, 'INST_EDITOR')}">
                                <g:if test="${docctx.isShared}">
                                        <ui:remoteLink class="${Btn.MODERN.POSITIVE_TOOLTIP} js-no-wait-wheel"
                                                       controller="ajax"
                                                       action="toggleShare"
                                                       params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"documents", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?:  actionName]'
                                                       data-content="${message(code:'property.share.tooltip.on')}"
                                                       data-done=""
                                                       data-update="container-documents"
                                                       role="button">
                                            <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                        </ui:remoteLink>
                                </g:if>
                                <g:else>
                                    <ui:remoteLink class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP} js-no-wait-wheel"
                                                   controller="ajax"
                                                   action="toggleShare"
                                                   params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"documents", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?:  actionName]'
                                                   data-content="${message(code:'property.share.tooltip.off')}"
                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.share.element.member", args: [docctx.owner.title])}"
                                                   data-confirm-term-how="share"
                                                   data-done=""
                                                   data-update="container-documents"
                                                   role="button">
                                        <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                    </ui:remoteLink>
                                </g:else>
                            </g:if>

                            <%-- 4 --%>
                            <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id && !docctx.isShared}">
                                <g:link controller="document" action="deleteDocument" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                        data-confirm-term-how="delete"
                                        params='[instanceId:"${ownobj.id}", deleteId:"${docctx.id}", redirectController:"${ajaxCallController ?: controllerName}", redirectAction:"${ajaxCallAction ?: actionName}"]'
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </g:if>
                            <g:elseif test="${!(docctx.shareConf in [RDStore.SHARE_CONF_ALL, RDStore.SHARE_CONF_UPLOADER_AND_TARGET])}">
                                <div class="${Btn.ICON.SIMPLE} la-hidden">
                                    <icon:placeholder /><%-- Hidden Fake Button --%>
                                </div>
                            </g:elseif>
                        </g:else>%{-- (editable || editable2) --}%
                        </div>
                    </div>
                </div>
            </g:if>
        </g:each>
    </ui:card>
</g:if>
<g:if test="${sharedItems}">
    <ui:card message="license.documents.shared" class="documents ${css_class}" editable="${editable}">
        <g:each in="${sharedItems}" var="docctx">
            <g:if test="${docctx.isDocAFile() && (docctx.status?.value!='Deleted')}">
                <div class="ui small feed content">

                    <div class="ui grid summary">
                        <div class="eleven wide column">
                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}" />
                            <g:if test="${supportedMimeType}">
                                <a href="#documentPreview" data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code:'template.documents.missing')}</a>
                            </g:if>
                            <g:else>
                                ${docctx.owner.title ?: docctx.owner.filename ?: message(code:'template.documents.missing')}
                            </g:else>
                            <g:if test="${docctx.getDocType()}">
                                (${docctx.getDocType().getI10n("value")})
                            </g:if>
                        </div>

                        <div class="five wide right aligned column">
                            <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}" class="${Btn.MODERN.SIMPLE}" target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                        </div>

                    </div>
                </div>
            </g:if>

        </g:each>
    </ui:card>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    docs.init('#container-documents');
</laser:script>
