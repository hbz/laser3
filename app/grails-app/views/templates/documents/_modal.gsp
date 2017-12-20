<semui:modal id="modalCreateDocument" text="Neues Dokument hinzufügen">

    <g:form id="upload_new_doc_form" class="ui form" url="[controller:'docWidget',action:'uploadDocument']" method="post" enctype="multipart/form-data">
        <input type="hidden" name="ownerid" value="${ownobj.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj.class.name}"/>
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
                    <input type="file" name="upload_file" />
                </dd>
            </dl>
            <dl>
                <dt>
                    <label>${message(code: 'template.addDocument.type', default: 'Document Type')}:</label>
                </dt>
                <dd>
                    <select name="doctype">
                        <option value="License"><g:message code="license" default="License"/></option>
                        <option value="General"><g:message code="template.addDocument.type.general" default="General"/></option>
                        <option value="General"><g:message code="template.addDocument.type.addendum" default="Addendum"/></option>
                    </select>
                </dd>
            </dl>
        </div>

    </g:form>

</semui:modal>

