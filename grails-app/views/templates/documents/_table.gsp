<%@page import="de.laser.utils.RandomUtils; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.*; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection/>
<%
    boolean parentAtChild = false

    //for ERMS-2393/3933: this is a request parameter for the DMS rights management interface
    if(instance instanceof Subscription) {
        if(contextService.getOrg().id == instance.getConsortium()?.id && instance.instanceOf) {
            if(instance._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)
                parentAtChild = true
        }
    }
    else if(instance instanceof License) {
        if(contextService.getOrg().id == instance.getLicensingConsortium()?.id && instance.instanceOf) {
            parentAtChild = true
        }
    }

    List<String> colWide = (controllerName == 'myInstitution' && actionName != 'subscriptionsManagement') ?  ['one', /*'one',*/ 'five', 'two', 'three', 'three', 'two'] : ['one',  'seven', 'three', 'three', 'two']
    if (controllerName == 'subscription' && actionName == 'membersSubscriptionsManagement') {
        colWide = ['nine', 'three', 'three', 'two']
    }

    int cwCounter = 0
    int trCounter = 1

    List<DocContext> securityWorkaroundList = []
    String randomId = RandomUtils.getHtmlID()
%>

    <table class="ui celled la-js-responsive-table la-table table documents-table-${randomId}">
        <thead>
            <tr>
                <g:if test="${!(controllerName == 'subscription' && actionName == 'membersSubscriptionsManagement')}">
                    <th scope="col" class="${colWide[cwCounter++]} center aligned wide" rowspan="2">#</th>
                </g:if>
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
                <th scope="col" class="${colWide[cwCounter]} wide center aligned" rowspan="2">
                    <ui:optionsIcon />
                </th>
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
                boolean showBulkDelete = false
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
                    if(docctx.targetOrg)
                        inTargetOrg = contextService.getOrg().id == docctx.targetOrg.id
                    else if(docctx.subscription)
                        inTargetOrg = contextService.getOrg().id == docctx.subscription.getSubscriberRespConsortia().id
                    else if(docctx.license)
                        inTargetOrg = contextService.getOrg().id in docctx.license.getDerivedLicensees().id
                    switch(docctx.shareConf) {
                        case RDStore.SHARE_CONF_UPLOADER_ORG: if(inOwnerOrg) visible = true //visible only for thes users of org which uploaded the document
                            break
                        case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: if(inOwnerOrg || inTargetOrg) visible = true //the owner org and the target org may see the document i.e. the document has been shared with the target org
                            break
                        case RDStore.SHARE_CONF_ALL: visible = true //definition says that everyone with "access" to target org
                            break
                        default:
                            if(docctx.org) {
                                //fallback: documents are visible if share configuration is missing or obsolete
                                visible = inOwnerOrg
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
                            break
                    }
                %>
                <g:if test="${docctx.isDocAFile() && visible && (docctx.status != RDStore.DOC_CTX_STATUS_DELETED)}">
                    <% showBulkDelete = true %>
                    <tr>
                        <g:if test="${!(controllerName == 'subscription' && actionName == 'membersSubscriptionsManagement')}">
                            <td class="center aligned">
                                <g:if test="${docctx.owner.owner.id == contextService.getOrg().id && !docctx.sharedFrom}">
                                    <g:if test="${editable}">
                                        <g:checkBox id="bulk_doc_${docctx.owner.id}" name="bulk_doc" value="${docctx.owner.id}" checked="false"/>
                                    </g:if>
                                </g:if>
                            </td>
                        </g:if>
                        <td>
                            <ui:documentShareConfigIcon docctx="${docctx}"/>
                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}" />
                            <strong>
                                <g:if test="${docctx.isDocAFile() && visible && supportedMimeType}">
                                    <a href="#documentPreview" data-dctx="${docctx.id}">${docctx.owner.title}</a>
                                </g:if>
                                <g:else>
                                    ${docctx.owner.title}
                                </g:else>
                            </strong>
                            <br />
                            ${docctx.owner.filename}
                            <g:if test="${docctx.owner.ckey}">
                                <span class="la-long-tooltip la-popup-tooltip" data-content="${message(code:'license.docs.table.encrypted')}">
                                    <i class="icon award pink"></i>
                                </span>
                            </g:if>
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
                        %{--
                            <td>
                                <g:if test="${docctx.org}">
                                    <g:link controller="organisation" action="show" params="[id:docctx.org.id]"><i class="${Icon.ORG} small"></i> ${docctx.org.name}</g:link>
                                </g:if>
                                <g:elseif test="${docctx.license}">
                                    <g:link controller="license" action="show" params="[id:docctx.license.id]"><i class="${Icon.LICENSE} small"></i> ${docctx.license.reference}</g:link>
                                </g:elseif>
                                <g:elseif test="${docctx.subscription}">
                                    <g:link controller="subscription" action="show" params="[id:docctx.subscription.id]"><i class="folder open icon small"></i> ${docctx.subscription.name}</g:link>
                                </g:elseif>
                                <g:elseif test="${docctx.pkg}">
                                    <g:link controller="package" action="show" params="[id:docctx.pkg.id]"><i class="${Icon.PACKAGE} small"></i> ${docctx.pkg.name}</g:link>
                                </g:elseif>
                            </td>
                        --}%
                        </g:if>
                        <td class="x">
                            <g:if test="${docctx.isDocAFile()}">
                                <g:if test="${instance?.respondsTo('showUIShareButton')}">
                                    <g:if test="${docctx.sharedFrom}">
                                        <span class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.on')}">
                                            <i class="${Icon.SIG.SHARED_OBJECT_ON} grey"></i>
                                        </span>
                                    </g:if>
                                    <g:if test="${instance?.showUIShareButton()}">
                                        <g:if test="${editable}">
                                            <g:if test="${docctx.isShared}">
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.on')}">
                                                    <g:link controller="ajax" action="toggleShare" class="${Btn.MODERN.POSITIVE}"
                                                            params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(docctx), reload:true, ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                                        <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                                    </g:link>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.off')}">
                                                    <g:link controller="ajax" action="toggleShare" class="${Btn.MODERN.SIMPLE}"
                                                            params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(docctx), reload:true, ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                                        <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                                    </g:link>
                                                </span>
                                            </g:else>
                                        </g:if>
                                        <g:else>
                                            %{-- TODO: ERMS-6253 - show shared icon --}%
                                            <g:if test="${docctx.isShared}">
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.on')}">
                                                    <span class="${Btn.MODERN.SIMPLE} disabled">
                                                        <i class="${Icon.SIG.SHARED_OBJECT_ON} green"></i>
                                                    </span>
                                                </span>
                                            </g:if>
                                        </g:else>
                                    </g:if>
                                </g:if>
                                <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}" class="${Btn.MODERN.SIMPLE}" target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                                %{-- todo: !docctx.sharedFrom --}%
                                <g:if test="${userService.hasFormalAffiliation(docctx.owner.owner, 'INST_EDITOR') && inOwnerOrg && !docctx.sharedFrom}">
                                    <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-ui="modal" data-href="#modalEditDocument_${docctx.id}" data-content="${message(code:"template.documents.edit")}"><i class="${Icon.CMD.EDIT}"></i></button>
                                    <%
                                        securityWorkaroundList.add(docctx as DocContext)
                                    %>
                                </g:if>
                                <g:if test="${!docctx.sharedFrom && !docctx.isShared && userService.hasFormalAffiliation(docctx.owner.owner, 'INST_EDITOR') && inOwnerOrg}">
                                    <%
                                        String redirectId = actionName == 'membersSubscriptionsManagement' && instance.instanceOf ? instance.instanceOf.id : instance.id
                                    %>
                                    <g:set var="linkParams" value="${[instanceId:"${redirectId}", deleteId:"${docctx.id}", redirectController:"${controllerName}", redirectAction:"${actionName}"]}" />
%{--                                    params='[instanceId:"${redirectId}", deleteId:"${docctx.id}", redirectController:"${controllerName}", redirectAction:"${actionName}", redirectTab: "${params.tab}"]'--}%
                                    <g:link controller="document" action="deleteDocument" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                            data-confirm-term-how="delete"
                                            params="${params.tab ? linkParams << [redirectTab: "${params.tab}"] : linkParams}"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.CMD.DELETE}"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <div class="${Btn.ICON.SIMPLE} la-hidden">
                                        <icon:placeholder /><%-- Hidden Fake Button --%>
                                    </div>
                                </g:else>
                            </g:if>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </tbody>
        <g:if test="${!(controllerName == 'subscription' && actionName == 'membersSubscriptionsManagement')}">
        <g:if test="${editable && documentSet && showBulkDelete}">
            <tfoot>
                <tr>
                    <td class="center aligned">
%{--                        <g:if test="${!(controllerName == 'myInstitution' && actionName == 'subscriptionsManagement')}">--}%
                            <g:checkBox name="bulk_selectionToggler" id="bulk_selectionToggler_${randomId}" checked="false"/>
%{--                        </g:if>--}%
                    </td>
                    <td></td>
                    <td></td>
                    <td>
                        <form id="bulk_form_${randomId}" class="ui form" method="POST">
                            <ui:select class="ui dropdown clearable search"
                                       name="bulk_docConf" id="bulk_docConf_${randomId}"
                                       from="${RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_CONFIDENTIALITY)}"
                                       optionKey="id"
                                       optionValue="value"
                            />
                            <input name="bulk_docIdList" id="bulk_docIdList_${randomId}" type="hidden" value="" />
                            <input name="bulk_op" type="hidden" value="${RDConstants.DOCUMENT_CONFIDENTIALITY}" />
                        </form>
                    </td>
                    <g:if test="${controllerName == 'myInstitution' && actionName != 'subscriptionsManagement'}">
                        <td></td>
                    </g:if>
                    <td>
                        <button id="bulk_submit_${randomId}" class="${Btn.PRIMARY}">Übernehmen</button>
                    </td>
                </tr>
            </tfoot>
        </g:if>
        </g:if>
    </table>

<laser:script file="${this.getGroovyPageFileName()}">
    docs.init('.documents-table-${randomId}')

    $('#bulk_submit_${randomId}').click (function () {
        let ids = []
        $('.documents-table-${randomId} input[name=bulk_doc]:checked').each( function (i, e) {
            ids.push($(e).attr ('value'))
        })
        $('#bulk_docIdList_${randomId}').attr ('value', ids.join (','))
        $('#bulk_form_${randomId}').submit()
    })

    $('#bulk_selectionToggler_${randomId}').click (function () {
        if ($(this).prop ('checked')) {
            $('.documents-table-${randomId} input[name=bulk_doc]').prop ('checked', true)
        } else {
            $('.documents-table-${randomId} input[name=bulk_doc]').prop ('checked', false)
        }
    })

    $('.documents-table-${randomId} input[name=bulk_doc]').click (function () {
        $('#bulk_selectionToggler_${randomId}').prop ('checked', false)
    })
</laser:script>

<%-- a form within a form is not permitted --%>
<g:each in="${securityWorkaroundList}" var="docctx">
    <laser:render template="/templates/documents/modal" model="${[ownobj: instance, owntp: owntp, docctx: docctx, doc: docctx.owner]}"/>
</g:each>