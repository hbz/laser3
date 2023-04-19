%{-- template or XHR --}%
<div id="filter-${filter}" class="${xhr ? 'hidden' : ''}">
    <g:form action="reporting" method="POST" class="ui form">
        <laser:render template="/myInstitution/reporting/filter/${filter}" />

        <div class="field">
            <g:link action="reporting" class="ui button secondary">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui button primary" value="${message(code:'default.button.search.label')}" />
            <input type="hidden" name="filter" value="${filter}" />
            <input type="hidden" name="token" value="${token}" />
        </div>
    </g:form>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#filter-${filter} input[type=submit]').on('click', function() {
        $('#globalLoadingIndicator').show();
    })
</laser:script>