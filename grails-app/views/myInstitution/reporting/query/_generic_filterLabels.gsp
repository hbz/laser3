<div class="ui ${tmplSize ?: 'small'} steps">
    <g:if test="${tmplShowLabel}">
        <div class="step active">
            <div class="content">
                <div class="title">Abfrage :</div>
            </div>
        </div>
    </g:if>
    <g:each in="${filterLabels}" var="lblGroup">
        <div class="step">
            <div class="content">
                <div class="title">${lblGroup.value.source}</div>
                <div class="description">
                    <g:each in="${lblGroup.value}" var="label">
                        <g:if test="${label.key != 'source'}">
                            <g:if test="${label.key in ['endDate', 'startDate']}">
                                - ${label.value.label} ${label.value.value} <br/>
                            </g:if>
                            <g:else>
                                - ${label.value.label}: ${label.value.value} <br/>
                            </g:else>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </div>
    </g:each>
</div>
%{-- <g:each in="${filterLabels}" var="labelGroup">
    <p>
        <strong>${labelGroup.value.source}</strong>
        <br />
        <g:each in="${labelGroup.value}" var="label">
            <g:if test="${label.key != 'source'}">
                <g:if test="${label.key in ['endDate', 'startDate']}">
                    - ${label.value.label} ${label.value.value} <br/>
                </g:if>
                <g:else>
                    - ${label.value.label}: ${label.value.value} <br/>
                </g:else>
            </g:if>
        </g:each>
    </p>
</g:each> --}%
