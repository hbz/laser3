<%@page import="de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="package-filter-tab-1">${message(code:'package.plural')}</a>
%{--            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).provider}">--}%
%{--                <a class="item" data-tab="package-filter-tab-2">${message(code:'default.provider.label')}</a>--}%
%{--            </g:if>--}%
%{--            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).platform}">--}%
%{--                <a class="item" data-tab="package-filter-tab-3">${message(code:'platform.label')}</a>--}%
%{--            </g:if>--}%

            <a class="item" data-tab="package-filter-tab-help"> ? %{--<i class="icon question"></i>--}%</a>
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

        <div class="ui bottom attached tab segment" data-tab="package-filter-tab-help">
            <div class="field">
                <div style="text-align:center; padding:2em 0">
                    <asset:image src="help/reporting.packages.png" absolute="true" style="width:96%" />
                </div>
            </div>
        </div><!-- .tab -->

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).provider}" />
        <g:if test="${config}">
            <input type="hidden" name="filter:provider_source" value="filter-depending-provider" />
        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).platform}" />
        <g:if test="${config}">
            <input type="hidden" name="filter:platform_source" value="filter-depending-platform" />
        </g:if>

%{--        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).provider}" />--}%
%{--        <g:if test="${config}">--}%
%{--            <div class="ui bottom attached tab segment" data-tab="package-filter-tab-2">--}%
%{--                <div class="field">--}%
%{--                    <label for="filter:provider_source">${message(code:'reporting.filter.selection')}</label>--}%

%{--                    <g:select name="filter:provider_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getMessage(config.meta.cfgKey + '.source.' + it)}}" value="${params.get('filter:provider_source')}" />--}%
%{--                </div>--}%

%{--                <g:each in="${config.filter.default}" var="cfgFilter">--}%
%{--                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">--}%
%{--                        <g:each in="${cfgFilter}" var="field">--}%
%{--                            <laser:reportFilterField config="${config}" field="${field}" key="provider" />--}%
%{--                        </g:each>--}%
%{--                    </div>--}%
%{--                </g:each>--}%
%{--            </div><!-- .tab -->--}%
%{--        </g:if>--}%

%{--        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).platform}" />--}%
%{--        <g:if test="${config}">--}%
%{--            <div class="ui bottom attached tab segment" data-tab="package-filter-tab-3">--}%
%{--                <div class="field">--}%
%{--                    <label for="filter:platform_source">${message(code:'reporting.filter.selection')}</label>--}%

%{--                    <g:select name="filter:platform_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getMessage(config.meta.cfgKey + '.source.' + it)}}" value="${params.get('filter:platform_source')}" />--}%
%{--                </div>--}%

%{--                <g:each in="${config.filter.default}" var="cfgFilter">--}%
%{--                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">--}%
%{--                        <g:each in="${cfgFilter}" var="field">--}%
%{--                            <laser:reportFilterField config="${config}" field="${field}" key="platform" />--}%
%{--                        </g:each>--}%
%{--                    </div>--}%
%{--                </g:each>--}%
%{--            </div><!-- .tab -->--}%
%{--        </g:if>--}%

