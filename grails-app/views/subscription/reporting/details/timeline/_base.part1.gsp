<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.reporting.report.myInstitution.base.BaseQuery; de.laser.reporting.export.LocalExportHelper;" %>
<br />

<div style="text-align: right">
    <button id="details-export-button" class="${Btn.SIMPLE_ICON}" data-href="#detailsExportModal" data-ui="modal">
        <i class="${Icon.CMD.DOWNLOAD}"></i>
    </button>
</div>

<div class="ui tiny steps">
    <g:each in="${labels}" var="lbl" status="i">
        <g:if test="${i+1 == labels.size()}">
            <div class="step">
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

<laser:script file="${this.getGroovyPageFileName()}">
    $(function(){ responsiveTables.go(); })
</laser:script>

