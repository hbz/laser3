<%@page import="de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

    <g:form action="reporting" method="POST" class="ui form">

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="org-filter-tab-1">Einrichtung</a>
        </div><!-- .menu -->

        <div class="ui bottom attached active tab segment" data-tab="org-filter-tab-1">
            <div class="field">
                <label for="filter:org_organisation">Einrichtungsauswahl</label>
                <input type="text" id="filter:org_organisation" value="Meine Einrichtungen" readonly="readonly" />
            </div>

            <div class="fields">
                <g:set var="config" value="${ReportingService.config.Organisation}" />
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

        <div class="field">
            <g:link action="reporting" class="ui button primary">Zurücksetzen</g:link>
            <input type="submit" class="ui button secondary" value="Suchen" />
            <input type="hidden" name="filter" value="organisation" />
        </div>

    </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
    </laser:script>

