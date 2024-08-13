<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<h3 class="ui header">${message(code:'reporting.ui.global.step3')}</h3>

<g:set var="queryPart" value="${query.split('-')[0]}" />

<div style="text-align:right; margin-right:0.5em">
    <g:if test="${!(queryPart in ['issueEntitlement'])}">
        <button id="details-export-button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code: 'default.button.export.label')}"
                data-href="#detailsExportModal" data-ui="modal">
            <i class="${Icon.CMD.DOWNLOAD}"></i>
        </button>
    </g:if>

    <g:if test="${queryPart in ['package', 'platform']}">
        <button id="details-dtc-button" class="${Btn.MODERN.SIMPLE}">
            <i class="icon th"></i>
        </button>
    </g:if>

    <g:if test="${queryPart in ['org', 'member', 'consortium', 'provider', 'vendor']}">
        <button id="details-copy-email-button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code: 'menu.institutions.copy_emailaddresses.button')}"
                data-href="#detailsCopyEmailModal" data-ui="modal">
            <i class="${Icon.SYM.EMAIL}"></i>
        </button>
    </g:if>
</div>

<laser:render template="/myInstitution/reporting/details/generic_queryLabels" model="${[queryLabels: labels]}" />

<laser:render template="/myInstitution/reporting/details/details_tableConfig" />
