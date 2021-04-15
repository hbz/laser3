<div class="ui segment form">
    <div class="fields <laser:numberToString number="${cfgQueryList.size()}" min="2"/>">
        <g:each in="${cfgQueryList}" var="cfgQuery" status="qci">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-1-${qci}">${field.key}</label>
                    <g:select name="query-chooser"
                              id="query-chooser-1-${qci}"
                              from="${field.value}"
                              optionKey="key"
                              optionValue="value"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>
    </div>

    <div class="fields <laser:numberToString number="${2 + cfgQuery2List.size()}"/>">
        <g:each in="${cfgQuery2List}" var="cfgQuery" status="qci">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-2-${qci}">${field.key}</label>
                         <g:select name="query-chooser"
                              id="query-chooser-2-${qci}"
                              from="${field.value}"
                              optionKey="key"
                              optionValue="${{it.value.label}}"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>

        <div class="field">
            <label for="query-chooser">${message(code: 'reporting.query.chart')}</label>
            <g:select name="chart-chooser"
                      from="${cfgChartsList}"
                      optionKey="key"
                      optionValue="value"
                      class="ui selection dropdown la-not-clearable"
                      noSelection="${['': message(code: 'default.select.choose.label')]}" />
        </div>

        <div class="field">
            %{--
            <label for="chart-export">Exportieren</label>
            <button id="chart-export" class="ui icon button" disabled><i class="ui icon download"></i></button>
            --}%
        </div>
    </div>
</div>