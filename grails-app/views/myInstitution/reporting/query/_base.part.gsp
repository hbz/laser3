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