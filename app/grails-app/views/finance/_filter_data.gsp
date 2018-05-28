<!-- _filter_data.gsp -->
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
%{--Two rows of data per CostItem--}%

<g:each in="${cost_items}" var="ci">
    <tr id="bulkdelete-b${ci.id}">
        <td>
            <span class="costInLocalCurrency" data-costInLocalCurrency="<g:formatNumber number="${ci.costInLocalCurrency}" locale="en" maxFractionDigits="2"/>">
                <g:formatNumber number="${ci.costInLocalCurrency}" type="currency" currencyCode="EUR"/>
                ( <g:formatNumber number="${ci.costInBillingCurrency}" type="currency" currencyCode="${ci.billingCurrency}"/> )
            </span>
        </td>
        <%--<td>
            <semui:xEditableRefData config="CostItemCategory" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemCategory" />
        </td>--%>
        <td>
            <semui:xEditableRefData config="CostItemElement" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemElement" />
        </td>
        <td>
            <semui:xEditableRefData config="CostItemStatus" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemStatus" />
        </td>
        <td>
            <semui:xEditable emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costTitle" />
        </td>
        <td>
            <semui:xEditable owner="${ci}" type="date" field="startDate" />
        </td>
        <td>
            <semui:xEditable owner="${ci}" type="date" field="endDate" />
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
                <g:link controller="finance" action="deleteCostItem" id="${ci.id}" class="ui icon negative button">
                    <i class="trash alternate icon"></i>
                </g:link>
            </g:if>
        </td>

    </tr>
</g:each>

<script>
     $('#costTable .x .button:not(.negative)').on('click', function(e) {
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
