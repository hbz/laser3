<%@page import="de.laser.ReportingService;de.laser.Org;de.laser.Subscription;de.laser.reporting.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

    <g:form action="reporting" method="POST" class="ui form">

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="sub-filter-tab-1">${message(code:'subscription.plural')}</a>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).member}">
                <a class="item" data-tab="sub-filter-tab-2">${message(code:'subscription.details.consortiaMembers.label')}</a>
            </g:if>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).consortium}">
                <a class="item filter-wrapper-consortium" data-tab="sub-filter-tab-3">${message(code:'subscription.details.consortia.label')}</a>
            </g:if>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).provider}">
                <a class="item" data-tab="sub-filter-tab-4">${message(code:'default.provider.label')}</a>
            </g:if>
            <g:if test="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).agency}">
                <a class="item" data-tab="sub-filter-tab-5">${message(code:'default.agency.plural.label')}</a>
            </g:if>
        </div><!-- .menu -->

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base}" />

        <div class="ui bottom attached active tab segment" data-tab="sub-filter-tab-1">
            <div class="field">
                <label for="filter:subscription_source">${message(code:'reporting.filter.selection')}</label>
                <g:select name="filter:subscription_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="key" optionValue="value" value="${params.get('filter:subscription_source')}" />
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

        </div><!-- .first -->

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).member}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-2">
                <div class="field">
                    <label for="filter:member_source">${message(code:'reporting.filter.selection')}</label>
                    <g:select name="filter:member_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="key" optionValue="value" value="${params.get('filter:member_source')}" />
                </div>

                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                        <g:each in="${cfgFilter}" var="field">
                            <laser:reportFilterField config="${config}" field="${field}" key="member" />
                        </g:each>
                    </div>
                </g:each>
            </div><!-- .second -->

        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).consortium}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-3">
                <div class="filter-wrapper-consortium">
                    <div class="field">
                        <label for="filter:consortium_source">${message(code:'reporting.filter.selection')}</label>
                        <g:select name="filter:consortium_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="key" optionValue="value" value="${params.get('filter:consortium_source')}" />
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
            </div><!-- .second -->

        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).provider}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-4">
                <div class="field">
                    <label for="filter:provider_source">${message(code:'reporting.filter.selection')}</label>

                    <g:select name="filter:provider_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="key" optionValue="value" value="${params.get('filter:provider_source')}" />
                </div>

                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                        <g:each in="${cfgFilter}" var="field">
                            <laser:reportFilterField config="${config}" field="${field}" key="provider" />
                        </g:each>
                    </div>
                </g:each>
            </div><!-- .second -->

        </g:if>

        <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).agency}" />
        <g:if test="${config}">

            <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-5">
                <div class="field">
                    <label for="filter:agency_source">${message(code:'reporting.filter.selection')}</label>

                    <g:select name="filter:agency_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="key" optionValue="value" value="${params.get('filter:agency_source')}" />
                </div>

                <g:each in="${config.filter.default}" var="cfgFilter">
                    <div class="fields <laser:numberToString number="${cfgFilter.size()}" min="2"/>">
                        <g:each in="${cfgFilter}" var="field">
                            <laser:reportFilterField config="${config}" field="${field}" key="agency" />
                        </g:each>
                    </div>
                </g:each>
            </div><!-- .second -->

        </g:if>

        <div class="field">
            <g:link action="reporting" class="ui button primary">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui button secondary" value="${message(code:'default.button.search.label')}" />
            <input type="hidden" name="filter" value="${BaseConfig.KEY_SUBSCRIPTION}" />
            <input type="hidden" name="token" value="${token}" />
        </div>

    </g:form>

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