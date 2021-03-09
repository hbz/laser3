<div class="ui segment form">

    <h2 class="ui icon header la-clear-before la-noMargin-top">
        <g:message code="myinst.reporting"/> <span class="ui label red">DEMO : in Entwicklung</span>
    </h2>

    <div class="fields <laser:numberToString number="${2 + cfgQueryList.size()}"/>">
        <g:each in="${cfgQueryList}" var="cfgQuery" status="qci">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-${qci}">${field.key}</label>
                         <g:select name="query-chooser"
                              id="query-chooser-${qci}"
                              from="${field.value}"
                              optionKey="key"
                              optionValue="${{it.value.label}}"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>

        <div class="field">
            <label for="chart-export">Exportieren</label>
            <button id="chart-export" class="ui icon button" disabled><i class="ui icon download"></i></button>
        </div>
    </div>
</div>