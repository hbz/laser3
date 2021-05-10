<div class="ui ${tmplSize ?: 'small'} steps">
    <g:each in="${queryLabels}" var="lbl" status="i">
        <div class="step">
            <div class="content">
                <div class="title">${lbl}</div>
            </div>
        </div>
    </g:each>
</div>
