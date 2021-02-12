<%@page import="de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.orgIdList}">

        <div class="ui message success">
            <p>
                ${result.orgIdList.size()} Einrichtungen
                wurden anhand der Filtereinstellungen gefunden.
            </p>
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

        <div class="ui segment form">
            <div class="fields">
                <g:each in="${cfgQueryList}" var="cfgQueryGroup" status="qci">
                    <g:each in="${cfgQueryGroup}" var="field">
                        <div class="field">
                            <label for="query-chooser-${qci}">${field.key}</label>
                            <g:select name="query-chooser"
                                      id="query-chooser-${qci}"
                                      from="${field.value}"
                                      optionKey="key"
                                      optionValue="value"
                                      class="ui selection dropdown la-not-clearable"
                                      value="opt1"
                                      noSelection="${['': message(code: 'default.select.choose.label')]}" />
                        </div>
                    </g:each>
                </g:each>

                <div class="field">
                    <label for="query-chooser">Visualisierung</label>
                    <g:select name="chart-chooser"
                              from="${cfgChartsList}"
                              optionKey="key"
                             optionValue="value"
                             class="ui selection dropdown la-not-clearable"
                             noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>

                <div class="field">
                    <label for="chart-export">Exportieren</label>
                    <button id="chart-export" class="ui icon button" disabled><i class="ui icon download"></i></button>
                </div>
            </div>
        </div>

        <laser:script file="${this.getGroovyPageFileName()}">
            if (! JSPC.app.reporting) { JSPC.app.reporting = {}; }
            if (! JSPC.app.reporting.current) { JSPC.app.reporting.current = {}; }

            JSPC.app.reporting.current.request = {
                orgIdList: [${result.orgIdList.join(',')}]
            }
        </laser:script>

    </g:if>
    <g:else>
        <div class="ui message negative">
            Keine Treffer gefunden ..
        </div>
    </g:else>
</g:if>