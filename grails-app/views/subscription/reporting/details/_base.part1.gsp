<br />

<div style="text-align: right">
    <button id="details-export-button" class="ui icon button" data-href="#detailsExportModal" data-ui="modal">
        <i class="ui icon download"></i>
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
