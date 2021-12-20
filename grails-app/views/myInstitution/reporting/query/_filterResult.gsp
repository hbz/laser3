<%@ page import="de.laser.reporting.report.myInstitution.base.BaseConfig;" %>

<g:if test="${filter == BaseConfig.KEY_COSTITEM}">
    ${message(code: 'reporting.filterResult.costItem', args: [filterResult.data.costItemIdList.size()])}
</g:if>

<g:elseif test="${filter == BaseConfig.KEY_LICENSE}">
    ${message(code: 'reporting.filterResult.license.part', args: [filterResult.data.licenseIdList.size()])}
    <g:if test="${filterResult.data.licensorIdList.size()}">
        ${message(code: 'reporting.filterResult.and.licensor', args: [filterResult.data.licensorIdList.size()])}
    </g:if>
    ${message(code: 'reporting.filterResult.end')}
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_ORGANISATION}">
    ${message(code: 'reporting.filterResult.organisation', args: [filterResult.data.orgIdList.size()])}
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_PACKAGE}">
    ${message(code: 'reporting.filterResult.package.part', args: [filterResult.data.packageIdList.size(), filterResult.data.packageESRecords.size()])}
    <g:if test="${filterResult.data.providerIdList}">
        ${message(code: 'reporting.filterResult.and.provider', args: [filterResult.data.providerIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.platformIdList}">
        ${message(code: 'reporting.filterResult.and.platform', args: [filterResult.data.platformIdList.size()])}
    </g:if>
    ${message(code: 'reporting.filterResult.end')}
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_PLATFORM}">
   ${message(code: 'reporting.filterResult.platform', args: [filterResult.data.platformIdList.size(), filterResult.data.platformESRecords.size()])}
</g:elseif>

<g:elseif test="${filter == BaseConfig.KEY_SUBSCRIPTION}">
    ${message(code: 'reporting.filterResult.subscription.part', args: [filterResult.data.subscriptionIdList.size()])}
    <g:if test="${filterResult.data.memberSubscriptionIdList}">
        ${message(code: 'reporting.filterResult.and.memberSubscription', args: [filterResult.data.memberSubscriptionIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.memberIdList}">
        ${message(code: 'reporting.filterResult.and.member', args: [filterResult.data.memberIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.consortiumIdList}">
        ${message(code: 'reporting.filterResult.and.consortium', args: [filterResult.data.consortiumIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.providerIdList}">
        ${message(code: 'reporting.filterResult.and.provider', args: [filterResult.data.providerIdList.size()])}
    </g:if>
    <g:if test="${filterResult.data.agencyIdList}">
        ${message(code: 'reporting.filterResult.and.agency', args: [filterResult.data.agencyIdList.size()])}
    </g:if>
    ${message(code: 'reporting.filterResult.end')}
</g:elseif>

