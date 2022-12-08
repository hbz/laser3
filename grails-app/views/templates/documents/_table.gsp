<%@page import="de.laser.workflow.WfWorkflow; de.laser.storage.RDConstants; org.apache.commons.lang3.RandomStringUtils; de.laser.storage.RDStore; de.laser.*; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection/>
<%
    boolean parentAtChild = false

    //for ERMS-2393/3933: this is a request parameter for the DMS rights management interface
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

    List<String> colWide = (controllerName == 'myInstitution' && actionName != 'subscriptionsManagement') ? ['one', /*'one',*/ 'five', 'two', 'three', 'three', 'two'] : ['one',  'seven', 'three', 'three', 'two']
    int cwCounter = 0
    int trCounter = 1

    List<DocContext> securityWorkaroundList = []
    String randomId = RandomStringUtils.randomAlphanumeric(8)
%>

    <table class="ui celled la-js-responsive-table la-table table documents-table-${randomId}">
        <thead>
            <tr>
                <g:if test="${!(controllerName == 'myInstitution' && actionName == 'subscriptionsManagement')}">
                    <th scope="col" class="${colWide[cwCounter++]} center aligned wide" rowspan="2">#</th>
                </g:if>

%{--                <g:if test="${controllerName == 'myInstitution'}">--}%
%{--                    <th scope="col" class="${colWide[cwCounter++]} wide" rowspan="2">${message(code:'sidewide.number')}</th>--}%
%{--                </g:if>--}%
                <th scope="col" class="${colWide[cwCounter++]} wide la-smaller-table-head">${message(code:'template.addDocument.name')}</th>
                <th scope="col" class="${colWide[cwCounter++]} wide" rowspan="2">${message(code:'license.docs.table.type')}</th>
                <th scope="col" class="${colWide[cwCounter++]} wide" rowspan="2">${message(code:'template.addDocument.confidentiality')}</th>
                <%--<th>${message(code:'org.docs.table.ownerOrg')}</th>--%>
                <g:if test="${controllerName == 'myInstitution' && actionName != 'subscriptionsManagement'}">
                    <th scope="col" class="${colWide[cwCounter++]} wide la-smaller-table-head">${message(code:'org.docs.table.shareConf')}</th>
                </g:if>
                <%--<g:elseif test="${controllerName == 'organisation'}">
                    <th>${message(code:'org.docs.table.targetFor')}</th>
                    <th>${message(code:'org.docs.table.shareConf')}</th>
                </g:elseif>--%>
                <th scope="col" class="${colWide[cwCounter]} wide" rowspan="2">${message(code:'default.actions.label')}</th>
            </tr>
            <tr>
                <th scope="col" class="la-smaller-table-head">${message(code:'license.docs.table.fileName')}</th>
                <g:if test="${controllerName == 'myInstitution' && actionName != 'subscriptionsManagement'}">
                    <th scope="col" class="la-smaller-table-head">${message(code:'org.docs.table.targetBy')}</th>
                </g:if>
            </tr>
        </thead>
        <tbody>
            <%-- Those are the rights settings the DMS needs to cope with. See the following documentation which is currently a requirement specification, too, and serves as base for ERMS-2393 --%>
            <%
                Set documentSet = instance.documents
                if(instance instanceof Org && inContextOrg) {
                    //get all documents which has been attached to this org
                    documentSet.addAll(docstoreService.getTargettedDocuments(instance))
                }
            %>
            <g:each in="${documentSet.sort{it?.owner?.title?.toLowerCase()}}" var="docctx">
                <%
                    boolean visible = false //is the document visible?
                    boolean inOwnerOrg = false //are we owners of the document?
                    boolean inTargetOrg = false //are we in the org to which a document is attached?

                    //these settings count only if we view an org's documents
                    if(docctx.owner.owner?.id == contextService.getOrg().id)
                        inOwnerOrg = true
                    else if(contextService.getOrg().id == docctx.targetOrg?.id)
                        inTargetOrg = true
                    if(docctx.org) {
                        switch(docctx.shareConf) {
                            case RDStore.SHARE_CONF_UPLOADER_ORG: if(inOwnerOrg) visible = true //visible only for thes users of org which uploaded the document
                                break
                            case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: if(inOwnerOrg || inTargetOrg) visible = true //the owner org and the target org may see the document i.e. the document has been shared with the target org
                                break
                            case [ RDStore.SHARE_CONF_CONSORTIUM, RDStore.SHARE_CONF_ALL ]: visible = true //definition says that everyone with "access" to target org. How are such access roles defined and where?
                                break
                            default:
                                //fallback: documents are visible if share configuration is missing or obsolete
                                if(docctx.shareConf) println docctx.shareConf
                                else visible = true
                                break
                        }
                    }
                    else if(inOwnerOrg || docctx.sharedFrom) {
                        //other owner objects than orgs - in particular licenses and subscriptions: visibility is set if the owner org visits the owner object or sharing is activated
                        visible = true
                    }
                    else {
                        /*
                            this is a special clause for consortia member objects:
                            - the consortium can see the documents it shared itself (directly attached documents are catched by the clause above because inOwnerOrg is true)
                            - the single user member uploaded a document which should only be visible by the uploading member itself
                         */
                        if((parentAtChild && docctx.sharedFrom) || !parentAtChild && docctx.owner?.owner?.id == contextService.getOrg().id) {
                            visible = true
                        }
                    }
                %>
                <g:if test="${docctx.isDocAFile() && visible && (docctx.status != RDStore.DOC_CTX_STATUS_DELETED)}">
                    <tr>
                        <g:if test="${!(controllerName == 'myInstitution' && actionName == 'subscriptionsManagement')}">
                            <td class="center aligned">
                                <g:if test="${docctx.owner.owner.id == contextService.getOrg().id && !docctx.sharedFrom}">
                                    <g:set var="blukEnabled" value="${true}" />
                                    <g:checkBox id="bulk_doc_${docctx.owner.id}" name="bulk_doc" value="${docctx.owner.id}" checked="false"/>
                                </g:if>
                            </td>
                        </g:if>
%{--                        <g:if test="${controllerName == 'myInstitution'}">--}%
%{--                            <td class="center aligned">--}%
%{--                                ${trCounter++}--}%
%{--                            </td>--}%
%{--                        </g:if>--}%
                        <td>
                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}" />
                            <strong>
                                <g:if test="${docctx.isDocAFile() && visible && supportedMimeType}">
                                    <a href="#documentPreview" data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title}</a>
                                </g:if>
                                <g:else>
                                    ${docctx.owner.title}
                                </g:else>
                            </strong>
                            <%
                                List<WfWorkflow> usedByWorkflowsList = []
                                WfWorkflow.getWorkflowsByObject( instance ).each { wf ->
                                    if (docctx.isUsedByWorkflow(wf)) {
                                        usedByWorkflowsList.add(wf)
                                    }
                                }
                                if (usedByWorkflowsList) { print '&nbsp;' }
                            %>
                            <g:each in="${usedByWorkflowsList}" var="ctxWorkflow">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="Wird referenziert in: ${ctxWorkflow.title}">
                                    <i class="exclamation circle brown icon"></i>
                                </span>
                            </g:each>
                            <br />
                            ${docctx.owner.filename}
                        </td>
                        <td>
                            ${docctx.getDocType()?.getI10n('value')}
                        </td>
                        <td>
                            <ui:documentIcon doc="${docctx.owner}" showText="true" showTooltip="false"/>
                        </td>
                        <g:if test="${controllerName == 'myInstitution' && actionName != 'subscriptionsManagement'}">
                            <td>
                                ${docctx.shareConf?.getI10n("value")}
                                <br />
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
                        </g:if>
                        <td class="x">
                            <g:if test="${docctx.isDocAFile()}">
                                <g:if test="${instance?.respondsTo('showUIShareButton')}">
                                    <g:if test="${docctx.sharedFrom}">
                                        <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                            <i class="grey alternate share icon"></i>
                                        </span>
                                    </g:if>
                                    <g:if test="${instance?.showUIShareButton()}">
                                        <g:if test="${docctx.isShared}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                                <g:link controller="ajax" action="toggleShare" class="ui icon button green la-modern-button"
                                                        params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(docctx), reload:true, ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                                    <i class="alternate share icon"></i>
                                                </g:link>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
                                                <g:link controller="ajax" action="toggleShare" class="ui icon button blue la-modern-button"
                                                        params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(docctx), reload:true, ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                                    <i class="la-share slash icon"></i>
                                                </g:link>
                                            </span>
                                        </g:else>
                                    </g:if>
                                </g:if>
                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue button la-modern-button" target="_blank"><i class="download icon"></i></g:link>
                                %{-- todo: !docctx.sharedFrom --}%
                                <g:if test="${accessService.checkMinUserOrgRole(user,docctx.owner.owner,"INST_EDITOR") && inOwnerOrg && !docctx.sharedFrom}">
                                    <button type="button" class="ui icon blue button la-modern-button la-popup-tooltip la-delay" data-ui="modal" data-href="#modalEditDocument_${docctx.id}" data-content="${message(code:"template.documents.edit")}"><i class="pencil icon"></i></button>
                                    <%
                                        securityWorkaroundList.add(docctx as DocContext)
                                    %>
                                </g:if>
                                <g:if test="${!docctx.sharedFrom && !docctx.isShared && accessService.checkMinUserOrgRole(user,docctx.owner.owner,"INST_EDITOR") && inOwnerOrg}">
                                    <g:link controller="${controllerName}" action="deleteDocuments" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                            data-confirm-term-how="delete"
                                            params='[instanceId:"${instance.id}", deleteId:"${docctx.id}", redirectAction:"${actionName}"]'
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="trash alternate outline icon"></i>
                                    </g:link>
                                </g:if>
                            </g:if>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </tbody>
        <g:if test="${blukEnabled}">
            <tfoot>
                <tr>
                    <td class="center aligned">
                        <g:checkBox name="bulk_selectionToggler" id="bulk_selectionToggler" checked="false"/>
                    </td>
%{--                    <g:if test="${controllerName == 'myInstitution'}">--}%
%{--                        <td></td>--}%
%{--                    </g:if>--}%
                    <td></td>
                    <td></td>
                    <td>
                        <form id="bulk_form" class="ui form" method="POST">
                            <ui:select class="ui dropdown search"
                                       name="bulk_docConf" id="bulk_docConf"
                                       from="${RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_CONFIDENTIALITY)}"
                                       optionKey="id"
                                       optionValue="value"
                            />
                            <input name="bulk_docIdList" id="bulk_docIdList" type="hidden" value="" />
                            <input name="bulk_op" type="hidden" value="${RDConstants.DOCUMENT_CONFIDENTIALITY}" />
                        </form>
                    </td>
                    <g:if test="${controllerName == 'myInstitution'}">
                        <td></td>
                    </g:if>
                    <td>
                        <button id="bulk_submit" class="ui button primary">Übernehmen</button>
                    </td>
                </tr>
            </tfoot>
        </g:if>
    </table>

<laser:script file="${this.getGroovyPageFileName()}">
    docs.init('.documents-table-${randomId}')

    <g:if test="${!(controllerName == 'myInstitution' && actionName == 'subscriptionsManagement')}">
    $('#bulk_submit').click (function () {
        let ids = []
        $('input[name=bulk_doc]:checked').each( function (i, e) {
            ids.push($(e).attr ('value'))
        })
        $('#bulk_docIdList').attr ('value', ids.join (','))
        $('#bulk_form').submit()
    })

    $('#bulk_selectionToggler').click (function () {
        if ($(this).prop ('checked')) {
            $('input[name=bulk_doc]').prop ('checked', true)
        } else {
            $('input[name=bulk_doc]').prop ('checked', false)
        }
    })
    </g:if>
</laser:script>

<%-- a form within a form is not permitted --%>
<g:each in="${securityWorkaroundList}" var="docctx">
    <laser:render template="/templates/documents/modal" model="${[ownobj: instance, owntp: owntp, docctx: docctx, doc: docctx.owner]}"/>
</g:each>