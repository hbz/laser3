<h3 class="ui header">${message(code:'reporting.macro.step3')}</h3>

<div class="ui right aligned">
    <button id="chart-export-button" class="ui icon button" disabled>
        <i class="ui icon download"></i>
    </button>
    <g:if test="${query.split('-')[0] in ['org', 'member', 'provider', 'licensor']}">
        <button id="chart-email-button" class="ui icon button" href="#chartDetailsCopyEmailModal" data-semui="modal">
            <i class="icon envelope"></i>
        </button>
    </g:if>
</div>

<div class="ui small steps">
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
