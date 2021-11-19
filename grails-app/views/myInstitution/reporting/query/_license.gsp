<%@page import="de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.ReportingGlobalService;de.laser.Org;de.laser.License" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${filterResult.data.licenseIdList || filterResult.data.licensorIdList}">

        <div class="ui message success">
            <p>
                <g:render template="/myInstitution/reporting/query/filterResult" model="${[filter: filter, filterResult: filterResult]}" />
            </p>
        </div>
        <g:render template="/myInstitution/reporting/query/form" model="${[cfgKey: "${BaseConfig.KEY_LICENSE}"]}" />

    </g:if>
    <g:else>
        <div class="ui message negative">
            <p><g:message code="reporting.filter.no.matches" /></p>
        </div>
    </g:else>
</g:if>

