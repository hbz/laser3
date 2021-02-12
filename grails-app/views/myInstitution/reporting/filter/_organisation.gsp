<%@page import="de.laser.reporting.Cfg;de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

    <g:form action="reporting" method="POST" class="ui form">

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="org-filter-tab-1">Organisationen</a>
        </div><!-- .menu -->

        <div class="ui bottom attached active tab segment" data-tab="org-filter-tab-1">
            <div class="field">
                <label for="filter:org_filter">Auswahl</label>
                <g:set var="config" value="${Cfg.config.Organisation}" />
                <g:select name="filter:org_filter" class="ui selection dropdown la-not-clearable" from="${config.filter}" optionKey="key" optionValue="value" value="${params.get('filter:org_filter')}" />
            </div>

            <g:each in="${config.form}" var="cfgFormGroup">
                <div class="fields">
                    <g:each in="${cfgFormGroup.keySet()}" var="field">
                        <laser:reportFilterField config="${config}" field="${field}" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .first -->

        <div class="field">
            <g:link action="reporting" class="ui button primary">Zurücksetzen</g:link>
            <input type="submit" class="ui button secondary" value="Suchen" />
            <input type="hidden" name="filter" value="organisation" />
        </div>

    </g:form>

