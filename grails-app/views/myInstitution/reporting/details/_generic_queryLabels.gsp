<div class="ui ${tmplSize ?: 'small'} steps">
    <g:each in="${queryLabels}" var="lbl" status="i">
        <g:if test="${i+1 == labels.size() && tmplShowActive}">
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
