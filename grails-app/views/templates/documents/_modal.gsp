<%@page import="de.laser.*; de.laser.storage.RDStore; de.laser.storage.RDConstants"%>
<laser:serviceInjection/>
<%
    String modalText
    String submitButtonLabel
    String formUrl
    String modalId
    boolean docForAll = false
    if(docctx && doc) {
        modalText = message(code:'template.documents.edit')
        submitButtonLabel = message(code:'default.button.edit.label')
        formUrl = createLink(controller:'docstore', action:'editDocument')
        modalId = "modalEditDocument_${docctx.id}"
    }
    else if(owntp == 'surveyConfig') {
        modalText = message(code:'surveyConfigDocs.createSurveyDoc')
        submitButtonLabel = message(code:'default.button.create_new.label')
        formUrl = createLink(controller: 'docstore', action:'uploadDocument')
        modalId = "modalCreateDocument"
        docForAll = false
    }
    else {
        modalText = message(code:'template.documents.add')
        submitButtonLabel = message(code:'default.button.create_new.label')
        formUrl = createLink(controller: 'docstore', action:'uploadDocument')
        modalId = "modalCreateDocument"
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

        <div class="inline-lists">
            <dl>
                <dt>
                    <label>${message(code: 'template.addDocument.name')}:</label>
                </dt>
                <dd>
                    <input type="text" name="upload_title" value="${doc?.title}"/>
                </dd>
            </dl>
            <g:if test="${!docctx && !doc}">
                <dl>
                    <dt>
                        <label>${message(code: 'template.addDocument.file')}:</label>
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

                <laser:script file="${this.getGroovyPageFileName()}">
                    $('#modalCreateDocument .action .icon.button').click( function() {
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
                    <label>${message(code: 'template.addDocument.type')}:</label>
                </dt>
                <dd>
                    <%
                        List notAvailable = [RDStore.DOC_TYPE_NOTE,RDStore.DOC_TYPE_ANNOUNCEMENT,RDStore.DOC_TYPE_ONIXPL]
                        List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
                    %>
                    <g:select from="${documentTypes}"
                              class="ui dropdown fluid"
                              optionKey="value"
                              optionValue="${{ it.getI10n('value') }}"
                              name="doctype"
                              value="${doc?.type?.value}"/>
                </dd>
            </dl>

            <g:if test="${controllerName == 'organisation'}"> <%-- orgs and availableConfigs are set in getResultGenerics() in OrganisationControllerService --%>
                <g:if test="${inContextOrg}">
                    <dl>
                        <dt>
                            <label>${message(code:'template.addDocument.target')}</label>
                        </dt>
                        <dd>
                            <g:select name="targetOrg" id="targetOrg" from="${controlledListService.getOrgs()}" optionKey="id" optionValue="text" class="ui search select dropdown fluid" value="${docctx?.targetOrg?.id}" noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                        </dd>
                    </dl>
                </g:if>
                <g:else>
                    <g:hiddenField name="targetOrg" id="targetOrg" value="${ownobj.id}"/>
                </g:else>
                <dl>
                    <dt>
                        <label>${message(code:'template.addDocument.shareConf')}</label>
                    </dt>
                    <dd>
                        <%
                            Long value = RDStore.SHARE_CONF_UPLOADER_ORG.id
                            if(docctx) {
                                value = docctx.shareConf?.id
                            }
                        %>
                        <ui:select from="${availableConfigs}" class="ui dropdown fluid la-not-clearable" name="shareConf"
                                      optionKey="id" optionValue="value" value="${value}"/>
                    </dd>
                </dl>
            </g:if>
        <g:if test="${docForAll}">
            <dl>
                <dt>

                </dt>
                <dd>
                    <div class="ui checkbox">
                        <input type="checkbox" name="docForAllSurveyConfigs">
                        <label>${message(code: 'surveyconfig.documents.docForAllSurveyConfigs')}</label>
                    </div>
                </dd>
            </dl>
        </g:if>

        </div>

    </g:form>

</ui:modal>