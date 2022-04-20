<%@page import="de.laser.reporting.report.ElasticSearchHelper; de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.ReportingGlobalService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:set var="validFilterResult" value="${filterResult.data.issueEntitlementIdList}" />

    <g:if test="${validFilterResult}">
        <div class="ui message success">
            <p>
                <g:render template="/myInstitution/reporting/query/filterResult" model="${[filter: filter, filterResult: filterResult]}" />
            </p>
        </div>
    </g:if>
    <g:else>
        <div class="ui message negative">
            <p><g:message code="reporting.filter.result.x.empty" /></p>
        </div>
    </g:else>

    <g:if test="${filterResult.get(ElasticSearchHelper.ELASTICSEARCH_IS_NOT_REACHABLE)}">
        <div class="ui message negative">
            <p><i class="icon large exclamation circle"></i> ${message(code:'reporting.filter.result.x.elasticSearchIsNotReachable')}</p>
        </div>
    </g:if>

    <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${validFilterResult}">
        <g:render template="/myInstitution/reporting/query/form" model="${[cfgKey: "${BaseConfig.KEY_ISSUEENTITLEMENT}"]}"/>
    </g:if>
</g:if>