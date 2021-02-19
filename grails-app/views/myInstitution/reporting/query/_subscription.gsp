<%@page import="de.laser.reporting.SubscriptionConfig; de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.subIdList || result.memberIdList || result.providerIdList}">

        <div class="ui message success">
            <p>
                ${result.subIdList.size()} Lizenzen,
                ${result.memberIdList.size()} Teilnehmer und
                ${result.providerIdList.size()} Anbieter
                wurden anhand der Filtereinstellungen gefunden.
            </p>
            <p>
                <g:each in="${result.filterLabels}" var="labelGroup">
                    <strong>${labelGroup.value.source}</strong>
                    <br />
                    <g:each in="${labelGroup.value}" var="label">
                        <g:if test="${label.key != 'source'}">
                            - ${label.value} <br/>
                        </g:if>
                    </g:each>
                </g:each>
            </p>
        </div>

        %{--
        <div class="ui styled fluid accordion">
            <g:if test="${result.subIdList}">
                <div class="title">
                    <i class="dropdown icon"></i>${result.subIdList.size()} Lizenzen
                </div>
                <div class="content">
                    <p>
                        <g:each in="${result.subIdList}" var="sub" status="i">
                            <g:link controller="subscription" action="show" params="[id: sub]"><i class="ui icon clipboard"></i>${Subscription.get(sub)}</g:link>
                            &nbsp;
                        </g:each>
                    </p>
                </div>
            </g:if>
            <g:if test="${result.memberIdList}">
                <div class="title">
                    <i class="dropdown icon"></i>${result.memberIdList.size()} Teilnehmer
                </div>
                <div class="content">
                    <p>
                        <g:each in="${result.memberIdList}" var="org" status="i">
                            <g:link controller="organisation" action="show" params="[id: org]"><i class="ui icon university"></i>${Org.get(org)}</g:link>
                            &nbsp;
                        </g:each>
                    </p>
                </div>
            </g:if>
            <g:if test="${result.providerIdList}">
                <div class="title">
                    <i class="dropdown icon"></i>${result.providerIdList.size()} Anbieter
                </div>
                <div class="content">
                    <p>
                        <g:each in="${result.providerIdList}" var="org" status="i">
                            <g:link controller="organisation" action="show" params="[id: org]"><i class="ui icon university"></i>${Org.get(org)}</g:link>
                            &nbsp;
                        </g:each>
                    </p>
                </div>
            </g:if>
        </div>
        --}%

        <g:render template="/myInstitution/reporting/query/base.part" />

        <laser:script file="${this.getGroovyPageFileName()}">
            if (! JSPC.app.reporting) { JSPC.app.reporting = {}; }
            if (! JSPC.app.reporting.current) { JSPC.app.reporting.current = {}; }

            JSPC.app.reporting.current.request = {
                subscriptionIdList: [${result.subIdList.join(',')}],
                memberIdList: [${result.memberIdList.join(',')}],
                providerIdList: [${result.providerIdList.join(',')}]
            }
        </laser:script>

    </g:if>
    <g:else>

        <div class="ui message negative">
            ${message(code: 'default.search.no.matches')}
        </div>

    </g:else>
</g:if>
