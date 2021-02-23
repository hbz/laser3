<g:each in="${result.filterLabels}" var="labelGroup">
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
</g:each>
