<!-- _filter_data.gsp -->
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
%{--Two rows of data per CostItem--}%

<g:each in="${cost_items}" var="ci">
    <tr id="bulkdelete-b${ci.id}">
        <td>
            <g:if test="${editable}">
                Cost: <semui:xEditable emptytext="Edit Cost" owner="${ci}" field="costInBillingCurrency" /> <br />
                <semui:xEditableRefData config="Currency" emptytext="Edit billed" owner="${ci}" field="billingCurrency" /> <br /><br />
                Local: <semui:xEditable emptytext="Edit local" owner="${ci}" field="costInLocalCurrency" />
            </g:if>
            <g:else>
                ${ci?.costInBillingCurrency} <br />
                ${ci?.billingCurrency} <br />
                ${ci?.costInLocalCurrency}
            </g:else>
        </td>
        <td>
            <g:if test="${editable}">
                <semui:xEditableRefData config="CostItemCategory" emptytext="Edit Category" owner="${ci}" field="costItemCategory" />
            </g:if>
        </td>
        <td>
            <g:if test="${editable}">
                <semui:xEditableRefData config="CostItemElement" emptytext="Edit Element" owner="${ci}" field="costItemElement" />
            </g:if>
        </td>
        <td>
            <g:if test="${editable}">
                <semui:xEditableRefData config="CostItemStatus" emptytext="Edit Status" owner="${ci}" field="costItemStatus" />
            </g:if>
        </td>
        <td>
            <g:if test="${editable}">
                <semui:xEditable emptytext="Edit Description" owner="${ci}" field="costDescription" />
            </g:if>
            <g:else>
                ${ci?.costDescription}
            </g:else>
        </td>
        <td>
            <g:if test="${editable}">
                <semui:xEditable emptytext="Edit Start-Date" owner="${ci}" type="date" field="startDate" />
            </g:if>
        </td>
        <td>
            <g:if test="${editable}">
                <semui:xEditable emptytext="Edit End-Date" owner="${ci}" type="date" field="endDate" />
            </g:if>
        </td>

        <td class="x">
            <g:if test="${editable}">
                <g:if test="${inSubMode}">
                    <g:link mapping="subfinanceEditCI" params='[sub:"${fixedSubscription?.id}", id:"${ci.id}"]' class="ui icon button">
                        <i class="write icon"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="finance" action="editCostItem" id="${ci.id}" class="ui icon button">
                        <i class="write icon"></i>
                    </g:link>
                </g:else>
            </g:if>
            <g:if test="${editable}">
                <g:link class="ui icon negative button">
                    <i class="trash alternate icon"></i>
                </g:link>
            </g:if>
        </td>

    </tr>
</g:each>

<script>
     $('#costTable .x .button.positive').on('click', function(e) {
        e.preventDefault()

        $.ajax({
            url: $(this).attr('href')
        }).done( function(data) {
            $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
            $('#dynamicModalContainer').empty().html(data);

            $('#dynamicModalContainer .ui.modal').modal({
                onVisible: function () {
                    r2d2.initDynamicSemuiStuff('#costItem_ajaxModal');
                    r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');

                    ajaxPostFunc()
                },
                detachable: true,
                closable: true,
                transition: 'fade up',
                onApprove : function() {
                    $(this).find('.ui.form').submit();
                    return false;
                }
            }).modal('show');
        })
    })
</script>
<!-- _filter_data.gsp -->
