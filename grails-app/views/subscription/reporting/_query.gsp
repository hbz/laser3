<div class="ui segment form">

    <div class="fields <laser:numberToString number="${cfgQueryList2.size() + 2}" min="2"/>">
        <g:each in="${cfgQueryList2}" var="cfgQuery" status="qci">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-2-${qci}">${field.key}</label>
                    <g:select name="query-chooser-2"
                              id="query-chooser-2-${qci}"
                              from="${field.value}"
                              optionKey="key"
                              optionValue="${{it.value.label}}"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>

        <g:each in="${cfgQueryList}" var="cfgQuery" status="qci">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-1-${qci}">${field.key}</label>
                    <g:select name="query-chooser-1"
                              id="query-chooser-1-${qci}"
                              from="${field.value}"
                              optionKey="key"
                              optionValue="${{it.value}}"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>

        <div class="field">
            <label for="query-export-button">Exportieren</label>
            <button id="query-export-button" class="ui icon button" href="#queryExportModal" data-semui="modal" disabled><i class="ui icon download"></i></button>
        </div>
    </div>
</div>

<g:render template="/subscription/reporting/export/queryModal" model="[modalID: 'queryExportModal', token: token]" />