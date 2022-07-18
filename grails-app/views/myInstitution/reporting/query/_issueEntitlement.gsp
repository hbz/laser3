<%@page import="de.laser.reporting.report.ElasticSearchHelper; de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.ReportingGlobalService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:set var="validFilterResult" value="${filterResult.data.issueEntitlementIdList}" />

    <g:if test="${validFilterResult}">
        <ui:msg class="positive" noClose="true">
            <laser:render template="/myInstitution/reporting/query/filterResult" model="${[filter: filter, filterResult: filterResult]}" />
        </ui:msg>
    </g:if>
    <g:else>
        <ui:msg class="negative" noClose="true" message="reporting.filter.result.x.empty" />
    </g:else>

    <g:if test="${filterResult.get(ElasticSearchHelper.ELASTICSEARCH_IS_NOT_REACHABLE)}">
        <ui:msg class="negative" icon="large exclamation circle" message="reporting.filter.result.x.elasticSearchIsNotReachable" noClose="true" />
    </g:if>

    <laser:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${validFilterResult}">
        <laser:render template="/myInstitution/reporting/query/form" model="${[cfgKey: "${BaseConfig.KEY_ISSUEENTITLEMENT}"]}"/>
    </g:if>
</g:if>