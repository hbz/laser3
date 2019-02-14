
<g:form id="delete_doc_form" url="[controller:"${controllerName}" ,action:'deleteDocuments']" method="post">

    <table class="ui celled la-table table license-documents">
        <thead>
        <tr>
            <%--<g:if test="${editable}"><th>${message(code:'license.docs.table.select', default:'Select')}</th></g:if> : REMOVED BULK--%>
            <th>${message(code:'license.docs.table.title', default:'Title')}</th>
            <th>${message(code:'license.docs.table.fileName', default:'File Name')}</th>
            <th>${message(code:'license.docs.table.type', default:'Type')}</th>
            <th>${message(code:'license.docs.table.creator', default:'Creator')}</th>
            <th>${message(code:'default.actions', default:'Actions')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${instance.documents.sort{it.owner?.title}}" var="docctx">
            <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
                <tr>
                    <%--<g:if test="${editable}"><td><input type="checkbox" name="_deleteflag.${docctx.id}" value="true"/></td></g:if> : REMOVED BULK--%>
                    <td>
                        <g:if test="${! docctx.sharedFrom}">
                            <semui:xEditable owner="${docctx.owner}" field="title" id="title" />
                        </g:if>
                        <g:else>
                            ${docctx.owner.title}
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${! docctx.sharedFrom}">
                            <semui:xEditable owner="${docctx.owner}" field="filename" id="filename" validation="notEmpty"/>
                        </g:if>
                        <g:else>
                            ${docctx.owner.filename}
                        </g:else>
                    </td>
                    <td>
                        ${docctx.owner?.type?.getI10n('value')}
                    </td>
                    <td>
                        ${docctx.owner.creator}
                    </td>

                    <td class="x">
                        <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">
                            <g:if test="${docctx.sharedFrom}">
                                [ Wird geteilt ]
                            </g:if>

                            <g:if test="${instance.showShareButton()}">

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

                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon button"><i class="download icon"></i></g:link>

                            <g:if test="${editable && ! docctx.sharedFrom}">
                                <g:link controller="${controllerName}" action="deleteDocuments" class="ui icon negative button"
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

