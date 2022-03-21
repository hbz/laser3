<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataValue; de.laser.RefdataCategory" %>
<semui:modal id="osel_add_modal" message="template.orgLinksModal">

    <g:form id="create_org_role_link" class="ui form" url="[controller:'ajax', action:'addOrgRole']" method="post" onsubmit="return JSPC.app.validateAddOrgRole();">
        <input type="hidden" name="parent" value="${parent}"/>
        <input type="hidden" name="property" value="${property}"/>
        <input type="hidden" name="recip_prop" value="${recip_prop}"/>

        <h3 class="ui header">DEPRECATED</h3>

        <div class="field">
            <table id="org_role_tab" class="ui celled compact la-js-responsive-table la-table table">
                <thead>
                    <tr>
                        <th>${message(code:'template.orgLinksModal.name.label')}</th>
                        <th>${message(code:'template.orgLinksModal.select')}</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>

        <div class="field">
            <label class="control-label">${message(code:'template.orgLinksModal.role')}</label>
            <g:set var="varSelectOne" value="${message(code:'default.selectOne.label')}" />
            <g:if test="${linkType}">
                <g:select name="orm_orgRole"
                      noSelection="${['':varSelectOne]}"
                      from="${RefdataValue.findAllByOwnerAndGroup(RefdataCategory.getByDesc(RDConstants.ORGANISATIONAL_ROLE), linkType)}"
                      optionKey="id"
                      optionValue="${{it.getI10n('value')}}"/>
            </g:if>
            <g:else>
                <g:select name="orm_orgRole"
                      noSelection="${['':varSelectOne]}"
                      from="${RefdataCategory.getAllRefdataValues(RDConstants.ORGANISATIONAL_ROLE)}"
                      optionKey="id"
                      optionValue="${{it.getI10n('value')}}"/>
            </g:else>
        </div>
    </g:form>

</semui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.oOrTable = null

    JSPC.app.validateAddOrgRole = function () {
      if ( $('#orm_orgRole').val() == '' ) {
        return confirm("${message(code:'template.orgLinksModal.warn')}");
      }
      return true;
    }

    $('#add_org_head_row').empty()

        JSPC.app.oOrTable = $('#org_role_tab').dataTable( {
            "ajax": "<g:createLink controller="ajax" action="refdataAllOrgs" params="${[format:'json']}"/>",
            "scrollY":          '25vh',
            "scrollCollapse":   false,
            "paging":           false,
            "destroy":          true,
            "processing":       true,
            // "serverSide":       true,
            "columnDefs":       [ {
                "targets": [1],
                "data": "DT_RowId",
                "render": function (data, type, full) {
                    return '<input type="checkbox" name="orm_orgOid" value="' + data + '"/>';
                }
            } ],
            "language": {
                "decimal":        "<g:message code='datatables.decimal' />",
                "emptyTable":     "<g:message code='datatables.emptyTable' />",
                "info":           "<g:message code='datatables.info' />",
                "infoEmpty":      "<g:message code='datatables.infoEmpty' />",
                "infoFiltered":   "<g:message code='datatables.infoFiltered' />",
                "infoPostFix":    "<g:message code='datatables.infoPostFix' />",
                "thousands":      "<g:message code='datatables.thousands' />",
                "lengthMenu":     "<g:message code='datatables.lengthMenu' />",
                "loadingRecords": "<g:message code='datatables.loadingRecords' />",
                "processing":     "<g:message code='datatables.processing' />",
                "search":         "<g:message code='datatables.search' />",
                "zeroRecords":    "<g:message code='datatables.zeroRecords' />",
                "paginate": {
                    "first":      "<g:message code='datatables.paginate.first' />",
                    "last":       "<g:message code='datatables.paginate.last' />",
                    "next":       "<g:message code='datatables.paginate.next' />",
                    "previous":   "<g:message code='datatables.paginate.previous' />"
                },
                "aria": {
                    "sortAscending":  "<g:message code='datatables.aria.sortAscending' />",
                    "sortDescending": "<g:message code='datatables.aria.sortDescending' />"
                }
            }
        } );

        JSPC.app.oOrTable.fnAdjustColumnSizing();
</laser:script>
