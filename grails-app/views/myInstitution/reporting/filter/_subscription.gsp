<%@page import="de.laser.ReportingGlobalService;de.laser.Org;de.laser.Subscription;de.laser.reporting.report.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

        <div class="menu ui top attached tabular la-tab-with-js">
            <a class="active item" data-tab="sub-filter-tab-1">${message(code:'subscription.plural')}</a>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).memberSubscription}">
                <a class="item" data-tab="sub-filter-tab-2">${message(code:'subscriptionsManagement.subscriber')}</a>
            </g:if>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).member}">
                <a class="item" data-tab="sub-filter-tab-3">${message(code:'subscription.details.consortiaMembers.label')}</a>
            </g:if>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).consortium}">
                <a class="item filter-wrapper-consortium" data-tab="sub-filter-tab-4">${message(code:'subscription.details.consortia.label')}</a>
            </g:if>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).provider}">
                <a class="item" data-tab="sub-filter-tab-5">${message(code:'default.provider.label')}</a>
            </g:if>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).agency}">
                <a class="item" data-tab="sub-filter-tab-6">${message(code:'default.agency.plural.label')}</a>
            </g:if>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).memberSubscription}">
                <a class="item" data-tab="sub-filter-tab-help"> ? %{--<i class="icon question"></i>--}%</a>
            </g:if>
        </div><!-- .menu -->

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base}" />

        <div class="ui bottom attached active tab segment" data-tab="sub-filter-tab-1">
            <div class="field">
                <label for="filter:subscription_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                <g:select name="filter:subscription_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:subscription_source')}" />
            </div>

            <g:each in="${config.filter.default}" var="cfgFilter">
                <g:if test="${cfgFilter.containsAll(['startDateLimit','endDateLimit']) && cfgFilter.size() == 2}">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="4"/>">
                </g:if>
                <g:elseif test="${cfgFilter.findAll{it.contains('Date')}.size() == cfgFilter.size()}"> %{-- tmp datepicker layout fix --}%
                    <div class="fields">
                </g:elseif>
                <g:else>
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                </g:else>
                    <g:each in="${cfgFilter}" var="field">
                        <laser:reportFilterField config="${config}" field="${field}" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .tab -->

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).memberSubscription}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-2">
                <div class="field">
                    <label for="filter:memberSubscription_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                    <g:select name="filter:memberSubscription_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:memberSubscription_source')}" />
                </div>

                <g:each in="${config.filter.default}" var="cfgFilter">
                    <g:if test="${cfgFilter.containsAll(['startDateLimit','endDateLimit']) && cfgFilter.size() == 2}">
                        <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="4"/>">
                    </g:if>
                    <g:elseif test="${cfgFilter.findAll{it.contains('Date')}.size() == cfgFilter.size()}"> %{-- tmp datepicker layout fix --}%
                        <div class="fields">
                    </g:elseif>
                    <g:else>
                        <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                    </g:else>
                        <g:each in="${cfgFilter}" var="field">
                            <laser:reportFilterField config="${config}" field="${field}" key="memberSubscription" />
                        </g:each>
                    </div>
                </g:each>
            </div><!-- .tab -->

        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).member}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-3">
                <div class="field">
                    <label for="filter:member_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                    <g:select name="filter:member_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:member_source')}" />
                </div>

                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                        <g:each in="${cfgFilter}" var="field">
                            <laser:reportFilterField config="${config}" field="${field}" key="member" />
                        </g:each>
                    </div>
                </g:each>
            </div><!-- .tab -->

        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).consortium}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-4">
                <div class="filter-wrapper-consortium">
                    <div class="field">
                        <label for="filter:consortium_source">${message(code:'reporting.ui.global.filter.selection')}</label>
                        <g:select name="filter:consortium_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:consortium_source')}" />
                    </div>

                    %{--
                    <g:each in="${config.filter.default}" var="cfgFilter">
                        <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                            <g:each in="${cfgFilter}" var="field">
                                <laser:reportFilterField config="${config}" field="${field}" key="consortium" />
                            </g:each>
                        </div>
                    </g:each>

                    --}%
                </div>
            </div><!-- .tab -->

        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).provider}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-5">
                <div class="field">
                    <label for="filter:provider_source">${message(code:'reporting.ui.global.filter.selection')}</label>

                    <g:select name="filter:provider_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:provider_source')}" />
                </div>

                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                        <g:each in="${cfgFilter}" var="field">
                            <laser:reportFilterField config="${config}" field="${field}" key="provider" />
                        </g:each>
                    </div>
                </g:each>
            </div><!-- .tab -->

        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).agency}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-6">
                <div class="field">
                    <label for="filter:agency_source">${message(code:'reporting.ui.global.filter.selection')}</label>

                    <g:select name="filter:agency_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="${it}" optionValue="${{BaseConfig.getSourceLabel(config.meta.cfgKey, it)}}" value="${params.get('filter:agency_source')}" />
                </div>

                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                        <g:each in="${cfgFilter}" var="field">
                            <laser:reportFilterField config="${config}" field="${field}" key="agency" />
                        </g:each>
                    </div>
                </g:each>
            </div><!-- .tab -->

        </g:if>

        <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).memberSubscription}">
            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-help">
                <div class="field">
                    <div style="text-align:center; padding:2em 0">
                        <asset:image src="help/reporting.subscriptions.png" absolute="true" style="width:98%" />
                    </div>
                </div>
            </div><!-- .tab -->
        </g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#filter\\:subscription_source').on( 'change', function(e) {

        var $fwConsortium = $('.filter-wrapper-consortium')

        if ( $(e.target).dropdown('get value') == 'inst-sub-local' ) {
            $fwConsortium.find('*').attr('disabled', 'disabled');
            $fwConsortium.hide();
        }
        else {
            $fwConsortium.find('*').removeAttr('disabled');
            $fwConsortium.show();
        }
    })

    $('#filter\\:subscription_source').trigger('change');
</laser:script>