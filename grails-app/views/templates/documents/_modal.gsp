<%@page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.*; de.laser.storage.RDStore; de.laser.storage.RDConstants"%>
<laser:serviceInjection/>
<%
    String modalText
    String submitButtonLabel
    String formUrl
    String modalId

    if(docctx && doc) {
        modalText = message(code:'template.documents.edit')
        submitButtonLabel = message(code:'default.button.edit.label')
        formUrl = createLink(controller:'document', action:'editDocument')
        modalId = "modalEditDocument_${docctx.id}"
    }
    else if(owntp == 'surveyConfig') {
        modalText = message(code:'surveyConfigDocs.createSurveyDoc')
        submitButtonLabel = message(code:'default.button.create_new.label')
        formUrl = createLink(controller: 'document', action:'uploadDocument')
        modalId = "modalCreateDocument"
    }
    else {
        modalText = message(code:'template.documents.add')
        submitButtonLabel = message(code:'default.button.create_new.label')
        formUrl = createLink(controller: 'document', action:'uploadDocument')
        modalId = newModalId ?: "modalCreateDocument"
    }
%>
<ui:modal id="${modalId}" text="${modalText}" msgSave="${submitButtonLabel}">

    <g:form class="ui form" url="${formUrl}" method="post" enctype="multipart/form-data">
        <input type="hidden" name="ownerid" value="${ownobj?.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj?.class?.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>
        <g:if test="${docctx}">
            <input type="hidden" name="docctx" value="${docctx.id}"/>
        </g:if>

        <g:set var="labelId" value="${doc?.id ?: '000'}" />

        <div class="inline-lists">
            <dl>
                <dt>
                    <label for="upload_title-${labelId}">${message(code: 'template.addDocument.name')}:</label>
                </dt>
                <dd>
                    <input type="text" id="upload_title-${labelId}" name="upload_title" value="${doc?.title}"/>
                </dd>
            </dl>
            <g:if test="${!docctx && !doc}">
                <dl>
                    <dt>
                        <label>${message(code: 'template.addDocument.file')}:</label>
                    </dt>
                    <dd>
                        <div class="ui fluid action input">
                            <input type="text" name="upload_file_placeholder" readonly="readonly" placeholder="${message(code:'template.addDocument.selectFile')}">
                            <input type="file" name="upload_file" style="display: none;">
                            <div class="${Btn.ICON.SIMPLE}" style="padding-left:30px; padding-right:30px">
                                <i class="${Icon.CMD.ATTACHMENT}"></i>
                            </div>
                        </div>
                    </dd>
                </dl>

                <laser:script file="${this.getGroovyPageFileName()}">
                    JSPC.callbacks.modal.onShow['${modalId}'] = function(trigger) {
                        r2d2.helper.resetModalForm ('#${modalId}');
                        $('#${modalId} input[name=upload_file_placeholder]').val('');
                    };

                    $('#${modalId} .action .icon.button').click( function() {
                        $(this).parent('.action').find('input:file').click();
                    });

                    $('input:file', '.ui.action.input').on('change', function(e) {
                        var name = e.target.files[0].name;
                        $('input:text', $(e.target).parent()).val(name);
                    });
                </laser:script>
            </g:if>
            <dl>
                <dt>
                    <label for="doctype-${labelId}">${message(code: 'template.addDocument.type')}:</label>
                </dt>
                <dd>
                    <%
                        List documentTypes
                        if(actionName == 'currentSubscriptionsTransfer'){
                            documentTypes = [RDStore.DOC_TYPE_RENEWAL, RDStore.DOC_TYPE_OFFER]
                        }else {
                            List notAvailable = [RDStore.DOC_TYPE_NOTE,RDStore.DOC_TYPE_ANNOUNCEMENT,RDStore.DOC_TYPE_ONIXPL]
                            documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
                        }
                    %>
                    <g:select from="${documentTypes}"
                              class="ui dropdown clearable fluid"
                              optionKey="value"
                              optionValue="${{ it.getI10n('value') }}"
                              id="doctype-${labelId}"
                              name="doctype"
                              value="${selectedDocType ?: doc?.type?.value}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </dd>
            </dl>
            <dl>
                <dt>
                    <label for="confidentiality-${labelId}">${message(code: 'template.addDocument.confidentiality')}:</label>
                </dt>
                <dd>
                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_CONFIDENTIALITY)}"
                              class="ui dropdown clearable fluid"
                              optionKey="value"
                              optionValue="${{ it.getI10n('value') }}"
                              id="confidentiality-${labelId}"
                              name="confidentiality"
                              value="${doc?.confidentiality?.value}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </dd>
            </dl>

            <g:if test="${controllerName == 'organisation'}">
                <g:if test="${inContextOrg}">
                    <dl>
                        <dt>
                            <label for="targetOrg-${labelId}">${message(code:'template.addDocument.target')}</label>
                        </dt>
                        <dd>
                            <g:select id="targetOrg-${labelId}" name="targetOrg" from="${controlledListService.getOrgs()}" optionKey="id" optionValue="text" class="ui search select dropdown fluid" value="${docctx?.targetOrg?.id}" noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                        </dd>
                    </dl>
                </g:if>
                <g:elseif test="${controllerName == 'organisation'}">
                    <g:hiddenField name="targetOrg" id="targetOrg-${labelId}" value="${ownobj.id}"/>
                </g:elseif>
            </g:if>
            <%
                Long value = RDStore.SHARE_CONF_UPLOADER_ORG.id
                if(docctx) {
                    value = docctx.shareConf?.id
                }
                List<RefdataValue> availableConfigs = []
                if(controllerName == 'organisation') {
                    availableConfigs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.SHARE_CONFIGURATION)
                }
                else if(controllerName in ['license', 'subscription']) {
                    availableConfigs = [RDStore.SHARE_CONF_UPLOADER_ORG, RDStore.SHARE_CONF_UPLOADER_AND_TARGET]
                }
                if(inContextOrg)
                    availableConfigs.remove(RDStore.SHARE_CONF_UPLOADER_AND_TARGET)
            %>
            <g:if test="${availableConfigs.size() > 1}">
                <dl>
                    <dt>
                        <label for="shareConf-${labelId}">${message(code:'template.addDocument.shareConf')}</label>
                    </dt>
                    <dd>
                        <ui:select from="${availableConfigs}" class="ui dropdown fluid la-not-clearable" name="shareConf" id="shareConf-${labelId}" optionKey="id" optionValue="value" value="${value}"/>
                    </dd>
                </dl>
            </g:if>
            <g:elseif test="${!showConsortiaFunctions}">
                <g:hiddenField name="shareConf" value="${RDStore.SHARE_CONF_UPLOADER_ORG}"/>
            </g:elseif>
            <g:if test="${controllerName != 'myInstitution' && showConsortiaFunctions}">
                <dl>
                    <dt>
                        <label for="setSharing-${labelId}">${message(code:'template.addDocument.setSharing')}</label>
                    </dt>
                    <dd><g:checkBox id="setSharing-${labelId}" name="setSharing" class="ui checkbox" value="${docctx?.isShared}"/></dd>
                </dl>
            </g:if>

        </div>
    </g:form>

</ui:modal>