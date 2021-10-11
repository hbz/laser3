<%@ page import="de.laser.reporting.report.local.SubscriptionReport" %>
<div class="ui segment form">

    <div class="fields <laser:numberToString number="${cfgQueryList2.size() + 2}" min="2"/>">
        <g:each in="${cfgQueryList2}" var="cfgQuery" status="qci">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-2-${qci}">${SubscriptionReport.getMessage(field.key)}</label>
                    <g:select name="query-chooser-2"
                              id="query-chooser-2-${qci}"
                              from="${field.value}"
                              optionKey="${{it.key}}"
                              optionValue="${{SubscriptionReport.getMessage('timeline.' + it.key)}}"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>

        <g:each in="${cfgQueryList}" var="cfgQuery" status="qci">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-1-${qci}">${SubscriptionReport.getMessage(field.key)}</label>
                    <g:select name="query-chooser-1"
                              id="query-chooser-1-${qci}"
                              from="${field.value}"
                              optionKey="${{it}}"
                              optionValue="${{SubscriptionReport.getMessage('query.' + it)}}"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>

        <div class="field" style="text-align: right">
            <label for="query-export-button">&nbsp;</label>
            <button id="query-export-button" class="ui icon button" data-href="#queryExportModal" data-semui="modal" disabled><i class="ui icon download"></i></button>
            <button id="query-help-button" class="ui icon button" data-href="#queryHelpModal" data-semui="modal" disabled><i class="ui icon question"></i></button>
        </div>
    </div>
</div>

<g:render template="/subscription/reporting/export/queryModal" model="[modalID: 'queryExportModal', token: token]" />
<g:render template="/subscription/reporting/query/helpModal" model="[modalID: 'queryHelpModal', token: token]" />