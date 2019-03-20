
<g:form id="delete_doc_form" url="[controller:"${controllerName}",action:'deleteDocuments']" method="post">

    <table class="ui celled la-table table license-documents">
        <thead>
        <tr>
            <%--<g:if test="${editable}"><th>${message(code:'default.select.label', default:'Select')}</th></g:if> : REMOVED BULK --%>
            <th>${message(code:'title.label', default:'Title')}</th>
            <th>${message(code:'default.note.label', default:'Note')}</th>
            <th>${message(code:'default.date.label', default:'Date')}</th>
            <th>${message(code:'default.creator.label', default:'Creator')}</th>
            <th>${message(code:'default.actions', default:'Actions')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${instance.documents.sort{it.owner?.title}}" var="docctx">
            <g:if test="${docctx.owner.contentType == 0 && (docctx.status == null || docctx.status?.value != 'Deleted')}">
                <tr>
                    <%--<g:if test="${editable}"><td><input type="checkbox" name="_deleteflag.${docctx.id}" value="true"/></td></g:if> : REMOVED BULK --%>

                    <td>
                        ${docctx.owner.title}
                    </td>
                    <td>
                        ${docctx.owner.content}
                    </td>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${docctx.owner.dateCreated}"/>
                    </td>
                    <td>
                            ${docctx.owner.user}
                    </td>
                    <td class="x">
                        <g:if test="${docctx.sharedFrom}">
                            [ Wird geteilt ]
                        </g:if>

                        <g:if test="${instance.showUIShareButton()}">
                            <g:if test="${docctx.isShared}">
                                <span data-position="top right" data-tooltip="${message(code:'property.share.tooltip.on')}">
                                    <g:link controller="ajax" action="toggleShare" class="ui icon button green"
                                            params='[owner:"${instance.class.name}:${instance.id}", sharedObject:"${docctx.class.name}:${docctx.id}"]'>
                                        <i class="alternate share icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" data-tooltip="${message(code:'property.share.tooltip.off')}">
                                    <g:link controller="ajax" action="toggleShare" class="ui icon button"
                                            params='[owner:"${instance.class.name}:${instance.id}", sharedObject:"${docctx.class.name}:${docctx.id}"]'>
                                        <i class="alternate share icon"></i>
                                    </g:link>
                                </span>
                            </g:else>
                        </g:if>

                        <g:if test="${editable && ! docctx.sharedFrom}">
                            <a onclick="noteedit(${docctx.owner.id});" class="ui icon button">
                                <i class="write icon"></i>
                            </a>
                            <g:link controller="${controllerName}" action="deleteDocuments" class="ui icon negative button"
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


<r:script>
    function noteedit(id) {

        $.ajax({
            url: '<g:createLink controller="ajax" action="NoteEdit"/>?id='+id,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalEditNote").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal('show');
            }
        });
    }
</r:script>
