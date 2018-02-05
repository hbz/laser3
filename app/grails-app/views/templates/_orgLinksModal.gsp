<semui:modal id="osel_add_modal" message="template.orgLinksModal">

    <g:form id="create_org_role_link" class="ui form" url="[controller:'ajax', action:'addOrgRole']" method="post" onsubmit="return validateAddOrgRole();">
        <input type="hidden" name="parent" value="${parent}"/>
        <input type="hidden" name="property" value="${property}"/>
        <input type="hidden" name="recip_prop" value="${recip_prop}"/>

        <div class="field">
            <table id="org_role_tab" class="ui celled la-table table">
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

            <g:if test="${linkType}">
                <g:select name="orm_orgRole"
                      noSelection="${['':'Select One...']}"
                      from="${com.k_int.kbplus.RefdataValue.findAllByOwnerAndGroup(com.k_int.kbplus.RefdataCategory.findByDesc('Organisational Role'),linkType)}"
                      optionKey="id"
                      optionValue="${{it.getI10n('value')}}"/>
            </g:if>
            <g:else>
                <g:select name="orm_orgRole"
                      noSelection="${['':'Select One...']}"
                      from="${com.k_int.kbplus.RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Organisational Role'))}"
                      optionKey="id"
                      optionValue="${{it.getI10n('value')}}"/>
            </g:else>
        </div>
    </g:form>

</semui:modal>

<g:javascript>
    var oOrTable;

    $(document).ready(function(){

        $('#add_org_head_row').empty()

        oOrTable = $('#org_role_tab').dataTable( {
            'bAutoWidth':  true,
            "sScrollY":    "240px",
            "sAjaxSource": "<g:createLink controller="ajax" action="refdataSearch" id="ContentProvider" params="${[format:'json']}"/>",
            "bServerSide": true,
            "bProcessing": true,
            "bDestroy":    true,
            "bSort":       false,
            "sDom":        "frtiS",
            "oScroller": {
                "loadingIndicator": false
            },
            "aoColumnDefs": [ {
                    "aTargets": [ 1 ],
                    "mData": "DT_RowId",
                    "mRender": function ( data, type, full ) {
                        return '<input type="checkbox" name="orm_orgoid" value="' + data + '"/>';
                    }
                } ]
        } );

        oOrTable.fnAdjustColumnSizing();

    });

    function validateAddOrgRole() {
      if ( $('#orm_orgRole').val() == '' ) {
        return confirm("${message(code:'template.orgLinksModal.warn')}");
      }
      return true;
    }

</g:javascript>
