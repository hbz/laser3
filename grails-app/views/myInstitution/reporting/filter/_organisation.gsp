<%@page import="de.laser.ReportingGlobalService;de.laser.Org;de.laser.Subscription;de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

        <div class="menu ui top attached tabular la-tab-with-js">
            <a class="active item" data-tab="org-filter-tab-1">${message(code:'org.plural.label')}</a>
        </div><!-- .menu -->

        <div class="ui bottom attached active tab segment" data-tab="org-filter-tab-1">
            <div class="field">
                <label for="filter:org_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base}" />
                <g:select name="filter:org_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:org_source')}" />
            </div>

            <div class="filter-wrapper-default">
                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                    <g:each in="${cfgFilter}" var="field">
                        <laser:reportFilterField config="${config}" field="${field}" />
                    </g:each>
                    </div>
                </g:each>
            </div>

            <div class="filter-wrapper-provider">
                <g:each in="${config.filter.provider}" var="cfgFilter">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                        <g:each in="${cfgFilter}" var="field">
                            <laser:reportFilterField config="${config}" field="${field}" />
                        </g:each>
                    </div>
                </g:each>
            </div>
        </div><!-- .tab -->

<laser:script file="${this.getGroovyPageFileName()}">
    $('#filter\\:org_source').on( 'change', function(e) {

        var $fwDefault = $('.filter-wrapper-default')
        var $fwProvider = $('.filter-wrapper-provider')

        if (JSPC.helper.contains( ['all-provider', 'all-agency', 'all-providerAndAgency', 'my-provider', 'my-agency', 'my-providerAndAgency'], $(e.target).dropdown('get value') )) {
            $fwDefault.find('*').attr('disabled', 'disabled');
            $fwDefault.hide();
            $fwProvider.find('*').removeAttr('disabled');
            $fwProvider.show();
        }
        else {
            $fwProvider.find('*').attr('disabled', 'disabled');
            $fwProvider.hide();
            $fwDefault.find('*').removeAttr('disabled');
            $fwDefault.show();
        }
    })

    $('#filter\\:org_source').trigger('change');
</laser:script>

