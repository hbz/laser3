<%@page import="com.k_int.kbplus.*;de.laser.helper.RDStore;de.laser.helper.RDConstants"%>
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
        formUrl = createLink(controller:'docWidget',action:'editDocument')
        modalId = "modalEditDocument_${docctx.id}"
    }
    else if(owntp == 'surveyConfig') {
        modalText = message(code:'surveyConfigDocs.createSurveyDoc')
        submitButtonLabel = message(code:'default.button.create_new.label')
        formUrl = createLink(controller: 'docWidget',action:'uploadDocument')
        modalId = "modalCreateDocument"
        docForAll = false
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
                        List notAvailable = [RefdataValue.getByValueAndCategory('ONIX-PL License', RDConstants.DOCUMENT_TYPE),
                                             RefdataValue.getByValueAndCategory('Note', RDConstants.DOCUMENT_TYPE),
                                             RefdataValue.getByValueAndCategory('Announcement', RDConstants.DOCUMENT_TYPE)]
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

            <g:if test="${ownobj instanceof Org}">
                <g:if test="${inContextOrg}">
                    <dl>
                        <dt>
                            <label>${message(code:'template.addDocument.target')}</label>
                        </dt>
                        <dd>
                            <%
                                //Germans would say 'Durch das Knie in's Auge und zurück' ...
                                List<Org> orgs = []
                                if(institution.hasPerm('ORG_CONSORTIUM'))
                                    orgs = Org.executeQuery("select distinct o from Combo c join c.fromOrg o where (o.status = null or o.status != :deleted) and (o != :contextOrg and not c.type = :department)",[contextOrg:institution, deleted:RDStore.O_STATUS_DELETED, department:RDStore.COMBO_TYPE_DEPARTMENT])
                                else if(institution.hasPerm('ORG_INST_COLLECTIVE'))
                                    orgs = Org.executeQuery("select distinct o from Combo c join c.fromOrg o where (o.status = null or o.status != :deleted) and (o != :contextOrg and (c.toOrg = :contextOrg and c.type = :department))",[contextOrg:institution, deleted:RDStore.O_STATUS_DELETED, department:RDStore.COMBO_TYPE_DEPARTMENT])
                                //feel free to include this sort clause in the queries above!
                                orgs.addAll(Org.findAllBySector(RDStore.O_SECTOR_PUBLISHER))
                                orgs.sort { x,y ->
                                    if(x.sortname == y.sortname)
                                        x.name <=> y.name
                                    else x.sortname <=> y.sortname
                                }
                            %>
                            <g:select name="targetOrg" id="targetOrg" from="${orgs}" optionKey="id" class="ui search select dropdown fluid" value="${docctx?.targetOrg?.id}" noSelection="${['': message(code: 'default.select.choose.label')]}"/>
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
                            String value = "${RefdataValue.class.name}:${RDStore.SHARE_CONF_UPLOADER_ORG.id}"
                            if(docctx) {
                                value = "${RefdataValue.class.name}:${docctx.shareConf?.id}"
                            }
                            List allConfigs = RefdataValue.executeQuery("select rdv from RefdataValue rdv where rdv.owner.desc = '${de.laser.helper.RDConstants.SHARE_CONFIGURATION}' and rdv.isHardData = true order by rdv.order asc")
                            List availableConfigs = []
                            if(!accessService.checkPerm("ORG_CONSORTIUM")){
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