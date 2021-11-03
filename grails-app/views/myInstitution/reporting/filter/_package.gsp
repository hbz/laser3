<%@page import="de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="package-filter-tab-1">${message(code:'package.plural')}</a>
        </div><!-- .menu -->
        <div class="ui bottom attached active tab segment" data-tab="package-filter-tab-1">
            <div class="field">
                <label for="filter:package_source">${message(code:'reporting.filter.selection')}</label>
                <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base}" />
                <g:select name="filter:package_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getMessage(config.meta.cfgKey + '.source.' + it)}}" value="${params.get('filter:package_source')}" />
            </div>

            <g:each in="${config.filter.default}" var="cfgFilter">
                <g:if test="${cfgFilter.findAll{it.contains('Date')}.size() == cfgFilter.size()}">%{-- tmp datepicker layout fix --}%
                    <div class="fields">
                </g:if>
                <g:else>
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                </g:else>
                    <g:each in="${cfgFilter}" var="field">
                        <laser:reportFilterField config="${config}" field="${field}" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .tab -->