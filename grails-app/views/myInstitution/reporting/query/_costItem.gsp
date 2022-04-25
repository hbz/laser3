<%@page import="de.laser.reporting.report.myInstitution.base.BaseConfig;" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:set var="validFilterResult" value="${filterResult.data.costItemIdList}" />

    <g:if test="${validFilterResult}">
        <div class="ui message success">
            <p>
                <laser:render template="/myInstitution/reporting/query/filterResult" model="${[filter: filter, filterResult: filterResult]}" />
            </p>
        </div>
    </g:if>
    <g:else>
        <div class="ui message negative">
            <p><g:message code="reporting.filter.result.x.empty" /></p>
        </div>
    </g:else>

    <laser:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${validFilterResult}">
        <laser:render template="/myInstitution/reporting/query/form" model="${[cfgKey: "${BaseConfig.KEY_COSTITEM}"]}" />
    </g:if>
</g:if>