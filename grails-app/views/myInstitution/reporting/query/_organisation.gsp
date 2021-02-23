<%@page import="de.laser.reporting.OrganisationConfig; de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.orgIdList}">

        <div class="ui message success">
            <p>
                ${result.orgIdList.size()} Organisationen
                wurden anhand der Filtereinstellungen gefunden.
            </p>

            <g:render template="/myInstitution/reporting/query/base.part1" />
        </div>

        %{--
        <div class="ui styled fluid accordion">
            <g:if test="${result.orgIdList}">
                <div class="title">
                    <i class="dropdown icon"></i>${result.orgIdList.size()} Einrichtungen
                </div>
                <div class="content">
                    <p>
                        <g:each in="${result.orgIdList}" var="org" status="i">
                            <g:link controller="organisation" action="show" params="[id: org]"><i class="ui icon university"></i>${Org.get(org)}</g:link>
                            &nbsp;
                        </g:each>
                    </p>
                </div>
            </g:if>
        </div>
        --}%

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