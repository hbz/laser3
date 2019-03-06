<%@page import="com.k_int.kbplus.*;de.laser.helper.RDStore"%>
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
                              value=""/>
                </dd>
            </dl>
            <g:if test="${ownobj.class.name.equals(Org.class.name)}">
                <dl>
                    <dt>
                        <label>${message(code:'template.addDocument.shareConf')}</label>
                    </dt>
                    <dd>
                        <laser:select from="${RefdataCategory.getAllRefdataValues('Share Configuration')}" class="ui dropdown fluid" name="shareConf"
                                      optionKey="${{it.class.name+":"+it.id}}" optionValue="value" value="${RefdataValue.class.name}:${RDStore.SHARE_CONF_CREATOR}"/>
                    </dd>
                </dl>
                <dl>
                    <dt>
                        <label>${message(code:'template.addDocument.targetOrg')}</label>
                    </dt>
                    <dd>
                        <g:select class="ui dropdown search la-full-width"
                                  name="targetOrg"
                                  from="${Org.executeQuery('select o from Org o where o.status != :deleted or o.status is null order by o.sortname asc',[deleted:RDStore.O_STATUS_DELETED])}"
                                  optionKey="id"
                                  optionValue="name"
                                  value="${contextService.getOrg().id}"
                        />
                    </dd>
                </dl>
            </g:if>
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