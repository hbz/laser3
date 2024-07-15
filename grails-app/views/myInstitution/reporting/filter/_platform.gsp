<%@page import="de.laser.ui.Icon; de.laser.reporting.report.myInstitution.base.BaseFilter; de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

    <div class="menu ui top attached tabular la-tab-with-js">
        <a class="active item" data-tab="platform-filter-tab-1">${message(code:'platform.plural')}</a>
        <a class="item" data-tab="platform-filter-tab-help"> ? %{--<i class="${Icon.UI.HELP}"></i>--}%</a>
    </div><!-- .menu -->

    <div class="ui bottom attached active tab segment" data-tab="platform-filter-tab-1">
        <div class="field">
            <label for="filter:platform_source">${message(code:'reporting.ui.global.filter.selection')}</label>
            <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base}" />
            <g:select name="filter:platform_source" class="ui selection dropdown la-not-clearable"
                      from="${BaseFilter.getRestrictedConfigSources(config as Map)}"
                      optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}"
                      value="${params.get('filter:platform_source')}" />
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
        <div class="filter-wrapper-my">
            <g:each in="${config.filter.my}" var="cfgFilter">
                <div class="fields <uiReporting:numberToString number="${cfgFilter.size()}" min="2"/>">
                <g:each in="${cfgFilter}" var="field">
                    <uiReporting:filterField config="${config}" field="${field}" />
                </g:each>
                </div>
            </g:each>
        </div>

    </div><!-- .tab -->
    <div class="ui bottom attached tab segment" data-tab="platform-filter-tab-help">
        <div class="field">
            <div style="text-align:center; padding:2em 0">
                <img class="ui fluid image" alt="${message(code:'reporting.ui.global.help')}" src="${resource(dir: 'help', file: 'reporting/global.platforms.png')}"/>
            </div>
        </div>
    </div><!-- .tab -->

    <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).provider}" />
    <g:if test="${config}">
        <input type="hidden" name="filter:provider_source" value="filter-subset-provider" />
    </g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#filter\\:platform_source').on( 'change', function(e) {

        var $fwDefault = $('.filter-wrapper-default')
        var $fwMy = $('.filter-wrapper-my')

        if (JSPC.helper.contains( ['my-plt'], $(e.target).dropdown('get value') )) {
            $fwDefault.find('*').attr('disabled', 'disabled');
            $fwDefault.hide();
            $fwMy.find('*').removeAttr('disabled');
            $fwMy.show();
        }
        else {
            $fwMy.find('*').attr('disabled', 'disabled');
            $fwMy.hide();
            $fwDefault.find('*').removeAttr('disabled');
            $fwDefault.show();
        }
    })

    $('#filter\\:platform_source').trigger('change');
</laser:script>
