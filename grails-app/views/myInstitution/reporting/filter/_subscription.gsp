<%@page import="de.laser.reporting.myInstitution.SubscriptionConfig;de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

    <g:form action="reporting" method="POST" class="ui form">

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="sub-filter-tab-1">${message(code:'subscription.plural')}</a>
            <a class="item" data-tab="sub-filter-tab-2">${message(code:'subscription.details.consortiaMembers.label')}</a>
            <a class="item" data-tab="sub-filter-tab-3">${message(code:'default.provider.label')}</a>
        </div><!-- .menu -->
        <div class="ui bottom attached active tab segment" data-tab="sub-filter-tab-1">
            <div class="field">
                <label for="filter:subscription_source">${message(code:'reporting.filter.selection')}</label>
                <g:set var="config" value="${SubscriptionConfig.CONFIG.base}" />
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

        <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-2">
            <div class="field">
                <label for="filter:member_source">${message(code:'reporting.filter.member.source')}</label>
                <g:set var="config" value="${SubscriptionConfig.CONFIG.member}" />
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

        <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-3">
            <div class="field">
                <label for="filter:provider_source">${message(code:'reporting.filter.provider.source')}</label>
                <g:set var="config" value="${SubscriptionConfig.CONFIG.provider}" />
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

        <div class="field">
            <g:link action="reporting" class="ui button primary">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui button secondary" value="${message(code:'default.button.search.label')}" />
            <input type="hidden" name="filter" value="${SubscriptionConfig.KEY}" />
            <input type="hidden" name="token" value="${token}" />
        </div>

    </g:form>