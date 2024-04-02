<!-- _ajaxModal.gsp -->
<%@ page import="de.laser.finance.CostItem; de.laser.UserSetting; de.laser.storage.RDStore; de.laser.*;" %>
<laser:serviceInjection/>

<g:if test="${setting == 'bulkForAll'}">
    <g:set var="modalText" value="${message(code: 'financials.addNewCostForAll') + " ("+ surveyOrgList.size()+ ") "}"/>
</g:if>
<g:else>
    <g:set var="modalText" value="${message(code: 'financials.addNewCostFor', args: [surveyOrg.org.name])}"/>
</g:else>
<g:set var="submitButtonLabel" value="${message(code: 'default.button.create_new.label')}"/>
<g:set var="org" value="${contextService.getOrg()}"/>


<%
    if (costItem) {
        if (mode && mode.equals("edit")) {
            modalText = g.message(code: 'financials.editCostFor', args: [surveyOrg.org.name])
            submitButtonLabel = g.message(code: 'default.button.save.label')
        }
    }

%>

<ui:modal id="${modalID ?: 'modalSurveyCostItem'}" text="${modalText + (surveyOrg ? ' ('+ surveyOrg.surveyConfig.subscription+ ')' : '')}" msgSave="${submitButtonLabel}">
    <g:form class="ui small form" name="editCost_${idSuffix}" action="createSurveyCostItem">

        <laser:render template="costItemInputSurvey" model="[idSuffix: 'createSurveyCostItem']"/>

    </g:form>

</ui:modal>
<!-- _ajaxModal.gsp -->
