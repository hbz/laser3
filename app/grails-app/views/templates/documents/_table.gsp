
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
                        <semui:xEditable owner="${docctx.owner}" field="title" id="title" />
                    </td>
                    <td>
                        <semui:xEditable owner="${docctx.owner}" field="filename" id="filename" validation="notEmpty"/>
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
    <%-- <g:if test="${editable}">
        <div class="well license-documents-options" style="display:none">
            <button class="ui negative button delete-document" id="delete-doc">${message(code:'template.documents.delete', default:'Delete Selected Documents')}</button>
            <input type="hidden" name="instanceId" value="${instance.id}"/>
            <input type="hidden" name="redirectAction" value="${redirect}"/>
        </div>
    </g:if> : REMOVED BULK --%>

</g:form>

<!-- JS for show/hide of delete button -->
<%--
<r:script type="text/javascript">
    var showEditButtons =function () {
        if ($('.license-documents input:checked').length > 0) {
            $('.license-documents-options').slideDown('fast');
        } else {
            $('.license-documents-options').slideUp('fast');
        }
    }

    $(document).ready(showEditButtons);

    $('.license-documents input[type="checkbox"]').click(showEditButtons);

    $('.license-documents-options .delete-document').click(function () {
        if (!confirm('${message(code:'template.documents.delete.confirm', default:'Are you sure you wish to delete these documents?')}')) {
            $('.license-documents input:checked').attr('checked', false);
            return false;
        }
        $('.license-documents input:checked').each(function () {
            $(this).parent().parent().fadeOut('slow');
            $('.license-documents-options').slideUp('fast');
        });
    })

</r:script>
: REMOVED BULK --%>