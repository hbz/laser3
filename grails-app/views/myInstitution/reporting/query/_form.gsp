<%@ page import="de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<g:set var="esdConfig" value="${BaseConfig.getCurrentConfigElasticsearchData(cfgKey)}" />

<div class="ui segment form">
    <div class="fields <laser:numberToString number="${cfgQueryList.size()}" min="2"/>">
        <g:each in="${cfgQueryList}" var="cfgQuery" status="qi">
            <g:each in="${cfgQuery}" var="field">
                <div class="field">
                    <label for="query-chooser-1-${qi}">${BaseConfig.getConfigLabel(field.key)}</label>
                    <semui:dropdownWithI18nExplanations name="query-chooser"
                                                        id="query-chooser-1-${qi}"
                                                        from="${field.value}"
                                                        optionKey="${{it.key}}"
                                                        optionValue="${{BaseConfig.getQueryLabel(cfgKey, it.key, it.value)}}"
                                                        optionExpl="${{esdConfig.keySet().contains(it.key) ? '(we:kb)' : ''}}"
                                                        class="ui selection dropdown la-not-clearable"
                                                        noSelection="${message(code: 'default.select.choose.label')}" />
                </div>
            </g:each>
        </g:each>
    </div>

    <div class="fields <laser:numberToString number="${2 + cfgDistributionList.size()}"/>">
        <g:each in="${cfgDistributionList}" var="cfgDistribution" status="di">
            <g:each in="${cfgDistribution}" var="field">
                <div class="field">
                    <label for="query-chooser-2-${di}">${BaseConfig.getConfigLabel('distribution')}</label>
                         <semui:dropdownWithI18nExplanations name="query-chooser"
                                                             id="query-chooser-2-${di}"
                                                             from="${field.value}"
                                                             optionKey="${{it.key}}"
                                                             optionValue="${{BaseConfig.getDistributionLabel(cfgKey, it.key)}}"
                                                             optionExpl="${{esdConfig.keySet().contains(it.key) ? '(we:kb)' : ''}}"
                                                             class="ui selection dropdown la-not-clearable"
                                                             noSelection="${message(code: 'default.select.choose.label')}" />
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

        <div class="field" style="text-align: right">
            <label for="query-export-button">&nbsp;</label>
            <button id="query-export-button" class="ui icon button la-modern-button" data-href="#queryExportModal" data-semui="modal" disabled><i class="ui icon download"></i></button>
            <button id="query-help-button" class="ui icon button la-modern-button" data-href="#queryHelpModal" data-semui="modal" disabled><i class="ui icon question"></i></button>
        </div>
    </div>
</div>

<g:render template="/myInstitution/reporting/export/queryModal" model="[modalID: 'queryExportModal', token: token]" />
<g:render template="/myInstitution/reporting/query/helpModal" model="[modalID: 'queryHelpModal', token: token]" />

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.reporting.current.request = {
        context: '${BaseConfig.KEY_MYINST}',
        filter: '${cfgKey}',
        token: '${token}'
    }
    $('#filter-package input.button[type=submit]').on('click', function() {
        $('#loadingIndicator').show();
    })
</laser:script>

