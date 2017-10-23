<div class="modal hide" id="modalCreateDocument">
    <g:form id="upload_new_doc_form" url="[controller:'docWidget',action:'uploadDocument']" method="post" enctype="multipart/form-data">
        <input type="hidden" name="ownerid" value="${ownobj.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj.class.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>
        <div class="modal-body">
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
        </div>
        <div class="modal-footer">
            <a href="#" class="ui button" data-dismiss="modal">${message(code: 'default.button.close.label', default:'Close')}</a>
            <input type="submit" class="ui primary button" name ="SaveDoc" value="${message(code: 'default.button.save_changes', default: 'Save Changes')}">
        </div>
    </g:form>

</div>

