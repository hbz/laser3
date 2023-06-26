<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; de.laser.*; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>
<%
    List<DocContext> baseItems = []
    List<DocContext> sharedItems = []
    Org contextOrg = contextOrg ?: contextService.getOrg()
    String documentMessage
    switch(ownobj.class.name) {
        case Org.class.name: documentMessage = "default.documents.label"
            editable = userService.checkAffiliationAndCtxOrg_or_ROLEADMIN(contextService.getUser(), contextOrg, 'INST_EDITOR')
            break
        default: documentMessage = "license.documents"
            break
    }

    boolean editable2 = contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    Set<DocContext> documentSet = ownobj.documents

    //Those are the rights settings the DMS needs to cope with. See the following documentation which is currently a requirement specification, too, and serves as base for ERMS-2393
    if(ownobj instanceof Org && ownobj.id == contextOrg.id) {
        //get all documents which has been attached to this org
        documentSet.addAll(docstoreService.getTargettedDocuments(ownobj))
    }

    documentSet.sort{it.owner?.title}.each{ it ->
        boolean visible = false //is the document visible?
        boolean inOwnerOrg = false //are we owners of the document?
        boolean inTargetOrg = false //are we in the org to which a document is attached?

        if(it.owner.owner?.id == contextOrg.id){
            inOwnerOrg = true
        }
        if(contextOrg.id == it.targetOrg?.id) {
            inTargetOrg = true
        }

        if(it.org) {
            switch(it.shareConf) {
                case RDStore.SHARE_CONF_UPLOADER_ORG: if(inOwnerOrg) visible = true //visible only for thes users of org which uploaded the document
                    break
                case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: if(inOwnerOrg || inTargetOrg) visible = true //the owner org and the target org may see the document i.e. the document has been shared with the target org
                    break
                case [ RDStore.SHARE_CONF_CONSORTIUM, RDStore.SHARE_CONF_ALL ]: visible = true //definition says that everyone with "access" to target org. How are such access roles defined and where?
                    break
                default:
                    //fallback: documents are visible if share configuration is missing or obsolete
                    if (!it.shareConf) {
                        visible = it.org == null
                    }
                    break
            }
        }
        else if(inOwnerOrg || it.sharedFrom)
            //other owner objects than orgs - in particular licenses and subscriptions: visibility is set if the owner org visits the owner object or sharing is activated
            visible = true
        if ((it.sharedFrom || inTargetOrg) && visible) {
            //a shared item; assign it to the shared docs section
            sharedItems << it
        }
        else if(visible) {
            //an item directly attached to the owner object
            baseItems << it
        }
    }
%>
<g:if test="${contextService.hasPerm(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
    <ui:card message="${documentMessage}" class="documents la-js-hideable ${css_class}" href="#modalCreateDocument" editable="${editable || editable2}">
        <g:each in="${baseItems}" var="docctx">
            <g:if test="${docctx.isDocAFile() && (docctx.status?.value!='Deleted')}">
                <div class="ui small feed content la-js-dont-hide-this-card">
                    <div class="ui grid summary">
                        <div class="eight wide column la-column-right-lessPadding">

                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}" />
                            <g:if test="${supportedMimeType}">
                                <a href="#documentPreview" data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code:'template.documents.missing')}</a>
                            </g:if>
                            <g:else>
                                ${docctx.owner.title ?: docctx.owner.filename ?: message(code:'template.documents.missing')}
                            </g:else>
                            <g:if test="${docctx.getDocType()}">
                                (${docctx.getDocType().getI10n("value")})
                            </g:if>

                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                        </div>
                        <div class="right aligned eight wide column la-column-left-lessPadding">

                        <g:if test="${! (editable || editable2)}">
                            <%-- 1 --%>
                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue button la-modern-button la-js-dont-hide-button" target="_blank"><i class="download icon"></i></g:link>
                        </g:if>
                        <g:else>
                            <g:if test="${docctx.owner.owner?.id == contextOrg.id}">
                                <%-- 1 --%>
                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue button la-modern-button la-js-dont-hide-button" target="_blank"><i class="download icon"></i></g:link>

                                <%-- 2 --%>
                                <laser:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="ui icon blue button la-modern-button"
                                        data-ui="modal"
                                        data-href="#modalEditDocument_${docctx.id}"
                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                    <i class="pencil icon"></i>
                                </button>
                            </g:if>

                            <%-- 3 --%>
                            <g:if test="${!(ownobj instanceof Org) && ownobj?.showUIShareButton() && userService.checkAffiliationAndCtxOrg(contextService.getUser(), docctx.owner.owner, 'INST_EDITOR')}">
                                <g:if test="${docctx?.isShared}">
                                    <span class="la-js-editmode-container">
                                        <ui:remoteLink class="ui icon green button la-modern-button js-no-wait-wheel la-popup-tooltip la-delay"
                                                       controller="ajax"
                                                       action="toggleShare"
                                                       params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"documents", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?:  actionName]'
                                                       data-content="${message(code:'property.share.tooltip.on')}"
                                                       data-done=""
                                                       data-update="container-documents"
                                                       role="button">
                                            <i class="icon la-share la-js-editmode-icon"></i>
                                        </ui:remoteLink>
                                    </span>
                                </g:if>
                                <g:else>
                                    <ui:remoteLink class="ui icon blue button la-modern-button js-no-wait-wheel la-popup-tooltip la-delay js-open-confirm-modal"
                                                   controller="ajax"
                                                   action="toggleShare"
                                                   params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"documents", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?:  actionName]'
                                                   data-content="${message(code:'property.share.tooltip.off')}"
                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.share.element.member", args: [docctx.owner.title])}"
                                                   data-confirm-term-how="share"
                                                   data-done=""
                                                   data-update="container-documents"
                                                   role="button">
                                        <i class="la-share slash icon la-js-editmode-icon"></i>
                                    </ui:remoteLink>
                                </g:else>
                            </g:if>

                            <%-- 4 --%>
                            <g:if test="${docctx.owner.owner?.id == contextOrg.id && !docctx.isShared}">
                                <g:link controller="${ajaxCallController ?: controllerName}" action="deleteDocuments" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                        data-confirm-term-how="delete"
                                        params='[instanceId:"${ownobj.id}", deleteId:"${docctx.id}", redirectAction:"${ajaxCallAction ?: actionName}"]'
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="trash alternate outline icon"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <div class="ui icon button la-hidden">
                                    <i class="fake icon"></i><%-- Hidden Fake Button --%>
                                </div>
                            </g:else>
                        </g:else>%{-- (editable || editable2) --}%
                        </div>

                                %{-- old --}%

%{--                            <g:if test="${docctx.owner.owner?.id == contextOrg.id}">--}%
%{--                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue button la-modern-button la-js-dont-hide-button" target="_blank"><i class="download icon"></i></g:link>--}%

%{--                                <%-- START First Button --%>--}%
%{--                                <laser:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />--}%
%{--                                <button type="button" class="ui icon blue button la-modern-button"--}%
%{--                                        data-ui="modal"--}%
%{--                                        data-href="#modalEditDocument_${docctx.id}"--}%
%{--                                        aria-label="${message(code: 'ariaLabel.change.universal')}">--}%
%{--                                    <i class="pencil icon"></i></button>--}%

%{--                                <%-- START Second Button --%>--}%
%{--                                <g:if test="${!docctx.isShared}">--}%
%{--                                    <g:link controller="${ajaxCallController ?: controllerName}" action="deleteDocuments" class="ui icon negative button la-modern-button js-open-confirm-modal"--}%
%{--                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"--}%
%{--                                            data-confirm-term-how="delete"--}%
%{--                                            params='[instanceId:"${ownobj.id}", deleteId:"${docctx.id}", redirectAction:"${ajaxCallAction ?: actionName}"]'--}%
%{--                                            role="button"--}%
%{--                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">--}%
%{--                                        <i class="trash alternate outline icon"></i>--}%
%{--                                    </g:link>--}%
%{--                                </g:if>--}%
%{--                                <g:else>--}%
%{--                                    <div class="ui icon button la-hidden">--}%
%{--                                        <i class="fake icon"></i><%-- Hidden Fake Button --%>--}%
%{--                                    </div>--}%
%{--                                </g:else>--}%
%{--                                <%-- STOP Second Button --%>--}%
%{--                            </g:if>--}%
%{--                            <g:else>--}%
%{--                                <div class="ui icon button la-hidden">--}%
%{--                                    <i class="fake icon"></i><%-- Hidden Fake Button --%>--}%
%{--                                </div>--}%
%{--                                <div class="ui icon button la-hidden">--}%
%{--                                    <i class="fake icon"></i><%-- Hidden Fake Button --%>--}%
%{--                                </div>--}%
%{--                            </g:else>--}%
%{--                            <%-- START Third Button --%>--}%
%{--                            <g:if test="${!(ownobj instanceof Org) && ownobj?.showUIShareButton() && userService.checkAffiliationAndCtxOrg(contextService.getUser(), docctx.owner.owner, 'INST_EDITOR')}">--}%
%{--                                <g:if test="${docctx?.isShared}">--}%
%{--                                    <span class="la-js-editmode-container">--}%
%{--                                    <ui:remoteLink class="ui icon green button la-modern-button js-no-wait-wheel la-popup-tooltip la-delay"--}%
%{--                                                      controller="ajax"--}%
%{--                                                      action="toggleShare"--}%
%{--                                                      params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"documents", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?:  actionName]'--}%
%{--                                                      data-content="${message(code:'property.share.tooltip.on')}"--}%
%{--                                                      data-done=""--}%
%{--                                                      data-update="container-documents"--}%
%{--                                                      role="button">--}%
%{--                                        <i class="icon la-share la-js-editmode-icon"></i>--}%
%{--                                    </ui:remoteLink>--}%
%{--                                    </span>--}%
%{--                                </g:if>--}%
%{--                                <g:else>--}%
%{--                                    <ui:remoteLink class="ui icon blue button la-modern-button js-no-wait-wheel la-popup-tooltip la-delay js-open-confirm-modal"--}%
%{--                                                      controller="ajax"--}%
%{--                                                      action="toggleShare"--}%
%{--                                                      params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"documents", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?:  actionName]'--}%
%{--                                                      data-content="${message(code:'property.share.tooltip.off')}"--}%
%{--                                                      data-confirm-tokenMsg="${message(code: "confirm.dialog.share.element.member", args: [docctx.owner.title])}"--}%
%{--                                                      data-confirm-term-how="share"--}%
%{--                                                      data-done=""--}%
%{--                                                      data-update="container-documents"--}%
%{--                                                      role="button">--}%
%{--                                        <i class="la-share slash icon la-js-editmode-icon"></i>--}%
%{--                                    </ui:remoteLink>--}%
%{--                                </g:else>--}%
%{--                            </g:if>--}%
%{--                            <%-- STOP Third Button --%>--}%

                    </div>
                </div>
            </g:if>
        </g:each>
    </ui:card>
</g:if>
<g:if test="${sharedItems}">
    <ui:card message="license.documents.shared" class="documents la-js-hideable ${css_class}" editable="${editable}">
        <g:each in="${sharedItems}" var="docctx">
            <g:if test="${docctx.isDocAFile() && (docctx.status?.value!='Deleted')}">
                <div class="ui small feed content la-js-dont-hide-this-card">

                    <div class="ui grid summary">
                        <div class="eleven wide column">
                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}" />
                            <g:if test="${supportedMimeType}">
                                <a href="#documentPreview" data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code:'template.documents.missing')}</a>
                            </g:if>
                            <g:else>
                                ${docctx.owner.title ?: docctx.owner.filename ?: message(code:'template.documents.missing')}
                            </g:else>
                            <g:if test="${docctx.getDocType()}">
                                (${docctx.getDocType().getI10n("value")})
                            </g:if>

                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                        </div>

                        <div class="five wide right aligned column">
                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue button la-modern-button la-js-dont-hide-button" target="_blank"><i class="download icon"></i></g:link>

                            %{--
                            <g:if test="${docctx.owner.owner?.id == contextOrg.id}">
                                <laser:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="ui icon blue button la-modern-button" data-ui="modal"
                                        data-href="#modalEditDocument_${docctx.id}"
                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                <i class="pencil icon"></i></button>
                            </g:if>
                            --}%
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
