<%@page import="de.laser.reporting.myInstitution.SubscriptionConfig; de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.subscriptionIdList || result.memberIdList || result.providerIdList}">

        <div class="ui message success">
            <p>
                ${result.subscriptionIdList.size()} Lizenzen,
                ${result.memberIdList.size()} Teilnehmer und
                ${result.providerIdList.size()} Anbieter
                wurden anhand der Filtereinstellungen gefunden.
            </p>

            <g:render template="/myInstitution/reporting/query/base.part1" />
        </div>

        <g:render template="/myInstitution/reporting/query/base.part2" />

        <laser:script file="${this.getGroovyPageFileName()}">
            if (! JSPC.app.reporting) { JSPC.app.reporting = {}; }
            if (! JSPC.app.reporting.current) { JSPC.app.reporting.current = {}; }

            JSPC.app.reporting.current.request = {
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