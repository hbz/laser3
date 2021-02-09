<%@page import="de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.subIdList || result.memberIdList || result.providerIdList}">

        <div class="ui message info">
            <p>
                ${result.subIdList.size()} Einrichtungen,
                ${result.memberIdList.size()} Teilnehmer,
                ${result.providerIdList.size()} Anbieter
            </p>
        </div>

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

        <div class="ui segment">
            <g:select name="query-chooser"
                      from="${cfgQueryList}"
                      optionKey="key"
                      optionValue="value"
                      class="ui selection dropdown"
                      value="opt1"
                      noSelection="${['': message(code: 'default.select.choose.label')]}" />

            <g:select name="chart-chooser"
                      from="${cfgChartsList}"
                      optionKey="key"
                      optionValue="value"
                      class="ui selection dropdown"
                      noSelection="${['': message(code: 'default.select.choose.label')]}" />
        </div>

        <div id="chart-wrapper"></div>

        <style>
        #chart-wrapper {
            width: 100%;
            height: 400px;
        }
        </style>

        <laser:script file="${this.getGroovyPageFileName()}">

            if (! JSPC.app.reporting) { JSPC.app.reporting = {} }

            JSPC.app.reporting.requestConfig = {
                subcriptionIdList: [${result.subIdList.join(',')}],
                memberIdList: [${result.memberIdList.join(',')}],
                providerIdList: [${result.providerIdList.join(',')}]
            }
        </laser:script>

    </g:if>
    <g:else>
        <div class="ui message info">
            Keine Treffer ..
        </div>
    </g:else>
</g:if>
