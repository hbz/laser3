<g:form id="delete_doc_form" url="[controller:"${controllerName}" ,action:'deleteDocuments']" method="post">
    <g:if test="${editable}">

        <div class="well hide license-documents-options">
            <button class="btn btn-danger delete-document" id="delete-doc">${message(code:'template.documents.delete', default:'Delete Selected Documents')}</button>
            <input type="hidden" name="instanceId" value="${instance.id}"/>
            <input type="hidden" name="redirectAction" value="${redirect}"/>
        </div>
    </g:if>

    <table class="ui celled table license-documents">
        <thead>
        <tr>
            <g:if test="${editable}"><th>${message(code:'license.docs.table.select', default:'Select')}</th></g:if>
            <th>${message(code:'license.docs.table.title', default:'Title')}</th>
            <th>${message(code:'license.docs.table.fileName', default:'File Name')}</th>
            <th>${message(code:'license.docs.table.download', default:'Download')}</th>
            <th>${message(code:'license.docs.table.creator', default:'Creator')}</th>
            <th>${message(code:'license.docs.table.type', default:'Type')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${instance.documents}" var="docctx">
            <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
                <tr>
                    <g:if test="${editable}"><td><input type="checkbox" name="_deleteflag.${docctx.id}" value="true"/>
                    </td></g:if>
                    <td style="max-width: 300px;overflow: hidden;text-overflow: ellipsis;">
                        <g:xEditable owner="${docctx.owner}" field="title" id="title"/>
                    </td>
                    <td style="max-width: 300px;overflow: hidden;text-overflow: ellipsis;">
                        <g:xEditable owner="${docctx.owner}" field="filename" id="filename"/>
                    </td>
                    <td>
                        <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">
                            <g:link controller="docstore" id="${docctx.owner.uuid}">${message(code:'template.documents.download', default:'Download Doc')}</g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:xEditable owner="${docctx.owner}" field="creator" id="creator"/>
                    </td>
                    <td>${docctx.owner?.type?.value}</td>
                </tr>
            </g:if>
        </g:each>
        </tbody>
    </table>
    <g:if test="${editable}">          
      <input type="button" class="ui primary button" value="${message(code:'template.documents.add', default:'Add new document')}" data-semui="modal" href="#modalCreateDocument"/>
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
