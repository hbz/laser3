<%@page import="de.laser.reporting.myInstitution.base.BaseConfig;de.laser.reporting.myInstitution.SubscriptionConfig;de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:render template="/myInstitution/reporting/query/base.part1" />

    <g:if test="${result.data.subscriptionIdList || result.data.memberIdList || result.data.providerIdList}">

        <div class="ui message success">
            <p>
                Mit diesen Filtereinstellungen wurden
                <strong>${result.data.subscriptionIdList.size()} Lizenzen</strong>,
                <strong>${result.data.memberIdList.size()} Teilnehmer</strong> und
                <strong>${result.data.providerIdList.size()} Anbieter</strong> gefunden.
            </p>
        </div>

        <g:render template="/myInstitution/reporting/query/base.part2" />

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.reporting.current.request = {
                context: '${BaseConfig.KEY}',
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