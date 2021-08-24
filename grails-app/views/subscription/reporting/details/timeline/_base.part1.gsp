<%@ page import="de.laser.reporting.export.local.ExportLocalHelper;" %>
<br />

<g:if test="${! (ExportLocalHelper.getDetailsCache(token).query in ['timeline-cost', 'timeline-annualMember'])}"> %{-- TODO --}%
    <div class="ui right aligned">
        <button id="details-export-button" class="ui icon button" href="#detailsExportModal" data-semui="modal">
            <i class="ui icon download"></i>
        </button>
    </div>
</g:if>

<div class="ui tiny steps">
    <g:each in="${labels}" var="lbl" status="i">
        <g:if test="${i+1 == labels.size()}">
            <div class="step active">
        </g:if>
        <g:else>
            <div class="step">
        </g:else>
        <div class="content">
            <div class="title">${lbl}</div>
        </div>
        </div>
    </g:each>
</div>

