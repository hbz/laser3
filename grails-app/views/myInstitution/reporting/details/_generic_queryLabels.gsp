<div class="ui ${tmplSize ?: 'small'} steps">
    <g:if test="${tmplShowLabel}">
        <div class="step active">
            <div class="content">
                <div class="title">Filter :</div>
            </div>
        </div>
    </g:if>
    <g:each in="${queryLabels}" var="lbl" status="i">
        <div class="step">
            <div class="content">
                <div class="title">${lbl}</div>
            </div>
        </div>
    </g:each>
</div>
