<%@ page import="de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.finance.CostItem" %>
<laser:serviceInjection/>


<div class="ui segment">
    <h3>
        <g:message code="costItem.label"/> in <g:message code="survey.label"/>
    </h3>
    <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'financials.costItemElement')}</th>
            <th>${message(code: 'default.count.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${costItemsByCostItemElement}" var="ctByCostItemElement" status="i">
            <g:set var="costItemElement" value="${RefdataValue.findByValueAndOwner(ctByCostItemElement.key, RefdataCategory.findByDesc(RDConstants.COST_ITEM_ELEMENT))}"/>
            <tr>
                <td>${i + 1}</td>
                <td>${costItemElement.getI10n('value')}</td>
                <td><g:link controller="survey" action="$actionName" params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID, selectedCostItemElementID: costItemElement.id]}">${ctByCostItemElement.value.size()}</g:link></td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
