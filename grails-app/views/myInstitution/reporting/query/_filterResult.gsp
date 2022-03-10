<%@ page import="de.laser.reporting.report.myInstitution.IssueEntitlementFilter; de.laser.reporting.report.myInstitution.base.BaseConfig;" %>

<g:if test="${filter == BaseConfig.KEY_COSTITEM}">
    ${message(code: 'reporting.filter.result.costItem', args: [filterResult.data.costItemIdList.size()])}
</g:if>

<g:elseif test="${filter == BaseConfig.KEY_ISSUEENTITLEMENT}">
    <g:if test="${filterResult.data.issueEntitlementIdList.size() < IssueEntitlementFilter.TMP_QUERY_CONSTRAINT}">
        ${message(code: 'reporting.filter.result.issueEntitlement', args: [filterResult.data.issueEntitlementIdList.size()])}
    </g:if>
    <g:else>
        ${message(code: 'reporting.filter.result.issueEntitlementTMP', args: [filterResult.data.issueEntitlementIdList.size()])}
    </g:else>
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_LICENSE}">
    ${message(code: 'reporting.filter.result.license.part', args: [filterResult.data.licenseIdList.size()])}
    <g:if test="${filterResult.data.licensorIdList.size()}">
        ${message(code: 'reporting.filter.result.and.licensor', args: [filterResult.data.licensorIdList.size()])}
    </g:if>
    ${message(code: 'reporting.filter.result.end')}
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_ORGANISATION}">
    ${message(code: 'reporting.filter.result.organisation', args: [filterResult.data.orgIdList.size()])}
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_PACKAGE}">
    ${message(code: 'reporting.filter.result.package', args: [filterResult.data.packageIdList.size(), filterResult.data.packageESRecords.size()])}
    %{-- <g:if test="${filterResult.data.providerIdList}">
        ${message(code: 'reporting.filter.result.and.provider', args: [filterResult.data.providerIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.platformIdList}">
        ${message(code: 'reporting.filter.result.and.platform', args: [filterResult.data.platformIdList.size()])}
    </g:if>
    ${message(code: 'reporting.filter.result.end')} --}%
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_PLATFORM}">
    ${message(code: 'reporting.filter.result.platform', args: [filterResult.data.platformIdList.size(), filterResult.data.platformESRecords.size()])}
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_SUBSCRIPTION}">
    ${message(code: 'reporting.filter.result.subscription.part', args: [filterResult.data.subscriptionIdList.size()])}
    <g:if test="${filterResult.data.memberSubscriptionIdList}">
        ${message(code: 'reporting.filter.result.and.memberSubscription', args: [filterResult.data.memberSubscriptionIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.memberIdList}">
        ${message(code: 'reporting.filter.result.and.member', args: [filterResult.data.memberIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.consortiumIdList}">
        ${message(code: 'reporting.filter.result.and.consortium', args: [filterResult.data.consortiumIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.providerIdList}">
        ${message(code: 'reporting.filter.result.and.provider', args: [filterResult.data.providerIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.agencyIdList}">
        ${message(code: 'reporting.filter.result.and.agency', args: [filterResult.data.agencyIdList.size()])}
    </g:if>
    ${message(code: 'reporting.filter.result.end')}
</g:elseif>

