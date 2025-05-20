<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<br />

<div style="text-align: right">
    <button id="details-export-button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code: 'default.button.export.label')}"
            data-href="#detailsExportModal" data-ui="modal">
        <i class="${Icon.CMD.DOWNLOAD}"></i>
    </button>
</div>

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

<laser:script file="${this.getGroovyPageFileName()}">
    $(function(){ responsiveTables.go(); })
</laser:script>
