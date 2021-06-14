<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<h3 class="ui header">${message(code:'reporting.macro.step3')}</h3>

<div class="ui right aligned">
    <button id="chart-details-export-button" class="ui icon button" href="#chartDetailsExportModal" data-semui="modal">
        <i class="ui icon download"></i>
    </button>

    <g:if test="${query.split('-')[0] in ['org', 'member', 'consortium', 'provider', 'licensor']}">
        <button id="chart-email-button" class="ui icon button" href="#chartDetailsCopyEmailModal" data-semui="modal">
            <i class="icon envelope"></i>
        </button>
    </g:if>
</div>

<g:render template="/myInstitution/reporting/details/generic_queryLabels" model="${[queryLabels: labels]}" />
