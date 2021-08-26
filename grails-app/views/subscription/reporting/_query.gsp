<div class="ui segment form">

    <g:set var="fieldSize" value="${cfgQueryList2.size()}" />

    <sec:ifAnyGranted roles="ROLE_YODA"><%-- TODO --%>
        <g:set var="fieldSize" value="${fieldSize + 1}" />
    </sec:ifAnyGranted>

    <div class="fields <laser:numberToString number="${fieldSize}" min="2"/>">
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

        <sec:ifAnyGranted roles="ROLE_YODA"><%-- TODO --%>
            <div class="field">
                <label for="query-export-button">Exportieren</label>
                <button id="query-export-button" class="ui icon button" href="#queryExportModal" data-semui="modal" disabled><i class="ui icon download"></i></button>
            </div>
        </sec:ifAnyGranted>
    </div>
</div>

<sec:ifAnyGranted roles="ROLE_YODA"><%-- TODO --%>
    <g:render template="/subscription/reporting/export/queryModal" model="[modalID: 'queryExportModal', token: token]" />
</sec:ifAnyGranted>