<!-- _ajaxModal.gsp -->
<%@ page import="de.laser.finance.CostItem; de.laser.UserSetting; de.laser.helper.RDStore; com.k_int.kbplus.*; de.laser.*; org.springframework.context.i18n.LocaleContextHolder" %>
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

<semui:modal id="${modalID ?: 'modalSurveyCostItem'}" text="${modalText + (surveyOrg ? ' ('+ surveyOrg.surveyConfig.subscription+ ')' : '')}" msgSave="${submitButtonLabel}">
    <g:form class="ui small form" name="editCost_${idSuffix}" action="newSurveyCostItem">

        <g:render template="costItemInputSurvey" model="[idSuffix: 'newSurveyCostItem']"/>

    </g:form>

</semui:modal>
<!-- _ajaxModal.gsp -->
