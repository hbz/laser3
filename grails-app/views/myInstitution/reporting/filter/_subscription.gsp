<%@page import="de.laser.reporting.Cfg;de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

    <g:form action="reporting" method="POST" class="ui form">

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="sub-filter-tab-1">Lizenzen</a>
            <a class="item" data-tab="sub-filter-tab-2">Teilnehmer</a>
            <a class="item" data-tab="sub-filter-tab-3">Anbieter</a>
        </div><!-- .menu -->

        <div class="ui bottom attached active tab segment" data-tab="sub-filter-tab-1">
            <div class="field">
                <label for="filter:sub_filter">Auswahl</label>
                <g:set var="config" value="${Cfg.config.Subscription}" />
                <g:select name="filter:sub_filter" class="ui selection dropdown" from="${config.filter}" optionKey="key" optionValue="value" value="${params.get('filter:sub_filter')}" />
            </div>

            <div class="fields">
                <g:set var="config" value="${Cfg.config.Subscription}" />
                <g:each in="${config.properties}" var="prop">
                    <laser:reportFilterProperty config="${config}" property="${prop}" />
                </g:each>
            </div>

            <div class="fields">
                <g:each in="${config.refdata}" var="rd">
                    <laser:reportFilterRefdata config="${config}" refdata="${rd}" />
                </g:each>
            </div>
        </div><!-- .first -->

        <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-2">
            <div class="field">
                <label for="filter:org_member">Teilnehmerauswahl</label>
                <input type="text" id="filter:org_member" value="Alle betroffenen Teilnehmer" readonly="readonly" />
            </div>

            <div class="fields">
                <g:set var="config" value="${Cfg.config.Organisation}" />
                <g:each in="${config.properties}" var="prop">
                    <laser:reportFilterProperty config="${config}" property="${prop}" key="member" />
                </g:each>
            </div>

            <div class="fields">
                <g:each in="${config.refdata}" var="rd">
                    <laser:reportFilterRefdata config="${config}" refdata="${rd}" key="member" />
                </g:each>
            </div>
        </div><!-- .second -->

        <div class="ui bottom attached tab segment" data-tab="sub-filter-tab-3">
            <div class="field">
                <label for="filter:org_provider">Anbieterauswahl</label>
                <input type="text" id="filter:org_provider" value="Alle betroffenen Anbieter" readonly="readonly" />
            </div>

            <div class="fields">
                <g:set var="config" value="${Cfg.config.Organisation}" />
                <g:each in="${config.properties}" var="prop">
                    <laser:reportFilterProperty config="${config}" property="${prop}" key="provider" />
                </g:each>
            </div>

            <div class="fields">
                <g:each in="${config.refdata}" var="rd">
                    <laser:reportFilterRefdata config="${config}" refdata="${rd}" key="provider" />
                </g:each>
            </div>
        </div><!-- .second -->

        <div class="field">
            <g:link action="reporting" class="ui button primary">Zurücksetzen</g:link>
            <input type="submit" class="ui button secondary" value="Suchen" />
            <input type="hidden" name="filter" value="subscription" />
        </div>

    </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
    </laser:script>

