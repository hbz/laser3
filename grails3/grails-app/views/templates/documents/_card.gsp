<%@ page import="de.laser.helper.AjaxUtils; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.*; de.laser.helper.RDStore;" %>
<laser:serviceInjection/>
<%
    List<DocContext> baseItems = []
    List<DocContext> sharedItems = []
    Org contextOrg = contextOrg ?: contextService.getOrg()
    String documentMessage
    switch(ownobj.class.name) {
        case Org.class.name: documentMessage = "menu.my.documents"
            editable = accessService.checkMinUserOrgRole(contextService.getUser(), contextOrg, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
            break
        default: documentMessage = "license.documents"
            break
    }

    boolean editable2 = accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")
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
                case RDStore.SHARE_CONF_CONSORTIUM:
                case RDStore.SHARE_CONF_ALL: visible = true //definition says that everyone with "access" to target org. How are such access roles defined and where?
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
<g:if test="${accessService.checkPerm("ORG_INST,ORG_CONSORTIUM")}">
    <semui:card message="${documentMessage}" class="documents la-js-hideable ${css_class}" href="#modalCreateDocument" editable="${editable || editable2}">
        <g:each in="${baseItems}" var="docctx">
            <g:if test="${(( docctx.owner?.contentType==Doc.CONTENT_TYPE_FILE ) && ( docctx.status?.value!='Deleted'))}">
                <div class="ui small feed content la-js-dont-hide-this-card">
                    <div class="ui grid summary">
                        <div class="eight wide column la-column-right-lessPadding">
                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel la-break-all">
                                <g:if test="${docctx.owner?.title}">
                                    ${docctx.owner.title}
                                </g:if>
                                <g:elseif test="${docctx.owner?.filename}">
                                    ${docctx.owner?.filename}
                                </g:elseif>
                                <g:else>
                                    ${message(code:'template.documents.missing')}
                                </g:else>
                            </g:link>(${docctx.owner?.type?.getI10n("value")})
                        </div>
                        <div class="right aligned eight wide column la-column-left-lessPadding">
                            <g:if test="${docctx.owner.owner?.id == contextOrg.id}">
                                <%-- START First Button --%>
                                <g:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="ui icon blue button la-modern-button editable-cancel"
                                        data-semui="modal"
                                        data-href="#modalEditDocument_${docctx.id}"
                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                    <i class="pencil icon"></i></button>
                                <%-- STOP First Small Column --%>
                                <g:if test="${!docctx.isShared}">
                                <%-- START Second Button --%>
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
                                    <%-- Hidden Fake Button To hold the other Botton in Place --%>
                                    <div class="ui icon mini button la-hidden">
                                        <i class="coffe icon"></i>
                                    </div>
                                </g:else>
                                <%-- STOP Second Button --%>
                            </g:if>
                            <g:else>
                                <%-- Hidden Fake Button To hold the other Botton in Place --%>
                                <div class="ui icon mini button la-hidden">
                                    <i class="coffe icon"></i>
                                </div>

                                <%-- Hidden Fake Button To hold the other Botton in Place --%>
                                <div class="ui icon mini button la-hidden">
                                    <i class="coffe icon"></i>
                                </div>
                            </g:else>
                            <%-- START Third Button --%>
                            <g:if test="${!(ownobj instanceof Org) && ownobj?.showUIShareButton() && accessService.checkMinUserOrgRole(contextService.getUser(), docctx.owner.owner, "INST_EDITOR")}">
                                <g:if test="${docctx?.isShared}">
                                    <laser:remoteLink class="ui icon green button la-modern-button js-no-wait-wheel la-popup-tooltip la-delay"
                                                      controller="ajax"
                                                      action="toggleShare"
                                                      params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"documents", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?:  actionName]'
                                                      data-content="${message(code:'property.share.tooltip.on')}"
                                                      data-done=""
                                                      data-update="container-documents"
                                                      role="button"
                                    >
                                        <i class="icon la-share la-js-editmode-icon"></i>
                                    </laser:remoteLink>
                                </g:if>
                                <g:else>
                                    <laser:remoteLink class="ui icon blue button la-modern-button js-no-wait-wheel la-popup-tooltip la-delay js-open-confirm-modal"
                                                      controller="ajax"
                                                      action="toggleShare"
                                                      params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"documents", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?:  actionName]'
                                                      data-content="${message(code:'property.share.tooltip.off')}"
                                                      data-confirm-tokenMsg="${message(code: "confirm.dialog.share.element.member", args: [docctx.owner.title])}"
                                                      data-confirm-term-how="share"
                                                      data-done=""
                                                      data-update="container-documents"
                                                      role="button"
                                    >
                                        <i class="la-share slash icon la-js-editmode-icon"></i>
                                    </laser:remoteLink>
                                </g:else>
                            </g:if>
                        </div>
                    </div>
                </div>
            </g:if>
        </g:each>
    </semui:card>
</g:if>
<g:if test="${sharedItems}">
    <semui:card message="license.documents.shared" class="documents la-js-hideable ${css_class}" editable="${editable}">
        <g:each in="${sharedItems}" var="docctx">
            <g:if test="${((docctx.owner?.contentType==Doc.CONTENT_TYPE_FILE) && (docctx.status?.value!='Deleted'))}">
                <div class="ui small feed content la-js-dont-hide-this-card">

                    <div class="ui grid summary">
                        <div class="twelve wide column">
                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel">
                                <g:if test="${docctx.owner?.title}">
                                    ${docctx.owner.title}
                                </g:if>
                                <g:elseif test="${docctx.owner?.filename}">
                                    ${docctx.owner.filename}
                                </g:elseif>
                                <g:else>
                                    ${message(code:'template.documents.missing')}
                                </g:else>

                            </g:link>(${docctx.owner?.type?.getI10n("value")})
                        </div>
                        <g:if test="${docctx.owner.owner?.id == contextOrg.id}">
                            <div class="two wide column">
                                <g:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="ui icon blue button la-modern-button editable-cancel" data-semui="modal"
                                        data-href="#modalEditDocument_${docctx.id}"
                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                <i class="pencil icon"></i></button>
                            </div>
                        </g:if>
                    </div>
                </div>
            </g:if>

        </g:each>
    </semui:card>
</g:if>

