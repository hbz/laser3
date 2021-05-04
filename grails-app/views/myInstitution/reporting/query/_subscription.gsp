<%@page import="de.laser.reporting.myInstitution.base.BaseConfig;de.laser.reporting.myInstitution.SubscriptionConfig;de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${filterResult.data.subscriptionIdList || filterResult.data.memberIdList || filterResult.data.consortiumIdList || filterResult.data.providerIdList || filterResult.data.agencyIdList}">

        <div class="ui message success">
            <p>
                Mit diesen Filtereinstellungen wurden
                <strong>${filterResult.data.subscriptionIdList.size()} Lizenzen</strong>
                <g:if test="${filterResult.data.memberIdList}">
                    und <strong>${filterResult.data.memberIdList.size()} Teilnehmer</strong>
                </g:if>
                <g:if test="${filterResult.data.consortiumIdList}">
                    und <strong>${filterResult.data.consortiumIdList.size()} Konsortialstellen</strong>
                </g:if>
                <g:if test="${filterResult.data.providerIdList}">
                    und <strong>${filterResult.data.providerIdList.size()} Anbieter</strong>
                </g:if>
                <g:if test="${filterResult.data.agencyIdList}">
                    und <strong>${filterResult.data.agencyIdList.size()} Lieferanten</strong>
                </g:if>
                gefunden.
            </p>
        </div>

        <g:render template="/myInstitution/reporting/query/base.part1" />

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