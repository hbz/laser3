<semui:modal id="modalCreateDocument" text="Neues Dokument hinzufügen">

    <g:form id="upload_new_doc_form" class="ui form" url="[controller:'docWidget',action:'uploadDocument']" method="post" enctype="multipart/form-data">
        <input type="hidden" name="ownerid" value="${ownobj?.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj?.class?.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>

        <div class="inline-lists">
            <dl>
                <dt>
                    <label>${message(code: 'template.addDocument.name', default: 'Document Name')}:</label>
                </dt>
                <dd>
                    <input type="text" name="upload_title">
                </dd>
            </dl>
            <dl>
                <dt>
                    <label>${message(code: 'template.addDocument.file', default: 'File')}:</label>
                </dt>
                <dd>
                    <div class="ui fluid action input">
                        <input type="text" readonly="readonly" placeholder="${message(code:'template.addDocument.selectFile')}">
                        <input type="file" name="upload_file" style="display: none;">
                        <div class="ui icon button" style="padding-left:30px; padding-right:30px">
                            <i class="attach icon"></i>
                        </div>
                    </div>
                </dd>
            </dl>
            <dl>
                <dt>
                    <label>${message(code: 'template.addDocument.type', default: 'Document Type')}:</label>
                </dt>
                <dd>
                    <select class="ui dropdown fluid" name="doctype">
                        <option value="${com.k_int.kbplus.RefdataValue.loc('Document Type', [en:'License'])}"><g:message code="license" default="License"/></option>
                        <option value="${com.k_int.kbplus.RefdataValue.loc('Document Type', [en:'General'])}"><g:message code="template.addDocument.type.general" default="General"/></option>
                        <option value="${com.k_int.kbplus.RefdataValue.loc('Document Type', [en:'Addendum'])}"><g:message code="template.addDocument.type.addendum" default="Addendum"/></option>
                    </select>
                </dd>
            </dl>
        </div>

    </g:form>

</semui:modal>
<r:script type="text/javascript">
    $('#modalCreateDocument .action .icon.button').click( function() {
         $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function(e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });
</r:script>