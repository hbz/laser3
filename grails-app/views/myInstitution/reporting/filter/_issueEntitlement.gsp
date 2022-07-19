<%@page import="de.laser.reporting.report.myInstitution.base.BaseFilter; de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

        <div class="menu ui top attached tabular la-tab-with-js">
            <a class="active item" data-tab="issueEntitlement-filter-tab-1">${message(code:'issueEntitlement.plural')}</a>
%{--            <a class="item" data-tab="issueEntitlement-filter-tab-help"> ? <i class="icon question"></i></a>--}%
        </div><!-- .menu -->

        <div class="ui bottom attached active tab segment" data-tab="issueEntitlement-filter-tab-1">
            <div class="field">
                <label for="filter:issueEntitlement_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).base}" />
                <g:select name="filter:issueEntitlement_source" class="ui selection dropdown la-not-clearable"
                          from="${BaseFilter.getRestrictedConfigSources(config as Map)}"
                          optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}"
                          value="${params.get('filter:issueEntitlement_source')}" />
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

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).platform}" />
        <g:if test="${config}">
            <input type="hidden" name="filter:platform_source" value="filter-restricting-platform" />
        </g:if>
%{--        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).nominalPlatform}" />--}%
%{--        <g:if test="${config}">--}%
%{--            <input type="hidden" name="filter:nominalPlatform_source" value="filter-restricting-nominalPlatform" />--}%
%{--        </g:if>--}%

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).package}" />
        <g:if test="${config}">
            <input type="hidden" name="filter:package_source" value="filter-restricting-package" />
        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).provider}" />
        <g:if test="${config}">
            <input type="hidden" name="filter:provider_source" value="filter-restricting-provider" />
        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).subscription}" />
        <g:if test="${config}">
            <input type="hidden" name="filter:subscription_source" value="filter-restricting-subscription" />
        </g:if>

%{--        <div class="ui bottom attached tab segment" data-tab="issueEntitlement-filter-tab-help">--}%
%{--            <div class="field">--}%
%{--                <div style="text-align:center; padding:2em 0">--}%
%{--                    <asset:image src="help/reporting.packages.png" absolute="true" style="width:96%" />--}%
%{--                </div>--}%
%{--            </div>--}%
%{--        </div><!-- .tab -->--}%

    %{--        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).provider}" />--}%
    %{--        <g:if test="${config}">--}%
    %{--            <input type="hidden" name="filter:provider_source" value="filter-restricting-provider" />--}%
    %{--        </g:if>--}%

    %{--        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).platform}" />--}%
    %{--        <g:if test="${config}">--}%
    %{--            <input type="hidden" name="filter:platform_source" value="filter-restricting-platform" />--}%
    %{--        </g:if>--}%


