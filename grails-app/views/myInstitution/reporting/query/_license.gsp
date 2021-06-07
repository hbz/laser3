<%@page import="de.laser.reporting.myInstitution.base.BaseConfig;de.laser.ReportingService;de.laser.Org;de.laser.License" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${filterResult.data.licenseIdList || filterResult.data.licensorIdList}">

        <div class="ui message success">
            <p>
                ${message(code: 'reporting.filterResult.license.part', args: [filterResult.data.licenseIdList.size()])}

                <g:if test="${filterResult.data.licensorIdList.size()}">
                    ${message(code: 'reporting.filterResult.and.licensor', args: [filterResult.data.licensorIdList.size()])}
                </g:if>
                ${message(code: 'reporting.filterResult.end')}
            </p>
        </div>

        <g:render template="/myInstitution/reporting/query/base.part1" />

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.reporting.current.request = {
                context: '${BaseConfig.KEY_MYINST}',
                filter: '${BaseConfig.KEY_LICENSE}',
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

