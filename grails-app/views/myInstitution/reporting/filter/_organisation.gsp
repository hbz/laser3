<%@page import="de.laser.reporting.OrganisationConfig;de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

    <g:form action="reporting" method="POST" class="ui form">

        <div class="menu ui top attached tabular">
            <a class="active item" data-tab="org-filter-tab-1">Organisationen</a>
        </div><!-- .menu -->

        <div class="ui bottom attached active tab segment" data-tab="org-filter-tab-1">
            <div class="field">
                <label for="filter:org_source">Auswahl</label>
                <g:set var="config" value="${OrganisationConfig.CONFIG.base}" />
                <g:select name="filter:org_source" class="ui selection dropdown la-not-clearable" from="${config.source}" optionKey="key" optionValue="value" value="${params.get('filter:org_source')}" />
            </div>

            <br /><br />

            <g:each in="${config.filter}" var="cfgFormGroup">
                <div class="fields <laser:numberToString number="${cfgFormGroup.size()}"/>">
                    <g:each in="${cfgFormGroup.keySet()}" var="field">
                        <laser:reportFilterField config="${config}" field="${field}" />
                    </g:each>
                </div>
            </g:each>

        </div><!-- .first -->

        <div class="field">
            <g:link action="reporting" class="ui button primary">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui button secondary" value="${message(code:'default.button.search.label')}" />
            <input type="hidden" name="filter" value="organisation" />
            <input type="hidden" name="token" value="${token}" />
        </div>

    </g:form>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#filter\\:org_source').on( 'change', function(e) {
        var providerNegativeList = ['org_libraryType', 'org_libraryNetwork', 'org_funderType', 'org_funderHskType', 'org_eInvoice']
        var selector = $(providerNegativeList).map(function() { return '#filter\\:' + this; }).get().join()

        if (JSPC.helper.contains( ['all-provider', 'my-provider'], $(e.target).dropdown('get value') )) {
            $(selector).attr('disabled', 'disabled');
            $(selector).parent().addClass('disabled');
            $(selector).parent().parent().hide();
        }
        else {
            $(selector).removeAttr('disabled');
            $(selector).parent().removeClass('disabled');
            $(selector).parent().parent().show();
        }
    })

    $('#filter\\:org_source').trigger('change');
</laser:script>

