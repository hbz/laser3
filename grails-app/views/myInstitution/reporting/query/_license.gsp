<%@page import="de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.ReportingGlobalService;de.laser.Org;de.laser.License" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:set var="validFilterResult" value="${filterResult.data.licenseIdList || filterResult.data.licensorIdList}" />

    <g:if test="${validFilterResult}">
        <ui:msg class="success" noClose="true">
                <laser:render template="/myInstitution/reporting/query/query_filterResult" model="${[filter: filter, filterResult: filterResult]}" />
        </ui:msg>
    </g:if>
    <g:else>
        <ui:msg class="error" noClose="true" message="reporting.filter.result.x.empty" />
    </g:else>

    <laser:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${validFilterResult}">
        <laser:render template="/myInstitution/reporting/query/query_form" model="${[cfgKey: "${BaseConfig.KEY_LICENSE}"]}" />
    </g:if>
</g:if>

