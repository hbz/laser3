<%@page import="de.laser.reporting.myInstitution.GenericConfig; de.laser.reporting.myInstitution.LicenseConfig; de.laser.ReportingService;de.laser.Org;de.laser.License" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:render template="/myInstitution/reporting/query/base.part1" />

    <g:if test="${result.licenseIdList || result.memberIdList || result.licensorIdList}">

        <div class="ui message success">
            <p>
                Mit diesen Filtereinstellungen wurden
                <strong>${result.licenseIdList.size()} Verträge</strong> und
                <strong>${result.licensorIdList.size()} Lizenzgeber</strong> gefunden.
            </p>
        </div>

        <g:render template="/myInstitution/reporting/query/base.part2" />

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.reporting.current.request = {
                context: '${GenericConfig.KEY}',
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

