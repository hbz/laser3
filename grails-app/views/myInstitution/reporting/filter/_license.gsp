<%@page import="de.laser.ReportingGlobalService;de.laser.Org;de.laser.License;de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

        <div class="menu ui top attached tabular la-tab-with-js">
            <a class="active item" data-tab="lic-filter-tab-1">${message(code:'license.plural')}</a>
            %{--<a class="item" data-tab="lic-filter-tab-2">Einrichtung</a>--}%
            <a class="item" data-tab="lic-filter-tab-3">${message(code:'license.licensor.label')}</a>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).vendor}">
                <a class="item" data-tab="lic-filter-tab-4">${message(code:'default.vendor.export.label')}</a>
            </g:if>
        </div><!-- .menu -->

        <div class="ui bottom attached active tab segment" data-tab="lic-filter-tab-1">
            <div class="field">
                <label for="filter:license_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).base}" />
                <g:select name="filter:license_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:license_source')}" />
            </div>

            <g:each in="${config.filter.default}" var="cfgFilter">
                <g:if test="${cfgFilter.containsAll(['startDateLimit','endDateLimit']) && cfgFilter.size() == 2}">
                    <div class="fields <uiReporting:numberToString number="${cfgFilter.size()}" min="4"/>">
                </g:if>
                <g:elseif test="${cfgFilter.findAll{it.contains('Date')}.size() == cfgFilter.size()}"> %{-- tmp datepicker layout fix --}%
                    <div class="fields">
                </g:elseif>
                <g:else>
                    <div class="fields <uiReporting:numberToString number="${cfgFilter.size()}" min="2"/>">
                </g:else>
                    <g:each in="${cfgFilter}" var="field">
                        <uiReporting:filterField config="${config}" field="${field}" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .tab -->

        %{--<div class="ui bottom attached tab segment" data-tab="lic-filter-tab-2">
            <div class="field">
                <label for="filter:member_source">Einrichtungsauswahl</label>
                <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).member}" />
                <g:select name="filter:member_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="key" optionValue="value" value="${params.get('filter:member_source')}" />
            </div>

            <g:each in="${config.filter.default}" var="cfgFilter">
                <div class="fields <uiReporting:numberToString number="${cfgFilter.size()}" min="2"/>">
                    <g:each in="${cfgFilter}" var="field">
                        <uiReporting:filterField config="${config}" field="${field}" key="member" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .tab --> --}%

        <div class="ui bottom attached tab segment" data-tab="lic-filter-tab-3">
            <div class="field">
                <label for="filter:licensor_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).licensor}" />
                <g:select name="filter:licensor_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:licensor_source')}" />
            </div>

            <g:each in="${config.filter.default}" var="cfgFilter">
                <div class="fields <uiReporting:numberToString number="${cfgFilter.size()}" min="2"/>">
                    <g:each in="${cfgFilter}" var="field">
                        <uiReporting:filterField config="${config}" field="${field}" key="licensor" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .tab -->

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).vendor}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="lic-filter-tab-4">
                <div class="field">
                    <label for="filter:vendor_source">${message(code:'reporting.ui.global.filter.selection')}</label>

                    <g:select name="filter:vendor_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:vendor_source')}" />
                </div>

                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <uiReporting:numberToString number="${cfgFilter.size()}" min="2"/>">
                        <g:each in="${cfgFilter}" var="field">
                            <uiReporting:filterField config="${config}" field="${field}" key="vendor" />
                        </g:each>
                    </div>
                </g:each>
            </div><!-- .tab -->

        </g:if>
