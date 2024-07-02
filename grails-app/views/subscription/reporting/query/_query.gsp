<%@ page import="de.laser.helper.Icons; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.report.local.SubscriptionReport" %>
<div class="ui segment form">

    <div class="fields <uiReporting:numberToString number="${cfgTimelineList.size() + 2}" min="2"/>">
        <g:each in="${cfgQueryList}" var="cfgQuery" status="qi">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-1-${qi}">${SubscriptionReport.getMessage(field.key)}</label>
                    <g:select name="query-chooser-1"
                              id="query-chooser-1-${qi}"
                              from="${field.value}"
                              optionKey="${{it.key}}"
                              optionValue="${{SubscriptionReport.getQueryLabel(it.key, it.value)}}"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>
        <div class="field">
            <label for="chart-chooser">${message(code: 'reporting.cfg.chart')}</label>
            <g:select name="chart-chooser"
                      from="${cfgChartsList}"
                      optionKey="${{it}}"
                      optionValue="${{BaseConfig.getConfigLabel('chart.' + it)}}"
                      class="ui selection dropdown la-not-clearable"
                      noSelection="${['': message(code: 'default.select.choose.label')]}" />
        </div>
    </div>

    <div class="fields three">
        <g:each in="${cfgTimelineList}" var="cfgTimeline" status="ti">
            <g:each in="${cfgTimeline}" var="field">
                <div class="field">
                    <label for="query-chooser-2-${ti}">${SubscriptionReport.getMessage('timeline')}</label>
                    <g:select name="query-chooser-2"
                              id="query-chooser-2-${ti}"
                              from="${field.value}"
                              optionKey="${{it.key}}"
                              optionValue="${{SubscriptionReport.getMessage('timeline.' + it.key)}}"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </g:each>
        </g:each>
        <div class="field">
        </div>
        <div class="field" style="text-align: right">
            <label for="query-export-button">&nbsp;</label>
            <button id="query-export-button" class="ui icon button" data-href="#queryExportModal" data-ui="modal" disabled><i class="${Icons.CMD_DOWNLOAD}"></i></button>
            <button id="query-help-button" class="ui icon button" data-href="#queryHelpModal" data-ui="modal" disabled><i class="icon question"></i></button>
        </div>
    </div>

</div>

<style>
.ui.form .fields .field { margin-bottom: 0 !important; }
</style>

<laser:render template="/subscription/reporting/export/queryModal" model="[modalID: 'queryExportModal', token: token]" />
<laser:render template="/subscription/reporting/query/helpModal" model="[modalID: 'queryHelpModal', token: token]" />