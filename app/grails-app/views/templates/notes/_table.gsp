<g:form id="delete_doc_form" url="[controller:"${controllerName}",action:'deleteDocuments']" method="post">

    <table class="ui celled la-table table license-documents">
        <thead>
        <tr>
            <g:if test="${editable}"><th>${message(code:'default.select.label', default:'Select')}</th></g:if>
            <th>${message(code:'title.label', default:'Title')}</th>
            <th>${message(code:'default.note.label', default:'Note')}</th>
            <th>${message(code:'default.date.label', default:'Date')}</th>
            <th>${message(code:'default.creator.label', default:'Creator')}</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${instance.documents.sort{it.owner?.title}}" var="docctx">
            <g:if test="${docctx.owner.contentType == 0 && (docctx.status == null || docctx.status?.value != 'Deleted')}">
                <tr>
                    <g:if test="${editable}"><td><input type="checkbox" name="_deleteflag.${docctx.id}" value="true"/>
                    </td></g:if>
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
                        <g:if test="${editable}">
                            <a onclick="noteedit(${docctx.owner.id});" class="ui icon button">
                                <i class="write icon"></i>
                            </a>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
        </tbody>
    </table>

    <g:if test="${editable}">
        <div class="well hide license-documents-options">

            <input type="hidden" name="redirectAction" value="${redirect}"/>
            <input type="hidden" name="instanceId" value="${instance.id}"/>
            <input type="submit" class="ui negative button delete-document" value="${message(code:'template.notes.delete', default:'Delete Selected Notes')}"/>
        </div>

    </g:if>

    <g:if test="${editable}">
        <input type="button" class="ui button" value="${message(code:'template.addNote', default:'Add new Note')}" data-semui="modal" href="#modalCreateNote"/>
   </g:if>

</g:form>

<!-- JS for show/hide of delete button -->
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
        if (!confirm('${message(code:'template.notes.delete.confirm', default:'Are you sure you wish to delete these notes?')}')) {
            $('.license-documents input:checked').attr('checked', false);
            return false;
        }
        $('.license-documents input:checked').each(function () {
            $(this).parent().parent().fadeOut('slow');
            $('.license-documents-options').slideUp('fast');
        });
    })
</r:script>
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
