<%@page import="de.laser.reporting.myInstitution.LicenseConfig; de.laser.ReportingService;de.laser.Org;de.laser.License" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.licenseIdList || result.memberIdList || result.licensorIdList}">

        <div class="ui message success">
            <p>
                ${result.licenseIdList.size()} Verträge und
                %{-- ${result.memberIdList.size()} Teilnehmer und --}%
                ${result.licensorIdList.size()} Lizenzgeber
                wurden anhand der Filtereinstellungen gefunden.
            </p>

            <g:render template="/myInstitution/reporting/query/base.part1" />
        </div>

        <g:render template="/myInstitution/reporting/query/base.part2" />

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.reporting.current.request = {
                filter: '${LicenseConfig.KEY}',
                token: '${token}'
            }
        </laser:script>

    </g:if>
    <g:else>

        <div class="ui message negative">
            <p>
                Mit diesen Filtereinstellungen wurden keine Treffer gefunden.
            </p>
            <g:render template="/myInstitution/reporting/query/base.part1" />
        </div>

    </g:else>
</g:if>

