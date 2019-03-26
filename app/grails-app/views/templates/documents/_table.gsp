<%@page import="de.laser.helper.RDStore; com.k_int.kbplus.*;de.laser.AccessService" %>
<g:form id="delete_doc_form" url="${[controller:"${controllerName}" ,action:'deleteDocuments']}" method="post">

    <table class="ui celled la-table table license-documents">
        <thead>
            <tr>
                <%--<g:if test="${editable}"><th>${message(code:'license.docs.table.select', default:'Select')}</th></g:if> : REMOVED BULK--%>
                <th>${message(code:'license.docs.table.title', default:'Title')}</th>
                <th>${message(code:'license.docs.table.fileName', default:'File Name')}</th>
                <th>${message(code:'license.docs.table.creator', default:'Creator')}</th>
                <th>${message(code:'license.docs.table.type', default:'Type')}</th>
                <th>${message(code:'org.docs.table.target')}</th>
                <%--<th>${message(code:'org.docs.table.ownerOrg')}</th>--%>
                <th>${message(code:'org.docs.table.shareConf')}</th>
                <th>${message(code:'default.actions', default:'Actions')}</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${instance.documents}" var="docctx">
                <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
                    <tr>
                        <td>
                            ${docctx.owner.title}
                        </td>
                        <td>
                            ${docctx.owner.filename}
                        </td>
                        <td>
                            <g:if test="${docctx?.owner?.owner?.id == org?.id || docctx.owner.creator.id == user.id}">
                                ${docctx.owner.creator}
                            </g:if>
                        </td>
                        <td>
                            ${docctx.owner?.type?.getI10n('value')}
                        </td>
                        <td>
                            <g:if test="${docctx.org}">
                                <g:link controller="organisations" action="show" params="[id:docctx.org.id]"><i class="university icon small"></i> ${docctx.org.name}</g:link>
                            </g:if>
                            <g:elseif test="${docctx.license}">
                                <g:link controller="licenseDetails" action="show" params="[id:docctx.license.id]"><i class="balance scale icon small"></i> ${docctx.license.name}</g:link>
                            </g:elseif>
                            <g:elseif test="${docctx.subscription}">
                                <g:link controller="subscriptionDetails" action="show" params="[id:docctx.subscription.id]"><i class="folder open icon small"></i> ${docctx.subscription.name}</g:link>
                            </g:elseif>
                            <g:elseif test="${docctx.pkg}">
                                <g:link controller="packageDetails" action="show" params="[id:docctx.pkg.id]"><i class="gift icon small"></i> ${docctx.pkg.name}</g:link>
                            </g:elseif>
                        </td>
                        <td>
                            ${docctx.shareConf.getI10n("value")}
                        </td>
                        <td class="x">
                            <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">
                                <g:if test="${!(instance instanceof Org)}">
                                    <g:if test="${docctx.sharedFrom}">
                                        [ Wird geteilt ]
                                    </g:if>
                                    <g:if test="${instance.respondsTo('showUIShareButton') && showUIShareButton()}">
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
                                <button type="button" class="ui icon button" data-semui="modal" href="#modalEditDocument_${docctx.id}" data-tooltip="${message(code:"template.documents.edit")}" params="[id:docctx.id]"><i class="pencil icon"></i></button>
                                <g:link conter="${controllerName}" action="deleteDocuments" class="ui icon negative button js-open-confirm-modal"
                                        data-confirm-term-what="document" data-confirm-term-what-detail="${docctx.owner.title}" data-confirm-term-how="delete"
                                        params='[instanceId:"${instance.id}", deleteId:"${docctx.id}", redirectAction:"${redirect}"]'>
                                    <i class="trash alternate icon"></i>
                                </g:link>
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
    <%

        if(instance instanceof LinkedHashMap) {
            if(docctx.org)
                ownerObj = docctx.org
            else if(docctx.license)
                ownerObj = docctx.license
            else if(docctx.subscription)
                ownerObj = docctx.subscription
            else if(docctx.pkg)
                ownerObj = docctx.pkg
        }
        else ownerObj = instance
    %>
    <g:render template="/templates/documents/modal" model="${[ownobj: ownerObj, owntp: owntp, docctx: docctx, doc: docctx.owner]}"/>
</g:each>

<r:script>
    <%--
    function showHideTargetableRefdata() {
        console.log($(org).val());
        if($(org).val().length === 0) {
            $("[data-value='com.k_int.kbplus.RefdataValue:${RDStore.SHARE_CONF_UPLOADER_AND_TARGET.id}']").hide();
        }
        else {
            $("[data-value='com.k_int.kbplus.RefdataValue:${RDStore.SHARE_CONF_UPLOADER_AND_TARGET.id}']").show();
        }
    }
    --%>

    function toggleTarget() {
        if($("#hasTarget")[0].checked)
            $("#target").show();
        else
            $("#target").hide();
    }
</r:script>