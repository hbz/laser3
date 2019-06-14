<%@page import="com.k_int.kbplus.*;de.laser.helper.RDStore;"%>
<%
    String modalText
    String submitButtonLabel
    String formUrl
    String modalId
    boolean docForAll = false
    if(docctx && doc) {
        modalText = message(code:'template.documents.edit')
        submitButtonLabel = message(code:'default.button.edit.label')
        formUrl = createLink(controller:'docWidget',action:'editDocument')
        modalId = "modalEditDocument_${docctx.id}"
    }
    else if(owntp == 'surveyConfig') {
        modalText = message(code:'surveyConfigDocs.createSurveyDoc')
        submitButtonLabel = message(code:'default.button.create_new.label')
        formUrl = createLink(controller: 'docWidget',action:'uploadDocument')
        modalId = "modalCreateDocument"
        docForAll = true
    }
    else {
        modalText = message(code:'template.documents.add')
        submitButtonLabel = message(code:'default.button.create_new.label')
        formUrl = createLink(controller: 'docWidget',action:'uploadDocument')
        modalId = "modalCreateDocument"
    }
%>
<semui:modal id="${modalId}" text="${modalText}" msgSave="${submitButtonLabel}">

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
                    <label>${message(code: 'template.addDocument.name', default: 'Document Name')}:</label>
                </dt>
                <dd>
                    <input type="text" name="upload_title" value="${doc?.title}"/>
                </dd>
            </dl>
            <g:if test="${!docctx && !doc}">
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

                <script>
                    $('#modalCreateDocument .action .icon.button').click( function() {
                        $(this).parent('.action').find('input:file').click();
                    });

                    $('input:file', '.ui.action.input').on('change', function(e) {
                        var name = e.target.files[0].name;
                        $('input:text', $(e.target).parent()).val(name);
                    });
                </script>
            </g:if>
            <dl>
                <dt>
                    <label>${message(code: 'template.addDocument.type', default: 'Document Type')}:</label>
                </dt>
                <dd>
                    <%
                        List notAvailable = [RefdataValue.getByValueAndCategory('ONIX-PL License','Document Type'),
                                             RefdataValue.getByValueAndCategory('Note','Document Type'),
                                             RefdataValue.getByValueAndCategory('Announcement','Document Type')]
                        List documentTypes = RefdataCategory.getAllRefdataValues("Document Type")-notAvailable
                    %>
                    <g:select from="${documentTypes}"
                              class="ui dropdown fluid"
                              optionKey="value"
                              optionValue="${{ it.getI10n('value') }}"
                              name="doctype"
                              value="${doc?.type?.value}"/>
                </dd>
            </dl>

            <g:if test="${ownobj instanceof Org}">
                <dl>
                    <dt>
                        <label>${message(code:'template.addDocument.target')}</label>
                    </dt>
                    <dd>
                        <g:select name="targetOrg" id="targetOrg" from="${Org.executeQuery("select o from Org o where (o.status = null or o.status != :deleted) and o != :contextOrg order by o.sortname asc",[contextOrg:institution,deleted:RDStore.O_STATUS_DELETED])}" optionKey="id" class="ui search select dropdown fluid" value="${docctx?.targetOrg?.id}" noSelection="${[null:'']}"/>
                    </dd>
                </dl>
                <dl>
                    <dt>
                        <label>${message(code:'template.addDocument.shareConf')}</label>
                    </dt>
                    <dd>
                        <%
                            String value = "${RefdataValue.class.name}:${RDStore.SHARE_CONF_UPLOADER_ORG.id}"
                            if(docctx) {
                                value = "${RefdataValue.class.name}:${docctx.shareConf?.id}"
                            }
                            List allConfigs = RefdataValue.executeQuery("select rdv from RefdataValue rdv where rdv.owner.desc = 'Share Configuration' and rdv.hardData = true order by rdv.order asc")
                            List availableConfigs = []
                            if(!institution.getallOrgTypeIds().contains(RDStore.OT_CONSORTIUM.id)){
                                availableConfigs = allConfigs-RDStore.SHARE_CONF_CONSORTIUM
                            }
                            else availableConfigs = allConfigs
                        %>
                        <laser:select from="${availableConfigs}" class="ui dropdown fluid" name="shareConf"
                                      optionKey="${{it.class.name+":"+it.id}}" optionValue="value" value="${value}"/>
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
                        <label>${message(code: 'surveyConfig.documents.docForAllSurveyConfigs')}</label>
                    </div>
                </dd>
            </dl>
        </g:if>

        </div>

    </g:form>

</semui:modal>