<%@page import="de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title><g:message code="laser"/> : <g:message code="myinst.reporting"/></title>
        <asset:stylesheet src="chartist.css"/><laser:javascript src="chartist.js"/>%{-- dont move --}%
    </head>

    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution.getDesignation()}"/>
            <semui:crumb text="${message(code:'myinst.reporting')}" class="active" />
        </semui:breadcrumbs>

        %{-- <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <g:link class="item" action="reporting_old" params="${exportParams}">${message(code: 'default.button.export.xls')}</g:link>
                </semui:exportDropdownItem>
            </semui:exportDropdown>
        </semui:controlButtons> --}%

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="myinst.reporting"/> <span class="ui label red">DEMO</span></h1>

        <h2 class="ui header la-noMargin-top">1. Suchanfrage definieren</h2>

        <g:form action="reporting" method="POST" class="ui form">

            <div class="menu ui top attached tabular">
                <a class="active item" data-tab="first">Lizenzen</a>
                <a class="item" data-tab="second">Teilnehmer</a>
                <a class="item" data-tab="third">Anbieter</a>
            </div><!-- .menu -->

            <div class="ui bottom attached active tab segment" data-tab="first">
                <div class="field">
                    <label for="filter:sub_subscription">Lizenzauswahl</label>
                    <input type="text" id="filter:sub_subscription" value="Meine Lizenzen" readonly="readonly" />
                </div>

                <div class="fields">
                    <g:set var="config" value="${ReportingService.config.Subscription}" />
                    <g:each in="${config.properties}" var="prop">
                        <laser:reportFilterProperty config="${config}" property="${prop}" />
                    </g:each>
                </div>

                <div class="fields">
                    <g:each in="${config.refdata}" var="rd">
                        <laser:reportFilterRefdata config="${config}" refdata="${rd}" />
                    </g:each>
                </div>
            </div><!-- .first -->

            <div class="ui bottom attached tab segment" data-tab="second">
                <div class="field">
                    <label for="filter:org_member">Teilnehmerauswahl</label>
                    <input type="text" id="filter:org_member" value="Alle betroffenen Teilnehmer" readonly="readonly" />
                </div>

                <div class="fields">
                    <g:set var="config" value="${ReportingService.config.Organisation}" />
                    <g:each in="${config.properties}" var="prop">
                        <laser:reportFilterProperty config="${config}" property="${prop}" key="member" />
                    </g:each>
                </div>

                <div class="fields">
                    <g:each in="${config.refdata}" var="rd">
                        <laser:reportFilterRefdata config="${config}" refdata="${rd}" key="member" />
                    </g:each>
                </div>
            </div><!-- .second -->

            <div class="ui bottom attached tab segment" data-tab="third">
                <div class="field">
                    <label for="filter:org_provider">Anbieterauswahl</label>
                    <input type="text" id="filter:org_provider" value="Alle betroffenen Anbieter" readonly="readonly" />
                </div>

                <div class="fields">
                    <g:set var="config" value="${ReportingService.config.Organisation}" />
                    <g:each in="${config.properties}" var="prop">
                        <laser:reportFilterProperty config="${config}" property="${prop}" key="provider" />
                    </g:each>
                </div>

                <div class="fields">
                    <g:each in="${config.refdata}" var="rd">
                        <laser:reportFilterRefdata config="${config}" refdata="${rd}" key="provider" />
                    </g:each>
                </div>
            </div><!-- .second -->

            <div class="fields">
                <div class="field la-field-right-aligned">
                    <g:link action="reporting" class="ui button primary">Zurücksetzen</g:link>
                    <input type="submit" class="ui button secondary" value="Suchen" />
                    <input type="hidden" name="filter" value="true" />
                </div>
            </div>

        </g:form>

        <g:if test="${result}">
            <g:if test="${result.subIdList || result.memberIdList || result.providerIdList}">

                <div class="ui message info">
                    <p>
                        ${result.subIdList.size()} Lizenzen,
                        ${result.memberIdList.size()} Teilnehmer,
                        ${result.providerIdList.size()} Anbieter
                    </p>
                </div>

                <g:if test="${result.subIdList}">
                    <div class="ui header">${result.subIdList.size()} Lizenzen</div>
                    <p>
                        <g:each in="${result.subIdList}" var="sub" status="i">
                            <g:if test="${i > 0}"> &nbsp;-&nbsp; </g:if>
                            <g:link controller="subscription" action="show" params="[id: sub]">${Subscription.get(sub)}</g:link>
                        </g:each>
                    </p>
                </g:if>
                <g:if test="${result.memberIdList}">
                    <div class="ui header">${result.memberIdList.size()} Teilnehmer</div>
                    <p>
                        <g:each in="${result.memberIdList}" var="org" status="i">
                            <g:if test="${i > 0}"> &nbsp;-&nbsp; </g:if>
                            <g:link controller="organisation" action="show" params="[id: org]">${Org.get(org)}</g:link>
                        </g:each>
                    </p>
                </g:if>
                <g:if test="${result.providerIdList}">
                    <div class="ui header">${result.providerIdList.size()} Anbieter</div>
                    <p>
                        <g:each in="${result.providerIdList}" var="org" status="i">
                            <g:if test="${i > 0}"> &nbsp;-&nbsp; </g:if>
                            <g:link controller="organisation" action="show" params="[id: org]">${Org.get(org)}</g:link>
                        </g:each>
                    </p>
                </g:if>
            </g:if>
            <g:else>
                <div class="ui message info">
                    Keine Treffer ..
                </div>
            </g:else>
        </g:if>

        <laser:script file="${this.getGroovyPageFileName()}">

        </laser:script>

    </body>
</html>
