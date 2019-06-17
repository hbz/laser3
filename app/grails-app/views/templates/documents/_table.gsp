<%@page import="de.laser.helper.RDStore; com.k_int.kbplus.*;" %>
<laser:serviceInjection/>
<g:form id="delete_doc_form" url="${[controller:"${controllerName}" ,action:'deleteDocuments']}" method="post">

    <table class="ui celled la-table table license-documents">
        <thead>
            <tr>
                <%--<g:if test="${editable}"><th>${message(code:'license.docs.table.select', default:'Select')}</th></g:if> : REMOVED BULK--%>
                <th>${message(code:'license.docs.table.title', default:'Title')}</th>
                <th>${message(code:'license.docs.table.fileName', default:'File Name')}</th>
                <th>${message(code:'license.docs.table.type', default:'Type')}</th>
                <th>${message(code:'org.docs.table.target')}</th>
                <%--<th>${message(code:'org.docs.table.ownerOrg')}</th>--%>
                <g:if test="${controllerName in ['myInstitution','organisation']}">
                    <th>${message(code:'org.docs.table.shareConf')}</th>
                </g:if>
                <th>${message(code:'default.actions', default:'Actions')}</th>
            </tr>
        </thead>
        <tbody>
            <%
                Set documentSet = instance.documents
                if(instance instanceof Org && instance.id == contextService.org.id){
                    documentSet.addAll(orgDocumentService.getTargettedDocuments(instance))
                }
            %>
            <g:each in="${documentSet}" var="docctx">
                <%
                    boolean visible = false
                    boolean inOwnerOrg = false
                    boolean inTargetOrg = false
                    boolean isCreator = false

                    if(docctx.owner.owner?.id == contextService.org.id)
                        inOwnerOrg = true
                    else if(contextService.org.id == docctx.targetOrg?.id)
                        inTargetOrg = true
                    if(docctx.owner.creator?.id == user.id)
                        isCreator = true
                    if(docctx.org) {
                        switch(docctx.shareConf) {
                            case RDStore.SHARE_CONF_CREATOR: if(isCreator) visible = true
                                break
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
                    else visible = true
                %>
                <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted') && visible)}">
                    <tr>
                        <td>
                            ${docctx.owner.title}
                        </td>
                        <td>
                            ${docctx.owner.filename}
                        </td>
                        <td>
                            ${docctx.owner?.type?.getI10n('value')}
                        </td>
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
                        <g:if test="${instance instanceof Org}">
                            <td>
                                ${docctx.shareConf?.getI10n("value")}
                            </td>
                        </g:if>
                        <td class="x">
                            <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">
                                <g:if test="${instance.respondsTo('showUIShareButton')}">
                                    <g:if test="${docctx.sharedFrom}">
                                        <span data-tooltip="${message(code:'property.share.tooltip.on')}">
                                            <i class="green alternate share icon"></i>
                                        </span>
                                    </g:if>
                                    <g:if test="${instance.showUIShareButton()}">
                                        <g:if test="${docctx.isShared}">
                                            <span data-position="top right" data-tooltip="${message(code:'property.share.tooltip.on')}">
                                                <g:link controller="ajax" action="toggleShare" class="ui icon button green"
                                                        params='[owner:"${instance.class.name}:${instance.id}", sharedObject:"${docctx.class.name}:${docctx.id}", reload:true]'>
                                                    <i class="alternate share icon"></i>
                                                </g:link>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <span data-position="top right" data-tooltip="${message(code:'property.share.tooltip.off')}">
                                                <g:link controller="ajax" action="toggleShare" class="ui icon button"
                                                        params='[owner:"${instance.class.name}:${instance.id}", sharedObject:"${docctx.class.name}:${docctx.id}", reload:true]'>
                                                    <i class="alternate share icon"></i>
                                                </g:link>
                                            </span>
                                        </g:else>
                                    </g:if>
                                </g:if>
                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon button"><i class="download icon"></i></g:link>
                                <g:if test="${editable && !docctx.sharedFrom && inOwnerOrg}">
                                    <button type="button" class="ui icon button" data-semui="modal" href="#modalEditDocument_${docctx.id}" data-tooltip="${message(code:"template.documents.edit")}"><i class="pencil icon"></i></button>
                                    <g:link controller="${controllerName}" action="deleteDocuments" class="ui icon negative button js-open-confirm-modal"
                                            data-confirm-term-what="document" data-confirm-term-what-detail="${docctx.owner.title}" data-confirm-term-how="delete"
                                            params='[instanceId:"${instance.id}", deleteId:"${docctx.id}", redirectAction:"${redirect}"]'>
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