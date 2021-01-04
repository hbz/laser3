<%@page import="de.laser.helper.RDStore; de.laser.*; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection/>
<%
    boolean parentAtChild = false

    if(instance instanceof Subscription) {
        if(contextService.getOrg().id == instance.getConsortia()?.id && instance.instanceOf) {
            if(instance._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)
                parentAtChild = true
        }
    }
    else if(instance instanceof License) {
        if(contextService.getOrg().id == instance.getLicensingConsortium()?.id && instance.instanceOf) {
            parentAtChild = true
        }
    }
%>

<g:form id="delete_doc_form" url="${[controller:"${controllerName}" ,action:'deleteDocuments']}" method="post">

    <table class="ui celled la-table table license-documents">
        <thead>
            <tr>
                <%--<g:if test="${editable}"><th>${message(code:'default.select.label')}</th></g:if> : REMOVED BULK--%>
                <th>${message(code:'license.docs.table.title')}</th>
                <th>${message(code:'license.docs.table.fileName')}</th>
                <th>${message(code:'license.docs.table.type')}</th>
                <%--<th>${message(code:'org.docs.table.ownerOrg')}</th>--%>
                <g:if test="${controllerName == 'myInstitution'}">
                    <th>${message(code:'org.docs.table.targetBy')}</th>
                    <th>${message(code:'org.docs.table.shareConf')}</th>
                </g:if>
                <%--<g:elseif test="${controllerName == 'organisation'}">
                    <th>${message(code:'org.docs.table.targetFor')}</th>
                    <th>${message(code:'org.docs.table.shareConf')}</th>
                </g:elseif>--%>
                <th>${message(code:'default.actions.label')}</th>
            </tr>
        </thead>
        <tbody>
            <%
                Set documentSet = instance.documents
                if(instance instanceof Org && inContextOrg){
                    documentSet.addAll(orgDocumentService.getTargettedDocuments(instance))
                }
            %>
            <g:each in="${documentSet.sort{it?.owner?.title?.toLowerCase()}}" var="docctx">
                <%
                    boolean visible = false
                    boolean inOwnerOrg = false
                    boolean inTargetOrg = false

                    if(docctx.owner.owner?.id == contextService.getOrg().id)
                        inOwnerOrg = true
                    else if(contextService.getOrg().id == docctx.targetOrg?.id)
                        inTargetOrg = true
                    if(docctx.org) {
                        switch(docctx.shareConf) {
                            case RDStore.SHARE_CONF_UPLOADER_ORG: if(inOwnerOrg) visible = true
                                break
                            case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: if(inOwnerOrg || inTargetOrg) visible = true
                                break
                            case RDStore.SHARE_CONF_CONSORTIUM:
                            case RDStore.SHARE_CONF_ALL: visible = true //definition says that everyone with "access" to target org. How are such access roles defined and where?
                                break
                            default:
                                if(docctx.shareConf) log.debug(docctx.shareConf)
                                else visible = true
                                break
                        }
                    }
                    else if(inOwnerOrg || docctx.sharedFrom) {
                        visible = true
                    }
                    else {
                        if((parentAtChild && docctx.sharedFrom) || !parentAtChild && docctx.owner?.owner?.id == contextService.getOrg().id) {
                            visible = true
                        }
                    }
                %>
                <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && visible && docctx.status != RDStore.DOC_CTX_STATUS_DELETED)}">
                    <tr>
                        <th scope="row" class="la-th-column la-main-object" >
                            ${docctx.owner.title}
                        </th>
                        <td>
                            ${docctx.owner.filename}
                        </td>
                        <td>
                            ${docctx.owner?.type?.getI10n('value')}
                        </td>
                        <g:if test="${controllerName == 'myInstitution'}">
                            <td>
                                ${inTargetOrg ? docctx.owner?.owner?.sortname :  docctx.targetOrg?.sortname}
                            </td>
                        <%--
                            <td>
                                <g:if test="${docctx.org}">
                                    <g:link controller="organisation" action="show" params="[id:docctx.org.id]"><i class="university icon small"></i> ${docctx.org.name}</g:link>
                                </g:if>
                                <g:elseif test="${docctx.license}">
                                    <g:link controller="license" action="show" params="[id:docctx.license.id]"><i class="balance scale icon small"></i> ${docctx.license.reference}</g:link>
                                </g:elseif>
                                <g:elseif test="${docctx.subscription}">
                                    <g:link controller="subscription" action="show" params="[id:docctx.subscription.id]"><i class="folder open icon small"></i> ${docctx.subscription.name}</g:link>
                                </g:elseif>
                                <g:elseif test="${docctx.pkg}">
                                    <g:link controller="package" action="show" params="[id:docctx.pkg.id]"><i class="gift icon small"></i> ${docctx.pkg.name}</g:link>
                                </g:elseif>
                            </td>
                        --%>
                            <td>
                                ${docctx.shareConf?.getI10n("value")}
                            </td>
                        </g:if>
                        <td class="x">
                            <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">
                                <g:if test="${instance?.respondsTo('showUIShareButton')}">
                                    <g:if test="${docctx.sharedFrom}">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                            <i class="green alternate share icon"></i>
                                        </span>
                                    </g:if>
                                    <g:if test="${instance?.showUIShareButton()}">
                                        <g:if test="${docctx.isShared}">
                                            <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                                <g:link controller="ajax" action="toggleShare" class="ui icon button green"
                                                        params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(instance), reload:true, ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                                    <i class="alternate share icon"></i>
                                                </g:link>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
                                                <g:link controller="ajax" action="toggleShare" class="ui icon button"
                                                        params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(instance), reload:true, ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                                    <i class="alternate share icon"></i>
                                                </g:link>
                                            </span>
                                        </g:else>
                                    </g:if>
                                </g:if>
                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon button"><i class="download icon"></i></g:link>
                                <g:if test="${accessService.checkMinUserOrgRole(user,docctx.owner.owner,"INST_EDITOR") && inOwnerOrg}">
                                    <button type="button" class="ui icon button la-popup-tooltip la-delay" data-semui="modal" href="#modalEditDocument_${docctx.id}" data-content="${message(code:"template.documents.edit")}"><i class="pencil icon"></i></button>
                                </g:if>
                                <g:if test="${!docctx.sharedFrom && accessService.checkMinUserOrgRole(user,docctx.owner.owner,"INST_EDITOR") && inOwnerOrg}">
                                    <g:link controller="${controllerName}" action="deleteDocuments" class="ui icon negative button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                            data-confirm-term-how="delete"
                                            params='[instanceId:"${instance.id}", deleteId:"${docctx.id}", redirectAction:"${actionName}"]'>
                                        <i class="trash alternate icon"></i>
                                    </g:link>
                                </g:if>
                            </g:if>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </tbody>
    </table>
</g:form>
<%-- a form within a form is not permitted --%>
<g:each in="${instance.documents}" var="docctx">
    <g:render template="/templates/documents/modal" model="${[ownobj: instance, owntp: owntp, docctx: docctx, doc: docctx.owner]}"/>
</g:each>