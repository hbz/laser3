<%@page import="de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.orgIdList}">

        <div class="ui message info">
            <p>
                ${result.orgIdList.size()} Einrichtungen
            </p>
        </div>

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
                orgIdList: [${result.orgIdList.join(',')}]
            }
        </laser:script>

    </g:if>
    <g:else>
        <div class="ui message info">
            Keine Treffer ..
        </div>
    </g:else>
</g:if>