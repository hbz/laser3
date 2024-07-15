<%@page import="de.laser.ui.Icon; de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

        <div class="menu ui top attached tabular la-tab-with-js">
            <a class="active item" data-tab="vendor-filter-tab-1">${message(code:'vendor.plural')}</a>
            <a class="item" data-tab="vendor-filter-tab-help"> ? %{--<i class="${Icon.UI.HELP}"></i>--}%</a>
        </div><!-- .menu -->

        <div class="ui bottom attached active tab segment" data-tab="vendor-filter-tab-1">
            <div class="field">
                <label for="filter:vendor_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_VENDOR ).base}" />
                <g:select name="filter:vendor_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:vendor_source')}" />
            </div>

            <div class="filter-wrapper-default">
                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <uiReporting:numberToString number="${cfgFilter.size()}" min="2"/>">
                    <g:each in="${cfgFilter}" var="field">
                        <uiReporting:filterField config="${config}" field="${field}" />
                    </g:each>
                    </div>
                </g:each>
            </div>

        </div><!-- .tab -->


