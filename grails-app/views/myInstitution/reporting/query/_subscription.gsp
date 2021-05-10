<%@page import="de.laser.reporting.myInstitution.base.BaseConfig;de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${filterResult.data.subscriptionIdList || filterResult.data.memberIdList || filterResult.data.consortiumIdList || filterResult.data.providerIdList || filterResult.data.agencyIdList}">

        <div class="ui message success">
            <p>
                ${message(code: 'reporting.filterResult.subscription.part', args: [filterResult.data.subscriptionIdList.size()])}

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
            </p>
        </div>

        <g:render template="/myInstitution/reporting/query/base.part1" />

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.reporting.current.request = {
                context: '${BaseConfig.KEY_MYINST}',
                filter: '${BaseConfig.KEY_SUBSCRIPTION}',
                token: '${token}'
            }
        </laser:script>

    </g:if>
    <g:else>
        <div class="ui message negative">
            <p><g:message code="reporting.filter.no.matches" /></p>
        </div>
    </g:else>
</g:if>