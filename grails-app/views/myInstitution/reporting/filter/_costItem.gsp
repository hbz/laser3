<%@page import="de.laser.reporting.myInstitution.base.BaseConfig" %>
<laser:serviceInjection/>

    <g:form action="reporting" method="POST" class="ui form">

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="costItem-filter-tab-1">${message(code:'subscription.costItems.label')}</a>
        </div><!-- .menu -->
        <div class="ui bottom attached active tab segment" data-tab="costItem-filter-tab-1">
            <div class="field">
                <label for="filter:costItem_source">${message(code:'reporting.filter.selection')}</label>
                <g:set var="config" value="${BaseConfig.getCurrentConfig( BaseConfig.KEY_COSTITEM ).base}" />
                <g:select name="filter:costItem_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="key" optionValue="value" value="${params.get('filter:costItem_source')}" />
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

        <div class="field">
            <g:link action="reporting" class="ui button primary">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui button secondary" value="${message(code:'default.button.search.label')}" />
            <input type="hidden" name="filter" value="${BaseConfig.KEY_COSTITEM}" />
            <input type="hidden" name="token" value="${token}" />
        </div>

    </g:form>