<!-- _filter_data.gsp -->
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
%{--Two rows of data per CostItem--}%

<g:each in="${cost_items}" var="ci">
    <tr id="bulkdelete-b${ci.id}">
        <td>
            <g:if test="${editable}">
                <semui:xEditable emptytext="Edit Cost" owner="${ci}" field="costInBillingCurrency" /> </br>
                <semui:xEditableRefData config="Currency" emptytext="Edit billed" owner="${ci}" field="billingCurrency" /> </br>
                <semui:xEditable emptytext="Edit local" owner="${ci}" field="costInLocalCurrency" />
            </g:if>
            <g:else>
                ${ci?.costInBillingCurrency} </br>
                ${ci?.billingCurrency} </br>
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

        <td>
            <g:link
                    class="ui icon positiv button">

                <i class="write icon"></i>

            </g:link>
            <g:link
                    class="ui icon negative button">

                <i class="trash alternate icon"></i>

            </g:link>
        </td>

    </tr>
</g:each>
<!-- _filter_data.gsp -->