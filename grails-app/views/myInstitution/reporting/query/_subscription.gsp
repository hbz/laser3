<%@page import="de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.ReportingGlobalService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:set var="validFilterResult" value="${filterResult.data.subscriptionIdList || filterResult.data.memberIdList || filterResult.data.consortiumIdList || filterResult.data.providerIdList || filterResult.data.vendorIdList}" />

    <g:if test="${validFilterResult}">
        <ui:msg class="success" hideClose="true">
                <laser:render template="/myInstitution/reporting/query/query_filterResult" model="${[filter: filter, filterResult: filterResult]}" />
        </ui:msg>
    </g:if>
    <g:else>
        <ui:msg class="error" hideClose="true" message="reporting.filter.result.x.empty" />
    </g:else>

    <laser:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${validFilterResult}">
        <laser:render template="/myInstitution/reporting/query/query_form" model="${[cfgKey: "${BaseConfig.KEY_SUBSCRIPTION}"]}"/>
    </g:if>
</g:if>