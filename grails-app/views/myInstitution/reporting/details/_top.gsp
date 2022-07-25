<h3 class="ui header">${message(code:'reporting.ui.global.step3')}</h3>

<div style="text-align:right; margin-right:0.5em">
    <g:if test="${query.split('-')[0] != 'issueEntitlement'}">
        <button id="details-export-button" class="ui icon button la-modern-button" data-href="#detailsExportModal" data-ui="modal">
            <i class="ui icon download"></i>
        </button>
    </g:if>

    <g:if test="${query.split('-')[0] in ['package', 'platform']}">
        <button id="details-dtc-button" class="ui icon button la-modern-button">
            <i class="ui icon th"></i>
        </button>
    </g:if>

    <g:if test="${query.split('-')[0] in ['org', 'member', 'consortium', 'provider', 'licensor']}">
        <button id="details-copy-email-button" class="ui icon button la-modern-button" data-href="#detailsCopyEmailModal" data-ui="modal">
            <i class="icon envelope"></i>
        </button>
    </g:if>
</div>

<laser:render template="/myInstitution/reporting/details/generic_queryLabels" model="${[queryLabels: labels]}" />

<laser:render template="/myInstitution/reporting/details/generic_tableConfig" />
