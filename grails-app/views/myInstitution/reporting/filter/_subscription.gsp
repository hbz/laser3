<%@page import="de.laser.reporting.RepCfg;de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

    <g:form action="reporting" method="POST" class="ui form">

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="sub-filter-tab-1">Lizenzen</a>
            <a class="item" data-tab="sub-filter-tab-2">Teilnehmer</a>
            <a class="item" data-tab="sub-filter-tab-3">Anbieter</a>
        </div><!-- .menu -->
        <div class="ui bottom attached active tab segment" data-tab="sub-filter-tab-1">
            <div class="field">
                <label for="filter:subscription_filter">Auswahl</label>
                <g:set var="config" value="${RepCfg.config.Subscription}" />
                <g:select name="filter:subscription_filter" class="ui selection dropdown la-not-clearable" from="${config.filter}" optionKey="key" optionValue="value" value="${params.get('filter:sub_filter')}" />
            </div>

            <g:each in="${config.form}" var="cfgFormGroup">
                <div class="fields">
                    <g:each in="${cfgFormGroup.keySet()}" var="field">
                        <laser:reportFilterField config="${config}" field="${field}" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .first -->

        <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-2">
            <div class="field">
                <label for="filter:org_member">Teilnehmerauswahl</label>
                <input type="text" id="filter:org_member" value="Alle betroffenen Teilnehmer" readonly="readonly" />
            </div>

            <g:set var="config" value="${RepCfg.config.Organisation}" />

            <g:each in="${config.form}" var="cfgFormGroup">
                <div class="fields">
                    <g:each in="${cfgFormGroup.keySet()}" var="field">
                        <laser:reportFilterField config="${config}" field="${field}" key="member" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .second -->

        <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-3">
            <div class="field">
                <label for="filter:org_provider">Anbieterauswahl</label>
                <input type="text" id="filter:org_provider" value="Alle betroffenen Anbieter" readonly="readonly" />
            </div>

            <g:set var="config" value="${RepCfg.config.Organisation}" />

            <g:each in="${config.form}" var="cfgFormGroup">
                <div class="fields">
                    <g:each in="${cfgFormGroup.keySet()}" var="field">
                        <laser:reportFilterField config="${config}" field="${field}" key="provider" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .second -->

        <div class="field">
            <g:link action="reporting" class="ui button primary">Zurücksetzen</g:link>
            <input type="submit" class="ui button secondary" value="Suchen" />
            <input type="hidden" name="filter" value="subscription" />
        </div>

    </g:form>