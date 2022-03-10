<g:if test="${stacked}">

    <div class="ui segment center aligned" style="background-color: #f9fafb;">
        ${queryLabels.join(' → ')}
    </div>
</g:if>
<g:else>

    <div class="ui small steps">
        <g:each in="${queryLabels}" var="lbl" status="i">
            <div class="step">
                <div class="content">
                    <div class="title">${lbl}</div>
                </div>
            </div>
        </g:each>
    </div>
</g:else>
