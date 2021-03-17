<%@page import="de.laser.reporting.myInstitution.GenericConfig; de.laser.reporting.myInstitution.SubscriptionConfig; de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:render template="/myInstitution/reporting/query/base.part1" />

    <g:if test="${result.subscriptionIdList || result.memberIdList || result.providerIdList}">

        <div class="ui message success">
            <p>
                Mit diesen Filtereinstellungen wurden
                <strong>${result.subscriptionIdList.size()} Lizenzen</strong>,
                <strong>${result.memberIdList.size()} Teilnehmer</strong> und
                <strong>${result.providerIdList.size()} Anbieter</strong> gefunden.
            </p>
        </div>

        <g:render template="/myInstitution/reporting/query/base.part2" />

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.reporting.current.request = {
                context: '${GenericConfig.KEY}',
                filter: '${SubscriptionConfig.KEY}',
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