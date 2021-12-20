<%@page import="de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

    <div class="menu ui top attached tabular">
        <a class="active item" data-tab="platform-filter-tab-1">${message(code:'platform.plural')}</a>
        <a class="item" data-tab="platform-filter-tab-help"> ? %{--<i class="icon question"></i>--}%</a>
    </div><!-- .menu -->

    <div class="ui bottom attached active tab segment" data-tab="platform-filter-tab-1">
        <div class="field">
            <label for="filter:platform_source">${message(code:'reporting.filter.selection')}</label>
            <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base}" />
            <g:select name="filter:platform_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getMessage(config.meta.cfgKey + '.source.' + it)}}" value="${params.get('filter:platform_source')}" />
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
    <div class="ui bottom attached tab segment" data-tab="platform-filter-tab-help">
        <div class="field">
            <div style="text-align:center; padding:2em 0">
                <asset:image src="help/reporting.platforms.png" absolute="true" style="width:96%" />
            </div>
        </div>
    </div><!-- .tab -->
