<%@page import="de.laser.reporting.myInstitution.base.BaseConfig;de.laser.reporting.myInstitution.LicenseConfig;de.laser.ReportingService;de.laser.Org;de.laser.License" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:render template="/myInstitution/reporting/query/base.part1" />

    <g:if test="${result.data.licenseIdList || result.data.licensorIdList}">

        <div class="ui message success">
            <p>
                Mit diesen Filtereinstellungen wurden
                <strong>${result.data.licenseIdList.size()} Verträge</strong> und
                <strong>${result.data.licensorIdList.size()} Lizenzgeber</strong> gefunden.
            </p>
        </div>

        <g:render template="/myInstitution/reporting/query/base.part2" />

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.reporting.current.request = {
                context: '${BaseConfig.KEY}',
                filter: '${LicenseConfig.KEY}',
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

